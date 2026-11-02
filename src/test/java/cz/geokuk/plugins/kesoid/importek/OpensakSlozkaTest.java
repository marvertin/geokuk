package cz.geokuk.plugins.kesoid.importek;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import cz.geokuk.framework.MyPreferences;
import cz.geokuk.framework.ProgressModel;
import cz.geokuk.plugins.kesoid.*;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.kind.KesoidPluginManager;
import cz.geokuk.plugins.kesoid.mvc.*;

/** Načítání databází z datové složky OpenSAKu. */
public class OpensakSlozkaTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final MyPreferences pref = MyPreferences.current().node("test-opensak-slozka");
	private final GsakParametryNacitani parametry = new GsakParametryNacitani();
	private final CountDownLatch pustit = new CountDownLatch(1);
	private final ExecutorService opensak = Executors.newSingleThreadExecutor();

	private File gpx;
	private File slozka;

	@Before
	public void setUp() throws Exception {
		gpx = tmp.newFolder("gpx");
		Files.write(new File(gpx, "a.gpx").toPath(), ImportKesiTest.gpx(ImportKesiTest.kes("GC9999", "Geocache", "Traditional Cache", "Cizí", 1, true, false, "2", "")).getBytes(StandardCharsets.UTF_8));
		// Známé databáze se ukládají s kanonickou cestou, ve Windows je TEMP zkrácený (RUNNER~1).
		slozka = tmp.newFolder("opensak").getCanonicalFile();
	}

	@After
	public void uklid() throws Exception {
		pustit.countDown();
		opensak.shutdown();
		pref.removeNode();
	}

	@Test
	public void kesePrevezmeZeSlozky() throws Exception {
		final File db = new File(slozka, "Default.db");
		OpensakTestDb.zaloz(db, OpensakTestDb.verze());
		OpensakDbLoaderTest.naplnKese(db);
		OpensakTestDb.vlozKes(db, 4, "GC4444", "Traditional", 49.0, 15.0, "owner_name", "Ja", "placed_by", "Ja");
		Files.write(new File(slozka, "opensak.json").toPath(), "{}".getBytes(StandardCharsets.UTF_8));
		final File zaloha = new File(slozka, "backup");
		Assert.assertTrue(zaloha.mkdir());
		final File vnorena = new File(zaloha, "Default.db");
		OpensakTestDb.zaloz(vnorena, OpensakTestDb.verze());
		OpensakTestDb.vlozKes(vnorena, 1, "GC5555", "Traditional Cache", 49.0, 15.0);

		final MultiNacitac nacitac = new MultiNacitac(model());
		nacitac.setRootDirs(true, gpx, null, null, slozka, Collections.emptySet());
		final KesBag bag = nacitac.nacti(null, new Genom());
		final Map<String, Kesoid> kese = kese(bag);
		Assert.assertEquals("načítá se jen *.db přímo ve složce", new HashSet<>(Arrays.asList("GC9999", "GC1111", "GC2222", "GC3333", "GC4444")), kese.keySet());

		Assert.assertEquals(EKesVztah.FOUND, kese.get("GC1111").getVztah());
		Assert.assertEquals(EKesVztah.OWN, kese.get("GC4444").getVztah());
		Assert.assertEquals(EKesVztah.NORMAL, kese.get("GC2222").getVztah());
		Assert.assertEquals(EKesStatus.ARCHIVED, kese.get("GC2222").getStatus());
		Assert.assertEquals(EKesStatus.DISABLED, kese.get("GC3333").getStatus());
		Assert.assertEquals(EKesStatus.ACTIVE, kese.get("GC4444").getStatus());
		Assert.assertEquals("Traditional Cache", kese.get("GC4444").getFirstWpt().getSym());

		final Kesoid mystery = kese.get("GC1111");
		Assert.assertEquals("final z opravených souřadnic je hlavní waypoint", 50.2, mystery.getMainWpt().lat, 1e-9);
		Assert.assertEquals(50.1, mystery.getFirstWpt().lat, 1e-9);
		Assert.assertEquals(2, kese.get("GC2222").getWptsCount());
	}

	@Test
	public void zamcenaDatabazeNezdrziOstatni() throws Exception {
		final File db = new File(slozka, "Default.db");
		OpensakTestDb.zaloz(db, OpensakTestDb.verze());
		OpensakDbLoaderTest.naplnKese(db);
		final CountDownLatch zamceno = new CountDownLatch(1);
		final Future<?> zapis = opensak.submit(() -> {
			try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db); Statement s = c.createStatement()) {
				s.execute("BEGIN EXCLUSIVE");
				s.execute("UPDATE caches SET name = 'Přejmenovaná' WHERE gc_code = 'GC2222'");
				zamceno.countDown();
				pustit.await();
				s.execute("COMMIT");
			}
			return null;
		});
		zamceno.await();

		final MultiNacitac nacitac = new MultiNacitac(model());
		nacitac.setRootDirs(true, gpx, null, null, slozka, Collections.emptySet());
		final long start = System.currentTimeMillis();
		final KesBag bag = nacitac.nacti(null, new Genom());
		Assert.assertTrue("čekalo se na zamčenou databázi", System.currentTimeMillis() - start < 10_000);
		Assert.assertEquals(Collections.singleton("GC9999"), kese(bag).keySet());
		Assert.assertTrue(nacitac.jeZamcena(db));

		pustit.countDown();
		zapis.get();
		Assert.assertTrue(kese(nacitac.nacti(null, new Genom())).containsKey("GC2222"));
		Assert.assertFalse(nacitac.jeZamcena(db));
	}

	@Test
	public void bezPovinnehoSloupceSeOstatniNactou() throws Exception {
		final File db = new File(slozka, "Jina.db");
		OpensakTestDb.zaloz(db, OpensakTestDb.verze(), "caches.cache_type");
		OpensakTestDb.vloz(db, "caches", "id", 1, "gc_code", "GC1111", "name", "x", "latitude", 50.0, "longitude", 14.0);
		final File dobra = new File(slozka, "Default.db");
		OpensakTestDb.zaloz(dobra, OpensakTestDb.verze());
		OpensakTestDb.vlozKes(dobra, 1, "GC2222", "Traditional Cache", 49.0, 15.0);

		final MultiNacitac nacitac = new MultiNacitac(model());
		nacitac.setRootDirs(true, gpx, null, null, slozka, Collections.emptySet());
		Assert.assertEquals(new HashSet<>(Arrays.asList("GC9999", "GC2222")), kese(nacitac.nacti(null, new Genom())).keySet());
	}

	/** „Načítat až po vybrání“: databáze, která do složky OpenSAKu přibyla, se nenačte, dokud ji uživatel nevybere. */
	@Test
	public void novaDatabazePoVybrani() throws Exception {
		parametry.setNoveDatabazeHned(true);
		parametry.setNoveDatabazeOpensakuHned(false);
		final File prvni = new File(slozka, "Default.db");
		OpensakTestDb.zaloz(prvni, OpensakTestDb.verze());
		OpensakTestDb.vlozKes(prvni, 1, "GC1111", "Traditional Cache", 49.0, 15.0);
		final KesoidModel model = model();
		final MultiNacitac nacitac = new MultiNacitac(model);
		nacitac.setRootDirs(true, gpx, null, null, slozka, Collections.emptySet());
		Assert.assertEquals(new HashSet<>(Arrays.asList("GC9999", "GC1111")), kese(nacitac.nacti(null, new Genom())).keySet());

		final File nova = new File(slozka, "Vylet.db");
		OpensakTestDb.zaloz(nova, OpensakTestDb.verze());
		OpensakTestDb.vlozKes(nova, 1, "GC2222", "Traditional Cache", 49.0, 15.0);
		Assert.assertEquals(new HashSet<>(Arrays.asList("GC9999", "GC1111")), kese(nacitac.nacti(null, new Genom())).keySet());
	}

	private KesoidModel model() {
		final ProgressModel progress = new ProgressModel();
		progress.inject(udalost -> {});
		final KesoidModel model = new KesoidModel() {
			// Bez EventFireru by události z vlákna načítání spadly na EDT.
			@Override
			public void fire(final cz.geokuk.framework.Event0<?> udalost) {}

			@Override
			protected MyPreferences currPrefe() {
				return pref;
			}

			@Override
			public GccomNick getGccomNick() {
				return new GccomNick("Ja", 42);
			}

			@Override
			public GsakParametryNacitani getGsakParametryNacitani() {
				return parametry;
			}
		};
		model.inject(progress);
		model.inject(new KesoidPluginManager());
		return model;
	}

	private static Map<String, Kesoid> kese(final KesBag bag) {
		final Map<String, Kesoid> kese = new HashMap<>();
		for (final Kesoid k : bag.getKesoidy()) {
			kese.put(k.getIdentifier(), k);
		}
		return kese;
	}
}
