package cz.geokuk.plugins.kesoid.importek;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import cz.geokuk.core.napoveda.Diagnostika;
import cz.geokuk.framework.ProgressModel;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.mvc.GccomNick;
import cz.geokuk.plugins.kesoid.mvc.GsakParametryNacitani;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;
import cz.geokuk.util.file.*;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Martin Veverka
 */
@Slf4j
public class MultiNacitac {


	// TODO: Doporučuji přejmenovat na GEOKUK_ROOTDIR_DEF resp. GEOGET_ROOTDIR_DEF. Už dávno nejde jen o jméno souboru/složky. [2016-04-09, Bohusz]
	static final Root.Def FILE_NAME_REGEX_GEOKUK_DIR = new Root.Def(Integer.MAX_VALUE, Pattern.compile("(?i).*\\.(geokuk|gpx|zip|jpg|raw|tif)"), null, true);
	static final Root.Def FILE_NAME_REGEX_GEOGET_DIR = new Root.Def(1, Pattern.compile("(?i).*\\.db3"), Pattern.compile("(?i).*\\.[0-9]{8}\\.db3"));
	static final Root.Def GSAK_ROOTDIR_DEF = new Root.Def(2, Pattern.compile("sqlite.db3"), null);
	static final Root.Def OPENSAK_ROOTDIR_DEF = new Root.Def(1, Pattern.compile("(?i).*\\.db"), null);

	private final DirScanner ds;
	private final RegistrStavuZdroju registr;
	/** Vzájemně se ovlivňující zdroje z posledního dokončeného načtení, každý člen ukazuje na svou skupinu. */
	private Map<File, SkupinyZdroju.Skupina> cacheSkupin = Collections.emptyMap();
	/** Klíče zdrojů z jejich posledního přečtení, i vypnutých. */
	private final Map<File, SkupinyZdroju.ZnamyZdroj> znameZdroje = new HashMap<>();
	private final CasyDatZdroju casyDat;
	/** Časy dat naposledy zapsané do nastavení; null = ještě nenačtené. */
	private Set<String> ulozeneCasy;
	private volatile Set<File> posledniPrectene = Collections.emptySet();
	private volatile int posledniPocetCteni;
	private volatile List<String> posledniRozpusteni = Collections.emptyList();

	private volatile File geogetDir;
	private volatile File gsakDir;
	private volatile File opensakDir;
	private final Set<File> ohlasenePrazdne = Collections.synchronizedSet(new HashSet<>());
	/** Kam jde zpráva o špatně zadaných datových složkách; okno se neukazuje, stav složky patří do přehledu zdrojů. */
	Consumer<String> ohlasovac = zprava -> log.warn(zprava);

	/** Databáze, které při posledním načítání zamykal jiný program; znovu se načítá, až je pustí. */
	private volatile Set<File> zamcene = Collections.emptySet();
	private List<KeFile> posledniSeznam;
	/** Zapnuté zdroje a kontext běhu, který naposledy zjistil zamčené databáze; jejich změna se načte, i když zámek trvá. */
	private Set<File> posledniZapnute = Collections.emptySet();
	private Object posledniKontext;
	/** Zdroje, jejichž keše jsou v naposledy vráceném (zobrazeném) výsledku. */
	private Set<File> zobrazene = Collections.emptySet();
	/**
	 * Databáze, které jiný program v tomto běhu zamkl, s otiskem souboru z jejich posledního načtení. Import může běžet v několika transakcích a mezi nimi se mohla načíst
	 * rozpracovaná; změnu po dokončení zápisu sken sám nepozná.
	 */
	private final Map<File, String> sledovane = new HashMap<>();
	/** Waypointy zobrazených databází; keše databáze, kterou jiný program zamkne, se z nich převezmou do dalšího výsledku. */
	private Map<File, List<Wpt>> zobrazeneWpty = Collections.emptyMap();
	private InformaceOZdrojich zobrazeneInformace;
	/** Genom, nick a parametry, se kterými se načetly zobrazené keše; keše zamčeného zdroje se převezmou jen při stejném kontextu. */
	private Object zobrazenyKontext;

	private final List<Nacitac0> nacitace = new ArrayList<>();
	private final KesoidModel kesoidModel;

	// private static final String CACHE_SUFFIX = ".cache.serialized";

	/**
	 * Checks whether the given file is a ZIP file. Copied from http://www.java2s.com/Code/Java/File-Input-Output/DeterminewhetherafileisaZIPFile.htm
	 */
	private static boolean isZipFile(final File fileToTest) {
		if (fileToTest.isDirectory()) {
			return false;
		}
		if (fileToTest.length() < 4) {
			return false;
		}
		try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(fileToTest)))) {
			final int test = in.readInt();
			return test == 0x504b0304;
		} catch (final IOException e) {
			throw new IllegalArgumentException("The file " + fileToTest + " cannot be checked!", e);
		}
	}

	public MultiNacitac(final KesoidModel kesoidModel) {
		this(kesoidModel, new DirScanner());
	}

	MultiNacitac(final KesoidModel kesoidModel, final DirScanner ds) {
		this(kesoidModel, ds, new RegistrStavuZdroju());
	}

	/** Registr si může předat model, který stav zdrojů rozesílá. */
	MultiNacitac(final KesoidModel kesoidModel, final DirScanner ds, final RegistrStavuZdroju registr) {
		this(kesoidModel, ds, registr, new CasyDatZdroju());
	}

	/** Časy dat zdrojů může sdílet víc načítačů (test shody s plným načtením). */
	MultiNacitac(final KesoidModel kesoidModel, final CasyDatZdroju casyDat) {
		this(kesoidModel, new DirScanner(), new RegistrStavuZdroju(), casyDat);
	}

	private MultiNacitac(final KesoidModel kesoidModel, final DirScanner ds, final RegistrStavuZdroju registr, final CasyDatZdroju casyDat) {
		this.casyDat = casyDat;
		this.registr = registr;
		this.kesoidModel = kesoidModel;
		this.ds = ds;
		nacitace.add(new NacitacGeokuk());
		nacitace.add(new NacitacGpx());
		nacitace.add(new NacitacImageMetadata());
		nacitace.add(new GeogetLoader());
		nacitace.add(new GsakDbLoader(kesoidModel::getGsakParametryNacitani));
		nacitace.add(new OpensakDbLoader());
	}

	public RegistrStavuZdroju getRegistr() {
		return registr;
	}

	static TypZdroje typ(final KeFile f) {
		final Root.Def def = f.root.def;
		if (GSAK_ROOTDIR_DEF.equals(def)) {
			return TypZdroje.GSAK;
		}
		if (OPENSAK_ROOTDIR_DEF.equals(def)) {
			return TypZdroje.OPENSAK;
		}
		return FILE_NAME_REGEX_GEOGET_DIR.equals(def) ? TypZdroje.GEOGET : TypZdroje.GPX;
	}

	/** Krátký popis zdroje pro průběh ve stavovém řádku: typ zdroje a cesta v jeho datové složce, u ZIPu i cesta v archivu. */
	static String popisPrubehu(final KeFile f, final ZipEntry entry) {
		Path cesta;
		try {
			cesta = f.getRelativePath();
		} catch (final KeFile.XRelativizeDubleDot e) {
			cesta = null;
		}
		final StringBuilder sb = new StringBuilder(typ(f).getNazev()).append(File.separatorChar);
		sb.append(cesta == null || cesta.toString().isEmpty() ? f.getFile().getName() : cesta.toString());
		if (entry != null) {
			sb.append(File.separatorChar).append(entry.getName().replace('/', File.separatorChar));
		}
		return sb.toString();
	}

	/** Velikost souboru i jeho WAL, kde se databáze zapisuje. */
	private static long velikostNaDisku(final File soubor) {
		return soubor.length() + new File(soubor.getPath() + "-wal").length();
	}

	private int prepisRegistr(final List<KeFile> list) {
		final Map<File, KeFile> poSouboru = new LinkedHashMap<>();
		for (final KeFile f : list) {
			poSouboru.put(f.getFile(), f);
		}
		return registr.prepis(new ArrayList<>(poSouboru.keySet()), f -> typ(poSouboru.get(f)), f -> poSouboru.get(f).getRelativePath().toString(), kesoidModel::jeZdrojZapnut, kesoidModel::isTypVypnut, MultiNacitac::velikostNaDisku);
	}

	public boolean jeZamcena(final File databaze) {
		return zamcene.contains(databaze);
	}

	/** Synchronizované: zrušené načítání může ještě doběhnout, když už začíná další. */
	public synchronized KesBag nacti(final Future<?> future, final Genom genom) throws IOException {
		DatabazeJinehoProgramu.setNacitani(future);
		try {
			return nactiZmeny(future, genom);
		} finally {
			DatabazeJinehoProgramu.setNacitani(null);
		}
	}

	/** Sekundy od {@code start} zaokrouhlené dolů na desetiny. */
	private static double desetinySekund(final long start) {
		final long desetiny = (System.currentTimeMillis() - start) / 100;
		return desetiny / 10.0;
	}

	private KesBag nactiZmeny(final Future<?> future, final Genom genom) throws IOException {
		List<KeFile> list = ds.coMamNacist();
		if (!zamcene.isEmpty()) {
			if (!zamcene.stream().allMatch(DatabazeJinehoProgramu::jeZamcena)) {
				ds.nulujLastScaned();
				list = ds.coMamNacist();
			} else if (list != null && bezZamcenych(list).equals(bezZamcenych(posledniSeznam)) && zapnute(list).equals(posledniZapnute) && kontext(genom).equals(posledniKontext)) {
				return null; // změnila se jen zamčená databáze, jiný program do ní pořád zapisuje
			}
		}
		if (list == null && zmenilaSeSledovana()) {
			ds.nulujLastScaned();
			list = ds.coMamNacist();
		}
		if (list == null) {
			return null;
		}
		posledniSeznam = list;
		final Map<TypZdroje, String> problemySlozek = ohlasPrazdneSlozky(list);
		final File gsak = gsakDir;
		// Platí čitelnost z doby skenu, pozdější kontrola by mohla vidět složku, která se mezitím vrátila.
		final Set<File> nedostupne = ds.getNedostupne();
		kesoidModel.setNedostupnePriNacitani(nedostupne);
		// Dočasně nedostupná složka (síť, USB) neznamená, že databáze zmizely; známé zůstanou známé.
		if (gsak == null || !nedostupne.contains(gsak)) {
			kesoidModel.zaradGsakDatabaze(databaze(list, GSAK_ROOTDIR_DEF), nedostupne);
		}
		final File opensak = opensakDir;
		if (opensak == null || !nedostupne.contains(opensak)) {
			kesoidModel.zaradOpensakDatabaze(databaze(list, OPENSAK_ROOTDIR_DEF), nedostupne);
		}
		// Až po zařazení nových databází, ty mohou být vypnuté („Načítat až po vybrání“).
		final int generace = prepisRegistr(list);
		registr.setProblemySlozek(generace, problemySlozek);
		if (kesoidModel.getVsechnyKesoidy() == null) {
			kesoidModel.setNacitaneZdroje(predbezneZdroje(list));
		}
		if (ulozeneCasy == null) {
			ulozeneCasy = kesoidModel.getCasyDatZdroju();
			casyDat.nacti(ulozeneCasy);
		}
		final Object kontext = kontext(genom);
		final Map<File, KeFile> poSouboru = new HashMap<>();
		final Map<File, String> otiskyTed = new HashMap<>();
		final Map<File, Long> poradi = new HashMap<>();
		for (final KeFile f : list) {
			final File soubor = f.getFile();
			poSouboru.put(soubor, f);
			if (kesoidModel.maSeNacist(soubor)) {
				otiskyTed.put(soubor, otisk(soubor));
				// Předpoklad: obsah se nezměnil; když ano, ukáže se po přečtení a běh se případně zopakuje.
				final CasyDatZdroju.Zaznam z = casyDat.get(soubor);
				poradi.put(soubor, z != null ? z.cas : CasyDatZdroju.casZmeny(soubor));
			}
		}
		final Set<SkupinyZdroju.Skupina> prevzate = platneSkupiny(poSouboru, otiskyTed, kontext);
		final Set<File> kCteni = new HashSet<>(otiskyTed.keySet());
		for (final SkupinyZdroju.Skupina g : prevzate) {
			kCteni.removeAll(g.otisky.keySet());
		}
		// Zdroj, jehož klíče známe z dřívějška (vypnutý a znovu zapnutý), se s převzatou skupinou čte rovnou spolu.
		final Map<File, KliceZdroje> znameKlice = new HashMap<>();
		for (final File f : kCteni) {
			final SkupinyZdroju.ZnamyZdroj z = znameZdroje.get(f);
			if (z != null) {
				znameKlice.put(f, z.klice);
			}
		}
		// Databáze s neznámými klíči (od startu vypnutá): stačí kódy, jinak by se překryv ukázal až po čtení a běh by se opakoval.
		if (!prevzate.isEmpty()) {
			for (final File f : kCteni) {
				if (!znameKlice.containsKey(f)) {
					final KliceZdroje predem = klicePredem(poSouboru.get(f));
					if (predem != null) {
						znameKlice.put(f, predem);
					}
				}
			}
		}
		posledniRozpusteni = new ArrayList<>();
		rozpustPrekryte(znameKlice, prevzate, kCteni, poSouboru);
		final long start = System.currentTimeMillis();
		Diagnostika.zaznamenej("Načítání keší: " + popisSouboru(list) + (kCteni.size() < otiskyTed.size() ? ", beze čtení " + (otiskyTed.size() - kCteni.size()) : ""));
		final Map<File, String> otiskyPredCtenim = new HashMap<>();
		for (final KeFile f : list) {
			if (sledovane.containsKey(f.getFile())) {
				otiskyPredCtenim.put(f.getFile(), otisk(f.getFile()));
			}
		}
		Cteni cteni;
		Map<File, KliceZdroje> klice;
		final Map<File, Long> casyPoCteni = new HashMap<>();
		int pocetCteni = 0;
		int pokus = 0;
		boolean posledniPokus = false;
		while (true) {
			cteni = new Cteni(new KesoidImportBuilder(genom, kesoidModel.getGccomNick(), kesoidModel.getProgressModel(), kesoidModel.getKesopidPluginManager()));
			precti(serazene(list, poradi), kCteni, prevzate, cteni, future, kontext, generace);
			pocetCteni += cteni.zkouseno.size();
			if (future != null && future.isCancelled()) {
				ds.nulujLastScaned();
				return null;
			}
			klice = cteni.builder.getKliceZdroju();
			casyPoCteni.clear();
			for (final File f : cteni.precteno) {
				casyPoCteni.put(f, casyDat.casPoPrecteni(f, klice.getOrDefault(f, KliceZdroje.PRAZDNE).otiskObsahu, cteni.casyZmeny.get(f)));
			}
			if (posledniPokus || !opakovat(cteni, klice, casyPoCteni, poradi, prevzate, kCteni, poSouboru)) {
				break;
			}
			poradi.putAll(casyPoCteni);
			// Zdroj, který jiný program pořád přepisuje, by se četl dokola; naposledy se čte vše bez převzatých skupin, aby keš nebyla v bagu dvakrát.
			if (++pokus >= 3) {
				posledniPokus = true;
				prevzate.clear();
				kCteni.addAll(otiskyTed.keySet());
			}
		}
		final KesoidImportBuilder builder = cteni.builder;
		final long startDone = System.currentTimeMillis();
		builder.done();
		final KesBag bag = builder.getKesBag();
		final Set<File> zamceneTed = cteni.zamceneTed;
		Diagnostika.zaznamenej("Načteno " + bag.getKesoidy().size() + " kešoidů, " + bag.getWpts().size() + " waypointů za " + desetinySekund(start) + " s"
				+ " (čteno " + pocetCteni + (pokus > 0 ? " v " + (pokus + 1) + " pokusech" : "") + ", párování a index " + desetinySekund(startDone) + " s)"
				+ (cteni.vadne.isEmpty() ? "" : ", chyba v souborech " + cteni.vadne) + (zamceneTed.isEmpty() ? "" : ", zamčené " + jmena(zamceneTed)));
		zamcene = zamceneTed;
		posledniZapnute = otiskyTed.keySet();
		posledniKontext = kontext;
		final Set<File> rozepsane = cteni.rozepsane;
		kesoidModel.setZamceneDatabaze(jmena(zamceneTed.stream().filter(f -> !rozepsane.contains(f)).collect(Collectors.toSet())));
		for (final Map.Entry<File, String> e : otiskyPredCtenim.entrySet()) {
			sledovane.put(e.getKey(), e.getValue());
		}
		// Zamčená databáze se načte po uvolnění zámku, otisk se jí zapíše až po přečtení.
		for (final File f : zamceneTed) {
			sledovane.put(f, "");
		}
		// Keše ze zamčené databáze, které nešly převzít, zůstanou zobrazené se vším ostatním, dokud ji jiný program nepustí.
		if (cteni.nelzePrevzit && kesoidModel.getVsechnyKesoidy() != null) {
			return null;
		}
		ulozCache(poSouboru.keySet(), cteni, klice, casyPoCteni, otiskyTed, prevzate, kontext);
		// Zdroj v dočasně nedostupné složce (síť, USB) si čas dat nechá.
		final Set<String> casy = casyDat.ponechej(f -> poSouboru.containsKey(f) || nedostupne.stream().anyMatch(d -> f.toPath().startsWith(d.toPath())));
		if (!casy.equals(ulozeneCasy)) {
			kesoidModel.setCasyDatZdroju(casy);
			ulozeneCasy = casy;
		}
		posledniPrectene = new HashSet<>(cteni.zkouseno);
		posledniPocetCteni = pocetCteni;
		final Set<File> nactene = new HashSet<>();
		for (final KeFile f : list) {
			if (!zamceneTed.contains(f.getFile()) || cteni.prevzateZamcene.contains(f.getFile())) {
				nactene.add(f.getFile());
			}
		}
		zobrazene = nactene;
		zobrazeneWpty = builder.getWptyPodleZdroje();
		zobrazeneInformace = bag.getInformaceOZdrojich();
		zobrazenyKontext = kontext;
		return bag;
	}

	/** Jeden průchod zdroji; při opakování běhu se zahodí celý. */
	private static final class Cteni {
		final KesoidImportBuilder builder;
		/** Zdroje, které se začaly číst. */
		final Set<File> zkouseno = new LinkedHashSet<>();
		/** Zdroje přečtené celé a bez chyby. */
		final Set<File> precteno = new HashSet<>();
		final Map<File, Long> casyZmeny = new HashMap<>();
		final Map<File, int[]> pocty = new HashMap<>();
		final List<String> vadne = new ArrayList<>();
		final Set<File> zamceneTed = new HashSet<>();
		/** Zamčené zdroje, které jsou ve skutečnosti čerstvě zapisované soubory (GPX), ne databáze. */
		final Set<File> rozepsane = new HashSet<>();
		final Set<File> prevzateZamcene = new HashSet<>();
		/** Pořadí členů převzatých skupin, ve kterém šly do bagu. */
		final Map<SkupinyZdroju.Skupina, List<File>> poradiCasti = new HashMap<>();
		boolean nelzePrevzit;

		Cteni(final KesoidImportBuilder builder) {
			this.builder = builder;
			builder.init();
			builder.setSledovaneZdroje(f -> true);
		}
	}

	/** Zapnuté zdroje od nejnovějších dat (při shodě podle cesty), první vyhrává duplicitu; vypnuté na konci, jen do přehledu zdrojů. */
	private static List<KeFile> serazene(final List<KeFile> list, final Map<File, Long> poradi) {
		final List<KeFile> vysledek = new ArrayList<>(list);
		vysledek.sort(Comparator.<KeFile, Boolean> comparing(f -> !poradi.containsKey(f.getFile())).thenComparing(f -> -poradi.getOrDefault(f.getFile(), 0L))
				.thenComparing(f -> f.getFile().getPath()));
		return vysledek;
	}

	private void precti(final List<KeFile> serazene, final Set<File> kCteni, final Set<SkupinyZdroju.Skupina> prevzate, final Cteni cteni, final Future<?> future,
			final Object kontext, final int generace) {
		final Map<File, SkupinyZdroju.Skupina> skupinaClena = new HashMap<>();
		for (final SkupinyZdroju.Skupina g : prevzate) {
			for (final File clen : g.otisky.keySet()) {
				skupinaClena.put(clen, g);
			}
		}
		final KesoidImportBuilder builder = cteni.builder;
		for (final KeFile file : serazene) {
			if (future != null && future.isCancelled()) {
				break;
			}
			final File soubor = file.getFile();
			final SkupinyZdroju.Skupina skupina = skupinaClena.get(soubor);
			if (skupina != null) {
				if (!cteni.poradiCasti.containsKey(skupina)) {
					final List<File> poradiClenu = new ArrayList<>();
					for (final KeFile f : serazene) {
						if (skupinaClena.get(f.getFile()) == skupina) {
							poradiClenu.add(f.getFile());
						}
					}
					cteni.poradiCasti.put(skupina, poradiClenu);
				}
				final KesBag.Cast hotova = cteni.poradiCasti.get(skupina).equals(skupina.poradiCasti) ? skupina.cast : null;
				final int[] pocty = skupina.pocty.get(soubor);
				builder.prevezmiZeSkupiny(file, skupina.wpty.getOrDefault(soubor, Collections.<Wpt> emptyList()), pocty[0], pocty[1], skupina, hotova);
				registr.hotovo(generace, soubor, pocty[0], pocty[1]);
				continue;
			}
			log.debug("Nacitam: " + file);
			final boolean cist = kCteni.contains(soubor);
			try {
				if (cist) {
					cteni.zkouseno.add(soubor);
					cteni.casyZmeny.put(soubor, CasyDatZdroju.casZmeny(soubor));
					registr.zacina(generace, soubor);
					ProgressModel.setSledovacPostupu(procent -> registr.postup(generace, soubor, procent));
				}
				zpracujJedenFile(file, builder, future);
				if (cist && !(future != null && future.isCancelled())) {
					final int[] pocty = builder.getPoctyCurrent();
					cteni.pocty.put(soubor, pocty);
					cteni.precteno.add(soubor);
					registr.hotovo(generace, soubor, pocty[0], pocty[1]);
				}
			} catch (final DatabazeJinehoProgramu.Zamcena e) {
				registr.cekaNaZapis(generace, soubor);
				log.info(e.getMessage());
				cteni.zamceneTed.add(soubor);
				if (e instanceof RozepsanySoubor.Rozepsany) {
					cteni.rozepsane.add(soubor);
				}
				if (zobrazene.contains(soubor)) {
					// Zamčená uprostřed čtení by měla v builderu část nových keší, ty se se starými míchat nesmí.
					if (kontext.equals(zobrazenyKontext) && !builder.maWaypointyZe(soubor)) {
						builder.prevezmi(file, zobrazeneWpty.getOrDefault(soubor, Collections.<Wpt> emptyList()), zobrazeneInformace == null ? null : zobrazeneInformace.get(file));
						cteni.prevzateZamcene.add(soubor);
					} else {
						cteni.nelzePrevzit = true;
					}
				}
			} catch (final Exception e) {
				// znovu se zkusí, až se soubory změní; jinak by se chyba opakovala každých pár vteřin
				FExceptionDumper.dump(e, EExceptionSeverity.DISPLAY, "Problém při čtení souboru " + file);
				cteni.vadne.add(soubor.getName());
				registr.chyba(generace, soubor, popisChyby(e));
			} finally {
				ProgressModel.setSledovacPostupu(null);
			}
		}
	}

	/** Klíče zdroje, který se nepřečetl celý: co se z něj stihlo přečíst, a co se o něm ví z dřívějška. */
	private KliceZdroje kliceNeprecteneho(final File f, final Map<File, KliceZdroje> klice) {
		final SkupinyZdroju.ZnamyZdroj z = znameZdroje.get(f);
		return KliceZdroje.slouc(Arrays.asList(klice.getOrDefault(f, KliceZdroje.PRAZDNE), z == null ? KliceZdroje.PRAZDNE : z.klice));
	}

	/**
	 * Po přečtení: převzatá skupina, která sdílí klíč se čteným zdrojem, by dala jiný výsledek než plné načtení, a čtený zdroj, jehož čas dat vyšel jinak, než se čekalo, se četl
	 * ve špatném pořadí vůči zdrojům, se kterými se překrývá. Pak se běh opakuje se skupinou mezi čtenými a se správným pořadím.
	 */
	private boolean opakovat(final Cteni cteni, final Map<File, KliceZdroje> klice, final Map<File, Long> casyPoCteni, final Map<File, Long> poradi, final Set<SkupinyZdroju.Skupina> prevzate,
			final Set<File> kCteni, final Map<File, KeFile> poSouboru) {
		final Map<File, KliceZdroje> ctene = new HashMap<>();
		for (final File f : cteni.zkouseno) {
			ctene.put(f, cteni.precteno.contains(f) ? klice.getOrDefault(f, KliceZdroje.PRAZDNE) : kliceNeprecteneho(f, klice));
		}
		final List<File> soubory = new ArrayList<>(ctene.keySet());
		final List<SkupinyZdroju.Skupina> skupiny = new ArrayList<>(prevzate);
		final List<KliceZdroje> jednotky = new ArrayList<>();
		for (final File f : soubory) {
			jednotky.add(ctene.get(f));
		}
		for (final SkupinyZdroju.Skupina g : skupiny) {
			jednotky.add(g.klice);
		}
		final int[] komponenta = SkupinyZdroju.komponenty(jednotky);
		final Map<Integer, Integer> velikost = new HashMap<>();
		for (final int k : komponenta) {
			velikost.merge(k, 1, Integer::sum);
		}
		final Set<Integer> sCtenym = new HashSet<>();
		for (int i = 0; i < soubory.size(); i++) {
			sCtenym.add(komponenta[i]);
		}
		boolean opakovat = false;
		for (int i = 0; i < skupiny.size(); i++) {
			if (sCtenym.contains(komponenta[soubory.size() + i])) {
				zaznamenejRozpusteni(skupiny.get(i), ctene, poSouboru, "zjištěno po čtení");
				prevzate.remove(skupiny.get(i));
				kCteni.addAll(skupiny.get(i).otisky.keySet());
				opakovat = true;
			}
		}
		for (int i = 0; i < soubory.size(); i++) {
			final Long cas = casyPoCteni.get(soubory.get(i));
			if (cas != null && !cas.equals(poradi.get(soubory.get(i))) && velikost.get(komponenta[i]) > 1) {
				opakovat = true;
			}
		}
		return opakovat;
	}

	/** Převzaté skupiny, které sdílejí klíč se zdrojem ke čtení, se přečtou s ním. */
	private void rozpustPrekryte(final Map<File, KliceZdroje> ctene, final Set<SkupinyZdroju.Skupina> prevzate, final Set<File> kCteni, final Map<File, KeFile> poSouboru) {
		if (ctene.isEmpty() || prevzate.isEmpty()) {
			return;
		}
		final List<KliceZdroje> jednotky = new ArrayList<>(ctene.values());
		final List<SkupinyZdroju.Skupina> skupiny = new ArrayList<>(prevzate);
		for (final SkupinyZdroju.Skupina g : skupiny) {
			jednotky.add(g.klice);
		}
		final int[] komponenta = SkupinyZdroju.komponenty(jednotky);
		final Set<Integer> sCtenym = new HashSet<>();
		for (int i = 0; i < ctene.size(); i++) {
			sCtenym.add(komponenta[i]);
		}
		for (int i = 0; i < skupiny.size(); i++) {
			if (sCtenym.contains(komponenta[ctene.size() + i])) {
				zaznamenejRozpusteni(skupiny.get(i), ctene, poSouboru, "zjištěno předem");
				prevzate.remove(skupiny.get(i));
				kCteni.addAll(skupiny.get(i).otisky.keySet());
			}
		}
	}

	/** Klíče databáze z jejích kódů, bez čtení ostatních údajů; null u GPX, zamčené nebo nečitelné. */
	private KliceZdroje klicePredem(final KeFile f) {
		final TypZdroje t = typ(f);
		final Class<? extends Nacitac0> trida = t == TypZdroje.GEOGET ? GeogetLoader.class : t == TypZdroje.GSAK ? GsakDbLoader.class : t == TypZdroje.OPENSAK ? OpensakDbLoader.class : null;
		if (trida == null || DatabazeJinehoProgramu.jeZamcena(f.getFile())) {
			return null;
		}
		for (final Nacitac0 n : nacitace) {
			if (n.getClass() == trida) {
				try {
					final Collection<String> jmena = n.jmenaPredem(f.getFile());
					if (jmena == null) {
						return null;
					}
					final KliceZdroje.Sberac s = new KliceZdroje.Sberac();
					for (final String jmeno : jmena) {
						s.pridej(KliceZdroje.klicJmena(jmeno));
					}
					return s.hotovo();
				} catch (final Exception e) {
					log.debug("Kódy zdroje {} předem nejdou zjistit: {}", f, e.toString());
					return null;
				}
			}
		}
		return null;
	}

	/** Do Diagnostiky (bez jmen souborů): kterou skupinu je třeba číst znovu a kvůli kterým zdrojům. */
	private void zaznamenejRozpusteni(final SkupinyZdroju.Skupina g, final Map<File, KliceZdroje> ctene, final Map<File, KeFile> poSouboru, final String kdy) {
		int wpt = 0;
		for (final int[] p : g.pocty.values()) {
			wpt += p[0];
		}
		final List<String> kvuli = new ArrayList<>();
		for (final Map.Entry<File, KliceZdroje> e : ctene.entrySet()) {
			final int spolecnych = e.getValue().spolecnych(g.klice);
			if (spolecnych > 0) {
				final KeFile kf = poSouboru.get(e.getKey());
				kvuli.add((kf == null ? "?" : typ(kf).getNazev()) + " " + spolecnych + " klíčů");
			}
		}
		final String zaznam = "Znovu se čte skupina " + g.otisky.size() + " zdrojů (" + wpt + " wpt), překryv: " + (kvuli.isEmpty() ? "přes jiný čtený zdroj" : String.join(", ", kvuli)) + ", " + kdy;
		posledniRozpusteni.add(zaznam);
		Diagnostika.zaznamenej(zaznam);
	}

	/** Záznamy o znovu čtených skupinách z posledního načtení (testy). */
	List<String> getPosledniRozpusteni() {
		return posledniRozpusteni;
	}

	/** Co ovlivňuje obsah načtených keší kromě souborů samotných. */
	private Object kontext(final Genom genom) {
		final GccomNick nick = kesoidModel.getGccomNick();
		final GsakParametryNacitani gsak = kesoidModel.getGsakParametryNacitani();
		final List<String> pluginy = new ArrayList<>();
		if (kesoidModel.getKesopidPluginManager() != null) {
			for (final Object p : kesoidModel.getKesopidPluginManager().getPlugins()) {
				pluginy.add(p.getClass().getName());
			}
		}
		return Arrays.asList(genom, nick == null ? null : nick.name, nick == null ? null : nick.id, gsak == null ? null : gsak.getCasNalezu(), pluginy);
	}

	/** Skupiny z minulého dokončeného načtení, jejichž členům se nic nezměnilo: zapnuté, stejný soubor, nezamčené, stejný kontext. */
	private Set<SkupinyZdroju.Skupina> platneSkupiny(final Map<File, KeFile> soubory, final Map<File, String> otiskyZapnutych, final Object kontext) {
		final Set<SkupinyZdroju.Skupina> vysledek = new LinkedHashSet<>();
		for (final SkupinyZdroju.Skupina skupina : new LinkedHashSet<>(cacheSkupin.values())) {
			if (!skupina.kontext.equals(kontext)) {
				continue;
			}
			boolean beze = true;
			for (final Map.Entry<File, String> e : skupina.otisky.entrySet()) {
				final File clen = e.getKey();
				// Databázi, kterou jiný program právě zamkl, je třeba zkusit číst, aby se zamčení ohlásilo a převzalo se z minula.
				if (!e.getValue().equals(otiskyZapnutych.get(clen)) || zamcene.contains(clen) || typ(soubory.get(clen)) != TypZdroje.GPX && DatabazeJinehoProgramu.jeZamcena(clen)) {
					beze = false;
					break;
				}
			}
			if (beze) {
				vysledek.add(skupina);
			}
		}
		return vysledek;
	}

	/**
	 * Uloží z dokončeného načtení, co je o zdrojích známo, a skupiny vzájemně se ovlivňujících zdrojů pro příští navázání. Skupina se zdrojem nepřečteným celým (zámek,
	 * chyba) nebo s bezejmennými waypointy se neukládá.
	 */
	private void ulozCache(final Set<File> vSeznamu, final Cteni cteni, final Map<File, KliceZdroje> klice, final Map<File, Long> casyPoCteni, final Map<File, String> otisky,
			final Set<SkupinyZdroju.Skupina> prevzate, final Object kontext) {
		znameZdroje.keySet().retainAll(vSeznamu);
		for (final File f : cteni.precteno) {
			final KliceZdroje k = klice.getOrDefault(f, KliceZdroje.PRAZDNE);
			znameZdroje.put(f, new SkupinyZdroju.ZnamyZdroj(otisky.get(f), k));
			casyDat.put(f, new CasyDatZdroju.Zaznam(k.otiskObsahu, casyPoCteni.get(f)));
		}
		final List<File> soubory = new ArrayList<>();
		final List<KliceZdroje> jednotky = new ArrayList<>();
		final Set<Integer> nepouzitelne = new HashSet<>();
		for (final File f : cteni.zkouseno) {
			soubory.add(f);
			jednotky.add(cteni.precteno.contains(f) ? klice.getOrDefault(f, KliceZdroje.PRAZDNE) : kliceNeprecteneho(f, klice));
		}
		for (final File f : cteni.prevzateZamcene) {
			if (!cteni.zkouseno.contains(f)) {
				soubory.add(f);
				jednotky.add(kliceNeprecteneho(f, klice));
			}
		}
		final int[] komponenta = SkupinyZdroju.komponenty(jednotky);
		for (int i = 0; i < soubory.size(); i++) {
			if (!cteni.precteno.contains(soubory.get(i)) || jednotky.get(i).bezejmenne) {
				nepouzitelne.add(komponenta[i]);
			}
		}
		final Map<Object, KesBag.Cast> casti = cteni.builder.getCastiSkupin();
		final Map<File, SkupinyZdroju.Skupina> nova = new HashMap<>();
		for (final SkupinyZdroju.Skupina g : prevzate) {
			for (final File clen : g.otisky.keySet()) {
				nova.put(clen, g);
			}
			if (casti.containsKey(g)) {
				g.cast = casti.get(g);
				g.poradiCasti = cteni.poradiCasti.get(g);
			}
		}
		final Map<Integer, List<Integer>> clenove = new HashMap<>();
		for (int i = 0; i < soubory.size(); i++) {
			if (!nepouzitelne.contains(komponenta[i])) {
				clenove.computeIfAbsent(komponenta[i], k -> new ArrayList<>()).add(i);
			}
		}
		for (final List<Integer> komp : clenove.values()) {
			final SkupinyZdroju.Skupina skupina = new SkupinyZdroju.Skupina(kontext);
			final List<KliceZdroje> kliceSkupiny = new ArrayList<>();
			for (final int i : komp) {
				final File f = soubory.get(i);
				skupina.otisky.put(f, otisky.get(f));
				skupina.wpty.put(f, cteni.builder.getWptyPodleZdroje().getOrDefault(f, Collections.<Wpt> emptyList()));
				skupina.pocty.put(f, cteni.pocty.get(f));
				kliceSkupiny.add(jednotky.get(i));
				nova.put(f, skupina);
			}
			skupina.klice = KliceZdroje.slouc(kliceSkupiny);
		}
		cacheSkupin = nova;
	}

	/** Zdroje, které se při posledním načtení opravdu četly; ostatní se převzaly z minulého načtení. */
	Set<File> getPosledniPrectene() {
		return posledniPrectene;
	}

	/** Kolikrát se při posledním načtení četl nějaký zdroj, včetně opakování běhu. */
	int getPosledniPocetCteni() {
		return posledniPocetCteni;
	}

	/** Sledovaná databáze se od načtení změnila a jiný program ji už nedrží. */
	private boolean zmenilaSeSledovana() {
		final Set<File> naposledyNactene = new HashSet<>();
		if (posledniSeznam != null) {
			for (final KeFile f : posledniSeznam) {
				naposledyNactene.add(f.getFile());
			}
		}
		// Databáze, která už není ve zdrojích, se nesleduje; chybějící zůstává, až se vrátí.
		sledovane.keySet().removeIf(f -> !naposledyNactene.contains(f) && f.exists());
		for (final Map.Entry<File, String> e : sledovane.entrySet()) {
			// Chybějící databázi (odpojený disk) najde sken, až se vrátí, a sledování pokračuje.
			if (e.getKey().exists() && !zamcene.contains(e.getKey()) && !otisk(e.getKey()).equals(e.getValue()) && !DatabazeJinehoProgramu.jeZamcena(e.getKey())) {
				return true;
			}
		}
		return false;
	}

	/** Čas a velikost databáze i jejího WAL, zápis se projeví aspoň v jednom z nich. */
	private static String otisk(final File databaze) {
		final File wal = new File(databaze.getPath() + "-wal");
		return databaze.lastModified() + ":" + databaze.length() + ":" + wal.lastModified() + ":" + wal.length();
	}

	/** Zdroje, které se právě načítají, aby šly v Přehledu zdrojů vypnout dřív, než se načtou. */
	private static InformaceOZdrojich predbezneZdroje(final List<KeFile> list) {
		final InformaceOZdrojich.Builder zdroje = InformaceOZdrojich.builder();
		for (final KeFile f : list) {
			zdroje.add(f, true);
		}
		return zdroje.done();
	}

	private static Set<File> databaze(final List<KeFile> list, final Root.Def def) {
		return list.stream().filter(f -> def.equals(f.root.def)).map(KeFile::getFile).collect(Collectors.toSet());
	}

	/**
	 * Aktivní složka GeoGetu, GSAKu nebo OpenSAKu bez databáze je skoro jistě špatně zadaná, uživatel by jinak jen koukal na prázdnou mapu. Do logu jednou za běh, důvod
	 * pro stav zdrojů při každém skenu.
	 */
	private Map<TypZdroje, String> ohlasPrazdneSlozky(final List<KeFile> list) {
		final Map<File, Object[]> slozky = new LinkedHashMap<>();
		if (geogetDir != null) {
			slozky.put(geogetDir, new Object[] { "GeoGetu", ".db3", TypZdroje.GEOGET });
		}
		if (gsakDir != null) {
			slozky.put(gsakDir, new Object[] { "GSAKu", ".db3", TypZdroje.GSAK });
		}
		if (opensakDir != null) {
			slozky.put(opensakDir, new Object[] { "OpenSAKu", ".db", TypZdroje.OPENSAK });
		}
		for (final KeFile f : list) {
			slozky.remove(f.root.dir);
		}
		final Map<TypZdroje, String> problemy = new EnumMap<>(TypZdroje.class);
		final StringBuilder zprava = new StringBuilder();
		for (final Map.Entry<File, Object[]> e : slozky.entrySet()) {
			final String pripona = (String) e.getValue()[1];
			problemy.put((TypZdroje) e.getValue()[2], duvodPrazdneSlozky(e.getKey(), pripona));
			if (ohlasenePrazdne.add(e.getKey())) {
				zprava.append(popisPrazdneSlozky(e.getKey(), (String) e.getValue()[0], pripona)).append('\n');
			}
		}
		if (zprava.length() > 0) {
			zprava.append("Zkontrolujte složky v Soubor > Umístění souborů.");
			ohlasovac.accept(zprava.toString());
		}
		return problemy;
	}

	/** Krátký důvod pro stav zdroje, bez cesty. */
	static String duvodPrazdneSlozky(final File slozka, final String pripona) {
		if (!slozka.exists()) {
			return "Složka není dostupná.";
		}
		if (!jeCitelnaSlozka(slozka)) {
			return "Složka není čitelná.";
		}
		return "Ve složce nejsou databáze " + pripona + ".";
	}

	static String popisPrazdneSlozky(final File slozka, final String program, final String pripona) {
		if (!slozka.exists()) {
			return "Datová složka " + program + " \"" + slozka + "\" neexistuje nebo není dostupná.";
		}
		if (!jeCitelnaSlozka(slozka)) {
			return "Datová složka " + program + " \"" + slozka + "\" není čitelná složka.";
		}
		return "V datové složce " + program + " \"" + slozka + "\" nejsou žádné databáze (" + pripona + ").";
	}

	private static String popisChyby(final Exception e) {
		final String zprava = e.getMessage();
		if (zprava == null || zprava.isEmpty()) {
			return "Soubor se nepodařilo přečíst.";
		}
		final int konec = zprava.indexOf('\n');
		return konec < 0 ? zprava : zprava.substring(0, konec);
	}

	private Set<File> zapnute(final List<KeFile> seznam) {
		final Set<File> vysledek = new HashSet<>();
		for (final KeFile f : seznam) {
			if (kesoidModel.maSeNacist(f.getFile())) {
				vysledek.add(f.getFile());
			}
		}
		return vysledek;
	}

	private List<KeFile> bezZamcenych(final List<KeFile> seznam) {
		if (seznam == null) {
			return null;
		}
		final List<KeFile> vysledek = new ArrayList<>();
		for (final KeFile f : seznam) {
			if (!zamcene.contains(f.getFile())) {
				vysledek.add(f);
			}
		}
		Collections.sort(vysledek, Comparator.comparing(f -> f.getFile().getPath()));
		return vysledek;
	}

	private static List<String> jmena(final Set<File> soubory) {
		final List<String> vysledek = new ArrayList<>();
		for (final File f : soubory) {
			vysledek.add(DatabazeJinehoProgramu.jmeno(f));
		}
		Collections.sort(vysledek);
		return vysledek;
	}

	public static boolean jeCitelnaSlozka(final File slozka) {
		return slozka.isDirectory() && slozka.list() != null;
	}

	/** Počet souborů podle přípony, bez cest (hlášení je veřejné). */
	private static String popisSouboru(final List<KeFile> soubory) {
		final Map<String, Integer> podlePripony = new TreeMap<>();
		for (final KeFile f : soubory) {
			final String jmeno = f.getFile().getName().toLowerCase(Locale.ROOT);
			podlePripony.merge(jmeno.contains(".") ? jmeno.substring(jmeno.lastIndexOf('.') + 1) : "bez přípony", 1, Integer::sum);
		}
		return soubory.size() + " souborů " + podlePripony;
	}

	// TODO Proč jsou tu ty File parametry, když máme k dispozici kesoidModel, odkud se jejich hodnoty vždy berou? [2016-04-09, Bohusz]
	public void setRootDirs(final boolean prenacti, final File kesDir, final File geogetDir, final File gsakDir, final Set<File> vynechane) {
		setRootDirs(prenacti, kesDir, geogetDir, gsakDir, null, vynechane);
	}

	public void setRootDirs(final boolean prenacti, final File kesDir, final File geogetDir, final File gsakDir, final File opensakDir, final Set<File> vynechane) {
		this.geogetDir = geogetDir;
		this.gsakDir = gsakDir;
		this.opensakDir = opensakDir;
		final List<Root> roots = new ArrayList<>();
		if (kesDir != null) {
			roots.add(new Root(kesDir, FILE_NAME_REGEX_GEOKUK_DIR, vynechane));
		}
		if (geogetDir != null) {
			roots.add(new Root(geogetDir, FILE_NAME_REGEX_GEOGET_DIR));
		}
		if (gsakDir != null) {
			roots.add(new Root(gsakDir, GSAK_ROOTDIR_DEF));
		}
		if (opensakDir != null) {
			roots.add(new Root(opensakDir, OPENSAK_ROOTDIR_DEF));
		}
		ds.seRootDirs(prenacti, roots.toArray(new Root[roots.size()]));
	}

	/** Cizí soubor s naší příponou jen přeskočíme, není to chyba uživatele. Databázi, kterou nejde přečíst, ale ohlásíme. */
	private boolean umiNacist(final Nacitac0 nacitac, final File file) {
		try {
			return nacitac.umiNacist(file);
		} catch (final Exception e) {
			if (DatabazeJinehoProgramu.jeZamcena(e)) {
				throw new DatabazeJinehoProgramu.Zamcena(file, e);
			}
			final String popis = DatabazeJinehoProgramu.popisChyby(file, e);
			if (popis != null) {
				throw new RuntimeException(popis, e);
			}
			log.warn("Soubor {} nelze rozpoznat, přeskakuji: {}", file, e.toString());
			return false;
		}
	}

	/** Zip, který se ještě kopíruje, nejde otevřít; načte se, až se dopíše, stejně jako ostatní rozepsané soubory. */
	private static ZipFile otevriZip(final File file) throws IOException {
		try {
			return new ZipFile(file);
		} catch (final IOException e) {
			// Zip z Windows bez příznaku UTF-8 má jména v cp852.
			try {
				return new ZipFile(file, Charset.forName("IBM852"));
			} catch (final IOException e852) {
				e.addSuppressed(e852);
			}
			if (Nacitac0.jeDrzenyJinymProgramem(e, file)) {
				throw new DatabazeJinehoProgramu.Zamcena(file, e);
			}
			if (RozepsanySoubor.jeRozepsany(file)) {
				throw new RozepsanySoubor.Rozepsany(file, e);
			}
			throw e;
		}
	}

	private void zpracujJedenFile(final KeFile kefile, final KesoidImportBuilder builder, final Future<?> future) throws IOException {
		final File file = kefile.getFile();
		if (isZipFile(file)) {
			try (ZipFile zipFile = otevriZip(file)) {
				Nacitac0.prectenCely(file);
				final boolean nacitat = kesoidModel.maSeNacist(kefile);
				for (final Enumeration<? extends ZipEntry> en = zipFile.entries(); en.hasMoreElements();) {
					final ZipEntry entry = en.nextElement();
					for (final Nacitac0 nacitac : nacitace) {
						builder.setCurrentlyLoading(kefile, nacitat);
						if (nacitat && nacitac.umiNacist(entry)) {
							Nacitac0.setPopisPrubehu(popisPrubehu(kefile, entry));
							try {
								nacitac.nactiBezVyjimky(zipFile, entry, builder, future, kesoidModel.getProgressModel());
							} finally {
								Nacitac0.setPopisPrubehu(null);
							}
						}
					}
				}
			}
		} else {
			for (final Nacitac0 nacitac : nacitace) {
				final boolean nacitat = kesoidModel.maSeNacist(kefile);
				builder.setCurrentlyLoading(kefile, nacitat);
				if (nacitat && umiNacist(nacitac, file)) {
					Nacitac0.setPopisPrubehu(popisPrubehu(kefile, null));
					try {
						nacitac.nactiBezVyjimky(file, builder, future, kesoidModel.getProgressModel());
					} finally {
						Nacitac0.setPopisPrubehu(null);
					}
				}
			}
		}
	}
}
