package cz.geokuk.plugins.kesoid.importek;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import cz.geokuk.framework.ProgressModel;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.kind.KesoidPluginManager;
import cz.geokuk.plugins.kesoid.mvc.GccomNick;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;

/** Databáze GeoGetu, do které GeoGet dlouho zapisuje, nezdrží načtení ostatních zdrojů. */
public class ZamcenaDatabazeNezdrziTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final CountDownLatch pustit = new CountDownLatch(1);
	private final ExecutorService geoget = Executors.newSingleThreadExecutor();

	@After
	public void uklid() {
		pustit.countDown();
		geoget.shutdown();
	}

	@Test
	public void ostatniZdrojeSeNactouHned() throws Exception {
		final File gpx = tmp.newFolder("gpx");
		Files.write(new File(gpx, "a.gpx").toPath(), ImportKesiTest.gpx(ImportKesiTest.kes("GC1111", "Geocache", "Traditional Cache", "Cizí", 1, true, false, "2", "")).getBytes(StandardCharsets.UTF_8));
		final File slozkaGeogetu = tmp.newFolder("geoget");
		final File db = new File(slozkaGeogetu, "geoget.db3");
		zalozGeoget(db);
		final CountDownLatch zamceno = new CountDownLatch(1);
		final Future<?> zapis = geoget.submit(() -> {
			try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db); Statement s = c.createStatement()) {
				s.execute("BEGIN EXCLUSIVE");
				s.execute("INSERT INTO geocache VALUES ('GC00002', 50.2, 14.5, 'Druhá', 'autor', 'Traditional Cache', 'Regular', '2', '3', 0, 1, 20200101, 'CZ', 'Praha', 0)");
				zamceno.countDown();
				pustit.await();
				s.execute("COMMIT");
			}
			return null;
		});
		zamceno.await();

		final MultiNacitac nacitac = new MultiNacitac(model());
		nacitac.setRootDirs(true, gpx, slozkaGeogetu, null, Collections.emptySet());
		final long start = System.currentTimeMillis();
		final KesBag bag = nacitac.nacti(null, new Genom());
		Assert.assertTrue("čekalo se na zamčenou databázi", System.currentTimeMillis() - start < 10_000);
		Assert.assertEquals(Collections.singleton("GC1111"), kody(bag));
		Assert.assertTrue(nacitac.jeZamcena(db));

		Assert.assertNull("dokud je zamčená, nic se znovu nenačítá", nacitac.nacti(null, new Genom()));

		pustit.countDown();
		zapis.get();
		final KesBag poZapisu = nacitac.nacti(null, new Genom());
		Assert.assertEquals(new HashSet<>(Arrays.asList("GC1111", "GC00001", "GC00002")), kody(poZapisu));
		Assert.assertFalse(nacitac.jeZamcena(db));
	}

	@Test
	public void priZamkuZustanouZobrazeneKese() throws Exception {
		final File gpx = tmp.newFolder("gpx");
		Files.write(new File(gpx, "a.gpx").toPath(), ImportKesiTest.gpx(ImportKesiTest.kes("GC1111", "Geocache", "Traditional Cache", "Cizí", 1, true, false, "2", "")).getBytes(StandardCharsets.UTF_8));
		final File slozkaGeogetu = tmp.newFolder("geoget");
		final File db = new File(slozkaGeogetu, "geoget.db3");
		zalozGeoget(db);
		final KesBag[] zobrazene = new KesBag[1];
		final KesoidModel model = model(() -> zobrazene[0]);
		final MultiNacitac nacitac = new MultiNacitac(model);
		nacitac.setRootDirs(true, gpx, slozkaGeogetu, null, Collections.emptySet());
		zobrazene[0] = nacitac.nacti(null, new Genom());
		Assert.assertEquals(new HashSet<>(Arrays.asList("GC1111", "GC00001")), kody(zobrazene[0]));

		final CountDownLatch zamceno = new CountDownLatch(1);
		final Future<?> zapis = geoget.submit(() -> {
			try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db); Statement s = c.createStatement()) {
				s.execute("BEGIN EXCLUSIVE");
				zamceno.countDown();
				pustit.await();
				s.execute("COMMIT");
			}
			return null;
		});
		zamceno.await();
		Files.write(new File(gpx, "b.gpx").toPath(), ImportKesiTest.gpx(ImportKesiTest.kes("GC2222", "Geocache", "Traditional Cache", "Cizí", 1, true, false, "2", "")).getBytes(StandardCharsets.UTF_8));

		Assert.assertNull("keše ze zamčené databáze by zmizely z mapy", nacitac.nacti(null, new Genom()));
		Assert.assertTrue(nacitac.jeZamcena(db));

		pustit.countDown();
		zapis.get();
		Assert.assertEquals(new HashSet<>(Arrays.asList("GC1111", "GC2222", "GC00001")), kody(nacitac.nacti(null, new Genom())));
	}

	private static Set<String> kody(final KesBag bag) {
		final Set<String> kody = new HashSet<>();
		for (final Kesoid k : bag.getKesoidy()) {
			kody.add(k.getIdentifier());
		}
		return kody;
	}

	private static KesoidModel model() {
		return model(() -> null);
	}

	private static KesoidModel model(final java.util.function.Supplier<KesBag> zobrazene) {
		final ProgressModel progress = new ProgressModel();
		progress.inject(udalost -> {});
		final KesoidModel model = new KesoidModel() {
			// Bez EventFireru by události z vlákna načítání spadly na EDT.
			@Override
			public void fire(final cz.geokuk.framework.Event0<?> udalost) {}

			@Override
			public GccomNick getGccomNick() {
				return new GccomNick("Ja", 42);
			}

			@Override
			public void zaradGsakDatabaze(final Set<File> databaze, final Set<File> nedostupne) {}

			@Override
			public void zaradOpensakDatabaze(final Set<File> databaze, final Set<File> nedostupne) {}
			@Override
			public void zaradGeogetDatabaze(final Set<File> databaze, final Set<File> nedostupne) {}

			@Override
			public KesBag getVsechnyKesoidy() {
				return zobrazene.get();
			}
		};
		model.inject(progress);
		model.inject(new KesoidPluginManager());
		return model;
	}

	private static void zalozGeoget(final File db) throws SQLException {
		try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db); Statement s = c.createStatement()) {
			s.execute("CREATE TABLE geocache (id TEXT, x REAL, y REAL, name TEXT, author TEXT, cachetype TEXT, cachesize TEXT, difficulty TEXT, terrain TEXT,"
					+ " cachestatus INTEGER, gs_ownerid INTEGER, dthidden INTEGER, country TEXT, state TEXT, dtfound INTEGER)");
			s.execute("CREATE TABLE geolist (id TEXT, shortdesc BLOB, hint TEXT)");
			s.execute("CREATE TABLE waypoint (id TEXT, x REAL, y REAL, prefixid TEXT, wpttype TEXT, name TEXT)");
			s.execute("CREATE TABLE geotag (id TEXT, ptrkat INTEGER, ptrvalue INTEGER)");
			s.execute("CREATE TABLE geotagcategory (key INTEGER, value TEXT)");
			s.execute("CREATE TABLE geotagvalue (key INTEGER, value TEXT)");
			s.execute("INSERT INTO geocache VALUES ('GC00001', 50.1, 14.4, 'První', 'autor', 'Traditional Cache', 'Regular', '2', '3', 0, 1, 20200101, 'CZ', 'Praha', 0)");
		}
	}
}
