package cz.geokuk.plugins.kesoid.importek;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

import javax.swing.SwingUtilities;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import cz.geokuk.framework.Event0;
import cz.geokuk.framework.MyPreferences;
import cz.geokuk.framework.ProgressModel;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.kind.KesoidPluginManager;
import cz.geokuk.plugins.kesoid.mvc.GccomNick;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.plugins.kesoid.mvc.KesoidUmisteniSouboru;
import cz.geokuk.util.file.Filex;

/** Přepnutí zdroje na EDT nečeká na probíhající načítání, ani když načítání čeká na zámek cizí databáze. */
public class PrepnutiZdrojuEdtTest {

	private static final int LIMIT_MS = 100;

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final MyPreferences pref = MyPreferences.current().node("test-prepnuti-edt");
	private final ExecutorService geoget = Executors.newCachedThreadPool();
	private final List<CountDownLatch> zamky = new ArrayList<>();
	private File slozkaGeogetu;
	private KesoidModel model;
	private MultiNacitacLoaderManager manager;
	private final Genom genom = new Genom();
	private int citacKodu = 1000;
	private volatile boolean konec;

	@Before
	public void setUp() throws Exception {
		slozkaGeogetu = tmp.newFolder("geoget");
		final File gpx = tmp.newFolder("gpx");
		for (int i = 0; i < 40; i++) {
			zalozGeoget("db" + i + ".db3", 200);
		}
		final File zamcena = zalozGeoget("zamcena.db3", 10);
		zamkni(zamcena);
		final KesoidUmisteniSouboru u = new KesoidUmisteniSouboru();
		u.setKesDir(new Filex(gpx, false, true));
		u.setGeogetDataDir(new Filex(slozkaGeogetu, false, true));
		u.setGsakDataDir(new Filex(tmp.newFolder("gsak"), false, false));
		u.setCestyDir(new Filex(tmp.newFolder("cesty"), false, true));
		u.setImage3rdPartyDir(new Filex(tmp.newFolder("i1"), false, true));
		u.setImageMyDir(new Filex(tmp.newFolder("i2"), false, true));
				model = model(u);
		manager = new MultiNacitacLoaderManager(model);
	}

	@After
	public void uklid() throws Exception {
		manager.zastav();
		konec = true; // pravidelná kontrola ikon v modelu už nemá co načítat
		zamky.forEach(CountDownLatch::countDown);
		geoget.shutdown();
		Thread.sleep(300);
		pref.removeNode();
	}

	@Test
	public void startLoadNaEdtNeceka() throws Exception {
		manager.startLoad(true, genom);
		Thread.sleep(1500); // načítání běží a čeká na zámek poslední databáze
		for (int i = 0; i < 5; i++) {
			final long edt = naEdt(() -> manager.startLoad(true, genom));
			Assert.assertTrue("startLoad na EDT trval " + edt + " ms", edt < LIMIT_MS);
			Thread.sleep(200);
		}
		final long odezva = naEdt(() -> {});
		Assert.assertTrue("EDT odpovídá, " + odezva + " ms", odezva < LIMIT_MS);
	}

	private static long naEdt(final Runnable r) throws Exception {
		final long[] cas = new long[1];
		SwingUtilities.invokeAndWait(() -> {
			final long start = System.nanoTime();
			r.run();
			cas[0] = (System.nanoTime() - start) / 1_000_000;
		});
		return cas[0];
	}

	private CountDownLatch zamkni(final File db) throws Exception {
		return zamkni(db, null);
	}

	/** Zamkne databázi; když je zadaný kód, jiný program do ní během zámku přidá keš. */
	private CountDownLatch zamkni(final File db, final String novaKes) throws Exception {
		final CountDownLatch zamceno = new CountDownLatch(1);
		final CountDownLatch pustit = new CountDownLatch(1);
		zamky.add(pustit);
		geoget.submit(() -> {
			try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db); Statement s = c.createStatement()) {
				s.execute("BEGIN EXCLUSIVE");
				if (novaKes != null) {
					s.execute(insertKese(novaKes));
				}
				zamceno.countDown();
				pustit.await();
				s.execute("COMMIT");
			}
			return null;
		});
		zamceno.await();
		return pustit;
	}

	private File zalozGeoget(final String jmeno, final int pocet) throws SQLException {
		final File db = new File(slozkaGeogetu, jmeno);
		try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db); Statement s = c.createStatement()) {
			s.execute("CREATE TABLE geocache (id TEXT, x REAL, y REAL, name TEXT, author TEXT, cachetype TEXT, cachesize TEXT, difficulty TEXT, terrain TEXT,"
					+ " cachestatus INTEGER, gs_ownerid INTEGER, dthidden INTEGER, country TEXT, state TEXT, dtfound INTEGER)");
			s.execute("CREATE TABLE geolist (id TEXT, shortdesc BLOB, hint TEXT)");
			s.execute("CREATE TABLE waypoint (id TEXT, x REAL, y REAL, prefixid TEXT, wpttype TEXT, name TEXT)");
			s.execute("CREATE TABLE geotag (id TEXT, ptrkat INTEGER, ptrvalue INTEGER)");
			s.execute("CREATE TABLE geotagcategory (key INTEGER, value TEXT)");
			s.execute("CREATE TABLE geotagvalue (key INTEGER, value TEXT)");
			s.execute("BEGIN");
			for (int i = 0; i < pocet; i++) {
				s.execute(insertKese("GC" + Integer.toString(++citacKodu, 36).toUpperCase()));
			}
			s.execute("COMMIT");
		}
		return db;
	}


	private static String insertKese(final String kod) {
		return "INSERT INTO geocache VALUES ('" + kod + "', 50.1, 14.4, 'Keš', 'autor', 'Traditional Cache', 'Regular', '2', '3', 0, 1, 20200101, 'CZ', 'Praha', 0)";
	}

	private KesoidModel model(final KesoidUmisteniSouboru u) {
		final ProgressModel progress = new ProgressModel();
		progress.inject(udalost -> {});
		final KesoidModel m = new KesoidModel() {
			@Override
			protected MyPreferences currPrefe() {
				return pref;
			}

			@Override
			public KesoidUmisteniSouboru getUmisteniSouboru() {
				return konec ? null : u;
			}

			@Override
			public GccomNick getGccomNick() {
				return new GccomNick("Ja", 42);
			}

			@Override
			public KesBag getVsechnyKesoidy() {
				return null;
			}

			@Override
			public void setVsechnyKesoidy(final KesBag bag) {}

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
		};
		m.inject(progress);
		m.inject(new KesoidPluginManager());
		return m;
	}
}
