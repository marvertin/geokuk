package cz.geokuk.plugins.kesoid.importek;

import java.io.File;
import java.lang.ref.WeakReference;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

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

/** Přepnutí zdroje se projeví, i když jiný program drží zamčenou jinou databázi, a vypnutý zdroj neblokuje paměť. */
public class VypnutiPriZamceneDatabaziTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final ExecutorService geoget = Executors.newCachedThreadPool();
	private final List<CountDownLatch> zamky = new ArrayList<>();
	private final Set<File> vypnute = new HashSet<>();
	private final Genom genom = new Genom();
	private File gpx;
	private File slozkaGeogetu;
	private MultiNacitac nacitac;
	private volatile KesBag zobrazene;

	@Before
	public void setUp() throws Exception {
		gpx = tmp.newFolder("gpx");
		slozkaGeogetu = tmp.newFolder("geoget");
		nacitac = new MultiNacitac(model());
	}

	@After
	public void uklid() {
		zamky.forEach(CountDownLatch::countDown);
		geoget.shutdown();
	}

	@Test(timeout = 60_000)
	public void vypnutiAZapnutiSeProjeviIPriZamceneDatabazi() throws Exception {
		final File zamcena = zalozGeoget("zamcena.db3", "GC000Z");
		final File velka = zalozGeoget("velka.db3", "GC000A", "GC000B");
		nacti();
		Assert.assertEquals(set("GC000Z", "GC000A", "GC000B"), kody(zobrazene));
		zamkni(zamcena);
		nacti();
		Assert.assertEquals("zamčená zůstane z minula", set("GC000Z", "GC000A", "GC000B"), kody(zobrazene));

		vypnute.add(velka);
		Assert.assertNotNull("vypnutí se nesmí zahodit jen proto, že jiná databáze je zamčená", nacti());
		Assert.assertEquals(set("GC000Z"), kody(zobrazene));
		vypnute.clear();
		Assert.assertNotNull(nacti());
		Assert.assertEquals(set("GC000Z", "GC000A", "GC000B"), kody(zobrazene));
		Assert.assertNull("beze změny se při zamčené databázi nic nenačítá", nacti());
	}

	@Test(timeout = 60_000)
	public void vypnutyZdrojUvolniPametHnedPoDokoncenemBehu() throws Exception {
		zalozGeoget("mala.db3", "GC000M");
		final File velka = zalozGeoget("velka.db3", "GC000A", "GC000B");
		gpxSKesi("vylet.gpx", "GC0100");
		nacti();
		final List<WeakReference<Object>> zVelke = new ArrayList<>();
		for (final Kesoid k : zobrazene.getKesoidy()) {
			if (k.getIdentifier().startsWith("GC000A") || k.getIdentifier().startsWith("GC000B")) {
				zVelke.add(new WeakReference<>(k));
				for (final Wpt w : k.getWpts()) {
					zVelke.add(new WeakReference<>(w));
				}
			}
		}
		Assert.assertEquals(4, zVelke.size());
		final WeakReference<KesBag> staryBag = new WeakReference<>(zobrazene);
		vypnute.add(velka);
		Assert.assertNotNull(nacti());
		for (int i = 0; i < 20 && (staryBag.get() != null || zVelke.stream().anyMatch(r -> r.get() != null)); i++) {
			System.gc();
			Thread.sleep(50);
		}
		Assert.assertNull("starý bag", staryBag.get());
		for (final WeakReference<Object> r : zVelke) {
			Assert.assertNull("kešoid nebo waypoint vypnutého zdroje drží načítání: " + r.get(), r.get());
		}
	}

	private KesBag nacti() throws Exception {
		nacitac.setRootDirs(true, gpx, slozkaGeogetu, null, Collections.<File> emptySet());
		final KesBag bag = nacitac.nacti(null, genom);
		if (bag != null) {
			zobrazene = bag;
		}
		return bag;
	}

	private static Set<String> set(final String... kody) {
		return new HashSet<>(Arrays.asList(kody));
	}

	private static Set<String> kody(final KesBag bag) {
		final Set<String> kody = new HashSet<>();
		for (final Kesoid k : bag.getKesoidy()) {
			kody.add(k.getIdentifier());
		}
		return kody;
	}

	private void gpxSKesi(final String jmeno, final String kod) throws Exception {
		java.nio.file.Files.write(new File(gpx, jmeno).toPath(),
				ImportKesiTest.gpx(ImportKesiTest.kes(kod, "Geocache", "Traditional Cache", "Cizí", 1, true, false, "2", "")).getBytes(java.nio.charset.StandardCharsets.UTF_8));
	}

	private void zamkni(final File db) throws Exception {
		final CountDownLatch zamceno = new CountDownLatch(1);
		final CountDownLatch pustit = new CountDownLatch(1);
		zamky.add(pustit);
		geoget.submit(() -> {
			try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db); Statement s = c.createStatement()) {
				s.execute("BEGIN EXCLUSIVE");
				zamceno.countDown();
				pustit.await();
				s.execute("COMMIT");
			}
			return null;
		});
		zamceno.await();
	}

	private File zalozGeoget(final String jmeno, final String... kody) throws SQLException {
		final File db = new File(slozkaGeogetu, jmeno);
		try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db); Statement s = c.createStatement()) {
			s.execute("CREATE TABLE geocache (id TEXT, x REAL, y REAL, name TEXT, author TEXT, cachetype TEXT, cachesize TEXT, difficulty TEXT, terrain TEXT,"
					+ " cachestatus INTEGER, gs_ownerid INTEGER, dthidden INTEGER, country TEXT, state TEXT, dtfound INTEGER)");
			s.execute("CREATE TABLE geolist (id TEXT, shortdesc BLOB, hint TEXT)");
			s.execute("CREATE TABLE waypoint (id TEXT, x REAL, y REAL, prefixid TEXT, wpttype TEXT, name TEXT)");
			s.execute("CREATE TABLE geotag (id TEXT, ptrkat INTEGER, ptrvalue INTEGER)");
			s.execute("CREATE TABLE geotagcategory (key INTEGER, value TEXT)");
			s.execute("CREATE TABLE geotagvalue (key INTEGER, value TEXT)");
			for (final String kod : kody) {
				s.execute("INSERT INTO geocache VALUES ('" + kod + "', 50.1, 14.4, 'Keš', 'autor', 'Traditional Cache', 'Regular', '2', '3', 0, 1, 20200101, 'CZ', 'Praha', 0)");
			}
		}
		return db;
	}

	private KesoidModel model() {
		final ProgressModel progress = new ProgressModel();
		progress.inject(udalost -> {});
		final KesoidModel model = new KesoidModel() {
			@Override
			public GccomNick getGccomNick() {
				return new GccomNick("Ja", 42);
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
			public boolean maSeNacist(final KeFile zdroj) {
				return maSeNacist(zdroj.getFile());
			}
		};
		model.inject(progress);
		model.inject(new KesoidPluginManager());
		return model;
	}
}
