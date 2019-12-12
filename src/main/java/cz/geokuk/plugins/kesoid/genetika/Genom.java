package cz.geokuk.plugins.kesoid.genetika;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.util.lang.CCounterMap;
import cz.geokuk.util.lang.CounterMap;

public class Genom {

	final String ODDELOVAC_KVALIFOVANY = ":";

	private static final Logger log = LogManager.getLogger(Genom.class.getSimpleName());

	private final Map<String, Alela> alely = new LinkedHashMap<>();

	// Geny jsou v mapš ui v seznamu pro snadný přístup
	private final Map<String, Gen> geny = new LinkedHashMap<>();
	private final List<Gen> genyList = new ArrayList<>();

	private final Map<String, Druh> druhy = new LinkedHashMap<>();

	public final Druh UNIVERZALNI_DRUH = druh("unidruh").displayName("Univerzální dočasný druh");
	/**
	 * Zde jsou zveřejněné alely a geny, které se používají v kódu. Řetězce uvedené pro jejich definici se nikde jinde v programu nesmějí vykytnout.
	 */

	// symbolové geny
	public final Gen GEN_gc = genu("gc", "Typ kešky", "Není keš", true);
	public final Gen GEN_gcawp = genu("gcawp", "Dodatečné waypointy", "Není dodapoint", true);
	public final Gen GEN_cgp = genu("cgp", "Chech Geodetic Point", "", true);
	public final Gen GEN_wm = genu("wm", "Waymark", "Není waymark", true);
	public final Gen GEN_mz = genu("mz", "Munzee", "Není munzee", true);
	public final Gen GEN_pic = genu("pic", "Fotka", "Není fotka", true);

	public final Gen GEN_simpleWaypoint = genu("waypoint", "Obyčejný waypoint", "Není obyčejný waypoint", true);

	// Má jen jednu alelu, neboť alely přicházejí dynamicky, jak se nahrávají ikony nebo data
	//public final Gen symGen = genu("sym", "Typ waypointu", "Obecný waypoint", true); // spoléháme se na to, že symgen je první
	//public final Alela ALELA_Waypoint = symGen.alela("Waypoint").displayName("Obecný waypoint");
	//public final Alela ALELA_Waypoint = symGen.getVychoziAlela();

	public final Gen GEN_postavenikMysi = genu("mouse", "Postavení k myši", "Žádná myš", false);
	//public final Alela ALELA_nomouse = GEN_postavenikMysi.alela("nomouse").displayName("Žádná myš");
	public final Alela ALELA_nomouse = GEN_postavenikMysi.getVychoziAlela();
	public final Alela ALELA_mousean = GEN_postavenikMysi.alela("mousean").displayName("Myš nad jiným wpt kešoidu");
	public final Alela ALELA_mouseon = GEN_postavenikMysi.alela("mouseon").displayName("Myš nad tímto wpt");

	public final Gen GEN_Postaveni = genu("postaveni", "Postavení", "Hlavní waypoint", true);
	//public final Alela ALELA_h = GEN_Postaveni.alela("h").displayName("Hlavní waypoint");
	public final Alela ALELA_h = GEN_Postaveni.getVychoziAlela();
	public final Alela ALELA_v = GEN_Postaveni.alela("v").displayName("Vedlejší waypoint");

	public final Gen GEN_druhKesoidu = genu("druh", "Druh kešoidu", "Neznámý", true);
	//public final Alela ALELA_00 = GEN_druhKesoidu.alela("00").displayName("Neznámý");
	public final Alela ALELA_00 = GEN_druhKesoidu.getVychoziAlela();
	public final Alela ALELA_gc = GEN_druhKesoidu.alela("gc").displayName("Keš");
	public final Alela ALELA_wm = GEN_druhKesoidu.alela("wm").displayName("Waymark");
	public final Alela ALELA_mz = GEN_druhKesoidu.alela("mz").displayName("Munzee");
	public final Alela ALELA_pic = GEN_druhKesoidu.alela("pic").displayName("Obrázek");
	public final Alela ALELA_gb = GEN_druhKesoidu.alela("gb").displayName("Český geodetický bod");
	public final Alela ALELA_wp = GEN_druhKesoidu.alela("wp").displayName("Obecný waypoint");

	public final Gen GEN_vztah = genu("vztah", "Vztah", "Nehledané", true);
	//public final Alela ALELA_hnf = GEN_vztah.alela("hnf").displayName("Nehledané");
	public final Alela ALELA_hnf = GEN_vztah.getVychoziAlela();
	public final Alela ALELA_fnd = GEN_vztah.alela("fnd").displayName("Nalezené");
	public final Alela ALELA_own = GEN_vztah.alela("own").displayName("Moje vlastní");
	public final Alela ALELA_not = GEN_vztah.alela("not").displayName("Neexistující");
	public final Alela ALELA_cpt = GEN_vztah.alela("cpt").displayName("Captured");
	public final Alela ALELA_dpl = GEN_vztah.alela("dpl").displayName("Deployed");

	public final Gen GEN_stav = genu("stav", "Stav", "Aktivmíx", true);
	//public final Alela ALELA_actv = GEN_stav.alela("actv").displayName("Aktivmí");
	public final Alela ALELA_actv = GEN_stav.getVychoziAlela();
	public final Alela ALELA_dsbl = GEN_stav.alela("dsbl").displayName("Disablovaná");
	public final Alela ALELA_arch = GEN_stav.alela("arch").displayName("Archivovaný");

	public final Gen GEN_velikost = genu("velikost", "Velikost", "Neznámá", false);
	//public final Alela ALELA_00000 = GEN_velikost.alela("00000").displayName("Neznámá");
	public final Alela ALELA_00000 = GEN_velikost.getVychoziAlela();
	public final Alela ALELA_nlist = GEN_velikost.alela("nlist").displayName("Not listed");
	public final Alela ALELA_micro = GEN_velikost.alela("micro").displayName("Micro");
	public final Alela ALELA_small = GEN_velikost.alela("small").displayName("Small");
	public final Alela ALELA_regul = GEN_velikost.alela("regul").displayName("Regular");
	public final Alela ALELA_large = GEN_velikost.alela("large").displayName("Large");
	public final Alela ALELA_hugex = GEN_velikost.alela("hugex").displayName("Huge");
	public final Alela ALELA_virtu = GEN_velikost.alela("virtu").displayName("Virtual");
	public final Alela ALELA_other = GEN_velikost.alela("other").displayName("Other");

	public final Gen GEN_vylet = genu("vylet", "Výlet", "Nerozhodnuto", false);
	//public final Alela ALELA_nevime = GEN_vylet.alela("nevime").displayName("Nerozhodnuto");
	public final Alela ALELA_nevime = GEN_vylet.getVychoziAlela();
	public final Alela ALELA_lovime = GEN_vylet.alela("lovime").displayName("Jdeme lovit");
	public final Alela ALELA_ignoru = GEN_vylet.alela("ignoru").displayName("Budeme ignorovat");

	public final Gen GEN_cesty = genu("cesty", "Cesty", "Mimo cestu", true);
	//public final Alela ALELA_mimocesticu = GEN_cesty.alela("mimocesticu").displayName("Mimo cestu");
	public final Alela ALELA_mimocesticu = GEN_cesty.getVychoziAlela();
	public final Alela ALELA_nacestejsou = GEN_cesty.alela("nacestejsou").displayName("Na cestě");

	public final Gen GEN_Vybranost = genu("vybranost", "Vybranost", "Nevybraný", false);
	//public final Alela ALELA_noselect = GEN_Vybranost.alela("noselect").displayName("Nevybraný");
	public final Alela ALELA_noselect = GEN_Vybranost.getVychoziAlela();
	public final Alela ALELA_selected = GEN_Vybranost.alela("selected").displayName("Vybraný");

	public final Gen GEN_vylustenost = genu("vylustenost", "Vyluštěnost", "Není vyluštěno", false);
	//public final Alela ALELA_nevyluste = GEN_vylustenost.alela("nevyluste").displayName("Není vyluštěno");
	public final Alela ALELA_nevyluste = GEN_vylustenost.getVychoziAlela();
	public final Alela ALELA_vylusteno = GEN_vylustenost.alela("vylusteno").displayName("Je vyluštěno");

	public final Gen GEN_zdroj = genu("zdroj", "Zdroj", "Imporotvané z PQ", false);
	//public final Alela ALELA_pqimported = GEN_zdroj.alela("pqimported").displayName("Imporotvané z PQ");
	public final Alela ALELA_pqimported = GEN_zdroj.getVychoziAlela();
	public final Alela ALELA_handedited = GEN_zdroj.alela("handedited").displayName("Ručně přidané");

	public final Gen GEN_teren = genu("ter", "Terén", "Nespecifikovaný", false);
	//public final Alela ALELA_ter0 = GEN_teren.alela("ter0").displayName("Nespecifikovaný");
	public final Alela ALELA_ter0 = GEN_teren.getVychoziAlela();
	public final Alela ALELA_ter10 = GEN_teren.alela("ter10").displayName("1");
	public final Alela ALELA_ter15 = GEN_teren.alela("ter15").displayName("1,5");
	public final Alela ALELA_ter20 = GEN_teren.alela("ter20").displayName("2");
	public final Alela ALELA_ter25 = GEN_teren.alela("ter25").displayName("2,5");
	public final Alela ALELA_ter30 = GEN_teren.alela("ter30").displayName("3");
	public final Alela ALELA_ter35 = GEN_teren.alela("ter35").displayName("3,5");
	public final Alela ALELA_ter40 = GEN_teren.alela("ter40").displayName("4");
	public final Alela ALELA_ter45 = GEN_teren.alela("ter45").displayName("4,5");
	public final Alela ALELA_ter50 = GEN_teren.alela("ter50").displayName("5");

	public final Gen GEN_obtiznost = genu("dif", "Obtížnost", "Nespecifikovaná", false);
	//public final Alela ALELA_def0 = GEN_obtiznost.alela("dif0").displayName("Nespecifikovaná");
	public final Alela ALELA_def0 = GEN_obtiznost.getVychoziAlela();
	public final Alela ALELA_dif10 = GEN_obtiznost.alela("dif10").displayName("1");
	public final Alela ALELA_dif15 = GEN_obtiznost.alela("dif15").displayName("1,5");
	public final Alela ALELA_dif20 = GEN_obtiznost.alela("dif20").displayName("2");
	public final Alela ALELA_dif25 = GEN_obtiznost.alela("dif25").displayName("2,5");
	public final Alela ALELA_dif30 = GEN_obtiznost.alela("dif30").displayName("3");
	public final Alela ALELA_dif35 = GEN_obtiznost.alela("dif35").displayName("3,5");
	public final Alela ALELA_dif40 = GEN_obtiznost.alela("dif40").displayName("4");
	public final Alela ALELA_dif45 = GEN_obtiznost.alela("dif45").displayName("4,5");
	public final Alela ALELA_dif50 = GEN_obtiznost.alela("dif50").displayName("5");

	// Toto jen dočasně, protože nefunguje vytváření uživatelských alel až za pochodu, nefunguje pak filtr.
//	public final Gen GEN_mysterstav = genu("mysterstav", true);
//	public final Alela ALELA_nic = GEN_mysterstav.alela(":mysterstav").displayName("Nic");
//	public final Alela ALELA_teren = GEN_mysterstav.alela("teren:mysterstav").displayName("Terén");
//	public final Alela ALELA_zasek = GEN_mysterstav.alela("zasek:mysterstav").displayName("Zásek");
//	public final Alela ALELA_lusteni = GEN_mysterstav.alela("lusteni:mysterstav").displayName("Luštění");

	/**
	 * @deprecated Grupy nechceme, použijeme druhy.
	 */
	@Deprecated
	public Grupa GRUPA_gcawp;

	/**
	 * @deprecated Grupy nechceme, použijeme druhy.
	 */
	@Deprecated
	public Grupa GRUPA_gc;


	/**
	 * Vrátí nebo zřídí alelu. Pokud vzniká nová alela, není přiřazena ke genu.
	 */
	synchronized Alela getOrCreateAlela(final String alelaName, final Gen gen) {
		final Alela alela = alely.computeIfAbsent(alelaName + ODDELOVAC_KVALIFOVANY + gen.getNazev(), name -> new Alela(alelaName, gen, alely.size()));
		return alela;
	}

	/** Vrátí nebo zřídí druh */
	public synchronized final Druh druh(final String nazev) {
		Druh druh = druhy.get(nazev);
		if (druh == null) {
			druh = new Druh(nazev, druhy.size(), this);
			druhy.put(nazev, druh);
		}
		return druh;
	}

	boolean existsGen(final String nazev) {
		return geny.containsKey(nazev);
	}

	/**
	 * Vrátí nebo zřídí gen.
	 *
	 * @param nazev
	 * @return
	 */
	public synchronized Gen gen(final String nazev) {
		Gen gen = geny.get(nazev);
		if (gen == null) {
			gen = new Gen(nazev, geny.size(), this);
			genyList.add(gen);
			geny.put(nazev, gen);
		}
		return gen;
	}

	/**
	 * Vrátí nebo zřídí gen a přidá ho do univerzálního druhu.
	 *
	 * @param nazev
	 * @param vychoziAlelaDisplayName
	 * @return
	 */
	public synchronized Gen genu(final String nazev, final String displayName, final String vychoziAlelaDisplayName, final boolean vypsatelnyVeZhasinaci) {
		final Gen gen = gen(nazev);
		gen.getVychoziAlela().displayName(vychoziAlelaDisplayName);
		gen.vypsatelnyVeZhasinaci = vypsatelnyVeZhasinaci;
		UNIVERZALNI_DRUH.addGen(gen);
		return gen;
	}

	public Collection<Alela> getAlely() {
		return Collections.unmodifiableCollection(alely.values());
	}

	public Collection<Druh> getDruhy() {
		return Collections.unmodifiableCollection(druhy.values());
	}

	/**
	 * Seznam všech registrovaných genů.
	 *
	 * @return
	 */
	public List<Gen> getGeny() {
		return Collections.unmodifiableList(genyList);
	}

	/**
	 * Převede kvalifikovaná jména alel na alely s tím, že ignoruje neexistující alely.
	 * @param qualNames KValifikovaná jména ale.
	 * @return Nalezené alely. Nevrací null. Pokud nějaká alela není nalezena dle jména, tak je jméno ignorováno.
	 */
	public Set<Alela> searchAlelasByQualNames(final QualAlelaNames qualNames) {
		return qualNames.getQualNames().stream()
				.filter(jmeno -> jmeno != null && jmeno.length() > 0)
				.map(this::locateQualAlela)    // najdeme alelu
				.filter(Optional::isPresent)   // jen pokud existuje daneho jmena
				.map(Optional::get)            // a uz vime, ze existuje
				.collect(Collectors.toSet());
	}

	/**
	 * FIXME genetika: neodpovídá sekku, jen varuje.
	 *
	 * @param alelaName
	 * @return
	 */
	public Alela seekAlela(final String alelaName) {
		final Alela alela = alely.get(alelaName);
		if (alela == null) {
			throw new IllegalArgumentException("Alela " + alelaName + " neni definovana, metodu lze volat jen v situaci, kdy vime, ze alela existuje!");
		}
		return alela;
	}


	/**
	 * Vyhledá alelu podle kvalifikovaného jména.
	 *
	 * @param alelaName
	 * @return Nalezenou alelu nebo empty, pokud alela není nalezena. Totéž vrátí pro nekvalifikvoané jméno neboď dle něho nemůže být nalezena.
	 */
	public Optional<Alela> locateQualAlela(final String qualName) {
		final int poz = qualName.lastIndexOf(ODDELOVAC_KVALIFOVANY);
		if (poz < 0) {
			return Optional.empty();
		}
		final String genName = qualName.substring(poz+ ODDELOVAC_KVALIFOVANY.length());
		final String alelaName = qualName.substring(0, poz);
		final Gen gen = geny.get(genName);
		if (gen == null) {
			return Optional.empty();
		} else {
			return gen.locateAlela(alelaName);
		}
	}


	/**
	 * Vytvoří čítač alel s nulama a umožňuje explicitně počítat alely.
	 *
	 * @return
	 */
	public CitacAlel createCitacAlel() {
		return new CitacAlel();
	}

	/**
	 * Umožnuje počítat alely rychlým způsobem bez mapy.
	 *
	 * @author veverka
	 *
	 */
	public class CitacAlel {

		private int[] pocty;

		public CitacAlel() {
			pocty = new int[1000];
		}

		public void add(final Alela alela) {
			assert alela != null;
			final int poradi = alela.getCelkovePoradi();
			if (poradi >= pocty.length) {
				final int[] p = pocty;
				pocty = new int[poradi + 1000];
				System.arraycopy(p, 0, pocty, 0, p.length);

			}
			pocty[poradi]++;
		}

		public CounterMap<Alela> getCounterMap() {
			final CounterMap<Alela> cm = new CCounterMap<>();
			for (final Alela alela : alely.values()) {
				final int celkovePoradi = alela.getCelkovePoradi();
				cm.set(alela, celkovePoradi >= pocty.length ? 0 : pocty[celkovePoradi]);
			}
			return cm;
		}
	}
}
