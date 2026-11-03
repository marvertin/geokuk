package cz.geokuk.plugins.kesoid.importek;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.*;
import org.junit.rules.TemporaryFolder;

import javax.swing.SwingUtilities;

import cz.geokuk.framework.Event0;
import cz.geokuk.framework.ProgressModel;
import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.kind.KesoidPluginManager;
import cz.geokuk.plugins.kesoid.mvc.GccomNick;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.plugins.kesoid.mvc.ZamceneDatabazeEvent;
import cz.geokuk.util.file.KeFile;

/** Opakované načítání, když jiný program drží některou databázi zamčenou. */
public class ZamcenaDatabazeOpakovaniTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final ExecutorService geoget = Executors.newCachedThreadPool();
	private final List<CountDownLatch> zamky = new ArrayList<>();
	private KesBag zobrazene;
	private File gpx;
	private File slozkaGeogetu;
	private MultiNacitac nacitac;
	private volatile File zamknoutPriNacitani;
	private final List<List<String>> ohlasenaZamceni = new CopyOnWriteArrayList<>();
	private final AtomicInteger zpracovanychSouboru = new AtomicInteger();
	private final List<InformaceOZdrojich> predbezneZdroje = new CopyOnWriteArrayList<>();
	private final Set<File> vypnute = new HashSet<>();
	private final Genom genom = new Genom();
	private volatile GccomNick nick = new GccomNick("Ja", 42);

	@Before
	public void setUp() throws Exception {
		gpx = tmp.newFolder("gpx");
		slozkaGeogetu = tmp.newFolder("geoget");
		zapisGpx("a.gpx", "GC1111");
		nacitac = new MultiNacitac(model());
	}

	@After
	public void uklid() {
		zamky.forEach(CountDownLatch::countDown);
		geoget.shutdown();
	}

	/** Každá databáze se načte, jakmile ji jiný program pustí, i když jiná je pořád zamčená. */
	@Test
	public void dveDatabazeSeUvolniSamostatne() throws Exception {
		final File a = zalozGeoget("a.db3", "GC000A");
		final File b = zalozGeoget("b.db3", "GC000B");
		final CountDownLatch pustitA = zamkni(a);
		zamkni(b);
		start();
		Assert.assertEquals(set("GC1111"), kody(nacti()));

		pustitA.countDown();
		Assert.assertEquals(set("GC1111", "GC000A"), kody(nacti()));
		Assert.assertTrue(nacitac.jeZamcena(b));
	}

	/** Změna jiného zdroje se ukáže hned, když zamčená databáze ještě nic nezobrazila. */
	@Test
	public void zmenaJinehoZdrojeNecekaNaZamek() throws Exception {
		zamkni(zalozGeoget("a.db3", "GC000A"));
		start();
		Assert.assertEquals(set("GC1111"), kody(nacti()));

		zapisGpx("b.gpx", "GC2222");
		Assert.assertEquals(set("GC1111", "GC2222"), kody(nacti()));
	}

	/** Keše ze zamčené databáze, které už jsou zobrazené, nezmizí a změna jiného zdroje se ukáže hned. */
	@Test
	public void zobrazeneKeseZamceneDatabazeZustanou() throws Exception {
		final File a = zalozGeoget("a.db3", "GC000A");
		try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + a); Statement s = c.createStatement()) {
			s.execute("INSERT INTO waypoint VALUES ('GC000A', 50.2, 14.5, 'PK', 'Parking Area', 'Parkoviště')");
		}
		start();
		final KesBag puvodni = nacti();
		Assert.assertEquals(set("GC1111", "GC000A"), kody(puvodni));
		final Set<String> waypointy = jmenaWaypointu(puvodni);
		Assert.assertEquals(3, waypointy.size());

		zamkni(a);
		zapisGpx("b.gpx", "GC2222");
		final KesBag bag = nacti();
		Assert.assertEquals(set("GC1111", "GC000A", "GC2222"), kody(bag));
		Assert.assertTrue("převezmou se i přídavné waypointy", jmenaWaypointu(bag).containsAll(waypointy));
		Assert.assertTrue(nacitac.jeZamcena(a));
		Assert.assertEquals("Přehled zdrojů ukazuje počty z minula", informace(puvodni, "a.db3").pocetWaypointuBranych, informace(bag, "a.db3").pocetWaypointuBranych);

		zapisGpx("c.gpx", "GC3333");
		Assert.assertEquals("převzaté keše se převezmou i podruhé", set("GC1111", "GC000A", "GC2222", "GC3333"), kody(nacti()));
	}

	/** Po změně nicku se keše zamčené databáze nepřevezmou se starým stavem „moje/nalezené“; zůstane zobrazené vše, dokud ji program nepustí. */
	@Test
	public void poZmeneNickuSeZamcenaNeprevezme() throws Exception {
		final File a = zalozGeoget("a.db3", "GC000A");
		start();
		Assert.assertEquals(set("GC1111", "GC000A"), kody(nacti()));

		final CountDownLatch pustit = zamkni(a);
		nick = new GccomNick("Jiny", 7);
		zapisGpx("b.gpx", "GC2222");
		Assert.assertNull("se starým nickem se nic nepřevezme, zobrazení zůstává", nacti());

		pustit.countDown();
		Assert.assertEquals(set("GC1111", "GC000A", "GC2222"), kody(nacti()));
	}

	/** Dvě zobrazené zamčené databáze: uvolněná se načte znovu, i když druhá je pořád zamčená. */
	@Test
	public void dveZobrazeneDatabazeSeUvolniSamostatne() throws Exception {
		final File a = zalozGeoget("a.db3", "GC000A");
		final File b = zalozGeoget("b.db3", "GC000B");
		start();
		Assert.assertEquals(set("GC1111", "GC000A", "GC000B"), kody(nacti()));

		final CountDownLatch pustitA = zamkni(a, "GC000C");
		zamkni(b);
		zapisGpx("b.gpx", "GC2222");
		Assert.assertEquals(set("GC1111", "GC000A", "GC000B", "GC2222"), kody(nacti()));

		pustitA.countDown();
		Assert.assertEquals(set("GC1111", "GC000A", "GC000C", "GC000B", "GC2222"), kody(nacti()));
		Assert.assertTrue(nacitac.jeZamcena(b));
		Assert.assertFalse(nacitac.jeZamcena(a));
	}

	/** Po změně sady ikon nejde staré keše převzít; zůstane zobrazené všechno, jak bylo. */
	@Test
	public void jinyGenomNechaZobrazene() throws Exception {
		final File a = zalozGeoget("a.db3", "GC000A");
		start();
		nacti();

		zamkni(a);
		zapisGpx("b.gpx", "GC2222");
		Assert.assertNull(nacitac.nacti(null, new Genom()));
	}

	/** Vypnutý zdroj se nepřevezme, i když je zamčený. */
	@Test
	public void vypnutaZamcenaDatabazeSeNeprevezme() throws Exception {
		final File a = zalozGeoget("a.db3", "GC000A");
		start();
		nacti();

		zamkni(a);
		vypnute.add(a);
		zapisGpx("b.gpx", "GC2222");
		Assert.assertEquals(set("GC1111", "GC2222"), kody(nacti()));
	}

	private static Set<String> jmenaWaypointu(final KesBag bag) {
		final Set<String> jmena = new HashSet<>();
		for (final Wpt w : bag.getWpts()) {
			jmena.add(w.getName());
		}
		return jmena;
	}

	private static InformaceOZdroji informace(final KesBag bag, final String jmeno) {
		for (final InformaceOZdroji i : bag.getInformaceOZdrojich().getSetInformaciOZdrojich()) {
			if (i.jmenoZdroje.getFile().getName().equals(jmeno)) {
				return i;
			}
		}
		throw new AssertionError("zdroj " + jmeno + " chybí");
	}

	/** Zamčené databáze se ohlásí jednou při změně, po uvolnění prázdným seznamem. */
	@Test
	public void zamceneDatabazeSeOhlasi() throws Exception {
		final File a = zalozGeoget("a.db3", "GC000A");
		final CountDownLatch pustitA = zamkni(a);
		start();
		nacti();
		Assert.assertEquals(Collections.singletonList(Collections.singletonList("a.db3")), ohlasenaZamceni());

		zapisGpx("b.gpx", "GC2222");
		nacti();
		Assert.assertEquals("beze změny se neohlašuje znovu", 1, ohlasenaZamceni().size());

		pustitA.countDown();
		nacti();
		Assert.assertEquals(Collections.emptyList(), ohlasenaZamceni().get(1));
	}

	private List<List<String>> ohlasenaZamceni() throws Exception {
		SwingUtilities.invokeAndWait(() -> {});
		return ohlasenaZamceni;
	}

	/** Import ve více transakcích: načte-li se databáze v mezeře mezi nimi, po dokončení zápisu se načte znovu. */
	@Test
	public void rozpracovanyImportSeNactePoDokonceni() throws Exception {
		final File a = zalozGeoget("a.db3", "GC000A");
		final CountDownLatch pustitA = zamkni(a);
		start();
		Assert.assertEquals(set("GC1111"), kody(nacti()));

		pustitA.countDown();
		Assert.assertEquals(set("GC1111", "GC000A"), kody(nacti()));

		pridejKes(a, "GC000B");
		Assert.assertEquals(set("GC1111", "GC000A", "GC000B"), kody(nacti()));
		Assert.assertNull("beze změny se znovu nenačítá", nacti());
	}

	/** Databáze, kterou jiný program nezamkl, se podle času změny nesleduje. */
	@Test
	public void nezamcenaDatabazeSeNesleduje() throws Exception {
		final File a = zalozGeoget("a.db3", "GC000A");
		start();
		nacti();
		pridejKes(a, "GC000B");
		Assert.assertNull(nacti());
	}

	/** Sledovaná databáze, která na chvíli zmizí (odpojený disk), se po návratu načte. */
	@Test
	public void sledovanaDatabazeSePoNavratuNacte() throws Exception {
		final File a = zalozGeoget("a.db3", "GC000A");
		final CountDownLatch pustitA = zamkni(a);
		start();
		nacti();
		pustitA.countDown();
		Assert.assertEquals(set("GC1111", "GC000A"), kody(nacti()));

		final File jinde = new File(tmp.getRoot(), "a.db3");
		Files.move(a.toPath(), jinde.toPath());
		Assert.assertEquals(set("GC1111"), kody(nacti()));
		Assert.assertNull("chybějící databáze se nenačítá pořád dokola", nacti());
		Files.move(jinde.toPath(), a.toPath());
		Assert.assertEquals(set("GC1111", "GC000A"), kody(nacti()));
		pridejKes(a, "GC000B");
		Assert.assertEquals(set("GC1111", "GC000A", "GC000B"), kody(nacti()));
	}

	/** Databáze, která přestala být ve zdrojích, se po změně jiným programem nenačítá pořád dokola. */
	@Test
	public void sledovanaDatabazeMimoZdrojeSeNesleduje() throws Exception {
		final File a = zalozGeoget("a.db3", "GC000A");
		final CountDownLatch pustitA = zamkni(a);
		start();
		nacti();
		pustitA.countDown();
		Assert.assertEquals(set("GC1111", "GC000A"), kody(nacti()));

		nacitac.setRootDirs(true, gpx, null, null, Collections.emptySet());
		Assert.assertEquals(set("GC1111"), kody(nacti()));
		pridejKes(a, "GC000B");
		Assert.assertNull(nacti());
	}

	private void pridejKes(final File db, final String kod) throws Exception {
		final long pred = db.lastModified();
		Thread.sleep(50);
		try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db); Statement s = c.createStatement()) {
			s.execute(insertKese(kod));
		}
		if (db.lastModified() == pred) {
			Assert.assertTrue(db.setLastModified(pred + 2000));
		}
	}

	/** Před prvním načtením se zdroje ohlásí, aby šly v Přehledu zdrojů vypnout; pak už ne. */
	@Test
	public void zdrojeSeOhlasiPredPrvnimNactenim() throws Exception {
		zalozGeoget("a.db3", "GC000A");
		start();
		nacti();
		Assert.assertEquals(1, predbezneZdroje.size());
		Assert.assertTrue(predbezneZdroje.get(0).getJmenaZdroju().contains(new File(slozkaGeogetu, "a.db3")));

		zapisGpx("b.gpx", "GC2222");
		nacti();
		Assert.assertEquals("po načtení se ukazují načtené zdroje", 1, predbezneZdroje.size());
	}

	/** Zrušené načítání se při dalším pokusu zopakuje celé. */
	@Test
	public void zruseneNacitaniSeZopakuje() throws Exception {
		start();
		final CompletableFuture<Void> zruseno = new CompletableFuture<>();
		zruseno.cancel(false);
		Assert.assertNull(nacitac.nacti(zruseno, new Genom()));
		Assert.assertEquals("zrušené načítání nemá číst další soubory", 0, zpracovanychSouboru.get());
		Assert.assertEquals(set("GC1111"), kody(nacti()));
	}

	/** Zrušení načítání ukončí čekání na zámek databáze, která se zamkla až po zjištění, že ji umíme načíst. */
	@Test
	public void zruseniPrerusiCekaniNaZamek() throws Exception {
		Assert.assertTrue(new File(gpx, "a.gpx").delete());
		zamknoutPriNacitani = zalozGeoget("a.db3", "GC000A");
		start();
		final CompletableFuture<Void> future = new CompletableFuture<>();
		final Future<KesBag> nacitani = geoget.submit(() -> nacitac.nacti(future, new Genom()));
		Thread.sleep(300);
		future.cancel(false);
		try {
			nacitani.get(20, TimeUnit.SECONDS);
		} catch (final TimeoutException e) {
			Assert.fail("načítání se po zrušení nepřerušilo");
		}
	}

	private void start() {
		nacitac.setRootDirs(true, gpx, slozkaGeogetu, null, Collections.emptySet());
	}

	private KesBag nacti() throws Exception {
		final KesBag bag = nacitac.nacti(null, genom);
		if (bag != null) {
			zobrazene = bag;
		}
		return bag;
	}

	private CountDownLatch zamkni(final File db) throws Exception {
		return zamkni(db, null);
	}

	/** Zamkne databázi; když je zadaný kód, jiný program do ní během zámku přidá keš. */
	private CountDownLatch zamkni(final File db, final String novaKes) throws Exception {
		final CountDownLatch zamceno = new CountDownLatch(1);
		final Zamek pustit = new Zamek();
		zamky.add(pustit);
		pustit.drzitel = geoget.submit(() -> {
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

	/** Uvolnění zámku počká, až „jiný program“ zápis dokončí a databázi zavře; pevné čekání na pomalém stroji nestačilo. */
	private static final class Zamek extends CountDownLatch {
		volatile Future<?> drzitel;

		Zamek() {
			super(1);
		}

		@Override
		public void countDown() {
			super.countDown();
			try {
				drzitel.get(30, TimeUnit.SECONDS);
			} catch (final Exception e) {
				throw new AssertionError("zámek databáze se neuvolnil", e);
			}
		}
	}

	private void zapisGpx(final String jmeno, final String kod) throws Exception {
		Files.write(new File(gpx, jmeno).toPath(), ImportKesiTest.gpx(ImportKesiTest.kes(kod, "Geocache", "Traditional Cache", "Cizí", 1, true, false, "2", "")).getBytes(StandardCharsets.UTF_8));
	}

	private File zalozGeoget(final String jmeno, final String kod) throws SQLException {
		final File db = new File(slozkaGeogetu, jmeno);
		try (Connection c = DriverManager.getConnection("jdbc:sqlite:" + db); Statement s = c.createStatement()) {
			s.execute("CREATE TABLE geocache (id TEXT, x REAL, y REAL, name TEXT, author TEXT, cachetype TEXT, cachesize TEXT, difficulty TEXT, terrain TEXT,"
					+ " cachestatus INTEGER, gs_ownerid INTEGER, dthidden INTEGER, country TEXT, state TEXT, dtfound INTEGER)");
			s.execute("CREATE TABLE geolist (id TEXT, shortdesc BLOB, hint TEXT)");
			s.execute("CREATE TABLE waypoint (id TEXT, x REAL, y REAL, prefixid TEXT, wpttype TEXT, name TEXT)");
			s.execute("CREATE TABLE geotag (id TEXT, ptrkat INTEGER, ptrvalue INTEGER)");
			s.execute("CREATE TABLE geotagcategory (key INTEGER, value TEXT)");
			s.execute("CREATE TABLE geotagvalue (key INTEGER, value TEXT)");
			s.execute(insertKese(kod));
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
		Assert.assertNotNull("má se načíst a zobrazit", bag);
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
			public ProgressModel getProgressModel() {
				final File db = zamknoutPriNacitani;
				// Zavolá se až po rozpoznání souboru, těsně před čtením.
				if (db != null && Arrays.stream(new Throwable().getStackTrace()).anyMatch(e -> "zpracujJedenFile".equals(e.getMethodName()))) {
					zamknoutPriNacitani = null;
					try {
						zamkni(db);
					} catch (final Exception e) {
						throw new IllegalStateException(e);
					}
				}
				return super.getProgressModel();
			}

			@Override
			public void fire(final Event0<?> udalost) {
				if (udalost instanceof ZamceneDatabazeEvent) {
					ohlasenaZamceni.add(((ZamceneDatabazeEvent) udalost).getJmena());
				}
			}

			@Override
			public void setNacitaneZdroje(final InformaceOZdrojich zdroje) {
				predbezneZdroje.add(zdroje);
			}

			@Override
			public void zaradGsakDatabaze(final Set<File> databaze, final Set<File> nedostupne) {}

			@Override
			public void zaradOpensakDatabaze(final Set<File> databaze, final Set<File> nedostupne) {}
			@Override
			public void zaradGeogetDatabaze(final Set<File> databaze, final Set<File> nedostupne) {}

			@Override
			public boolean maSeNacist(final KeFile soubor) {
				zpracovanychSouboru.incrementAndGet();
				return !vypnute.contains(soubor.getFile()) && super.maSeNacist(soubor);
			}
		};
		model.inject(progress);
		model.inject(new KesoidPluginManager());
		return model;
	}
}
