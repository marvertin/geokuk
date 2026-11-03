package cz.geokuk.plugins.mapy.kachle.podklady;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import cz.geokuk.core.coordinates.Wgs;

import lombok.extern.slf4j.Slf4j;

/**
 * Offline mapa: všechny soubory .map ve složce vykreslené jedním tématem. Složku průběžně sleduje, takže nově zkopírovaná nebo vyměněná mapa se projeví bez
 * restartu.
 */
@Slf4j
public class OfflineMapy {

	private static final long KONTROLA_SLOZKY_NS = TimeUnit.SECONDS.toNanos(2);
	private static final long OPAKOVANI_NS = TimeUnit.SECONDS.toNanos(30);

	private final Runnable priZmene;
	/** Průhledný překryv přes jiný podklad, viz {@link OfflineRenderer#otevri(List, OfflineRenderer.NacteneTema, boolean)}. */
	private final boolean prekryv;
	/** Zavolá se po každém otevření map s klíčem dlaždic a souborem symbolů (null = neukládají se). */
	private volatile Supplier<cz.geokuk.plugins.mapy.sprava.KatalogMap> katalog = cz.geokuk.plugins.mapy.sprava.KatalogMap::vestaveny;
	private volatile Supplier<cz.geokuk.plugins.mapy.sprava.KatalogVrstev> katalogVrstev = cz.geokuk.plugins.mapy.sprava.KatalogVrstev::vestaveny;
	/** Katalogy, podle kterých jsou otevřené mapy; po jejich změně se mapy otevřou znovu s novými obrysy. */
	private cz.geokuk.plugins.mapy.sprava.KatalogMap katalogOtevreny;
	private cz.geokuk.plugins.mapy.sprava.KatalogVrstev katalogVrstevOtevreny;
	private volatile BiConsumer<OblastniKlic, File> priOtevreni = (klic, symboly) -> {};

	/** Jak často se nejvýš dívá do složky, jestli se mapy nezměnily. */
	long kontrolaSlozkyNs = KONTROLA_SLOZKY_NS;
	/** Za jak dlouho se znovu zkusí mapa, kterou nešlo otevřít (třeba ji drží antivirus nebo synchronizace). */
	long opakovaniNs = OPAKOVANI_NS;

	// Nastavení se mění z EDT bez zámku; vlákno, které právě otevírá mapy pod zámkem, by EDT zdrželo.
	private volatile File slozka;
	/** Kam se ukládají vykreslené symboly témat, null = nikam. */
	private volatile File slozkaSymbolu;
	private volatile TemaOfflineMapy tema = TemaOfflineMapy.VYCHOZI;
	/** Měřítko displeje (1 = 100 %), podle něj se kreslí větší dlaždice. */
	private volatile double meritko = 1;
	/** Násobek velikosti písma a značek na mapě (1 = 100 %). */
	private volatile double pismo = 1;
	/** Překryv tras: vrstvy tras už kreslí podklad (offline mapa s tématem, které je umí), překryv je vynechá. */
	private volatile boolean trasyKresliPodklad;

	/** Nastavení, podle kterého jsou otevřené mapy; mění se jen v {@link #pouzij()}. */
	private File slozkaOtevrena;
	private TemaOfflineMapy temaOtevrene;
	private double meritkoOtevrene;
	private double pismoOtevrene;
	private boolean trasyKresliPodkladOtevrene;

	private volatile OfflineRenderer renderer;
	/** Názvy souborů otevřených map, podle nich se volí atribuce. */
	private volatile List<String> nazvyMap = Collections.emptyList();
	/** Téma se drží i přes změnu map, jeho načtení trvá u velkých témat sekundy. */
	private OfflineRenderer.NacteneTema nacteneTema;
	private volatile VrstvyTematu vrstvyTematu;
	/** Čte se i bez zámku pro informace pro hlášení chyby. */
	private volatile IOException chyba;
	/** Soubory, jejich velikosti a časy, ze kterých je renderer nebo chyba; null = zatím nic. */
	/** Stínování terénu z DEM dlaždic je zapnuté. */
	private boolean stinovani = true;
	private String otiskSlozky;
	/** Změněný otisk, který se teprve ověřuje: soubor, který se právě kopíruje, se mezi kontrolami mění. */
	private String otiskZmeny;
	private long posledniKontrola;
	/** Při hromadném stahování se změna složky projeví nejvýš jednou za {@link #odstupPriDavceNs}; jinak by každá stažená vrstva znovu otevřela všechny mapy. */
	private volatile BooleanSupplier davka = () -> false;
	long odstupPriDavceNs = TimeUnit.SECONDS.toNanos(30);
	/** Kdy se mapy naposledy otevřely kvůli změně ve složce; null = zatím ne. */
	private Long otevrenoPriZmene;
	/** Některé mapy nešly otevřít a zkusí se znovu od {@link #opakovatOd}. */
	private boolean opakovat;
	private long opakovatOd;
	private long otevrenoMs;
	/** Výsledek posledního otevření (chyba, nebo počet otevřených map); opakovaný pokus se stejným výsledkem obrazovku nezneplatní. */
	private String vysledekOtevreni;

	/**
	 * @param priZmene
	 *            zavolá se, když se změní vykreslování (mapy nebo téma, ne při prvním otevření), aby se zahodily dlaždice v paměti
	 */
	public OfflineMapy(final Runnable priZmene) {
		this(priZmene, false);
	}

	public OfflineMapy(final Runnable priZmene, final boolean prekryv) {
		this.priZmene = priZmene;
		this.prekryv = prekryv;
	}

	public void nastav(final File slozka, final TemaOfflineMapy tema) {
		this.slozka = slozka;
		this.tema = tema;
	}

	/** Jen pro překryv tras: podklad sám kreslí stažené vrstvy tras, překryv je proto nebere. */
	public void setTrasyKresliPodklad(final boolean kresli) {
		trasyKresliPodklad = kresli;
	}

	public boolean isTrasyKresliPodklad() {
		return trasyKresliPodklad;
	}

	/** Nastaví měřítko displeje, zaokrouhlené na čtvrtiny v rozsahu 1–3; při změně se mapy vykreslí znovu. */
	public void nastavMeritko(final double meritkoDispleje) {
		meritko = zaokrouhliMeritko(meritkoDispleje);
	}

	static double zaokrouhliMeritko(final double meritkoDispleje) {
		if (Double.isNaN(meritkoDispleje)) {
			return 1;
		}
		return Math.max(1, Math.min(3, Math.round(meritkoDispleje * 4) / 4.0));
	}

	/** Nastaví velikost písma a značek (1 = 100 %), zaokrouhlenou na 5 % v rozsahu 80–200 %; mapy se vykreslí znovu. */
	public void nastavPismo(final double velikost) {
		pismo = zaokrouhliPismo(velikost);
	}

	public static double zaokrouhliPismo(final double velikost) {
		if (Double.isNaN(velikost)) {
			return 1;
		}
		return Math.max(0.8, Math.min(2, Math.round(velikost * 20) / 20.0));
	}

	public double getMeritko() {
		return meritko;
	}

	/** Zda právě běží hromadné stahování; změny složky se pak otevírají s odstupem. */
	public void setDavka(final BooleanSupplier davka) {
		this.davka = davka;
	}

	/** Právě se stahují mapy nebo vrstvy, viz {@link #setDavka(BooleanSupplier)}. */
	boolean isDavka() {
		return davka.getAsBoolean();
	}

	/** Katalogy, podle kterých se katalogovým mapám a vrstvám přiřadí obrys pokrytí; výchozí jsou vestavěné. */
	public void setKatalogy(final Supplier<cz.geokuk.plugins.mapy.sprava.KatalogMap> katalog, final Supplier<cz.geokuk.plugins.mapy.sprava.KatalogVrstev> katalogVrstev) {
		this.katalog = katalog;
		this.katalogVrstev = katalogVrstev;
	}

	/** Katalog vrstev, podle kterého se vrstvám přiřazují obrysy. */
	public cz.geokuk.plugins.mapy.sprava.KatalogVrstev getKatalogVrstev() {
		return katalogVrstev.get();
	}

	public void setPriOtevreni(final BiConsumer<OblastniKlic, File> priOtevreni) {
		this.priOtevreni = priOtevreni;
	}

	public void setSlozkaSymbolu(final File slozkaSymbolu) {
		this.slozkaSymbolu = slozkaSymbolu;
	}

	/**
	 * Bod pokrývá podrobná mapa; true i tehdy, když mapy ještě nejsou otevřené nebo nejdou otevřít (o tom mluví hláška na dlaždicích). Neblokuje, lze
	 * volat z EDT.
	 */
	public boolean jePodrobnaMapa(final Wgs bod, final int zoom) {
		final OfflineRenderer r = renderer;
		return r == null || r.jePodrobnaMapa(bod.lat, bod.lon, zoom);
	}

	public List<String> getNazvyMap() {
		return nazvyMap;
	}

	public File getSlozka() {
		return slozka;
	}

	public TemaOfflineMapy getTema() {
		return tema;
	}

	/** Proč se nepoužilo zvolené téma, null když se použilo nebo mapa zatím není otevřená. */
	public String getChybaTematu() {
		final OfflineRenderer r = renderer;
		return r == null ? null : r.getChybaTematu();
	}

	/** Proč se mapy naposledy nepodařilo otevřít, null když se otevřely nebo se ještě neotevíraly. */
	public String getChyba() {
		final IOException e = chyba;
		return e == null ? null : e.getMessage();
	}

	/** Složka vrstev (trasy, vrstevnice, DEM dlaždice) uvnitř složky offline map. */
	public static final String SLOZKA_VRSTEV = cz.geokuk.plugins.mapy.sprava.SpravaMapModel.SLOZKA_VRSTEV;

	/** Soubory .map vrstev (trasy, vrstevnice) ze složky vrstev včetně podsložek. */
	public static List<File> vrstvyVeSlozce(final File slozka) {
		return slozka == null ? Collections.emptyList() : cz.geokuk.plugins.mapy.sprava.SouboryVrstev.mapy(new File(slozka, SLOZKA_VRSTEV));
	}

	/** Soubory .map ve složce podle abecedy, velikost písmen v příponě nehraje roli. */
	public static List<File> mapyVeSlozce(final File slozka) {
		final File[] soubory = slozka == null ? null : slozka.listFiles(f -> f.isFile() && f.getName().toLowerCase(Locale.ROOT).endsWith(".map"));
		if (soubory == null) {
			return Collections.emptyList();
		}
		final List<File> mapy = new ArrayList<>(Arrays.asList(soubory));
		mapy.sort(Comparator.comparing(File::getName));
		return mapy;
	}

	/**
	 * Renderer pro aktuální mapy a téma, už s {@link OfflineRenderer#zacni()}; volající musí zavolat {@link OfflineRenderer#skonci()}. Mapy otevírá při prvním
	 * použití a po změně ve složce, nevolat z EDT.
	 *
	 * @throws IOException
	 *             když ve složce není mapa nebo ji nejde otevřít
	 */
	synchronized OfflineRenderer pouzij() throws IOException {
		final File s = slozka;
		final TemaOfflineMapy t = tema;
		final double m = meritko;
		final double pi = pismo;
		final boolean k = trasyKresliPodklad;
		final cz.geokuk.plugins.mapy.sprava.KatalogMap km = katalog.get();
		final cz.geokuk.plugins.mapy.sprava.KatalogVrstev kv = katalogVrstev.get();
		if (!Objects.equals(s, slozkaOtevrena) || !t.equals(temaOtevrene) || m != meritkoOtevrene || pi != pismoOtevrene || k != trasyKresliPodkladOtevrene || km != katalogOtevreny
				|| kv != katalogVrstevOtevreny) {
			katalogOtevreny = km;
			katalogVrstevOtevreny = kv;
			slozkaOtevrena = s;
			temaOtevrene = t;
			meritkoOtevrene = m;
			pismoOtevrene = pi;
			trasyKresliPodkladOtevrene = k;
			otiskSlozky = null;
		}
		zkontrolujSlozku(System.nanoTime());
		if (chyba != null) {
			throw chyba;
		}
		if (!renderer.zacni()) {
			throw new IllegalStateException("Renderer offline mapy je zavřený");
		}
		return renderer;
	}

	/**
	 * Podívá se do složky, i když se zrovna nic nekreslí, aby se dokopírovaná mapa projevila bez pohybu mapou. Nic nedělá, dokud se mapy poprvé neotevřou.
	 * Nevolat z EDT.
	 */
	public synchronized void zkontrolujSlozku() {
		if (otiskSlozky != null && Objects.equals(slozka, slozkaOtevrena) && tema.equals(temaOtevrene) && meritko == meritkoOtevrene && pismo == pismoOtevrene
				&& trasyKresliPodklad == trasyKresliPodkladOtevrene && katalog.get() == katalogOtevreny && katalogVrstev.get() == katalogVrstevOtevreny) {
			zkontrolujSlozku(System.nanoTime());
		}
	}

	/** Změněnou složku otevře, až se dvě kontroly po sobě shodnou; mapu, kterou nešlo otevřít, zkusí znovu po {@link #opakovaniNs}. */
	private void zkontrolujSlozku(final long ted) {
		if (otiskSlozky != null && ted - posledniKontrola < kontrolaSlozkyNs) {
			return;
		}
		posledniKontrola = ted;
		final List<File> mapy = prekryv ? mapyPrekryvu(slozkaOtevrena, trasyKresliPodkladOtevrene) : mapyVeSlozce(slozkaOtevrena);
		final String otisk = otisk(mapy);
		if (otiskSlozky == null) {
			otiskSlozky = otisk;
			otiskZmeny = null;
			otevri(mapy, ted, true);
		} else if (!otisk.equals(otiskSlozky)) {
			if (otisk.equals(otiskZmeny)) {
				if (davka.getAsBoolean() && otevrenoPriZmene != null && ted - otevrenoPriZmene < odstupPriDavceNs) {
					return; // změna počká na konec dávky nebo na uplynutí odstupu
				}
				otevrenoPriZmene = ted;
				otiskSlozky = otisk;
				otiskZmeny = null;
				otevri(mapy, ted, true);
			} else {
				otiskZmeny = otisk; // soubor se možná ještě kopíruje
			}
		} else {
			otiskZmeny = null;
			if (opakovat && ted - opakovatOd >= 0) {
				otevri(mapy, ted, false);
			}
		}
	}

	/** Přípony stažené vrstvy tras ve složce vrstev: současná a starší verze. */
	static final List<String> PRIPONY_TRAS = Arrays.asList("." + cz.geokuk.plugins.mapy.sprava.KatalogovaVrstva.Typ.TRASY.getKod() + ".map",
			"." + cz.geokuk.plugins.mapy.sprava.KatalogovaVrstva.Typ.TRASY.getStarsiKod() + ".map");

	/**
	 * Mapy pro překryv tras: stažené vrstvy tras (stačí i bez základní mapy, třeba přes online podklad) a základní mapy, ke kterým vrstva tras stažená není.
	 * Základní mapa, ke které vrstva tras je, se vynechá, jinak by se stejné trasy kreslily dvakrát.
	 *
	 * @param trasyKresliPodklad
	 *            vrstvy tras kreslí už podklad: překryv je nebere a bere jen základní mapy bez nich
	 */
	static List<File> mapyPrekryvu(final File slozka, final boolean trasyKresliPodklad) {
		if (slozka == null) {
			return Collections.emptyList();
		}
		final List<File> vysledek = new ArrayList<>();
		final Set<String> klice = new HashSet<>();
		final File slozkaVrstev = new File(slozka, SLOZKA_VRSTEV);
		for (final File f : vrstvyVeSlozce(slozka)) {
			final String cesta = slozkaVrstev.toPath().relativize(f.toPath()).toString().replace('\\', '/');
			for (final String pripona : PRIPONY_TRAS) {
				if (cesta.toLowerCase(Locale.ROOT).endsWith(pripona)) {
					if (!trasyKresliPodklad) {
						vysledek.add(f);
					}
					klice.add(cesta.substring(0, cesta.length() - pripona.length()).toLowerCase(Locale.ROOT));
				}
			}
		}
		final cz.geokuk.plugins.mapy.sprava.KatalogMap katalog = cz.geokuk.plugins.mapy.sprava.KatalogMap.vestaveny();
		for (final File f : mapyVeSlozce(slozka)) {
			final String klic = katalog.podleSouboru(f.getName()).map(m -> m.getId().toLowerCase(Locale.ROOT)).orElse(null);
			if (klic == null || !klice.contains(klic)) {
				vysledek.add(f);
			}
		}
		return vysledek;
	}

	/** Vrstvy kreslené spolu se základní mapou; překryv tras je nekreslí, jeho vrstvy tras jsou v {@link #mapyPrekryvu(File, boolean)}. */
	private List<File> vrstvyPouzite() {
		return prekryv ? Collections.emptyList() : vrstvyVeSlozce(slozkaOtevrena);
	}

	/** DEM dlaždice pro stínování terénu; překryv tras je nepoužívá a uživatel stínování může vypnout. */
	private List<File> demPouzite() {
		return prekryv || !stinovani || slozkaOtevrena == null ? Collections.emptyList() : OfflineRenderer.demDlazdice(new File(slozkaOtevrena, SLOZKA_VRSTEV));
	}

	private String otisk(final List<File> mapy) {
		final StringBuilder sb = new StringBuilder(temaOtevrene.otisk()).append('\n').append(meritkoOtevrene).append(' ').append(pismoOtevrene).append('\n');
		for (final File f : mapy) {
			sb.append(f.getName()).append(':').append(f.length()).append(':').append(f.lastModified()).append('\n');
		}
		for (final File f : vrstvyPouzite()) {
			sb.append("vrstva:").append(f.getName()).append(':').append(f.length()).append(':').append(f.lastModified()).append('\n');
		}
		for (final File f : demPouzite()) {
			sb.append("dem:").append(f.getName()).append(':').append(f.length()).append(':').append(f.lastModified()).append('\n');
		}
		return sb.toString();
	}

	/**
	 * @param zmena
	 *            mapy nebo nastavení se změnily; jinak jde o opakovaný pokus a obrazovka se zneplatní, jen když dopadl jinak
	 */
	private void otevri(final List<File> mapy, final long ted, final boolean zmena) {
		// Při prvním otevření nejsou na obrazovce dlaždice, které by změna zneplatnila; ohlášení by zahodilo i rozdělaná vykreslení.
		final boolean predtim = renderer != null || chyba != null;
		final String vysledekPredtim = vysledekOtevreni;
		zavriRenderer();
		nazvyMap = mapy.stream().map(File::getName).collect(Collectors.toList());
		if (mapy.isEmpty()) {
			chyba = prekryv ? new OfflineMapaChyba("Ve složce " + slozkaOtevrena + " nejsou offline mapy ani stažené trasy.", "nejsou mapy ani trasy", null)
					: new OfflineMapaChyba("Ve složce " + slozkaOtevrena + " nejsou offline mapy (soubory .map).", "ve složce nejsou soubory .map", null);
		} else {
			try {
				final long start = System.nanoTime();
				renderer = OfflineRenderer.otevri(mapy, vrstvyPouzite(), demPouzite().isEmpty() ? null : new File(slozkaOtevrena, SLOZKA_VRSTEV), nacteneTema(), prekryv, katalogOtevreny, katalogVrstevOtevreny);
				chyba = null;
				otevrenoMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
				priOtevreni.accept(renderer.getOblastniKlic(), renderer.getSouborSymbolu());
			} catch (final IOException e) {
				chyba = e;
			} catch (final RuntimeException | OutOfMemoryError e) {
				chyba = new OfflineMapaChyba("Offline mapu nejde otevřít: " + e, "mapu nejde otevřít", e);
				log.warn("Offline mapu nejde otevřít", e);
			}
		}
		final boolean neuplne = !mapy.isEmpty() && (chyba != null || renderer.getPocetMap() < mapy.size());
		opakovat = neuplne;
		opakovatOd = ted + opakovaniNs;
		vysledekOtevreni = chyba != null ? "chyba " + chyba.getMessage() : "map " + renderer.getPocetMap() + ", " + renderer.getPreskoceno();
		final boolean jinak = !vysledekOtevreni.equals(vysledekPredtim);
		// Opakovaný pokus se stejným výsledkem (mapa pořád zamčená) log nezahlcuje.
		if (zmena || jinak) {
			if (chyba != null) {
				log.warn("Offline mapu nejde otevřít: {}", chyba.getMessage());
			} else {
				log.info("Offline mapa otevřena za {} ms: {}, téma {}, klíč {}", otevrenoMs, mapy, temaOtevrene, renderer.getKlic());
				if (renderer.getPreskoceno() != null) {
					log.warn("{}; kreslí se bez ní, za {} s se zkusí znovu", renderer.getPreskoceno(), TimeUnit.NANOSECONDS.toSeconds(opakovaniNs));
				}
			}
		}
		if (predtim && (zmena || jinak)) {
			priZmene.run();
		}
	}

	private OfflineRenderer.NacteneTema nacteneTema() throws IOException {
		if (nacteneTema != null && nacteneTema.otiskPozadovaneho.equals(temaOtevrene.otisk()) && nacteneTema.meritko == meritkoOtevrene && nacteneTema.pismo == pismoOtevrene) {
			return nacteneTema;
		}
		uvolniTema();
		nacteneTema = OfflineRenderer.nactiTema(temaOtevrene, slozkaSymbolu, meritkoOtevrene, pismoOtevrene);
		vrstvyTematu = nacteneTema.menu == null ? null : new VrstvyTematu(temaOtevrene, nacteneTema.menu);
		return nacteneTema;
	}

	/** Vrstvy naposledy načteného tématu, null když téma vrstvy nemá nebo ještě není načtené. Neblokuje, lze volat z EDT. */
	public VrstvyTematu getVrstvyTematu() {
		return vrstvyTematu;
	}

	private void uvolniTema() {
		if (nacteneTema != null) {
			nacteneTema.uvolni();
			nacteneTema = null;
		}
	}

	/** Otevře mapy a načte téma předem, aby první dlaždice nečekaly; chyby se ukážou až na dlaždicích. Nevolat z EDT. */
	public void predpriprav() {
		if (getSlozka() == null) {
			return; // složka ještě není nastavená, chyba „bez map“ by se zbytečně ohlásila
		}
		try {
			pouzij().skonci();
		} catch (final IOException | RuntimeException e) {
			log.debug("Offline mapu nejde připravit předem: {}", e.getMessage());
		}
	}

	private void zavriRenderer() {
		if (renderer != null) {
			renderer.zavri();
			renderer = null;
		}
	}

	/** Zapne nebo vypne stínování terénu; mapy se otevřou znovu. */
	public synchronized void setStinovani(final boolean zapnuto) {
		if (zapnuto != stinovani) {
			stinovani = zapnuto;
			otiskSlozky = null;
		}
	}

	/** Ve složce vrstev jsou DEM dlaždice (soubory .hgt nebo .zip). */
	public static boolean maDem(final File slozkaOfflineMap) {
		return slozkaOfflineMap != null && !OfflineRenderer.demDlazdice(new File(slozkaOfflineMap, SLOZKA_VRSTEV)).isEmpty();
	}

	/** Zavře mapy; při dalším použití se otevřou znovu. */
	public synchronized void zavri() {
		zavriRenderer();
		uvolniTema();
		otiskSlozky = null;
		otiskZmeny = null;
		opakovat = false;
	}
}
