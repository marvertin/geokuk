package cz.geokuk.plugins.kesoid.mapicon;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.util.lang.CCounterMap;
import cz.geokuk.util.lang.CounterMap;

public class Genom {

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

	// public static Genom G = new Genom();

	private static final Logger log = LogManager.getLogger(Genom.class.getSimpleName());
	public static final String NEZARAZENY_GEN = "<Nezařazení>";
	private final Map<String, Alela> alely = new LinkedHashMap<>();
	private final Map<String, Gen> geny = new LinkedHashMap<>();

	private final List<Gen> genyList = new ArrayList<>();

	private Gen symGen;

	// TODO : refactor this so the alelas are loaded from an external file (-> possibility of user addition of new alelas
	// easily).

	private Gen currentGen;

	public Alela ALELA_Waypoint = coale("Waypoint", "Obecný waypoint");
	public Alela ALELA_h = coale("h", "Hlavní waypoint");

	public Alela ALELA_v = coale("v", "Vedlejší waypoint");
	public Alela ALELA_00 = coale("00", "Neznámý");
	public Alela ALELA_gc = coale("gc", "Keš");
	public Alela ALELA_wm = coale("wm", "Waymark");
	public Alela ALELA_gb = coale("gb", "Český geodetický bod");
	public Alela ALELA_wp = coale("wp", "Obecný waypoint");
	public Alela ALELA_mz = coale("mz", "Munzee");

	public Alela ALELA_pic = coale("pic", "Obrázek");
	public Alela ALELA_hnf = coale("hnf", "Nehledané");
	public Alela ALELA_fnd = coale("fnd", "Nalezené");
	public Alela ALELA_own = coale("own", "Moje vlastní");
	public Alela ALELA_not = coale("not", "Neexistující");
	public Alela ALELA_cpt = coale("cpt", "Captured");

	public Alela ALELA_dpl = coale("dpl", "Deployed");
	public Alela ALELA_actv = coale("actv", "Aktivmí");
	public Alela ALELA_dsbl = coale("dsbl", "Disablovaná");

	public Alela ALELA_arch = coale("arch", "Archivovaný");
	public Alela ALELA_00000 = coale("00000", "Neznámá");
	public Alela ALELA_nlist = coale("nlist", "Not listed");
	public Alela ALELA_micro = coale("micro", "Micro");
	public Alela ALELA_small = coale("small", "Small");
	public Alela ALELA_regul = coale("regul", "Regular");
	public Alela ALELA_large = coale("large", "Large");
	public Alela ALELA_hugex = coale("hugex", "Huge");
	public Alela ALELA_virtu = coale("virtu", "Virtual");

	public Alela ALELA_other = coale("other", "Other");
	public Alela ALELA_nevime = coale("nevime", "Nerozhodnuto");
	public Alela ALELA_lovime = coale("lovime", "Jdeme lovit");

	public Alela ALELA_ignoru = coale("ignoru", "Budeme ignorovat");
	public Alela ALELA_nomouse = coale("nomouse", "Žádná myš");
	public Alela ALELA_mousean = coale("mousean", "Myš nad jiným wpt kešoidu");

	public Alela ALELA_mouseon = coale("mouseon", "Myš nad tímto wpt");
	public Alela ALELA_noselect = coale("noselect", "Nevybraný");

	public Alela ALELA_selected = coale("selected", "Vybraný");
	public Alela ALELA_nevyluste = coale("nevyluste", "Není vyluštěno");

	public Alela ALELA_vylusteno = coale("vylusteno", "Je vyluštěno");
	public Alela ALELA_pqimported = coale("pqimported", "Imporotvané z PQ");

	public Alela ALELA_handedited = coale("handedited", "Ručně přidané");
	public Alela ALELA_mimocesticu = coale("mimocesticu", "Mimo cestu");

	public Alela ALELA_nacestejsou = coale("nacestejsou", "Na cestě");
	public Alela ALELA_ter0 = coale("ter0", "Nespecifikovaný");
	public Alela ALELA_ter10 = coale("ter10", "1");
	public Alela ALELA_ter15 = coale("ter15", "1,5");
	public Alela ALELA_ter20 = coale("ter20", "2");
	public Alela ALELA_ter25 = coale("ter25", "2,5");
	public Alela ALELA_ter30 = coale("ter30", "3");
	public Alela ALELA_ter35 = coale("ter35", "3,5");
	public Alela ALELA_ter40 = coale("ter40", "4");
	public Alela ALELA_ter45 = coale("ter45", "4,5");

	public Alela ALELA_ter50 = coale("ter50", "5");
	public Alela ALELA_def0 = coale("dif0", "Nespecifikovaná");
	public Alela ALELA_dif10 = coale("dif10", "1");
	public Alela ALELA_dif15 = coale("dif15", "1,5");
	public Alela ALELA_dif20 = coale("dif20", "2");
	public Alela ALELA_dif25 = coale("dif25", "2,5");
	public Alela ALELA_dif30 = coale("dif30", "3");
	public Alela ALELA_dif35 = coale("dif35", "3,5");
	public Alela ALELA_dif40 = coale("dif40", "4");
	public Alela ALELA_dif45 = coale("dif45", "4,5");
	public Alela ALELA_dif50 = coale("dif50", "5");
	public Grupa GRUPA_gcawp;

	public Grupa GRUPA_gc;

	{
		symGen = gen("Typ waypointu", true);
		ale(ALELA_Waypoint);

		gen("Postavení", true);
		// ale(ALELA_0);
		ale(ALELA_h);
		ale(ALELA_v);

		gen("Druh kešoidu", true);
		ale(ALELA_00);
		ale(ALELA_gc);
		ale(ALELA_wm);
		ale(ALELA_mz);
		ale(ALELA_pic);
		ale(ALELA_gb);
		ale(ALELA_wp);

		gen("Vztah", true);
		ale(ALELA_hnf);
		ale(ALELA_fnd);
		ale(ALELA_own);
		ale(ALELA_not);
		ale(ALELA_cpt);
		ale(ALELA_dpl);

		gen("Stav", true);
		ale(ALELA_actv);
		ale(ALELA_dsbl);
		ale(ALELA_arch);

		gen("Velikost", false);
		ale(ALELA_00000);
		ale(ALELA_nlist);
		ale(ALELA_micro);
		ale(ALELA_small);
		ale(ALELA_regul);
		ale(ALELA_large);
		ale(ALELA_hugex);
		ale(ALELA_virtu);
		ale(ALELA_other);

		gen("Výlet", false);
		ale(ALELA_nevime);
		ale(ALELA_lovime);
		ale(ALELA_ignoru);

		gen("Cesty", true);
		ale(ALELA_mimocesticu);
		ale(ALELA_nacestejsou);

		gen("Postavení k myši", false);
		ale(ALELA_nomouse);
		ale(ALELA_mousean);
		ale(ALELA_mouseon);

		gen("Vybranost", false);
		ale(ALELA_noselect);
		ale(ALELA_selected);

		gen("Vyluštěnost", false);
		ale(ALELA_nevyluste);
		ale(ALELA_vylusteno);

		gen("Zdroj", false);
		ale(ALELA_pqimported);
		ale(ALELA_handedited);

		gen("Terén", false);
		ale(ALELA_ter0);
		ale(ALELA_ter10);
		ale(ALELA_ter15);
		ale(ALELA_ter20);
		ale(ALELA_ter25);
		ale(ALELA_ter30);
		ale(ALELA_ter35);
		ale(ALELA_ter40);
		ale(ALELA_ter45);
		ale(ALELA_ter50);

		gen("Obtížnost", false);
		ale(ALELA_def0);
		ale(ALELA_dif10);
		ale(ALELA_dif15);
		ale(ALELA_dif20);
		ale(ALELA_dif25);
		ale(ALELA_dif30);
		ale(ALELA_dif35);
		ale(ALELA_dif40);
		ale(ALELA_dif45);
		ale(ALELA_dif50);

		currentGen = null;
	}

	/**
	 * Určeno pro defince genů a alel externích. Pro každý nový gen se musí vytvořit též implicitní alela.
	 *
	 * @param alelaName
	 * @param genName
	 * @return
	 */
	public Alela alela(final String alelaName, final String genName) {
		final Gen gen = gen(genName, false);
		if (gen.getAlely().size() == 0) {
			gen.add(makeAlela(":" + genName), null);
		}
		// TODO Zkontrolovat: Doplnil jsem kvalifikaci jménem genu, protože jinak se mi generovalo obrovské množství těch errorů dole. Nevím, co to může všechno způsobit, ale stejné řetězce přece musí být možné použít jako alely různých genů, ne? [ISSUE#48, 2016-04-09, Bohusz]
		final Alela alela = makeAlela(alelaName + ":" + genName);
		if (alela.hasGen()) {
			if (alela.getGen() == gen) {
				return alela; // tak vrátíme tu alelu
			} else {
				log.error(String.format("Je pozadovana alela '%s' v genu %s, ale tato alela jiz existuje v genu %s", alelaName, genName, alela.getGen()));
				return null;
			}
		} else {
			gen.add(alela, null);
			return alela;
		}
	}

	public Alela alelaSym(final String wptsym, final String jmenoGrupy) {
		return alela(wptsym, symGen, jmenoGrupy);
	}

	public CitacAlel createCitacAlel() {
		return new CitacAlel();
	}

	public boolean existsGen(final String displayName) {
		return geny.containsKey(displayName);
	}

	public Genotyp getGenotypProAlelu(final Alela alela) {
		final Set<Alela> alely = new HashSet<>();
		for (final Gen gen : genyList) {
			if (gen.getAlely().contains(alela)) {
				alely.add(alela);
			} else {
				alely.add(gen.getVychoziAlela());
			}
		}
		return new Genotyp(alely, this);
	}

	public Genotyp getGenotypVychozi() {
		final Set<Alela> alely = new HashSet<>();
		for (final Gen gen : genyList) {
			assert gen != null;
			alely.add(gen.getVychoziAlela());
		}
		return new Genotyp(alely, this);
	}

	public List<Gen> getGeny() {
		return genyList;
	}

	public Gen getSymGen() {
		return symGen;
	}

	public Alela locateAlela(final String alelaName) {
		return alely.get(alelaName);
	}

	public Set<Alela> namesToAlely(final Set<String> jmenaAlel) {
		final Set<Alela> alely = new HashSet<>();
		for (final String jmeno : jmenaAlel) {
			if (jmeno != null && jmeno.length() > 0) {
				final Alela alela = seekAlela(jmeno);
				alely.add(alela);
			}
		}
		return alely;
	}

	public Set<Alela> namesToAlelyIgnorujNeexistujici(final Set<String> jmenaAlel) {
		final Set<Alela> alely = new HashSet<>();
		for (final String jmeno : jmenaAlel) {
			if (jmeno != null && jmeno.length() > 0) {
				final Alela alela = locateAlela(jmeno);
				if (alela != null) {
					alely.add(alela);
				}
			}
		}
		return alely;
	}

	public Alela seekAlela(final String alelaName) {
		Alela alela = alely.get(alelaName);
		if (alela == null) {
			log.warn("Alela [{}] neni definovana!", alelaName);
			alela = makeAlela(alelaName);
		}

		return alela;
	}

	boolean isAlelaSym(final Alela alela) {
		return alela.getGen() == symGen;
	}

	private void ale(final Alela alela) {
		currentGen.add(alela, null);
	}

	private synchronized Alela alela(final String alelaName, final Gen gen, final String jmenoGrupy) {
		final Alela alela = makeAlela(alelaName);
		gen.add(alela, jmenoGrupy);
		return alela;
	}

	private Alela coale(final String alelaName, final String displayName) {
		final Alela alela = makeAlela(alelaName);
		alela.setDisplayName(displayName);
		return alela;
	}

	private synchronized Gen gen(final String displayName, final boolean vypsatelnyVeZhasinaci) {
		Gen gen = geny.get(displayName);
		if (gen == null) {
			gen = new Gen(displayName, this, vypsatelnyVeZhasinaci);
			genyList.add(gen);
			geny.put(displayName, gen);
		}
		currentGen = gen;
		return gen;
	}

	private synchronized Alela makeAlela(final String alelaName) {
		Alela alela = alely.get(alelaName);
		if (alela == null) {
			alela = new Alela(alelaName, alely.size());
			alely.put(alelaName, alela);
		}
		return alela;
	}
}
