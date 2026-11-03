package cz.geokuk.plugins.kesoid.importek;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import cz.geokuk.framework.Event0;
import cz.geokuk.framework.ProgressModel;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.kind.KesoidPluginManager;
import cz.geokuk.plugins.kesoid.mvc.GccomNick;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.util.file.KeFile;

/**
 * Načtení po přepnutí zdrojů, změně souborů i zrušeném běhu dává po waypointech totéž co plné načtení stejných zdrojů: kešoidy, jejich waypointy, hlavní waypoint, druh, kopii
 * z nejnovějšího zdroje u duplicit a počty po zdrojích.
 */
public class ShodaPostupnehoNacteniTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final Set<File> vypnute = new HashSet<>();
	private final Genom genom = new Genom();
	private final GccomNick nick = new GccomNick("Ja", 42);
	private final CasyDatZdroju casy = new CasyDatZdroju();
	private File gpx;
	private File geoget;
	private MultiNacitac nacitac;
	private KesBag zobrazene;
	/** Uložené časy dat zdrojů, jako by byly v nastavení. */
	private Set<String> nastaveniCasu = new LinkedHashSet<>();
	private int zapisuCasu;
	/** Časy souborů řídí test, aby pořadí zdrojů nezáviselo na rychlosti disku. */
	private long hodiny = 1_700_000_000_000L;

	@Before
	public void setUp() throws Exception {
		gpx = tmp.newFolder("gpx");
		geoget = tmp.newFolder("geoget");
		nacitac = new MultiNacitac(model(() -> zobrazene), casy);
	}

	// ---------- jednotlivé případy ----------

	@Test
	public void duplicituVyhrajeNejnovejsiZdrojPriPlnemIPostupnemNacteni() throws Exception {
		final File stary = gpxKesi("a-stary.gpx", "stary", "GC0001", "GC0002");
		final File novy = gpxKesi("b-novy.gpx", "novy", "GC0001");
		Assert.assertEquals("novy", zdrojKese(nacti(), "GC0001"));
		Assert.assertEquals("stary", zdrojKese(zobrazene, "GC0002"));
		over("po plném načtení");

		vypnute.add(novy);
		Assert.assertEquals("stary", zdrojKese(nacti(), "GC0001"));
		vypnute.clear();
		Assert.assertEquals("novy", zdrojKese(nacti(), "GC0001"));
		over("vypnutí a zapnutí novějšího");

		vypnute.add(stary);
		nacti();
		vypnute.clear();
		Assert.assertEquals("znovu čtený starší nepředběhne novější", "novy", zdrojKese(nacti(), "GC0001"));
		over("vypnutí a zapnutí staršího");
	}

	@Test
	public void otevreniBezZmenyDatZdrojNeomladi() throws Exception {
		final File stary = gpxKesi("a-stary.gpx", "stary", "GC0001");
		gpxKesi("b-novy.gpx", "novy", "GC0001");
		nacti();
		dotkniSe(stary);
		Assert.assertEquals("jen nový čas souboru", "novy", zdrojKese(nacti(), "GC0001"));
		over("po dotyku");
		gpxKesi("a-stary.gpx", "stary2", "GC0001");
		Assert.assertEquals("změněný obsah je nejnovější", "stary2", zdrojKese(nacti(), "GC0001"));
		over("po změně obsahu");
	}

	@Test
	public void priShodeCasuRozhodneCesta() throws Exception {
		final File b = gpxKesi("b.gpx", "b", "GC0001");
		final File a = gpxKesi("a.gpx", "a", "GC0001");
		a.setLastModified(b.lastModified());
		Assert.assertEquals("a", zdrojKese(nacti(), "GC0001"));
		vypnute.add(a);
		nacti();
		vypnute.clear();
		Assert.assertEquals("a", zdrojKese(nacti(), "GC0001"));
		over("shoda časů");
	}

	@Test
	public void waymarkUGeodetickehoBoduZJinehoSouboru() throws Exception {
		gpx("cgp.gpx", cgp("GC12AB34", "1234-56.1", "Traditional Cache"), cgp("GC12AB35", "1234-56.2", "Letterbox Hybrid"), kes("GC0001", "cgp"));
		final File wm = gpx("wm.gpx", waymark("WM1234", "Bod 1234-56 Horní"));
		nacti();
		final String sWaymarkem = popis(zobrazene).toString();
		Assert.assertTrue(sWaymarkem, sWaymarkem.contains("Bod 1234-56 Horní"));
		vypnute.add(wm);
		nacti();
		over("waymark vypnutý");
		Assert.assertFalse("údaje z waymarku zmizí z bodu", popis(zobrazene).toString().contains("Bod 1234-56 Horní"));
		vypnute.clear();
		nacti();
		Assert.assertEquals(sWaymarkem, popis(zobrazene).toString());
		over("waymark zapnutý");
	}

	@Test
	public void databazeSPridavnymiWaypointyNeniSeVsimSpojena() throws Exception {
		final File db = geoget("a.db3", new String[] { "GC0001", "GC0002" }, new String[] { "GC0001:PK", "GC0002:FN" });
		final File jinde = gpxKesi("jinde.gpx", "jinde", "GC0100");
		final File prid = gpx("prid.gpx", "<wpt lat=\"50.3\" lon=\"14.6\"><name>FN0100</name><sym>Final Location</sym><type>Waypoint|Final Location</type></wpt>\n");
		nacti();
		vypnute.add(jinde);
		nacti();
		Assert.assertFalse("databáze se nečte, nepřekrývá se", nacitac.getPosledniPrectene().contains(db));
		vypnute.clear();
		nacti();
		Assert.assertEquals("jen zapnutý a jeho přídavný waypoint", new HashSet<>(Arrays.asList(jinde, prid)), nacitac.getPosledniPrectene());
		Assert.assertEquals("známé klíče: bez opakování", nacitac.getPosledniPrectene().size(), nacitac.getPosledniPocetCteni());
		over("přídavné waypointy");
	}

	@Test
	public void zmenenyZdrojSPrekryvemSeCteVeSpravnemPoradi() throws Exception {
		gpxKesi("a.gpx", "a", "GC0001");
		final File b = gpxKesi("b.gpx", "b", "GC0002");
		nacti();
		// b nově obsahuje i GC0001 a je novější: musí vyhrát, i když se o překryvu ví až po přečtení
		gpxKesi("b.gpx", "b2", "GC0001", "GC0002");
		Assert.assertEquals("b2", zdrojKese(nacti(), "GC0001"));
		Assert.assertTrue(nacitac.getPosledniPrectene().contains(b));
		over("změna s novým překryvem");
	}

	@Test
	public void casDatPrezijeRestartProgramu() throws Exception {
		final File stary = gpxKesi("a-stary.gpx", "stary", "GC0001");
		gpxKesi("b-novy.gpx", "novy", "GC0001");
		nacti();
		dotkniSe(stary);
		Assert.assertEquals("novy", zdrojKese(nacti(), "GC0001"));
		// nový program: prázdná paměť, jen uložené nastavení
		zobrazene = null;
		nacitac = new MultiNacitac(model(() -> zobrazene), new CasyDatZdroju());
		Assert.assertEquals("po restartu nevyhraje jen otevřený soubor", "novy", zdrojKese(nacti(), "GC0001"));
		final int zapisu = zapisuCasu;
		nacti();
		Assert.assertEquals("beze změny se nastavení nezapisuje", zapisu, zapisuCasu);
	}

	@Test
	public void bezUlozenehoCasuPlatiCasSouboruAZmizelyZdrojSeZapomene() throws Exception {
		final File stary = gpxKesi("a-stary.gpx", "stary", "GC0001");
		final File novy = gpxKesi("b-novy.gpx", "novy", "GC0001");
		nastaveniCasu = new LinkedHashSet<>(Arrays.asList("poškozený", "1;x;" + novy.getPath()));
		dotkniSe(stary);
		Assert.assertEquals("stary", zdrojKese(nacti(), "GC0001"));
		Assert.assertTrue(nastaveniCasu.toString(), nastaveniCasu.stream().anyMatch(r -> r.endsWith(novy.getPath())));
		Files.delete(novy.toPath());
		nacti();
		Assert.assertEquals("smazaný zdroj se z nastavení zapomene", 1, nastaveniCasu.size());
		Assert.assertTrue(nastaveniCasu.iterator().next().endsWith(stary.getPath()));
	}

	@Test(timeout = 60_000)
	public void prevzataKesSWaypointemBezSouradnic() throws Exception {
		gpx("kes.gpx", kes("GC0001", "kes"), "<wpt lat=\"0\" lon=\"0\"><name>PK0001</name><sym>Parking Area</sym></wpt>\n");
		final File jiny = gpxKesi("jiny.gpx", "jiny", "GC0002");
		nacti();
		for (int i = 0; i < 3; i++) {
			vypnute.add(jiny);
			nacti();
			vypnute.clear();
			nacti();
			over("převzetí " + i);
		}
	}

	@Test(timeout = 60_000)
	public void opakovaniBehuSkonciIKdyzSeCasyPoradPreji() throws Exception {
		gpxKesi("a.gpx", "a", "GC0001");
		gpxKesi("b.gpx", "b", "GC0001");
		final int[] volani = { 0 };
		// každé čtení dá jiný čas dat, jako by oba soubory jiný program pořád přepisoval
		nacitac = new MultiNacitac(model(() -> zobrazene), new CasyDatZdroju() {
			@Override
			synchronized long casPoPrecteni(final File zdroj, final long otiskObsahu, final long casZmeny) {
				return ++volani[0] * 1000L;
			}
		});
		Assert.assertNotNull(nacti());
		Assert.assertTrue("čteno " + nacitac.getPosledniPocetCteni(), nacitac.getPosledniPocetCteni() <= 4 * 2);
	}

	@Test(timeout = 60_000)
	public void poVycerpaniOpakovaniSeNaposledyCteVseBezPrevzeti() throws Exception {
		final File a = gpxKesi("a.gpx", "a", "GC0001");
		final File b = gpxKesi("b.gpx", "b", "GC0001");
		final File c = gpxKesi("c.gpx", "c", "GC0100");
		final boolean[] prepisuje = { false };
		final int[] volani = { 0 };
		nacitac = new MultiNacitac(model(() -> zobrazene), new CasyDatZdroju() {
			@Override
			synchronized long casPoPrecteni(final File zdroj, final long otiskObsahu, final long casZmeny) {
				return prepisuje[0] ? ++volani[0] * 1000L : super.casPoPrecteni(zdroj, otiskObsahu, casZmeny);
			}
		});
		nacti();
		prepisuje[0] = true;
		dotkniSe(a);
		dotkniSe(b);
		nacti();
		Assert.assertTrue("poslední pokus čte i jinak převzatou skupinu", nacitac.getPosledniPrectene().contains(c));
		Assert.assertEquals(new HashSet<>(Arrays.asList(a, b, c)), nacitac.getPosledniPrectene());
		Assert.assertEquals(2, zobrazene.getKesoidy().size());
	}

	@Test
	public void zapnutiDatabazeVypnuteOdStartuSeNeopakuje() throws Exception {
		final File a = geoget("a.db3", new String[] { "GC0001", "GC0002" }, new String[] { "GC0001:PK" });
		final File b = geoget("b.db3", new String[] { "GC0001", "GC0003" }, new String[0]);
		gpxKesi("c.gpx", "c", "GC0100");
		vypnute.add(b);
		nacti();
		vypnute.clear();
		nacti();
		Assert.assertEquals(new HashSet<>(Arrays.asList(a, b)), nacitac.getPosledniPrectene());
		Assert.assertEquals("překryv poznán z kódů předem, bez opakování", 2, nacitac.getPosledniPocetCteni());
		Assert.assertEquals(1, nacitac.getPosledniRozpusteni().size());
		final String zaznam = nacitac.getPosledniRozpusteni().get(0);
		Assert.assertTrue(zaznam, zaznam.contains("GeoGet 1 klíčů") && zaznam.contains("zjištěno předem") && !zaznam.contains("a.db3"));
		over("zapnutí od startu vypnuté databáze");
	}

	// ---------- náhodné posloupnosti ----------

	@Test
	public void nahodnePosloupnosti() throws Exception {
		final long zaklad = Long.getLong("shoda.seed", System.nanoTime());
		for (int i = 0; i < 6; i++) {
			posloupnost(zaklad + i, 25);
			tearDownSlozky();
		}
	}

	private void tearDownSlozky() throws Exception {
		for (final File f : Objects.requireNonNull(gpx.listFiles())) {
			Files.delete(f.toPath());
		}
		for (final File f : Objects.requireNonNull(geoget.listFiles())) {
			Files.delete(f.toPath());
		}
		vypnute.clear();
		zobrazene = null;
		nacitac = new MultiNacitac(model(() -> zobrazene), casy);
	}

	private void posloupnost(final long seed, final int kroku) throws Exception {
		final Random r = new Random(seed);
		final StringBuilder denik = new StringBuilder("seed=" + seed + " (-Dshoda.seed)");
		final List<File> zdroje = new ArrayList<>();
		zdroje.add(geoget("a.db3", new String[] { "GC0001", "GC0002", "GC0003", "GC0010" }, new String[] { "GC0001:PK", "GC0003:FN" }));
		zdroje.add(geoget("b.db3", new String[] { "GC0003", "GC0004", "GC0011" }, new String[] { "GC0004:FN" }));
		zdroje.add(gpxKesi("pq1.gpx", "pq1", "GC0001", "GC0020", "GC0021"));
		zdroje.add(gpxKesi("pq2.gpx", "pq2", "GC0021", "GC0022"));
		zdroje.add(gpx("pq1-wpts.gpx", "<wpt lat=\"50.2\" lon=\"14.5\"><name>FN0020</name><sym>Final Location</sym><type>Waypoint|Final Location</type></wpt>\n",
				"<wpt lat=\"50.21\" lon=\"14.5\"><name>PK0002</name><sym>Parking Area</sym></wpt>\n", "<wpt lat=\"50.22\" lon=\"14.5\"><name>FN0099</name><sym>Final Location</sym></wpt>\n"));
		zdroje.add(gpx("cgp.gpx", cgp("GC12AB34", "1234-56.1", "Traditional Cache"), cgp("GC12AB35", "1234-56.2", "Letterbox Hybrid")));
		zdroje.add(gpx("wm.gpx", waymark("WM1234", "Bod 1234-56 Horní")));
		zdroje.add(gpx("cgp2.gpx", cgp("GC12AB36", "1234-56.3", "Traditional Cache")));
		zdroje.add(gpx("bezejmenne.gpx", "<wpt lat=\"50.3\" lon=\"14.3\"><sym>Waypoint</sym></wpt>\n", "<wpt lat=\"50.31\" lon=\"14.3\"><sym>Waypoint</sym></wpt>\n"));
		zdroje.add(gpxKesi("samotny.gpx", "samotny", "GC0100"));
		zdroje.add(gpx("prazdny.gpx"));
		nacti();
		over(denik + " start");
		final List<String> poradiStavu = poradiStavu();
		for (int k = 0; k < kroku; k++) {
			final File f = zdroje.get(r.nextInt(zdroje.size()));
			final int op = r.nextInt(10);
			if (op < 4) {
				if (!vypnute.remove(f)) {
					vypnute.add(f);
				}
				denik.append(", přepni ").append(f.getName());
			} else if (op == 4) {
				dotkniSe(f);
				denik.append(", dotyk ").append(f.getName());
			} else if (op == 5 && f.getName().endsWith(".gpx") && f.getName().startsWith("pq")) {
				gpxKesi(f.getName(), f.getName() + k, "GC0001", "GC002" + r.nextInt(3), "GC0003");
				denik.append(", změň ").append(f.getName());
			} else if (op == 6) {
				final File g = zdroje.get(r.nextInt(zdroje.size()));
				g.setLastModified(f.lastModified());
				denik.append(", stejný čas ").append(g.getName()).append(" jako ").append(f.getName());
			} else if (op == 7) {
				vypnute.clear();
				denik.append(", vše zapnout");
			} else if (op == 8) {
				final int po = 1 + r.nextInt(6);
				nacitac.setRootDirs(true, gpx, geoget, null, Collections.<File> emptySet());
				Assert.assertNull(nacitac.nacti(zrusPo(po), genom));
				denik.append(", zrušeno po ").append(po);
			} else {
				denik.append(", nic");
			}
			nacti();
			over(denik.toString());
			Assert.assertEquals(denik.toString(), poradiStavu, poradiStavu());
		}
	}

	private List<String> poradiStavu() {
		final List<String> vysledek = new ArrayList<>();
		for (final StavPolozky p : nacitac.getRegistr().getSnimek().getPolozky()) {
			vysledek.add(p.getCesta());
		}
		return vysledek;
	}

	// ---------- porovnání ----------

	/** Porovná poslední výsledek s plným načtením stejných zdrojů novým načítačem. */
	private void over(final String kde) throws Exception {
		final KesBag[] plne = new KesBag[1];
		final MultiNacitac cisty = new MultiNacitac(model(() -> plne[0]), casy);
		cisty.setRootDirs(true, gpx, geoget, null, Collections.<File> emptySet());
		plne[0] = cisty.nacti(null, new Genom());
		Assert.assertEquals(kde, popis(plne[0]), popis(zobrazene));
	}

	private static SortedMap<String, String> popis(final KesBag bag) {
		final SortedMap<String, String> vysledek = new TreeMap<>();
		for (final Kesoid k : bag.getKesoidy()) {
			final List<String> wpty = new ArrayList<>();
			for (final Wpt w : k.getWpts()) {
				wpty.add(w.getName() + "|" + w.getNazev() + "|" + w.getWgs() + "|" + w.getSym());
			}
			Collections.sort(wpty);
			final String klic = k.getClass().getSimpleName() + ":" + k.getIdentifier();
			final String hodnota = "hlavní=" + k.getMainWpt().getName() + " název=" + k.getNazev() + " url=" + k.getUrl() + " autor=" + k.getAuthor() + " vztah=" + k.getVztah() + " "
					+ wpty;
			vysledek.merge(klic, hodnota, (a, b) -> a + " || " + b);
		}
		vysledek.put("~waypointů v bagu", String.valueOf(bag.getWpts().size()));
		for (final InformaceOZdroji info : bag.getInformaceOZdrojich().getSetInformaciOZdrojich()) {
			if (info.getChildren().isEmpty()) {
				vysledek.put("~zdroj " + info.jmenoZdroje.getFile().getName(), info.nacteno + " " + info.pocetWaypointuCelkem + "/" + info.pocetWaypointuBranych);
			}
		}
		return vysledek;
	}

	private static String zdrojKese(final KesBag bag, final String kod) {
		for (final Kesoid k : bag.getKesoidy()) {
			if (k.getIdentifier().equals(kod)) {
				return k.getAuthor();
			}
		}
		return null;
	}

	// ---------- data ----------

	private KesBag nacti() throws Exception {
		nacitac.setRootDirs(true, gpx, geoget, null, Collections.<File> emptySet());
		final KesBag bag = nacitac.nacti(null, genom);
		if (bag != null) {
			zobrazene = bag;
		}
		return zobrazene;
	}

	private void dotkniSe(final File f) {
		hodiny += 10_000;
		Assert.assertTrue(f.setLastModified(hodiny));
	}

	/** Autor keše nese jméno zdroje, aby bylo poznat, čí kopie vyhrála. */
	private static String kes(final String kod, final String zdroj) {
		return ImportKesiTest.kes(kod, "Geocache", "Traditional Cache", zdroj, 1, true, false, "2", "");
	}

	private File gpxKesi(final String jmeno, final String zdroj, final String... kody) throws Exception {
		final String[] wpts = new String[kody.length];
		for (int i = 0; i < kody.length; i++) {
			wpts[i] = kes(kody[i], zdroj);
		}
		return gpx(jmeno, wpts);
	}

	private File gpx(final String jmeno, final String... wpts) throws Exception {
		final File f = new File(gpx, jmeno);
		Files.write(f.toPath(), ImportKesiTest.gpx(wpts).getBytes(StandardCharsets.UTF_8));
		dotkniSe(f);
		return f;
	}

	private static String cgp(final String kod, final String bod, final String typ) {
		return "<wpt lat=\"49.5\" lon=\"15.5\"><name>" + kod + "</name><sym>Geocache</sym><type>Geocache|" + typ + "</type>"
				+ "<groundspeak:cache available=\"True\" archived=\"False\" xmlns:groundspeak=\"http://www.groundspeak.com/cache/1/0/1\"><groundspeak:name>" + bod
				+ "</groundspeak:name><groundspeak:placed_by>DATAZ</groundspeak:placed_by><groundspeak:owner id=\"7\">DATAZ</groundspeak:owner><groundspeak:type>" + typ
				+ "</groundspeak:type><groundspeak:encoded_hints>Bod " + bod + "</groundspeak:encoded_hints></groundspeak:cache></wpt>\n";
	}

	private static String waymark(final String kod, final String nazev) {
		return "<wpt lat=\"49.5\" lon=\"15.5\"><name>" + kod + "</name><sym>Waymark</sym><type>Czech Geodetic Points</type><url>http://waymarking.com/" + kod + "</url><urlname>" + nazev
				+ "</urlname></wpt>\n";
	}

	/** Databáze GeoGetu; přídavný waypoint jako „kód:prefix“. */
	private File geoget(final String jmeno, final String[] kody, final String[] waypointy) throws SQLException {
		final File db = new File(geoget, jmeno);
		try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db); Statement s = c.createStatement()) {
			s.execute("CREATE TABLE geocache (id TEXT, x REAL, y REAL, name TEXT, author TEXT, cachetype TEXT, cachesize TEXT, difficulty TEXT, terrain TEXT,"
					+ " cachestatus INTEGER, gs_ownerid INTEGER, dthidden INTEGER, country TEXT, state TEXT, dtfound INTEGER)");
			s.execute("CREATE TABLE geolist (id TEXT, shortdesc BLOB, hint TEXT)");
			s.execute("CREATE TABLE waypoint (id TEXT, x REAL, y REAL, prefixid TEXT, wpttype TEXT, name TEXT)");
			s.execute("CREATE TABLE geotag (id TEXT, ptrkat INTEGER, ptrvalue INTEGER)");
			s.execute("CREATE TABLE geotagcategory (key INTEGER, value TEXT)");
			s.execute("CREATE TABLE geotagvalue (key INTEGER, value TEXT)");
			for (final String kod : kody) {
				s.execute("INSERT INTO geocache VALUES ('" + kod + "', 50.1, 14.4, 'Keš', '" + jmeno + "', 'Traditional Cache', 'Regular', '2', '3', 0, 1, 20200101, 'CZ', 'Praha', 0)");
			}
			for (final String w : waypointy) {
				final String[] kp = w.split(":");
				s.execute("INSERT INTO waypoint VALUES ('" + kp[0] + "', 50.15, 14.45, '" + kp[1] + "', 'Parking Area', 'Bod')");
			}
		}
		dotkniSe(db);
		return db;
	}

	/** Zrušení po daném počtu dotazů na zrušení, tedy uprostřed běhu. */
	private static Future<Object> zrusPo(final int dotazu) {
		return new Future<Object>() {
			private int n;

			@Override
			public boolean cancel(final boolean b) {
				return false;
			}

			@Override
			public boolean isCancelled() {
				return ++n > dotazu;
			}

			@Override
			public boolean isDone() {
				return false;
			}

			@Override
			public Object get() {
				return null;
			}

			@Override
			public Object get(final long t, final TimeUnit u) {
				return null;
			}
		};
	}

	private KesoidModel model(final java.util.function.Supplier<KesBag> zobrazeno) {
		final ProgressModel progress = new ProgressModel();
		progress.inject(udalost -> {});
		final KesoidModel model = new KesoidModel() {
			@Override
			public GccomNick getGccomNick() {
				return nick;
			}

			@Override
			public KesBag getVsechnyKesoidy() {
				return zobrazeno.get();
			}

			@Override
			public void fire(final Event0<?> udalost) {}

			@Override
			public void setNacitaneZdroje(final InformaceOZdrojich zdroje) {}

			@Override
			public void zaradGsakDatabaze(final Set<File> databaze, final Set<File> nedostupne) {}

			@Override
			public void zaradOpensakDatabaze(final Set<File> databaze, final Set<File> nedostupne) {}
			@Override
			public void zaradGeogetDatabaze(final Set<File> databaze, final Set<File> nedostupne) {}

			@Override
			public boolean maSeNacist(final File zdroj) {
				return !vypnute.contains(zdroj);
			}

			@Override
			public boolean maSeNacist(final KeFile zdroj) {
				return maSeNacist(zdroj.getFile());
			}

			@Override
			public synchronized Set<String> getCasyDatZdroju() {
				return nastaveniCasu;
			}

			@Override
			public synchronized void setCasyDatZdroju(final Set<String> zaznamy) {
				nastaveniCasu = new LinkedHashSet<>(zaznamy);
				zapisuCasu++;
			}
		};
		model.inject(progress);
		model.inject(new KesoidPluginManager());
		return model;
	}
}
