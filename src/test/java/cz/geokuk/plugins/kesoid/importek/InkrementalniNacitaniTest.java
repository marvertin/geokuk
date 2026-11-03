package cz.geokuk.plugins.kesoid.importek;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;

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

/** Přepnutí zdroje čte jen to, co se změnilo, a výsledek je stejný jako po plném načtení. */
public class InkrementalniNacitaniTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final Set<File> vypnute = new HashSet<>();
	private final Genom genom = new Genom();
	private volatile GccomNick nick = new GccomNick("Ja", 42);
	private File gpx;
	private File slozkaGeogetu;
	private MultiNacitac nacitac;

	@Before
	public void setUp() throws Exception {
		gpx = tmp.newFolder("gpx");
		slozkaGeogetu = tmp.newFolder("geoget");
		nacitac = new MultiNacitac(model());
	}

	private File db(final String jmeno) {
		return new File(slozkaGeogetu, jmeno);
	}

	private File g(final String jmeno) {
		return new File(gpx, jmeno);
	}

	private Set<File> soubory(final File... soubory) {
		return new HashSet<>(Arrays.asList(soubory));
	}

	@Test
	public void vypnutiAZapnutiNepreklyvajicihoSeZdrojeNecteCeleZnovu() throws Exception {
		zalozGeoget("a.db3", "GC000A");
		zalozGeoget("b.db3", "GC000B");
		zapisGpx("a.gpx", "GC1111");
		nacti();
		Assert.assertEquals(soubory(db("a.db3"), db("b.db3"), g("a.gpx")), nacitac.getPosledniPrectene());

		vypnute.add(db("b.db3"));
		Assert.assertEquals(set("GC000A", "GC1111"), kody(nacti()));
		Assert.assertEquals("nic se nečte", soubory(), nacitac.getPosledniPrectene());

		vypnute.clear();
		Assert.assertEquals(set("GC000A", "GC000B", "GC1111"), kody(nacti()));
		Assert.assertEquals("čte se jen zapnutý", soubory(db("b.db3")), nacitac.getPosledniPrectene());
	}

	@Test
	public void zmenenySouborSePreteSam() throws Exception {
		final File a = zalozGeoget("a.db3", "GC000A");
		zalozGeoget("b.db3", "GC000B");
		zapisGpx("a.gpx", "GC1111");
		nacti();
		pridejKes(a, "GC000C");
		Assert.assertEquals(set("GC000A", "GC000B", "GC000C", "GC1111"), kody(nacti()));
		Assert.assertEquals(soubory(a), nacitac.getPosledniPrectene());
	}

	@Test
	public void gpxSeChovaJakoOstatniZdroje() throws Exception {
		zalozGeoget("a.db3", "GC000A");
		zapisGpx("a.gpx", "GC1111");
		zapisGpx("b.gpx", "GC2222");
		nacti();
		vypnute.add(g("b.gpx"));
		Assert.assertEquals(set("GC000A", "GC1111"), kody(nacti()));
		Assert.assertEquals(soubory(), nacitac.getPosledniPrectene());
		vypnute.clear();
		Assert.assertEquals(set("GC000A", "GC1111", "GC2222"), kody(nacti()));
		Assert.assertEquals(soubory(g("b.gpx")), nacitac.getPosledniPrectene());
	}

	@Test
	public void prekryvajiciSeZdrojeSeCtouSkupinove() throws Exception {
		zalozGeoget("a.db3", "GC0001", "GC0002");
		zalozGeoget("b.db3", "GC0002", "GC0003");
		zalozGeoget("c.db3", "GC0009");
		final KesBag plne = nacti();
		Assert.assertEquals(set("GC0001", "GC0002", "GC0003", "GC0009"), kody(plne));
		Assert.assertEquals(5, celkem(plne));
		Assert.assertEquals(4, brano(plne));

		vypnute.add(db("b.db3"));
		final KesBag bezB = nacti();
		Assert.assertEquals(set("GC0001", "GC0002", "GC0009"), kody(bezB));
		Assert.assertEquals("skupina a+b se rozpadla, c je nezávislý", soubory(db("a.db3")), nacitac.getPosledniPrectene());
		Assert.assertEquals(3, brano(bezB));

		vypnute.clear();
		final KesBag znovu = nacti();
		Assert.assertEquals(set("GC0001", "GC0002", "GC0003", "GC0009"), kody(znovu));
		Assert.assertEquals("nově zapnutý b se překrývá s a, čtou se spolu", soubory(db("a.db3"), db("b.db3")), nacitac.getPosledniPrectene());
		Assert.assertEquals(5, celkem(znovu));
		Assert.assertEquals(4, brano(znovu));
	}

	@Test
	public void priddavnyWaypointVNovemZdrojiSeSparujeSKesiZCache() throws Exception {
		zalozGeoget("a.db3", "GC000A");
		zalozGeoget("b.db3", "GC000B");
		Files.write(g("w.gpx").toPath(), ImportKesiTest.gpx("<wpt lat=\"50.2\" lon=\"14.5\"><name>PK000A</name><sym>Parking Area</sym></wpt>\n").getBytes(StandardCharsets.UTF_8));
		vypnute.add(g("w.gpx"));
		nacti();
		Assert.assertEquals("keš má jen hlavní waypoint", 1, waypointuKese(nacti0(), "GC000A"));

		vypnute.clear();
		final KesBag bag = nacti();
		Assert.assertEquals("přídavný waypoint se připojil ke keši z databáze", 2, waypointuKese(bag, "GC000A"));
		Assert.assertEquals("a se musí přečíst znovu, jinak by waypoint zůstal sirotek", soubory(db("a.db3"), db("b.db3"), g("w.gpx")).containsAll(nacitac.getPosledniPrectene()), true);
		Assert.assertTrue(nacitac.getPosledniPrectene().contains(db("a.db3")));
		Assert.assertEquals(set("GC000A", "GC000B"), kody(bag));
	}

	@Test
	public void zmenaNickuPrecteVsechno() throws Exception {
		zalozGeoget("a.db3", "GC000A");
		zapisGpx("a.gpx", "GC1111");
		nacti();
		nick = new GccomNick("Jiny", 43);
		nacti();
		Assert.assertEquals("změna nicku mění vztah k keším, čte se všechno", soubory(db("a.db3"), g("a.gpx")), nacitac.getPosledniPrectene());
	}

	@Test
	public void jinyGenomPrecteVsechno() throws Exception {
		zalozGeoget("a.db3", "GC000A");
		nacti();
		start();
		Assert.assertNotNull(nacitac.nacti(null, new Genom()));
		Assert.assertEquals(soubory(db("a.db3")), nacitac.getPosledniPrectene());
	}

	/** Po každém přepnutí je výsledek stejný jako při plném načtení stejných zdrojů. */
	@Test
	public void vysledekJeStejnyJakoPlneNacteni() throws Exception {
		zalozGeoget("a.db3", "GC0001", "GC0002", "GC0003");
		zalozGeoget("b.db3", "GC0003", "GC0004");
		zalozGeoget("c.db3", "GC0010", "GC0011");
		zapisGpx("p.gpx", "GC0002", "GC0020");
		Files.write(g("w.gpx").toPath(), ImportKesiTest.gpx("<wpt lat=\"50.2\" lon=\"14.5\"><name>PK0010</name><sym>Parking Area</sym></wpt>\n",
				"<wpt lat=\"50.3\" lon=\"14.6\"><name>PK0020</name><sym>Parking Area</sym></wpt>\n").getBytes(StandardCharsets.UTF_8));
		final List<List<File>> kroky = Arrays.asList(
				Arrays.asList(), Arrays.asList(db("b.db3")), Arrays.asList(db("b.db3"), g("w.gpx")), Arrays.asList(g("w.gpx")), Arrays.asList(db("a.db3"), g("p.gpx")),
				Arrays.asList(db("c.db3")), Arrays.asList(), Arrays.asList(db("a.db3"), db("b.db3"), db("c.db3"), g("p.gpx"), g("w.gpx")), Arrays.asList());
		for (final List<File> krok : kroky) {
			vypnute.clear();
			vypnute.addAll(krok);
			final KesBag postupne = nacti();
			final KesBag plne = plneNacteni();
			Assert.assertEquals("vypnuto " + krok, popis(plne), popis(postupne));
			Assert.assertEquals("vypnuto " + krok, brano(plne), brano(postupne));
			Assert.assertEquals("vypnuto " + krok, celkem(plne), celkem(postupne));
		}
	}

	private KesBag plneNacteni() throws Exception {
		final MultiNacitac cisty = new MultiNacitac(model());
		cisty.setRootDirs(true, gpx, slozkaGeogetu, null, Collections.emptySet());
		return cisty.nacti(null, genom);
	}

	/** Kešoidy s jmény jejich waypointů; nezávislé na tom, který duplicitní zdroj vyhrál. */
	private static SortedMap<String, SortedSet<String>> popis(final KesBag bag) {
		final SortedMap<String, SortedSet<String>> vysledek = new TreeMap<>();
		for (final Kesoid k : bag.getKesoidy()) {
			final SortedSet<String> jmena = new TreeSet<>();
			for (final Wpt w : k.getWpts()) {
				jmena.add(w.getName());
			}
			vysledek.put(k.getIdentifier(), jmena);
		}
		return vysledek;
	}

	private static int waypointuKese(final KesBag bag, final String kod) {
		for (final Kesoid k : bag.getKesoidy()) {
			if (kod.equals(k.getIdentifier())) {
				return k.getWptsCount();
			}
		}
		return 0;
	}

	private static int celkem(final KesBag bag) {
		return bag.getInformaceOZdrojich().getSetInformaciOZdrojich().stream().mapToInt(i -> i.pocetWaypointuCelkem).sum();
	}

	private static int brano(final KesBag bag) {
		return bag.getInformaceOZdrojich().getSetInformaciOZdrojich().stream().mapToInt(i -> i.pocetWaypointuBranych).sum();
	}

	private KesBag zobrazene;

	private KesBag nacti0() {
		return zobrazene;
	}

	private KesBag nacti() throws Exception {
		start();
		zobrazene = nacitac.nacti(null, genom);
		return zobrazene;
	}

	private void start() {
		nacitac.setRootDirs(true, gpx, slozkaGeogetu, null, Collections.emptySet());
	}

	private void pridejKes(final File db, final String kod) throws Exception {
		Thread.sleep(50);
		try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db); Statement s = c.createStatement()) {
			s.execute(insertKese(kod));
		}
		db.setLastModified(System.currentTimeMillis() + 5000);
	}

	private void zapisGpx(final String jmeno, final String... kody) throws Exception {
		final String[] wpts = new String[kody.length];
		for (int i = 0; i < kody.length; i++) {
			wpts[i] = ImportKesiTest.kes(kody[i], "Geocache", "Traditional Cache", "Cizí", 1, true, false, "2", "");
		}
		Files.write(g(jmeno).toPath(), ImportKesiTest.gpx(wpts).getBytes(StandardCharsets.UTF_8));
	}

	private File zalozGeoget(final String jmeno, final String... kody) throws SQLException {
		final File db = db(jmeno);
		try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db); Statement s = c.createStatement()) {
			s.execute("CREATE TABLE geocache (id TEXT, x REAL, y REAL, name TEXT, author TEXT, cachetype TEXT, cachesize TEXT, difficulty TEXT, terrain TEXT,"
					+ " cachestatus INTEGER, gs_ownerid INTEGER, dthidden INTEGER, country TEXT, state TEXT, dtfound INTEGER)");
			s.execute("CREATE TABLE geolist (id TEXT, shortdesc BLOB, hint TEXT)");
			s.execute("CREATE TABLE waypoint (id TEXT, x REAL, y REAL, prefixid TEXT, wpttype TEXT, name TEXT)");
			s.execute("CREATE TABLE geotag (id TEXT, ptrkat INTEGER, ptrvalue INTEGER)");
			s.execute("CREATE TABLE geotagcategory (key INTEGER, value TEXT)");
			s.execute("CREATE TABLE geotagvalue (key INTEGER, value TEXT)");
			for (final String kod : kody) {
				s.execute(insertKese(kod));
			}
		}
		return db;
	}

	private static String insertKese(final String kod) {
		return "INSERT INTO geocache VALUES ('" + kod + "', 50.1, 14.4, 'Keš', 'autor', 'Traditional Cache', 'Regular', '2', '3', 0, 1, 20200101, 'CZ', 'Praha', 0)";
	}

	private static Set<String> set(final String... kody) {
		return new HashSet<>(Arrays.asList(kody));
	}

	private static Set<String> kody(final KesBag bag) {
		Assert.assertNotNull("má se načíst", bag);
		final Set<String> kody = new HashSet<>();
		for (final Kesoid k : bag.getKesoidy()) {
			kody.add(k.getIdentifier());
		}
		return kody;
	}

	private KesoidModel model() {
		final ProgressModel progress = new ProgressModel();
		progress.inject(udalost -> {});
		final KesoidModel model = new KesoidModel() {
			@Override
			public GccomNick getGccomNick() {
				return nick;
			}

			@Override
			public KesBag getVsechnyKesoidy() {
				return zobrazene;
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
			public boolean maSeNacist(final cz.geokuk.util.file.KeFile zdroj) {
				return maSeNacist(zdroj.getFile());
			}
		};
		model.inject(progress);
		model.inject(new KesoidPluginManager());
		return model;
	}
}
