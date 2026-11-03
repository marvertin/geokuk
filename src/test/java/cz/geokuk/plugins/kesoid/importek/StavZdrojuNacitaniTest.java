package cz.geokuk.plugins.kesoid.importek;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

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
import cz.geokuk.plugins.kesoid.mvc.StavZdrojuEvent;
import cz.geokuk.util.file.DirScanner;

/** Stav po položce zdrojů při načítání a hromadné přepínání. */
public class StavZdrojuNacitaniTest {

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final MyPreferences pref = MyPreferences.current().node("test-stav-zdroju");
	private final ExecutorService geoget = Executors.newCachedThreadPool();
	private final List<CountDownLatch> zamky = new ArrayList<>();
	private final AtomicInteger pocetUdalosti = new AtomicInteger();
	private final Genom genom = new Genom();
	private File gpx;
	private File slozkaGeogetu;
	private KesoidModel model;
	private MultiNacitac nacitac;

	@Before
	public void setUp() throws Exception {
		gpx = tmp.newFolder("gpx");
		slozkaGeogetu = tmp.newFolder("geoget");
		zapisGpx("a.gpx", "GC1111");
		model = model();
		nacitac = new MultiNacitac(model, new DirScanner(), model.getRegistrStavuZdroju());
	}

	@After
	public void uklid() throws Exception {
		zamky.forEach(CountDownLatch::countDown);
		geoget.shutdown();
		pref.removeNode();
	}

	private StavPolozky polozka(final String jmeno) {
		return nacitac.getRegistr().getSnimek().getPolozky().stream().filter(p -> p.getSoubor().getName().equals(jmeno)).findFirst().get();
	}

	@Test
	public void stavyPoNacteni() throws Exception {
		zalozGeoget("a.db3", "GC000A");
		start();
		Assert.assertNotNull(nacitac.nacti(null, genom));

		final StavPolozky g = polozka("a.gpx");
		Assert.assertEquals("a.gpx", g.getNazev());
		Assert.assertEquals(new File(gpx, "a.gpx").getPath(), g.getCesta());
		Assert.assertEquals(TypZdroje.GPX, g.getTyp());
		Assert.assertEquals(StavZdroje.NACTENO, g.getStav());
		Assert.assertEquals(1, g.getWpCelkem());
		Assert.assertEquals(1, g.getWpBrano());
		Assert.assertTrue(g.getVelikostNaDisku() > 0);
		final StavPolozky db = polozka("a.db3");
		Assert.assertEquals(TypZdroje.GEOGET, db.getTyp());
		Assert.assertEquals(StavZdroje.NACTENO, db.getStav());
		Assert.assertEquals(1, db.getWpCelkem());
		Assert.assertTrue(db.getVelikostNaDisku() > 0);
		Assert.assertTrue(db.isZapnuto());
	}

	@Test
	public void prazdnaSlozkaGeogetuJeVeStavuAPoPridaniDatabazeZmizi() throws Exception {
		start();
		nacitac.nacti(null, genom);
		Assert.assertEquals("Ve složce nejsou databáze .db3.", nacitac.getRegistr().getSnimek().getProblemSlozky(TypZdroje.GEOGET));
		Assert.assertNull(nacitac.getRegistr().getSnimek().getProblemSlozky(TypZdroje.GPX));
		zalozGeoget("a.db3", "GC000A");
		start();
		nacitac.nacti(null, genom);
		Assert.assertNull(nacitac.getRegistr().getSnimek().getProblemSlozky(TypZdroje.GEOGET));
	}

	@Test
	public void duvodPrazdneSlozky() throws Exception {
		Assert.assertEquals("Složka není dostupná.", MultiNacitac.duvodPrazdneSlozky(new File(slozkaGeogetu, "neni"), ".db3"));
		Assert.assertEquals("Ve složce nejsou databáze .db.", MultiNacitac.duvodPrazdneSlozky(slozkaGeogetu, ".db"));
	}

	@Test
	public void problemySlozekZapisujeJenAktualniBeh() {
		final RegistrStavuZdroju r = new RegistrStavuZdroju();
		final int gen = r.prepis(Collections.<File>emptyList(), f -> TypZdroje.GPX, File::getName, f -> true, t -> false, f -> 0L);
		r.setProblemySlozek(gen - 1, Collections.singletonMap(TypZdroje.GSAK, "stará"));
		Assert.assertNull(r.getSnimek().getProblemSlozky(TypZdroje.GSAK));
		r.setProblemySlozek(gen, Collections.singletonMap(TypZdroje.GSAK, "nová"));
		Assert.assertEquals("nová", r.getSnimek().getProblemSlozky(TypZdroje.GSAK));
		r.setProblemySlozek(gen, Collections.<TypZdroje, String>emptyMap());
		Assert.assertNull(r.getSnimek().getProblemSlozky(TypZdroje.GSAK));
	}

	@Test
	public void nazevGpxVPodslozceJeCestaKeKoreni() throws Exception {
		Assert.assertTrue(new File(gpx, "sub").mkdir());
		Files.copy(new File(gpx, "a.gpx").toPath(), new File(gpx, "sub/x.gpx").toPath());
		start();
		nacitac.nacti(null, genom);
		Assert.assertEquals("sub" + File.separator + "x.gpx", polozka("x.gpx").getNazev());
	}

	@Test
	public void duplicitaSeUkazeRozdilem() throws Exception {
		zapisGpx("b.gpx", "GC1111");
		start();
		Assert.assertNotNull(nacitac.nacti(null, genom));
		final StavPolozky a = polozka("a.gpx");
		final StavPolozky b = polozka("b.gpx");
		Assert.assertEquals(1, a.getWpCelkem());
		Assert.assertEquals(1, b.getWpCelkem());
		Assert.assertEquals("keš se bere jen z jednoho zdroje", 1, a.getWpBrano() + b.getWpBrano());
		Assert.assertEquals(1, a.getPocetDuplicit() + b.getPocetDuplicit());
	}

	@Test
	public void vadnaDatabazeMaStavChyba() throws Exception {
		zalozGeoget("a.db3", "GC000A");
		Files.write(new File(slozkaGeogetu, "b.db3").toPath(), "to neni sqlite, jen text delsi nez hlavicka souboru sqlite".getBytes(StandardCharsets.UTF_8));
		start();
		nacitac.nacti(null, genom);
		Assert.assertEquals(StavZdroje.CHYBA, polozka("b.db3").getStav());
		Assert.assertNotNull(polozka("b.db3").getChyba());
		Assert.assertEquals(StavZdroje.NACTENO, polozka("a.db3").getStav());
		Assert.assertEquals(StavZdroje.CHYBA, nacitac.getRegistr().getSnimek().getStavTypu(TypZdroje.GEOGET));
	}

	@Test
	public void zamcenaDatabazeCekaNaZapis() throws Exception {
		final File db = zalozGeoget("a.db3", "GC000A");
		final CountDownLatch pustit = zamkni(db);
		start();
		nacitac.nacti(null, genom);
		Assert.assertEquals(StavZdroje.CEKA_NA_ZAPIS, polozka("a.db3").getStav());
		Assert.assertEquals(StavZdroje.NACTENO, polozka("a.gpx").getStav());
		Assert.assertEquals(StavZdroje.CEKA_NA_ZAPIS, nacitac.getRegistr().getSnimek().getStavTypu(TypZdroje.GEOGET));

		pustit.countDown();
		Thread.sleep(300);
		Assert.assertNotNull(nacitac.nacti(null, genom));
		Assert.assertEquals(StavZdroje.NACTENO, polozka("a.db3").getStav());
	}

	@Test
	public void vypnutyTypSeNenacitaAVyberZustane() throws Exception {
		final File a = zalozGeoget("a.db3", "GC000A");
		final File b = zalozGeoget("b.db3", "GC000B");
		start();
		zdroje = nacitac.nacti(null, genom).getInformaceOZdrojich();
		model.setNacitatSoubor(keFile(b), false);
		model.setNacitatTyp(TypZdroje.GEOGET, false);
		Assert.assertEquals(StavZdroje.VYPNUTO, polozka("a.db3").getStav());
		Assert.assertEquals("počet z minula zůstane", 1, polozka("a.db3").getWpCelkem());
		Assert.assertEquals(StavZdroje.NACTENO, polozka("a.gpx").getStav());
		Assert.assertFalse(model.maSeNacist(a));
		Assert.assertTrue("vlastní volba položky zůstala", model.jeZdrojZapnut(a));
		Assert.assertEquals("typ se do blokovaných zdrojů nezapisuje", Collections.singleton(b), new HashSet<>(pref.node("kesoid").getFileCollection("blokovaneZdroje", null)));

		model.setNacitatTyp(TypZdroje.GEOGET, true);
		Assert.assertEquals(StavZdroje.CEKA_NA_RADU, polozka("a.db3").getStav());
		Assert.assertEquals("b zůstala odškrtnutá", StavZdroje.VYPNUTO, polozka("b.db3").getStav());
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertEquals(StavZdroju.StavVyberu.CASTECNE, model.getStavZdroju().getStavVyberuTypu(TypZdroje.GEOGET));
	}

	@Test
	public void jenTatoPolozkaZapneTypAOstatniOdznaci() throws Exception {
		final File a = zalozGeoget("a.db3", "GC000A");
		final File b = zalozGeoget("b.db3", "GC000B");
		start();
		zdroje = nacitac.nacti(null, genom).getInformaceOZdrojich();
		model.setNacitatTyp(TypZdroje.GEOGET, false);
		model.setNacitatJenPolozku(TypZdroje.GEOGET, b);
		Assert.assertFalse(model.isTypVypnut(TypZdroje.GEOGET));
		Assert.assertTrue(model.maSeNacist(b));
		Assert.assertFalse(model.maSeNacist(a));
		Assert.assertFalse(model.jeZdrojZapnut(a));
		Assert.assertEquals(StavZdroje.VYPNUTO, polozka("a.db3").getStav());
		Assert.assertEquals(StavZdroje.NACTENO, polozka("a.gpx").getStav());
	}

	@Test
	public void jenTentoTypVypneOstatniTypy() throws Exception {
		final File a = zalozGeoget("a.db3", "GC000A");
		start();
		zdroje = nacitac.nacti(null, genom).getInformaceOZdrojich();
		model.setNacitatVseVTypu(TypZdroje.GEOGET, false);
		model.setNacitatJenTyp(TypZdroje.GEOGET);
		Assert.assertFalse(model.isTypVypnut(TypZdroje.GEOGET));
		Assert.assertTrue("bez výběru se zapnou všechny položky", model.maSeNacist(a));
		Assert.assertTrue(model.isTypVypnut(TypZdroje.GPX));
		Assert.assertEquals(StavZdroje.VYPNUTO, polozka("a.gpx").getStav());
	}

	@Test
	public void vyberTypuAPolozekPrezijeRestart() throws Exception {
		final File a = zalozGeoget("a.db3", "GC000A");
		final File b = zalozGeoget("b.db3", "GC000B");
		start();
		zdroje = nacitac.nacti(null, genom).getInformaceOZdrojich();
		model.setNacitatSoubor(keFile(b), false);
		model.setNacitatTyp(TypZdroje.GPX, false);

		final KesoidModel novy = model();
		novy.nactiVyberZdroju();
		Assert.assertFalse(novy.jeZdrojZapnut(b));
		Assert.assertTrue(novy.jeZdrojZapnut(a));
		Assert.assertTrue(novy.isTypVypnut(TypZdroje.GPX));
		Assert.assertFalse(novy.isTypVypnut(TypZdroje.GEOGET));
	}

	@Test
	public void casyDatZdrojuSeUloziDoNastaveniANovyModelJeNacte() throws Exception {
		zalozGeoget("a.db3", "GC000A");
		model.nactiVyberZdroju();
		start();
		nacitac.nacti(null, genom);
		final KesoidModel novy = model();
		novy.nactiVyberZdroju();
		final List<String> cesty = new ArrayList<>();
		for (final String zaznam : novy.getCasyDatZdroju()) {
			cesty.add(zaznam.substring(zaznam.indexOf(';', zaznam.indexOf(';') + 1) + 1));
		}
		Assert.assertEquals(new HashSet<>(Arrays.asList(new File(gpx, "a.gpx").getPath(), new File(slozkaGeogetu, "a.db3").getPath())), new HashSet<>(cesty));
	}

	/** Nová databáze vypnutá až při zařazení („Načítat až po vybrání“) se v registru nesmí ukazovat jako zapnutá. */
	@Test
	public void novaDatabazeVypnutaPriZarazeniJeVRegistruVypnuta() throws Exception {
		final File gsak = tmp.newFolder("gsak");
		final File nova = new File(new File(gsak, "nova"), "sqlite.db3");
		Assert.assertTrue(nova.getParentFile().mkdir());
		Files.write(nova.toPath(), new byte[] { 1, 2, 3 });
		final Set<File> blokovane = new HashSet<>();
		final KesoidModel m = new KesoidModel() {
			@Override
			public GccomNick getGccomNick() {
				return new GccomNick("Ja", 42);
			}

			@Override
			public KesBag getVsechnyKesoidy() {
				return null;
			}

			@Override
			public void fire(final Event0<?> udalost) {}

			@Override
			public void setNacitaneZdroje(final InformaceOZdrojich zdroje) {}

			@Override
			public void zaradGsakDatabaze(final Set<File> databaze, final Set<File> nedostupne) {
				blokovane.addAll(databaze);
			}

			@Override
			public boolean jeZdrojZapnut(final File zdroj) {
				return !blokovane.contains(zdroj);
			}

			@Override
			public boolean maSeNacist(final File zdroj) {
				return jeZdrojZapnut(zdroj);
			}

			@Override
			public void zaradOpensakDatabaze(final Set<File> databaze, final Set<File> nedostupne) {}
			@Override
			public void zaradGeogetDatabaze(final Set<File> databaze, final Set<File> nedostupne) {}
		};
		final ProgressModel progress = new ProgressModel();
		progress.inject(udalost -> {});
		m.inject(progress);
		m.inject(new KesoidPluginManager());
		final MultiNacitac n = new MultiNacitac(m);
		n.setRootDirs(true, gpx, slozkaGeogetu, gsak, Collections.emptySet());
		n.nacti(null, genom);
		final StavPolozky p = n.getRegistr().getSnimek().getPolozky().stream().filter(x -> x.getTyp() == TypZdroje.GSAK).findFirst().get();
		Assert.assertEquals(StavZdroje.VYPNUTO, p.getStav());
		Assert.assertFalse(p.isZapnuto());
		Assert.assertEquals(StavZdroje.VYPNUTO, n.getRegistr().getSnimek().getStavTypu(TypZdroje.GSAK));
		Assert.assertFalse(n.getRegistr().getSnimek().isNacitaSe());
	}

	/** Změny stavu z vlákna načítání se slučují: dokud EDT nestihne událost doručit, další změny žádnou novou nevyrobí. */
	@Test
	public void zmenyStavuSeSlucujiDoJedneUdalosti() throws Exception {
		zalozGeoget("a.db3", "GC000A");
		start();
		nacitac.nacti(null, genom);
		SwingUtilities.invokeAndWait(() -> {});
		final int pred = pocetUdalosti.get();
		final java.util.concurrent.CountDownLatch edtBlokovan = new java.util.concurrent.CountDownLatch(1);
		final java.util.concurrent.CountDownLatch pustit = new java.util.concurrent.CountDownLatch(1);
		SwingUtilities.invokeLater(() -> {
			edtBlokovan.countDown();
			try {
				pustit.await();
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
		edtBlokovan.await();
		final File a = new File(slozkaGeogetu, "a.db3");
		final int gen = nacitac.getRegistr().getGenerace();
		for (int i = 0; i < 50; i++) {
			nacitac.getRegistr().zacina(gen, a);
			nacitac.getRegistr().postup(gen, a, i);
			nacitac.getRegistr().hotovo(gen, a, 1, 1);
		}
		pustit.countDown();
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertEquals("150 změn, jedna událost", pred + 1, pocetUdalosti.get());
		Assert.assertEquals(StavZdroje.NACTENO, model.getStavZdroju().getPolozky().stream().filter(x -> x.getSoubor().equals(a)).findFirst().get().getStav());
	}

	@Test
	public void hromadnePrepnutiJeJednaUdalostAJednaZmenaStavu() throws Exception {
		zalozGeoget("a.db3", "GC000A");
		zalozGeoget("b.db3", "GC000B");
		start();
		nacitac.nacti(null, genom);
		SwingUtilities.invokeAndWait(() -> {});
		final int pred = pocetUdalosti.get();

		model.setNacitatVse(false);
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertEquals(pred + 1, pocetUdalosti.get());
		for (final StavPolozky p : model.getStavZdroju().getPolozky()) {
			Assert.assertEquals(p.toString(), StavZdroje.VYPNUTO, p.getStav());
		}
		Assert.assertFalse(model.getStavZdroju().isTypZapnut(TypZdroje.GPX));

		model.setNacitatVseVTypu(TypZdroje.GEOGET, true);
		SwingUtilities.invokeAndWait(() -> {});
		Assert.assertEquals(pred + 2, pocetUdalosti.get());
		Assert.assertEquals(StavZdroje.CEKA_NA_RADU, polozka("a.db3").getStav());
		Assert.assertEquals(StavZdroje.CEKA_NA_RADU, polozka("b.db3").getStav());
		Assert.assertEquals(StavZdroje.VYPNUTO, polozka("a.gpx").getStav());
		Assert.assertTrue(model.getStavZdroju().isCelyTypZapnut(TypZdroje.GEOGET));

		start(); // jako startLoad po změně nastavení
		Assert.assertNotNull(nacitac.nacti(null, genom));
		Assert.assertEquals(StavZdroje.NACTENO, polozka("a.db3").getStav());
		Assert.assertEquals(StavZdroje.VYPNUTO, polozka("a.gpx").getStav());
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

	private void zapisGpx(final String jmeno, final String kod) throws Exception {
		Files.write(new File(gpx, jmeno).toPath(), ImportKesiTest.gpx(ImportKesiTest.kes(kod, "Geocache", "Traditional Cache", "Cizí", 1, true, false, "2", "")).getBytes(StandardCharsets.UTF_8));
	}

	private cz.geokuk.util.file.KeFile keFile(final File soubor) {
		return nacitac.getRegistr() == null ? null : zdroje.getKeJmenaZdroju().stream().filter(k -> k.getFile().equals(soubor)).findFirst().get();
	}

	private InformaceOZdrojich zdroje;

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

	private void start() {
		nacitac.setRootDirs(true, gpx, slozkaGeogetu, null, Collections.emptySet());
	}

	private KesoidModel model() {
		final ProgressModel progress = new ProgressModel();
		progress.inject(udalost -> {});
		final KesoidModel m = new KesoidModel() {
			@Override
			protected MyPreferences currPrefe() {
				return pref;
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
			public void fire(final Event0<?> udalost) {
				if (udalost instanceof StavZdrojuEvent) {
					pocetUdalosti.incrementAndGet();
				}
			}

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
