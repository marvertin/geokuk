package cz.geokuk.plugins.mapy.stahovac;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import org.junit.*;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.Timeout;
import org.tmatesoft.sqljet.core.SqlJetTransactionMode;
import org.tmatesoft.sqljet.core.table.ISqlJetCursor;
import org.tmatesoft.sqljet.core.table.SqlJetDb;

import com.sun.net.httpserver.HttpServer;

import cz.geokuk.core.coord.*;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.core.onoffline.OnofflineModel;
import cz.geokuk.framework.MyPreferences;
import cz.geokuk.plugins.mapy.ZmenaMapNastalaEvent;
import cz.geokuk.plugins.mapy.kachle.KachleModel;
import cz.geokuk.plugins.mapy.kachle.data.*;
import cz.geokuk.plugins.mapy.kachle.podklady.*;
import cz.geokuk.util.file.Filex;

/**
 * Mapy > Postahovat dlaždice v reálném objemu: tisíce dlaždic ze syntetického serveru do skutečné cache (SQLite), pak zastavení uprostřed,
 * opakované stažení téhož výřezu a „restart“ programu. Počet dlaždic mění {@code scenare.dlazdic} (výchozí 1500, 0 test přeskočí, limit dialogu je 100 000).
 * Potřebuje displej (dialog), jinak se přeskočí.
 */
public class StahovaniVelkyObjemTest {

	private static final Wgs STRED = new Wgs(50.0875, 14.4214);
	private static final int MAX = 13;
	private static final int MIN = 10;
	private static final int CIL = Integer.getInteger("scenare.dlazdic", 1500);
	/** Poměr stran okna; šířka a výška se dopočítají tak, aby všechna měřítka dala zhruba {@link #CIL} dlaždic. */
	private static final Dimension VELIKOST = velikostProDlazdic(CIL);

	/** Čas na jedno stažení celého výřezu; při velkém počtu roste s ním (na CI se zapisuje stovky dlaždic za sekundu). */
	private static final long LIMIT_STAZENI_MS = Math.max(120_000L, CIL * 10L);

	@Rule
	public Timeout limitTestu = Timeout.millis(Math.max(180_000L, 2 * LIMIT_STAZENI_MS + 60_000L));

	@Rule
	public TemporaryFolder tmp = new TemporaryFolder();

	private final MyPreferences pref = MyPreferences.current().node("test-stahovani-velky-objem");
	private final Collection<String> pozadavky = new ConcurrentLinkedQueue<>();
	private final AtomicInteger zpozdeniMs = new AtomicInteger();
	/** Požadavky, které server přijal a ještě na ně neodpověděl. */
	private final AtomicInteger rozpracovanePozadavky = new AtomicInteger();
	private HttpServer server;
	private ExecutorService vlaknaServeru;
	private Boolean imageIoCache;
	private EKaType mapa;
	private File slozkaCache;
	private final List<JKachleOflinerDialog> dialogy = new ArrayList<>();
	private final List<DavkaStahovani> davky = new ArrayList<>();

	/** Okno 3:2, ve kterém měřítka MIN až MAX dají dohromady asi {@code cil} dlaždic (v nejpodrobnějším 16/21 z nich). */
	private static Dimension velikostProDlazdic(final int cil) {
		final double dlazdicNaMax = cil * 16.0 / 21;
		final double v = Math.sqrt(dlazdicNaMax / 1.5);
		return new Dimension((int) (v * 1.5 * 256), (int) (v * 256));
	}

	/** Program: ziskavac s modelem a dialogem nad skutečnou cache ve složce. */
	private class Sestava {
		final KachleZiskavac ziskavac = new KachleZiskavac();
		final KachleManager manager;
		final KachleModel kachleModel;
		JKachleOflinerDialog dialog;

		Sestava() throws Exception {
			final KachleCacheFolderHolder holder = new KachleCacheFolderHolder();
			holder.setKachleCacheDir(new Filex(slozkaCache, false, true));
			manager = KachleManagerFactory.getInstance(holder);
			ziskavac.inject(new OnofflineModel() {
				@Override
				public boolean isOnlineMode() {
					return true;
				}
			});
			kachleModel = new KachleModel() {
				@Override
				protected MyPreferences currPrefe() {
					return pref;
				}

				@Override
				public boolean isUkladatMapyNaDisk() {
					return true;
				}
			};
			kachleModel.inject(ziskavac);
			ziskavac.inject(kachleModel);
			// Model si při injektování zakládá vlastní správu cache, ta má tady jít do složky testu.
			ziskavac.setKachleManager(manager);
			SwingUtilities.invokeAndWait(() -> {
				dialog = new JKachleOflinerDialog();
				dialog.inject(kachleModel);
				dialog.onEvent(new ZmenaMapNastalaEvent(mapa));
				dialog.onEvent(new VyrezChangedEvent(new Coord(MAX, STRED.toMou(), VELIKOST, 0.0)));
				dialog.initAfterEventReceiverRegistration();
			});
			dialogy.add(dialog);
		}

		int spocitej() throws Exception {
			return dialog.new KachleOflinerSwingWorker(false).doInBackground();
		}

		/** Tlačítko „Stáhnout“ bez okna s průběhem; dávka se vrací hned, zařazuje se na pozadí. */
		DavkaStahovani spust(final int pocet) throws Exception {
			final DavkaStahovani davka = new DavkaStahovani(pocet, () -> {});
			final Field f = JKachleOflinerDialog.class.getDeclaredField("davka");
			f.setAccessible(true);
			f.set(dialog, davka);
			dialog.new KachleOflinerSwingWorker(true).doInBackground();
			davky.add(davka);
			return davka;
		}

		DavkaStahovani stahni(final int pocet, final long limitMs) throws Exception {
			final DavkaStahovani davka = spust(pocet);
			pockej(davka, limitMs);
			return davka;
		}

		/** Čeká, až skončí zařazování dlaždic a ve frontách a vláknech stahování nic nezbývá; teprve potom je počet požadavků na serveru konečný. */
		void pockejNaKlientaBezPrace(final long limitMs) throws Exception {
			final Field f = KachleZiskavac.class.getDeclaredField("exekucniSluzby");
			f.setAccessible(true);
			final Collection<?> sluzby = ((Map<?, ?>) f.get(ziskavac)).values();
			final long konec = System.currentTimeMillis() + limitMs;
			while (System.currentTimeMillis() < konec) {
				if (zarazovaniBezi() || !jsouNecinne(sluzby)) {
					Thread.sleep(5);
					continue;
				}
				// Nečinnost ve dvou po sobě jdoucích kontrolách: dlaždice mezi frontou a vláknem by první kontrolu mohla minout.
				Thread.sleep(5);
				if (!zarazovaniBezi() && jsouNecinne(sluzby)) {
					return;
				}
			}
			Assert.fail("klient stahování nedoběhl do " + limitMs + " ms");
		}

		private boolean zarazovaniBezi() {
			for (final Thread t : Thread.getAllStackTraces().keySet()) {
				if ("pro-offline".equals(t.getName()) && t.isAlive()) {
					return true;
				}
			}
			return false;
		}

		private boolean jsouNecinne(final Collection<?> sluzby) throws Exception {
			for (final Object o : sluzby) {
				final Method m = o.getClass().getDeclaredMethod("jeNecinna");
				m.setAccessible(true);
				if (!(Boolean) m.invoke(o)) {
					return false;
				}
			}
			return true;
		}

		/** Jako při ukončení programu: co čeká na zápis, se dopíše na disk. */
		void ukonciProgram() throws Exception {
			final Method m = KachleZiskavac.class.getDeclaredMethod("dopisNaDisk");
			m.setAccessible(true);
			m.invoke(ziskavac);
		}
	}

	private static void pockej(final DavkaStahovani davka, final long limitMs) throws Exception {
		final long konec = System.currentTimeMillis() + limitMs;
		while (!davka.jeHotova() && System.currentTimeMillis() < konec) {
			Thread.sleep(20);
		}
		Assert.assertTrue("stahování nedoběhlo do " + limitMs + " ms: " + davka.popis(), davka.jeHotova());
	}

	@Before
	public void setUp() throws Exception {
		Assume.assumeFalse("bez displeje", GraphicsEnvironment.isHeadless());
		Assume.assumeTrue("scenare.dlazdic=0 test vypíná", CIL > 0);
		imageIoCache = ImageIO.getUseCache();
		ImageIO.setUseCache(false);
		final ByteArrayOutputStream png = new ByteArrayOutputStream();
		final BufferedImage obrazek = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < 256; i++) {
			obrazek.setRGB(i, i, 0xFF8040);
		}
		ImageIO.write(obrazek, "png", png);
		final byte[] data = png.toByteArray();
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/", ex -> {
			pozadavky.add(ex.getRequestURI().getPath());
			rozpracovanePozadavky.incrementAndGet();
			try {
				final int cekani = zpozdeniMs.get();
				if (cekani > 0) {
					try {
						Thread.sleep(cekani);
					} catch (final InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
				ex.sendResponseHeaders(200, data.length);
				ex.getResponseBody().write(data);
				ex.close();
			} finally {
				rozpracovanePozadavky.decrementAndGet();
			}
		});
		vlaknaServeru = Executors.newFixedThreadPool(16);
		server.setExecutor(vlaknaServeru);
		server.start();
		mapa = UzivatelskeMapyPristup.sHromadnymStahovanim("velky-objem", "http://127.0.0.1:" + server.getAddress().getPort() + "/{z}/{x}/{y}", MIN, 18);
		slozkaCache = tmp.newFolder("cache");
	}

	@After
	public void tearDown() throws Exception {
		// Rozpracovaná dávka by po vypnutí serveru dál hlásila chyby spojení.
		davky.forEach(d -> d.zastav("konec testu."));
		SwingUtilities.invokeAndWait(() -> dialogy.forEach(JKachleOflinerDialog::dispose));
		if (server != null) {
			server.stop(0);
			vlaknaServeru.shutdownNow();
		}
		if (imageIoCache != null) {
			ImageIO.setUseCache(imageIoCache);
		}
		pref.removeNode();
	}

	private int radkuVCache() throws Exception {
		final File soubor = new File(slozkaCache, "tiles.sqlite");
		if (!soubor.isFile()) {
			return 0;
		}
		final SqlJetDb db = SqlJetDb.open(soubor, false);
		try {
			db.beginTransaction(SqlJetTransactionMode.READ_ONLY);
			final ISqlJetCursor kurzor = db.getTable("tiles").open();
			try {
				return (int) kurzor.getRowCount();
			} finally {
				kurzor.close();
				db.commit();
			}
		} finally {
			db.close();
		}
	}

	private static Set<String> bezDuplicit(final Collection<String> pozadavky, final String co) {
		final Set<String> jedinecne = new TreeSet<>(pozadavky);
		Assert.assertEquals(co + ": žádná dlaždice dvakrát", jedinecne.size(), pozadavky.size());
		return jedinecne;
	}

	/** Tisíce dlaždic jako uživatel: dialog spočítá, stáhne se, program se ukončí a po novém spuštění je všechno v cache. */
	@Test
	public void tisiceDlazdicDoCachePoRestartuVsechnyJsouTam() throws Exception {
		final Set<String> ocekavane = StahovacSyntetickaMapaTest.ocekavane(VELIKOST, STRED, MAX, MAX, MIN);
		Assert.assertTrue("výřez má dát tisíce dlaždic: " + ocekavane.size(), ocekavane.size() > CIL * 0.8);
		final Sestava s = new Sestava();
		Assert.assertEquals(ocekavane.size(), s.spocitej());
		final long t0 = System.currentTimeMillis();
		final DavkaStahovani davka = s.stahni(ocekavane.size(), LIMIT_STAZENI_MS);
		System.out.println("Stažení " + ocekavane.size() + " dlaždic: " + (System.currentTimeMillis() - t0) / 1000 + " s, " + ocekavane.size() * 1000L / Math.max(1, System.currentTimeMillis() - t0) + " dlaždic/s");
		Assert.assertTrue(davka.popis(), davka.popis().startsWith("Hotovo: v cache je " + ocekavane.size() + " z " + ocekavane.size() + " dlaždic, chyb 0."));
		Assert.assertEquals(ocekavane, bezDuplicit(pozadavky, "první stažení"));

		s.ukonciProgram();
		Assert.assertEquals("po ukončení programu jsou všechny dlaždice v souboru cache", ocekavane.size(), radkuVCache());

		pozadavky.clear();
		final Sestava po = new Sestava();
		final DavkaStahovani znovu = po.stahni(ocekavane.size(), LIMIT_STAZENI_MS);
		Assert.assertTrue(znovu.popis(), znovu.popis().startsWith("Hotovo: v cache je " + ocekavane.size() + " z " + ocekavane.size() + " dlaždic, chyb 0."));
		Assert.assertEquals("stejný výřez po restartu nestahuje nic", Collections.emptySet(), new TreeSet<>(pozadavky));
	}

	/** Uživatel stáhne část výřezu, zmáčkne Zastavit stahování a spustí stejný výřez znovu: dostáhne se jen zbytek. */
	@Test
	public void zastaveniUprostredAPokracovaniDostahneJenZbytek() throws Exception {
		final Set<String> ocekavane = StahovacSyntetickaMapaTest.ocekavane(VELIKOST, STRED, MAX, MAX, MIN);
		zpozdeniMs.set(10);
		final Sestava s = new Sestava();
		final DavkaStahovani davka = s.spust(ocekavane.size());
		// Při velkém počtu stačí rozjetá dávka, ne desetina všech dlaždic.
		final int rozjeto = Math.min(ocekavane.size() / 10, 2000);
		final long konec = System.currentTimeMillis() + 60_000;
		while (pozadavky.size() < rozjeto && System.currentTimeMillis() < konec) {
			Thread.sleep(20);
		}
		Assert.assertTrue("stahování se nerozběhlo: " + davka.popis(), pozadavky.size() >= rozjeto);
		davka.zastav("na přání uživatele.");
		pockej(davka, 10_000);
		Assert.assertTrue(davka.popis(), davka.popis().startsWith("Zastaveno: na přání uživatele."));
		// Požadavek už vyslaný klientem může na server dorazit i po zastavení; pak už se nic nežádá, až když zařazování skončilo a vlákna stahování jsou nečinná.
		s.pockejNaKlientaBezPrace(30_000);
		Assert.assertEquals("rozpracované požadavky nedoběhly", 0, rozpracovanePozadavky.get());
		final int poZastaveni = pozadavky.size();
		Assert.assertEquals("po zastavení se žádné další dlaždice nežádají", poZastaveni, pozadavky.size());
		// Zastavená dávka další dlaždici nepřijme a její stahování zruší.
		final AtomicInteger zruseno = new AtomicInteger();
		Assert.assertFalse("zastavená dávka další dlaždici nezařadí", davka.zarazeno(davka.dlazdice(), zruseno::incrementAndGet));
		Assert.assertEquals("dlaždice odmítnutá zastavenou dávkou se zruší", 1, zruseno.get());
		Assert.assertEquals(poZastaveni, pozadavky.size());
		Assert.assertTrue("zastavení má zastavit, ne počkat na všechny: " + poZastaveni, poZastaveni < ocekavane.size());
		s.ukonciProgram();
		final int vCache = radkuVCache();
		Assert.assertTrue("zastavená dávka nechala v cache aspoň část: " + vCache, vCache > 0);

		pozadavky.clear();
		zpozdeniMs.set(0);
		final Sestava po = new Sestava();
		final DavkaStahovani zbytek = po.stahni(ocekavane.size(), LIMIT_STAZENI_MS);
		Assert.assertTrue(zbytek.popis(), zbytek.popis().startsWith("Hotovo: v cache je " + ocekavane.size() + " z " + ocekavane.size() + " dlaždic, chyb 0."));
		Assert.assertEquals("stáhne se přesně to, co v cache chybělo", ocekavane.size() - vCache, bezDuplicit(pozadavky, "pokračování").size());
		po.ukonciProgram();
		Assert.assertEquals(ocekavane.size(), radkuVCache());
	}
}
