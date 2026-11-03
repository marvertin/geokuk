package cz.geokuk.plugins.mapy.sprava;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import cz.geokuk.core.coord.PrvniNacitaniMapy;
import cz.geokuk.core.napoveda.Diagnostika;
import cz.geokuk.core.program.FConst;
import cz.geokuk.framework.Event0;
import cz.geokuk.framework.Model0;
import cz.geokuk.plugins.mapy.KachleUmisteniSouboruChangedEvent;
import cz.geokuk.plugins.mapy.kachle.KachleModel;
import cz.geokuk.plugins.mapy.kachle.OfflineMapaChangedEvent;
import cz.geokuk.plugins.mapy.kachle.podklady.KachleZiskavac;
import cz.geokuk.plugins.mapy.sprava.StavMapy.Faze;
import lombok.extern.slf4j.Slf4j;

/**
 * Správa offline map z katalogu: stav map, kontrola na serveru a stažení. Ze serveru se samo stahuje jen katalog vrstev, jinak vše na žádost uživatele, a stahuje se vždy jen jedna
 * mapa (vrstvy nejvýš dvě souběžně). Síť i disk běží na vlastním vlákně, události {@link SpravaMapEvent} chodí na EDT.
 */
@Slf4j
public class SpravaMapModel extends Model0 {

	/** Volné místo, které stahovač nechává na disku navíc; popup podle něj hlídá, jestli se mapa vejde. */
	public static final long REZERVA_MISTA = StahovacMapy.REZERVA_MISTA;

	private static final long INTERVAL_PRUBEHU_NS = TimeUnit.MILLISECONDS.toNanos(250);

	/** Podsložka složky offline map; základní mapy se berou jen přímo ze složky, takže vrstvy mezi ně nepřijdou. */
	public static final String SLOZKA_VRSTEV = "layers";
	/** Složka vrstev z dřívějších bet; při startu se přejmenuje na {@link #SLOZKA_VRSTEV}. */
	static final String STARA_SLOZKA_VRSTEV = "vrstvy";
	/** Kopie staženého katalogu vrstev ve složce vrstev. */
	static final String SOUBOR_KATALOGU = "layers.json";
	/** Přípona souboru vedle kopie katalogu se značkou (ETag) jeho verze na serveru. */
	static final String PRIPONA_ETAG = ".etag";

	private final Supplier<KatalogMap> katalog;
	private final Supplier<KatalogVrstev> katalogVrstev;
	/** Katalog vrstev stažený ze serveru (kopie ve složce vrstev); null = vestavěný. */
	private volatile KatalogVrstev stazenyKatalog;
	private volatile File stazenyKatalogZ;
	URI urlKataloguVrstev = KatalogVrstev.URL_KATALOGU;
	/** Vrstev se stahuje nejvýš tolik současně; mapa jen jedna (zrcadla map mají omezený počet spojení), vedle ní mohou běžet vrstvy. */
	public static final int SOUBEZNE_VRSTVY = 2;
	private final ExecutorService vlakno = vlakno("Stahování offline mapy");
	private final ExecutorService vlaknaVrstev = Executors.newFixedThreadPool(SOUBEZNE_VRSTVY, r -> {
		final Thread t = new Thread(r, "Stahování vrstvy");
		t.setDaemon(true);
		return t;
	});
	/** Kontroly a stav z disku, aby nečekaly na dlouhé stahování. */
	private final ExecutorService kontrola = vlakno("Kontrola offline map");
	private final Map<String, StavMapy> stavy = new ConcurrentHashMap<>();
	private final Map<String, StavVrstvy> stavyVrstev = new ConcurrentHashMap<>();
	/** Právě stahovaná mapa nebo vrstva; stahuje se vždy jen jedna. */
	private final AtomicReference<KatalogovaMapa> beziciMapa = new AtomicReference<>();
	private final Set<KatalogovaVrstva> beziciVrstvy = ConcurrentHashMap.newKeySet();
	/** Pořadí spuštěných stahování; zrušení platí pro všechna spuštěná do {@link #zrusenoDo}. */
	private final AtomicLong poradi = new AtomicLong();
	private volatile long zrusenoDo;
	/** Konec posledního stahování; dávka (průvodce, Stáhnout vše) mezi položkami chvíli nic nestahuje. */
	private volatile long konecStahovaniNs = System.nanoTime() - DOZNIVANI_DAVKY_NS;
	static final long DOZNIVANI_DAVKY_NS = TimeUnit.SECONDS.toNanos(15);
	private final StahovacMapy stahovac;
	private final StahovacVrstev stahovacVrstev;
	private Supplier<File> slozka = () -> null;
	private Consumer<Event0<SpravaMapModel>> odpalovac = e -> SwingUtilities.invokeLater(() -> fire(e));
	private KachleModel kachleModel;
	/** Kdy se naposledy ohlásil průběh které mapy nebo vrstvy; souběžná stahování se při ohlašování nepřebíjejí. */
	private final Map<String, Long> posledniPrubeh = new ConcurrentHashMap<>();
	/** Jak často program sám ověří u serveru, jestli nemá novější katalog vrstev, než je kopie ve složce vrstev. */
	long stariKataloguMs = TimeUnit.HOURS.toMillis(1);
	/** Po nepovedeném samočinném stažení katalogu vrstev (třeba bez sítě) se další zkusí nejdřív za tuto dobu. */
	long opakovaniKataloguNs = TimeUnit.HOURS.toNanos(1);
	/** Prodlevy před dalšími pokusy, když se stahování mapy nebo vrstvy přeruší; pokračuje se od staženého místa. */
	long[] prodlevyOpakovaniMs = { 5_000, 15_000, 30_000 };
	/** Čas posledního nepovedeného samočinného stažení katalogu, jen na vlákně kontroly; null = žádné. */
	private Long posledniPokusKatalogu;
	/** Za jak dlouho po dokončení startu okna se poprvé ověří katalog vrstev; do té doby stavba okna nesoutěží o procesor a hlavní vlákno se sítí. */
	long zpozdeniPoStartuMs = TimeUnit.SECONDS.toMillis(3);
	volatile boolean startDokoncen;
	/** První pohled na mapu proběhl; do té doby se stavy map z disku při startu a při změně složky jen odkládají. */
	BooleanSupplier poPrvnimPohledu = () -> PrvniNacitaniMapy.jeUkonceno() && Diagnostika.dobaBehuMs() >= PrvniNacitaniMapy.LIMIT_MS;
	long kontrolaPoPrvnimPohleduMs = TimeUnit.SECONDS.toMillis(1);
	/** Stavy čekají na první pohled; otevření nabídky nebo dialogu je přečte hned. */
	private final AtomicBoolean obnovaPoPohleduCeka = new AtomicBoolean();
	/** Obnova stavů už čeká na vlákně kontroly; další žádosti se s ní sloučí, přečte aktuální stav. */
	private final AtomicBoolean obnovaStavuCeka = new AtomicBoolean();

	/** Požádá blok Mapy ve stavovém řádku, aby otevřel panel offline map s fokusem. */
	public void otevriPanel() {
		odpalovac.accept(new OtevriPanelMapEvent());
	}

	private static ExecutorService vlakno(final String jmeno) {
		return Executors.newSingleThreadExecutor(r -> {
			final Thread t = new Thread(r, jmeno);
			t.setDaemon(true);
			return t;
		});
	}

	public SpravaMapModel() {
		this(KatalogMap::vestaveny, KatalogVrstev::vestaveny);
	}

	SpravaMapModel(final KatalogMap katalog) {
		this(() -> katalog, KatalogVrstev::vestaveny);
	}

	SpravaMapModel(final KatalogMap katalog, final KatalogVrstev katalogVrstev) {
		this(() -> katalog, () -> katalogVrstev);
	}

	/** Katalogy se načtou hned na pozadí, ať je GUI nečte poprvé na EDT. */
	SpravaMapModel(final Supplier<KatalogMap> katalog, final Supplier<KatalogVrstev> katalogVrstev) {
		this.katalog = katalog;
		this.katalogVrstev = katalogVrstev;
		kontrola.execute(() -> {
			katalog.get();
			katalogVrstev.get();
		});
		stahovac = new StahovacMapy("GeoKuk/" + FConst.VERSION + " (" + FConst.WEB_PAGE_URL + ")", this::uvolniOfflineMapy);
		stahovacVrstev = new StahovacVrstev(stahovac);
	}

	public void inject(final KachleModel kachleModel) {
		this.kachleModel = kachleModel;
		slozka = () -> kachleModel.getUmisteniSouboru() == null ? null : kachleModel.getUmisteniSouboru().getOfflineMapyDir().getEffectiveFile();
	}

	/** Offline mapy dostanou živé katalogy, podle kterých se katalogovým mapám a vrstvám přiřadí obrys; po změně katalogu se otevřou znovu. */
	public void inject(final KachleZiskavac ziskavac) {
		ziskavac.getOfflineMapy().setKatalogy(this::getKatalog, this::getKatalogVrstevProOtevreni);
		ziskavac.getOfflineTrasy().setKatalogy(this::getKatalog, this::getKatalogVrstevProOtevreni);
	}

	/** Pro testy: složka a odpalování událostí bez Swingu. */
	void nastavProTest(final Supplier<File> slozka, final Consumer<Event0<SpravaMapModel>> odpalovac) {
		this.slozka = slozka;
		this.odpalovac = odpalovac;
	}

	/** Pro testy map: jen události map. */
	void nastavProTestMap(final Supplier<File> slozka, final Consumer<SpravaMapEvent> odpalovac) {
		nastavProTest(slozka, e -> {
			if (e instanceof SpravaMapEvent) {
				odpalovac.accept((SpravaMapEvent) e);
			}
		});
	}

	StahovacMapy getStahovac() {
		return stahovac;
	}

	StahovacVrstev getStahovacVrstev() {
		return stahovacVrstev;
	}

	@Override
	protected void initAndFire() {
		obnovStavyPoPrvnimPohledu();
		obnovKatalogVrstevKdyzJeStary();
	}

	public void onEvent(final OfflineMapaChangedEvent event) {
		obnovStavyPoPrvnimPohledu();
	}

	public void onEvent(final KachleUmisteniSouboruChangedEvent event) {
		obnovStavyPoPrvnimPohledu();
		obnovKatalogVrstevKdyzJeStary();
	}

	public KatalogMap getKatalog() {
		return katalog.get();
	}

	/** Poslední známý stav mapy, bez sítě a bez disku. */
	public StavMapy getStav(final KatalogovaMapa mapa) {
		return stavy.computeIfAbsent(mapa.getId(), id -> StavMapy.novy(mapa));
	}

	/** Běžící stahování mapy; prázdné, když se nestahuje nic nebo se stahuje vrstva. */
	public Optional<StavMapy> getBeziciStahovani() {
		final KatalogovaMapa m = beziciMapa.get();
		return m == null ? Optional.empty() : Optional.of(getStav(m));
	}

	/** Katalog vrstev: naposledy stažený ze serveru, jinak vestavěný. */
	public KatalogVrstev getKatalogVrstev() {
		final KatalogVrstev k = stazenyKatalog;
		return k != null ? k : katalogVrstev.get();
	}

	/**
	 * Katalog vrstev pro otevření offline map: uložený katalog se načte už před prvním otevřením, jinak by se mapy po jeho načtení na vlákně kontroly
	 * otevřely znovu. Nevolat z EDT.
	 */
	KatalogVrstev getKatalogVrstevProOtevreni() {
		final File s = slozka.get();
		if (s != null && stazenyKatalogZ == null) {
			nactiStazenyKatalog(new File(s, SLOZKA_VRSTEV));
		}
		return getKatalogVrstev();
	}

	/**
	 * Stáhne aktuální katalog vrstev ze serveru a uloží ho do složky vrstev, kde ho program najde i po dalším startu. Výsledek přijde
	 * událostí {@link KatalogVrstevEvent}.
	 */
	public void obnovKatalogVrstev() {
		kontrola.execute(() -> {
			final File sv = slozkaVrstevNaPozadi();
			if (sv == null) {
				odpalovac.accept(new KatalogVrstevEvent(getKatalogVrstev(), "Složka offline map není nastavená."));
				return;
			}
			try {
				stahniKatalogVrstev(sv, false);
			} catch (final IOException e) {
				log.info("Katalog vrstev nejde stáhnout: {}", e.toString());
				odpalovac.accept(new KatalogVrstevEvent(getKatalogVrstev(),
						e instanceof StahovacMapy.ChybaProUzivatele ? e.getMessage() : "Katalog vrstev nejde stáhnout ani načíst."));
			}
			obnovStavy();
		});
	}

	/**
	 * Stáhne katalog vrstev na pozadí, když jeho kopie ve složce vrstev chybí, nebo když je kopie starší než {@link #stariKataloguMs} a server má novější
	 * (podmíněný dotaz podle ETag, nezměněný katalog se nestahuje). Bez sítě nebo při chybě serveru
	 * zůstane poslední kopie (nebo vestavěný katalog) a uživatel nic nevidí; další pokus nejdřív za {@link #opakovaniKataloguNs}.
	 */
	public void obnovKatalogVrstevKdyzJeStary() {
		if (startDokoncen) {
			obnovKatalogVrstevPodminene(false);
		}
	}

	/** Okno je zobrazené a start dokončený: po {@link #zpozdeniPoStartuMs} se poprvé ověří katalog vrstev, dřív se samočinně nestahuje. */
	public void poStartu() {
		if (zpozdeniPoStartuMs <= 0) {
			dokonciStart();
			return;
		}
		final Thread t = new Thread(() -> {
			try {
				Thread.sleep(zpozdeniPoStartuMs);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
			dokonciStart();
		}, "Katalog vrstev po startu");
		t.setDaemon(true);
		t.start();
	}

	private void dokonciStart() {
		startDokoncen = true;
		obnovKatalogVrstevPodminene(false);
	}

	/** Hned ověří u serveru, jestli nemá novější katalog vrstev (podmíněný dotaz, nezměněný se nestahuje); na žádost uživatele. */
	public void zkontrolujKatalogVrstev() {
		obnovKatalogVrstevPodminene(true);
	}

	private void obnovKatalogVrstevPodminene(final boolean hned) {
		kontrola.execute(() -> {
			final File sv = slozkaVrstevNaPozadi();
			final long ted = System.nanoTime();
			if (sv == null || !hned && posledniPokusKatalogu != null && ted - posledniPokusKatalogu < opakovaniKataloguNs) {
				return;
			}
			final File kopie = new File(sv, SOUBOR_KATALOGU);
			if (!hned && kopie.isFile() && System.currentTimeMillis() - kopie.lastModified() < stariKataloguMs) {
				return;
			}
			try {
				posledniPokusKatalogu = null;
				if (stahniKatalogVrstev(sv, true)) {
					obnovStavy();
				}
			} catch (final IOException e) {
				posledniPokusKatalogu = ted;
				log.info("Katalog vrstev nejde stáhnout, platí poslední známý: {}", e.toString());
			}
		});
	}

	/**
	 * Stáhne katalog vrstev, uloží jeho kopii do složky vrstev a ohlásí ho. Podmíněně stáhne jen katalog změněný od kopie; vrátí false, když se nezměnil.
	 * Volá se na vlákně kontroly.
	 */
	private boolean stahniKatalogVrstev(final File sv, final boolean podminene) throws IOException {
		final File kopie = new File(sv, SOUBOR_KATALOGU);
		final File souborEtag = new File(sv, SOUBOR_KATALOGU + PRIPONA_ETAG);
		String etag = null;
		if (podminene && kopie.isFile() && souborEtag.isFile()) {
			etag = new String(Files.readAllBytes(souborEtag.toPath()), StandardCharsets.UTF_8).trim();
		}
		final String[] novyEtag = new String[1];
		final String text = stahovacVrstev.stahniKatalog(urlKataloguVrstev, etag == null || etag.isEmpty() ? null : etag, e -> novyEtag[0] = e);
		if (text == null) {
			if (!kopie.setLastModified(System.currentTimeMillis())) {
				log.info("Čas ověření katalogu vrstev nejde zapsat do {}", kopie);
			}
			return false;
		}
		final KatalogVrstev k = KatalogVrstev.nacti(text, getKatalog());
		Files.createDirectories(sv.toPath());
		final File docasny = new File(sv, SOUBOR_KATALOGU + StahovacMapy.PRIPONA);
		Files.write(docasny.toPath(), text.getBytes(StandardCharsets.UTF_8));
		Files.move(docasny.toPath(), kopie.toPath(), StandardCopyOption.REPLACE_EXISTING);
		if (novyEtag[0] == null) {
			Files.deleteIfExists(souborEtag.toPath());
		} else {
			Files.write(souborEtag.toPath(), novyEtag[0].getBytes(StandardCharsets.UTF_8));
		}
		zmenKatalogVrstev(k, sv);
		odpalovac.accept(new KatalogVrstevEvent(k, null));
		return true;
	}

	/** Kopie staženého katalogu ve složce vrstev; čte se jednou pro každou složku, na vlákně kontroly nebo před prvním otevřením offline map. */
	private synchronized void nactiStazenyKatalog(final File sv) {
		if (sv.equals(stazenyKatalogZ)) {
			return;
		}
		KatalogVrstev k = null;
		final File f = new File(sv, SOUBOR_KATALOGU);
		if (f.isFile()) {
			try {
				k = KatalogVrstev.nacti(new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8), getKatalog());
			} catch (final IOException | RuntimeException e) {
				log.info("Uložený katalog vrstev {} nejde načíst, platí vestavěný: {}", f, e.toString());
			}
		}
		if (k == null && stazenyKatalog == null) {
			stazenyKatalogZ = sv;
			return;
		}
		zmenKatalogVrstev(k, sv);
		odpalovac.accept(new KatalogVrstevEvent(getKatalogVrstev(), null));
	}

	private synchronized void zmenKatalogVrstev(final KatalogVrstev k, final File z) {
		stazenyKatalog = k;
		stazenyKatalogZ = z;
	}

	/** Složka vrstev pro práci na pozadí: složku z dřívějších bet napřed přejmenuje, ať uživatel nepřijde o stažené vrstvy. */
	private File slozkaVrstevNaPozadi() {
		final File s = slozka.get();
		if (s == null) {
			return null;
		}
		final File nova = new File(s, SLOZKA_VRSTEV);
		final File stara = new File(s, STARA_SLOZKA_VRSTEV);
		if (stara.isDirectory() && !nova.exists()) {
			try {
				Files.move(stara.toPath(), nova.toPath());
				// Kopie katalogu ve starém formátu; nový se stáhne znovu.
				Files.deleteIfExists(new File(nova, "vrstvy.json").toPath());
				log.info("Složka vrstev {} přejmenována na {}", stara, nova);
			} catch (final IOException e) {
				log.warn("Složku vrstev {} nejde přejmenovat na {}: {}", stara, nova, e.toString());
			}
		}
		return nova;
	}

	/** Složka, kam se stahují vrstvy; null, když složka offline map není nastavená. */
	public File getSlozkaVrstev() {
		final File s = slozka.get();
		return s == null ? null : new File(s, SLOZKA_VRSTEV);
	}

	/** Poslední známý stav vrstvy, bez sítě a bez disku; po novém katalogu vrstev platí starý stav, dokud ho {@link #obnovStavy()} nepřečte znovu. */
	public StavVrstvy getStav(final KatalogovaVrstva vrstva) {
		final StavVrstvy s = stavyVrstev.computeIfAbsent(vrstva.getId(), id -> StavVrstvy.novy(vrstva));
		return s.getVrstva() == vrstva || s.isBezi() ? s : s.sVrstvou(vrstva);
	}

	/** Jedno z běžících stahování vrstev; prázdné, když se žádná vrstva nestahuje. */
	public Optional<StavVrstvy> getBeziciStahovaniVrstvy() {
		return getBeziciStahovaniVrstev().stream().findFirst();
	}

	/** Běžící stahování vrstev (nejvýš {@link #SOUBEZNE_VRSTVY}). */
	public List<StavVrstvy> getBeziciStahovaniVrstev() {
		return beziciVrstvy.stream().map(this::getStav).collect(Collectors.toList());
	}

	/** Stáhne všechny soubory vrstvy do {@link #getSlozkaVrstev()}. False, když se tato vrstva už stahuje nebo jich běží {@link #SOUBEZNE_VRSTVY}. */
	public boolean stahni(final KatalogovaVrstva vrstva) {
		synchronized (beziciVrstvy) {
			if (beziciVrstvy.size() >= SOUBEZNE_VRSTVY || !beziciVrstvy.add(vrstva)) {
				return false;
			}
		}
		final BooleanSupplier zruseno = novyZruseni();
		nastavDavkuOfflineMapam();
		vlaknaVrstev.execute(() -> {
			try {
				final File s = slozkaVrstevNaPozadi();
				if (s == null) {
					zmena(getStav(vrstva).sFazi(Faze.CHYBA, "Složka offline map není nastavená."));
					return;
				}
				StavVrstvy st = stahovacVrstev.stahni(getStav(vrstva), s, zruseno, this::prubeh);
				for (int pokus = 0; st.getFaze() == Faze.CHYBA && StahovacMapy.lzeOpakovat(st.getChyba()) && pokus < prodlevyOpakovaniMs.length; pokus++) {
					log.info("Stahování vrstvy {} se přerušilo ({}), pokus {} za {} ms", vrstva, st.getChyba(), pokus + 1, prodlevyOpakovaniMs[pokus]);
					zmena(st.sFazi(Faze.STAHUJE, textOpakovani(pokus)));
					if (!pockejNaOpakovani(prodlevyOpakovaniMs[pokus], zruseno)) {
						st = st.sFazi(Faze.ZRUSENO, null);
						break;
					}
					st = stahovacVrstev.stahni(getStav(vrstva), s, zruseno, this::prubeh);
				}
				zmena(st);
			} catch (final RuntimeException e) {
				log.warn("Stahování vrstvy {} selhalo", vrstva, e);
				zmena(getStav(vrstva).sFazi(Faze.CHYBA, "Stahování selhalo, podrobnosti jsou v logu."));
			} finally {
				beziciVrstvy.remove(vrstva);
				konecStahovaniNs = System.nanoTime();
			}
		});
		return true;
	}

	/** Zahodí rozpracované stahování vrstvy (ne to, které právě běží). */
	public void zahodRozpracovane(final KatalogovaVrstva vrstva) {
		kontrola.execute(() -> {
			final File s = slozkaVrstevNaPozadi();
			if (s != null && !beziciVrstvy.contains(vrstva)) {
				StahovacVrstev.zahod(s, vrstva);
				zmena(StahovacVrstev.mistniStav(getStav(vrstva).sFazi(Faze.NIC, null), s));
			}
		});
	}

	/** Stavy map z disku nikdo nepotřebuje k prvnímu pohledu na mapu, přečtou se po něm; kdo je potřebuje dřív, volá {@link #obnovStavy()}. Stavy vrstev se čtou hned. */
	private void obnovStavyPoPrvnimPohledu() {
		if (poPrvnimPohledu.getAsBoolean()) {
			obnovStavy();
			return;
		}
		obnovStavyVrstevHned();
		if (!obnovaPoPohleduCeka.compareAndSet(false, true)) {
			return;
		}
		final Thread t = new Thread(() -> {
			try {
				while (obnovaPoPohleduCeka.get() && !poPrvnimPohledu.getAsBoolean()) {
					Thread.sleep(kontrolaPoPrvnimPohleduMs);
				}
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
			if (obnovaPoPohleduCeka.getAndSet(false)) {
				obnovStavy();
			}
		}, "Stavy map po prvním pohledu");
		t.setDaemon(true);
		t.start();
	}

	/** Přečte stav všech katalogových map z disku (bez sítě); jedna změna přijde svou událostí, víc změn jednou {@link StavyMapEvent}. */
	public void obnovStavy() {
		obnovaPoPohleduCeka.set(false);
		if (!obnovaStavuCeka.compareAndSet(false, true)) {
			return;
		}
		kontrola.execute(() -> {
			obnovaStavuCeka.set(false);
			final File s = slozka.get();
			if (s == null) {
				return;
			}
			final List<Event0<SpravaMapModel>> zmeny = new java.util.ArrayList<>();
			for (final KatalogovaMapa m : katalog.get().getMapy()) {
				final StavMapy predtim = getStav(m);
				if (predtim.isBezi()) {
					continue;
				}
				final StavMapy ted = StahovacMapy.mistniStav(predtim, s);
				stavy.put(m.getId(), ted);
				if (!ted.toString().equals(predtim.toString())) {
					zmeny.add(new SpravaMapEvent(ted));
				}
			}
			obnovStavyVrstev(zmeny);
			ohlasZmeny(zmeny);
		});
	}

	/** Stavy vrstev potřebuje hned atribuce mapy (uvádí se jen u vrstev, které máme), proto se čtou i před prvním pohledem. */
	private void obnovStavyVrstevHned() {
		kontrola.execute(() -> {
			if (slozka.get() == null) {
				return;
			}
			final List<Event0<SpravaMapModel>> zmeny = new java.util.ArrayList<>();
			obnovStavyVrstev(zmeny);
			ohlasZmeny(zmeny);
		});
	}

	/** Jen na vlákně kontroly. */
	private void obnovStavyVrstev(final List<Event0<SpravaMapModel>> zmeny) {
		final File sv = slozkaVrstevNaPozadi();
		nactiStazenyKatalog(sv);
		for (final KatalogovaVrstva v : getKatalogVrstev().getVrstvy()) {
			final StavVrstvy predtim = stavyVrstev.computeIfAbsent(v.getId(), id -> StavVrstvy.novy(v));
			if (predtim.isBezi()) {
				continue;
			}
			// Stav z dřívějšího katalogu vrstev se přečte znovu podle nové vrstvy.
			final StavVrstvy ted = StahovacVrstev.mistniStav(predtim.getVrstva() == v ? predtim : StavVrstvy.novy(v), sv);
			stavyVrstev.put(v.getId(), ted);
			if (!ted.toString().equals(predtim.toString())) {
				zmeny.add(new SpravaVrstevEvent(ted));
			}
		}
	}

	private void ohlasZmeny(final List<Event0<SpravaMapModel>> zmeny) {
		if (zmeny.size() == 1) {
			odpalovac.accept(zmeny.get(0));
		} else if (!zmeny.isEmpty()) {
			odpalovac.accept(new StavyMapEvent());
		}
	}

	/** Stáhne mapu do složky offline map. False, když se už nějaká mapa stahuje. */
	public boolean stahni(final KatalogovaMapa mapa) {
		if (!beziciMapa.compareAndSet(null, mapa)) {
			return false;
		}
		final BooleanSupplier zruseno = novyZruseni();
		nastavDavkuOfflineMapam();
		vlakno.execute(() -> {
			try {
				final File s = slozka.get();
				if (s == null) {
					zmena(getStav(mapa).sFazi(Faze.CHYBA, "Složka offline map není nastavená."));
					return;
				}
				StavMapy st = stahovac.stahni(getStav(mapa), s, zruseno, this::prubeh);
				for (int pokus = 0; st.getFaze() == Faze.CHYBA && StahovacMapy.lzeOpakovat(st.getChyba()) && pokus < prodlevyOpakovaniMs.length; pokus++) {
					log.info("Stahování mapy {} se přerušilo ({}), pokus {} za {} ms", mapa, st.getChyba(), pokus + 1, prodlevyOpakovaniMs[pokus]);
					zmena(st.sFazi(Faze.STAHUJE, textOpakovani(pokus)));
					if (!pockejNaOpakovani(prodlevyOpakovaniMs[pokus], zruseno)) {
						st = st.sFazi(Faze.ZRUSENO, null);
						break;
					}
					st = stahovac.stahni(getStav(mapa), s, zruseno, this::prubeh);
				}
				zmena(st);
			} catch (final RuntimeException e) {
				log.warn("Stahování mapy {} selhalo", mapa, e);
				zmena(getStav(mapa).sFazi(Faze.CHYBA, "Stahování selhalo, podrobnosti jsou v logu."));
			} finally {
				beziciMapa.set(null);
				konecStahovaniNs = System.nanoTime();
			}
		});
		return true;
	}

	private String textOpakovani(final int pokus) {
		return "Spojení se přerušilo, další pokus za " + prodlevyOpakovaniMs[pokus] / 1000 + " s (" + (pokus + 1) + ". ze " + prodlevyOpakovaniMs.length + ").";
	}

	/** Počká před dalším pokusem o stažení; false, když uživatel mezitím stahování zrušil. */
	private static boolean pockejNaOpakovani(final long ms, final BooleanSupplier zruseno) {
		final long konec = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(ms);
		while (!zruseno.getAsBoolean()) {
			final long zbyva = konec - System.nanoTime();
			if (zbyva <= 0) {
				return true;
			}
			try {
				Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(zbyva) + 1, 200));
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
				return false;
			}
		}
		return false;
	}

	/** Zruší všechna běžící stahování (mapu i vrstvy); rozpracované soubory zůstanou pro pokračování. */
	public void zrusStahovani() {
		zrusenoDo = poradi.get();
	}

	/** Příznak zrušení pro nově spuštěné stahování: platí, když se po spuštění zavolalo {@link #zrusStahovani()}. */
	private BooleanSupplier novyZruseni() {
		final long id = poradi.incrementAndGet();
		return () -> id <= zrusenoDo;
	}

	/** Zahodí rozpracované stahování mapy (ne to, které právě běží). */
	public void zahodRozpracovane(final KatalogovaMapa mapa) {
		kontrola.execute(() -> {
			final File s = slozka.get();
			if (s != null && beziciMapa.get() != mapa) {
				StahovacMapy.zahod(s, mapa);
				zmena(StahovacMapy.mistniStav(getStav(mapa).sPrubehem(0, -1, false).sFazi(Faze.NIC, null), s));
			}
		});
	}

	/** Zeptá se serveru na jednu mapu (podmíněný HEAD). Jen na výslovnou žádost uživatele. */
	public void zkontroluj(final KatalogovaMapa mapa) {
		if (getStav(mapa).isBezi()) {
			return;
		}
		kontrola.execute(() -> {
			final File s = slozka.get();
			if (s == null || getStav(mapa).isBezi() || beziciMapa.get() == mapa) {
				return;
			}
			zmena(stahovac.zkontroluj(getStav(mapa), s, this::prubeh));
		});
	}

	private void prubeh(final StavMapy stav) {
		final StavMapy predtim = stavy.put(stav.getMapa().getId(), stav);
		final long ted = System.nanoTime();
		if (predtim == null || predtim.getFaze() != stav.getFaze() || ted - posledniPrubeh.getOrDefault(stav.getMapa().getId(), 0L) >= INTERVAL_PRUBEHU_NS) {
			posledniPrubeh.put(stav.getMapa().getId(), ted);
			odpalovac.accept(new SpravaMapEvent(stav));
		}
	}

	private void prubeh(final StavVrstvy stav) {
		final StavVrstvy predtim = stavyVrstev.put(stav.getVrstva().getId(), stav);
		final long ted = System.nanoTime();
		if (predtim == null || predtim.getFaze() != stav.getFaze() || ted - posledniPrubeh.getOrDefault(stav.getVrstva().getId(), 0L) >= INTERVAL_PRUBEHU_NS) {
			posledniPrubeh.put(stav.getVrstva().getId(), ted);
			odpalovac.accept(new SpravaVrstevEvent(stav));
		}
	}

	private void zmena(final StavVrstvy stav) {
		stavyVrstev.put(stav.getVrstva().getId(), stav);
		odpalovac.accept(new SpravaVrstevEvent(stav));
	}

	private void zmena(final StavMapy stav) {
		stavy.put(stav.getMapa().getId(), stav);
		odpalovac.accept(new SpravaMapEvent(stav));
	}

	/** Stahuje se, nebo se stahovalo před chvílí (mezi položkami dávky). */
	public boolean isDavka() {
		return beziciMapa.get() != null || !beziciVrstvy.isEmpty() || System.nanoTime() - konecStahovaniNs < DOZNIVANI_DAVKY_NS;
	}

	/** Offline mapy během dávky neotevírají znovu po každém staženém souboru. */
	private void nastavDavkuOfflineMapam() {
		final KachleModel k = kachleModel;
		if (k != null && k.getZiskavac() != null) {
			k.getZiskavac().getOfflineMapy().setDavka(this::isDavka);
			k.getZiskavac().getOfflineTrasy().setDavka(this::isDavka);
		}
	}

	/** Zavře otevřené offline mapy, aby šlo starou mapu nahradit (Windows otevřený soubor nepřepíše); otevřou se znovu při dalším kreslení. */
	private void uvolniOfflineMapy() {
		final KachleModel k = kachleModel;
		if (k != null && k.getZiskavac() != null) {
			k.getZiskavac().getOfflineMapy().zavri();
			k.getZiskavac().getOfflineTrasy().zavri();
		}
	}

	/** Pro testy: počká, až vlákno dokončí zadanou práci. */
	void pockej() throws InterruptedException {
		for (final ExecutorService e : new ExecutorService[] { kontrola, vlakno, vlaknaVrstev }) {
			final java.util.concurrent.CountDownLatch l = new java.util.concurrent.CountDownLatch(1);
			e.execute(l::countDown);
			if (!l.await(30, TimeUnit.SECONDS)) {
				throw new IllegalStateException("Vlákno správy map nedoběhlo");
			}
		}
		// Vrstvy běží na více vláknech, úloha výše mohla doběhnout na volném z nich.
		final long konec = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
		while (!beziciVrstvy.isEmpty()) {
			if (System.nanoTime() > konec) {
				throw new IllegalStateException("Stahování vrstev nedoběhlo");
			}
			Thread.sleep(10);
		}
	}
}
