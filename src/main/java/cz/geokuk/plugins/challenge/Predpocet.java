package cz.geokuk.plugins.challenge;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.zip.CRC32;

import cz.geokuk.plugins.challenge.Challenge.ZdrojPravidel;
import cz.geokuk.plugins.challenge.SouborPredpoctu.Tabulka;
import cz.geokuk.plugins.challenge.VypocetSektoru.Klasifikace;
import cz.geokuk.plugins.challenge.VypocetSektoru.Klasifikator;
import lombok.extern.slf4j.Slf4j;

/**
 * Předpočet polohy keší vůči středu a oblasti (leží v oblasti, sektor, nejistota) na pozadí a jeho uložení do souboru. Klíč uložení tvoří otisk zdroje dat,
 * verze oblasti, střed a pravidla; při dalším zapnutí se přepočítají jen nové keše a keše s jinými souřadnicemi. Soubor je mimo {@code tiles.sqlite}, protože jde
 * o data, která nejsou dlaždice; poškozený nebo starý soubor se zahodí a vše se spočítá znovu. Běh se dá zrušit a hlásí stav posluchačům z vlákna předpočtu.
 */
@Slf4j
public final class Predpocet {

	/** Zvýšit při změně výpočtu klasifikace, ať se uložené výsledky zahodí. */
	private static final int VERZE_VYPOCTU = 2;

	private static final int KROK_STAVU = 4096;

	private static final int PONECHAT_SOUBORU = 8;

	private final Path adresar;
	private final String klic;
	private final Klasifikator primy;

	private final List<Consumer<StavPredpoctu>> posluchaci = new CopyOnWriteArrayList<>();
	private final ExecutorService vlakno = Executors.newSingleThreadExecutor(r -> {
		final Thread t = new Thread(r, "Předpočet challenge");
		t.setDaemon(true);
		t.setPriority(Thread.NORM_PRIORITY - 2);
		return t;
	});

	private volatile StavPredpoctu stav = StavPredpoctu.NEBEZI;
	private volatile Klasifikator predpocitany;
	private volatile int pocetPrepocitanych;
	private AtomicBoolean zruseno = new AtomicBoolean();
	private Future<?> bezi;

	/**
	 * @param adresar
	 *            složka pro soubory předpočtu
	 * @param klicZdroje
	 *            otisk zdroje dat; mění-li se, předpočet neplatí
	 * @param oblast
	 *            oblast, ve které keše leží; null = bez omezení
	 */
	public Predpocet(final Path adresar, final String klicZdroje, final ZdrojPravidel zdroj, final double lat, final double lon, final Oblast oblast) {
		if (!VypocetSektoru.jePlatnyStred(lat, lon)) {
			throw new IllegalArgumentException("Střed mimo rozsah souřadnic: " + lat + ", " + lon);
		}
		this.adresar = adresar;
		klic = "GKPC" + VERZE_VYPOCTU + "|" + klicZdroje + "|" + (oblast == null ? "-" : oblast.verze()) + "|" + String.format(Locale.ROOT, "%.7f,%.7f", lat, lon) + "|" + zdroj.name();
		primy = VypocetSektoru.primy(zdroj, lat, lon, oblast);
	}

	public StavPredpoctu stav() {
		return stav;
	}

	/** Posluchač dostává každou změnu stavu z vlákna předpočtu; výjimka v něm předpočet nepřeruší. */
	public void pridejPosluchace(final Consumer<StavPredpoctu> posluchac) {
		posluchaci.add(posluchac);
	}

	/** Klasifikátor, který použije předpočítané hodnoty a pro keš, kterou nezná, spočítá polohu přímo; před dokončením předpočtu počítá vždy přímo. */
	public Klasifikator klasifikator() {
		final Klasifikator p = predpocitany;
		return p == null ? primy : p;
	}

	/** Kolik keší se při posledním běhu muselo spočítat (ostatní byly z uloženého předpočtu). */
	public int pocetPrepocitanych() {
		return pocetPrepocitanych;
	}

	/** Spustí předpočet na pozadí a vrátí hned; běžící se zruší a nový začne, až skončí. Vrací úlohu, na kterou jde čekat. */
	public synchronized Future<?> spust(final Collection<? extends VstupKes> kese) {
		// Běžící předpočet se jen požádá o zrušení; nový běh čeká ve frontě vlákna, volající (třeba vlákno Swingu) nečeká.
		zruseno.set(true);
		final AtomicBoolean ruseni = new AtomicBoolean();
		zruseno = ruseni;
		bezi = vlakno.submit(() -> beh(kese, ruseni));
		return bezi;
	}

	/** Zruší běžící předpočet. */
	public synchronized void zrus() {
		zruseno.set(true);
	}

	/** Ukončí vlákno předpočtu. */
	public void zavri() {
		zrus();
		vlakno.shutdownNow();
	}

	/** Pro test: vlákno předpočtu je ukončené. */
	boolean jeZavreno() {
		return vlakno.isShutdown();
	}

	private Path soubor() {
		final CRC32 crc = new CRC32();
		crc.update(klic.getBytes(StandardCharsets.UTF_8));
		return adresar.resolve(String.format("challenge-%08x-%d.bin", crc.getValue(), klic.length()));
	}

	private void ohlas(final StavPredpoctu novy) {
		stav = novy;
		for (final Consumer<StavPredpoctu> p : posluchaci) {
			try {
				p.accept(novy);
			} catch (final RuntimeException e) {
				log.warn("Posluchač předpočtu selhal: {}", e.toString());
			}
		}
	}

	private void beh(final Collection<? extends VstupKes> kese, final AtomicBoolean ruseni) {
		final long celkem = kese.size();
		try {
			ohlas(new StavPredpoctu(StavPredpoctu.Faze.POCITA, 0, celkem, null));
			final long zacatek = System.nanoTime();
			final Path soubor = soubor();
			final Tabulka stara = SouborPredpoctu.nacti(soubor, klic);
			final Tabulka nova = new Tabulka((int) Math.min(Integer.MAX_VALUE / 4, celkem));
			int prepocteno = 0;
			long hotovo = 0;
			for (final VstupKes k : kese) {
				if (ruseni.get() || Thread.currentThread().isInterrupted()) {
					ohlas(new StavPredpoctu(StavPredpoctu.Faze.ZRUSENO, hotovo, celkem, null));
					return;
				}
				final long otisk = SouborPredpoctu.otisk(k.id());
				final int mLat = SouborPredpoctu.mikrostupne(k.lat());
				final int mLon = SouborPredpoctu.mikrostupne(k.lon());
				int kod = stara == null ? Tabulka.NENI : stara.najdi(otisk, mLat, mLon);
				if (kod == Tabulka.NENI) {
					kod = primy.klasifikuj(k).kod();
					prepocteno++;
				}
				nova.vloz(otisk, mLat, mLon, (short) kod);
				if (++hotovo % KROK_STAVU == 0) {
					ohlas(new StavPredpoctu(StavPredpoctu.Faze.POCITA, hotovo, celkem, null));
				}
			}
			final boolean zmena = stara == null || prepocteno > 0 || stara.pocet() != nova.pocet();
			predpocitany = nacteny(nova);
			pocetPrepocitanych = prepocteno;
			if (zmena) {
				uloz(soubor, nova);
			}
			log.info("Předpočet challenge: {} keší, přepočteno {}, za {} ms", celkem, prepocteno, (System.nanoTime() - zacatek) / 1_000_000);
			ohlas(new StavPredpoctu(StavPredpoctu.Faze.HOTOVO, celkem, celkem, null));
		} catch (final RuntimeException e) {
			log.warn("Předpočet challenge selhal: {}", e.toString());
			ohlas(new StavPredpoctu(StavPredpoctu.Faze.CHYBA, stav.hotovo(), celkem, String.valueOf(e.getMessage() != null ? e.getMessage() : e.toString())));
		}
	}

	private Klasifikator nacteny(final Tabulka tabulka) {
		return k -> {
			final int kod = tabulka.najdi(SouborPredpoctu.otisk(k.id()), SouborPredpoctu.mikrostupne(k.lat()), SouborPredpoctu.mikrostupne(k.lon()));
			return kod == Tabulka.NENI ? primy.klasifikuj(k) : Klasifikace.zKodu((short) kod);
		};
	}

	private void uloz(final Path soubor, final Tabulka tabulka) {
		try {
			SouborPredpoctu.zapis(soubor, klic, tabulka);
			uklid(soubor);
		} catch (final IOException e) {
			log.warn("Předpočet challenge nejde uložit, příště se spočítá znovu: {}", e.toString());
		}
	}

	/**
	 * Ponechá nejnovějších několik souborů předpočtu, starší (jiný střed, oblast nebo zdroj) smaže. Maže jen soubory přesného jména předpočtu se značkou a verzí formátu
	 * na začátku (cizí soubory s podobným jménem zůstanou) a zbylé dočasné soubory; jen v této složce, nerekurzivně, až po úspěšném zápisu nového.
	 */
	private void uklid(final Path aktualni) {
		final List<Path> soubory = new ArrayList<>();
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(adresar)) {
			for (final Path p : ds) {
				final Path nazev = p.getFileName();
				if (nazev == null || !Files.isRegularFile(p, java.nio.file.LinkOption.NOFOLLOW_LINKS)) {
					continue;
				}
				final String jmeno = nazev.toString();
				if (SouborPredpoctu.JMENO_DOCASNE.matcher(jmeno).matches()) {
					smaz(p);
				} else if (SouborPredpoctu.JMENO.matcher(jmeno).matches() && SouborPredpoctu.maZnacku(p)) {
					soubory.add(p);
				}
			}
		} catch (final IOException e) {
			return;
		}
		if (soubory.size() <= PONECHAT_SOUBORU) {
			return;
		}
		soubory.sort((a, b) -> Long.compare(cas(b), cas(a)));
		for (int i = PONECHAT_SOUBORU; i < soubory.size(); i++) {
			if (!soubory.get(i).equals(aktualni)) {
				smaz(soubory.get(i));
			}
		}
	}

	private static void smaz(final Path soubor) {
		try {
			Files.deleteIfExists(soubor);
		} catch (final IOException | RuntimeException e) {
			log.debug("Starý předpočet {} nejde smazat: {}", soubor, e.toString());
		}
	}

	private static long cas(final Path p) {
		try {
			return Files.getLastModifiedTime(p).toMillis();
		} catch (final IOException e) {
			return 0;
		}
	}
}
