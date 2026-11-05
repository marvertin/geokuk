package cz.geokuk.plugins.mapy.stahovac;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.junit.*;

import com.sun.net.httpserver.HttpServer;

import cz.geokuk.core.coord.*;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.core.onoffline.OnofflineModel;
import cz.geokuk.framework.MyPreferences;
import cz.geokuk.plugins.mapy.ZmenaMapNastalaEvent;
import cz.geokuk.plugins.mapy.kachle.KachleModel;
import cz.geokuk.plugins.mapy.kachle.data.*;
import cz.geokuk.plugins.mapy.kachle.podklady.KachleManager;
import cz.geokuk.plugins.mapy.kachle.podklady.KachleZiskavac;

/**
 * Hromadné stahování nad syntetickým podkladem: lokální server dlaždic (jednobarevné PNG podle z/x/y), pevný výřez a pevný sled operací.
 * Očekávané dlaždice se počítají nezávisle vzorcem pro XYZ (EPSG:3857), ne z kódu programu. Potřebuje displej (dialog), jinak se přeskočí.
 */
public class StahovacSyntetickaMapaTest {

	private static final Wgs STRED = new Wgs(50.0875, 14.4214);
	private static final Dimension VELIKOST = new Dimension(1000, 700);
	private static final int MAX = 12;
	private static final int MIN = 10;

	private final MyPreferences pref = MyPreferences.current().node("test-stahovac-synteticky");
	private final Collection<String> pozadavky = new ConcurrentLinkedQueue<>();
	private final AtomicInteger limit = new AtomicInteger(Integer.MAX_VALUE);
	private HttpServer server;
	private ExecutorService vlaknaServeru;
	private Boolean imageIoCache;
	private EKaType mapa;
	private KachleModel kachleModel;
	private KachleManager kachleManager;
	private JKachleOflinerDialog dialog;

	@Before
	public void setUp() throws Exception {
		Assume.assumeFalse("bez displeje", GraphicsEnvironment.isHeadless());
		// Jako program (GeokukMain): bez toho jde každá dlaždice přes dočasný soubor a pomalý disk zastaví server i stahování.
		imageIoCache = ImageIO.getUseCache();
		ImageIO.setUseCache(false);
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/", ex -> {
			pozadavky.add(ex.getRequestURI().getPath());
			if (pozadavky.size() > limit.get()) {
				ex.sendResponseHeaders(429, -1);
				ex.close();
				return;
			}
			final String[] c = ex.getRequestURI().getPath().substring(1).split("/");
			final BufferedImage img = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
			final int barva = (Integer.parseInt(c[0]) * 40) << 16 | (Integer.parseInt(c[1]) % 256) << 8 | Integer.parseInt(c[2]) % 256;
			for (int y = 0; y < 256; y++) {
				for (int x = 0; x < 256; x++) {
					img.setRGB(x, y, barva);
				}
			}
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			ImageIO.write(img, "png", out);
			ex.sendResponseHeaders(200, out.size());
			ex.getResponseBody().write(out.toByteArray());
			ex.close();
		});
		// Pomalá odpověď na jeden požadavek nezdrží ostatní.
		vlaknaServeru = Executors.newFixedThreadPool(10);
		server.setExecutor(vlaknaServeru);
		server.start();
		mapa = UzivatelskeMapyPristup.sHromadnymStahovanim("synteticka", "http://127.0.0.1:" + server.getAddress().getPort() + "/{z}/{x}/{y}", MIN, 18);

		final KachleZiskavac ziskavac = new KachleZiskavac();
		ziskavac.inject(new OnofflineModel() {
			@Override
			public boolean isOnlineMode() {
				return true;
			}
		});
		kachleManager = new KachleManager() {
			@Override
			public boolean exists(final Ka ki) {
				return false;
			}

			@Override
			public java.awt.Image load(final Ka ki) {
				return null;
			}

			@Override
			public boolean save(final Collection<ItemToSave> itemsToSave) {
				return true;
			}
		};
		ziskavac.setKachleManager(kachleManager);
		kachleModel = new KachleModel() {
			@Override
			protected MyPreferences currPrefe() {
				return pref;
			}

			// Zápis na disk zapnutý jako ve výchozím nastavení; asynchronní úlohy po testu tak nesahají do už smazaných Preferences.
			@Override
			public boolean isUkladatMapyNaDisk() {
				return true;
			}
		};
		kachleModel.inject(ziskavac);
		ziskavac.inject(kachleModel);
		// Model si při injektování dosadí vlastní správu cache (bez složky, zápis i čtení padají); bez tohoto by test nezávisel jen na syntetickém serveru.
		ziskavac.setKachleManager(kachleManager);

		SwingUtilities.invokeAndWait(() -> {
			dialog = new JKachleOflinerDialog();
			dialog.inject(kachleModel);
			dialog.onEvent(new ZmenaMapNastalaEvent(mapa));
			dialog.onEvent(new VyrezChangedEvent(coord(STRED, MAX)));
			dialog.initAfterEventReceiverRegistration();
		});
	}

	@After
	public void tearDown() throws Exception {
		if (dialog != null) {
			// Počítání dlaždic na pozadí by po doběhnutí sáhlo do už smazaných Preferences.
			final Field f = JKachleOflinerDialog.class.getDeclaredField("kosw");
			f.setAccessible(true);
			final SwingWorker<?, ?>[] pocitani = new SwingWorker<?, ?>[1];
			SwingUtilities.invokeAndWait(() -> {
				try {
					pocitani[0] = (SwingWorker<?, ?>) f.get(dialog);
				} catch (final IllegalAccessException e) {
					throw new IllegalStateException(e);
				}
				if (pocitani[0] != null) {
					pocitani[0].cancel(true);
				}
				dialog.dispose();
			});
			final long konec = System.currentTimeMillis() + 10000;
			while (pocitani[0] != null && !pocitani[0].isDone() && System.currentTimeMillis() < konec) {
				Thread.sleep(10);
			}
			// SwingWorker posílá done() na EDT se zpožděním přes vlastní časovač (asi 33 ms).
			Thread.sleep(200);
			SwingUtilities.invokeAndWait(() -> {});
		}
		if (server != null) {
			server.stop(0);
			vlaknaServeru.shutdownNow();
		}
		if (imageIoCache != null) {
			ImageIO.setUseCache(imageIoCache);
		}
		pref.removeNode();
	}

	private static Coord coord(final Wgs stred, final int moumer) {
		return new Coord(moumer, stred.toMou(), VELIKOST, 0.0);
	}

	/** Dlaždice (jako „z/x/y“) viditelné ve výřezu o středu a velikosti v daném měřítku, pro měřítka od nejvyššího po nejnižší; počítá se z pixelových souřadnic světa. */
	static Set<String> ocekavane(final Wgs stred, final int aktualniMoumer, final int maxMoumer, final int minMoumer) {
		return ocekavane(VELIKOST, stred, aktualniMoumer, maxMoumer, minMoumer);
	}

	static Set<String> ocekavane(final Dimension velikost, final Wgs stred, final int aktualniMoumer, final int maxMoumer, final int minMoumer) {
		final Set<String> vysledek = new TreeSet<>();
		for (int z = maxMoumer; z >= minMoumer; z--) {
			final double svet = 256.0 * (1L << z);
			final double sin = Math.sin(Math.toRadians(stred.lat));
			final double px = (stred.lon + 180) / 360 * svet;
			final double py = (0.5 - Math.log((1 + sin) / (1 - sin)) / (4 * Math.PI)) * svet;
			final double w = z >= aktualniMoumer ? velikost.width * (double) (1L << (z - aktualniMoumer)) : Math.floor(velikost.width / (double) (1L << (aktualniMoumer - z)));
			final double h = z >= aktualniMoumer ? velikost.height * (double) (1L << (z - aktualniMoumer)) : Math.floor(velikost.height / (double) (1L << (aktualniMoumer - z)));
			final int x0 = (int) Math.floor((px - w / 2) / 256);
			final int x1 = (int) Math.floor((px + w / 2 - 1) / 256);
			final int y0 = (int) Math.floor((py - h / 2) / 256);
			final int y1 = (int) Math.floor((py + h / 2 - 1) / 256);
			for (int x = x0; x <= x1; x++) {
				for (int y = y0; y <= y1; y++) {
					vysledek.add("/" + z + "/" + x + "/" + y);
				}
			}
		}
		return vysledek;
	}

	private void udalost(final Runnable r) throws Exception {
		SwingUtilities.invokeAndWait(r);
	}

	private DavkaStahovani stahni(final int pocet) throws Exception {
		final DavkaStahovani davka = new DavkaStahovani(pocet, () -> {});
		final Field f = JKachleOflinerDialog.class.getDeclaredField("davka");
		f.setAccessible(true);
		f.set(dialog, davka);
		dialog.new KachleOflinerSwingWorker(true).doInBackground();
		final long konec = System.currentTimeMillis() + 20000;
		while (!davka.jeHotova() && System.currentTimeMillis() < konec) {
			Thread.sleep(20);
		}
		Assert.assertTrue("stahování nedoběhlo: " + davka.popis(), davka.jeHotova());
		return davka;
	}

	private int spocitej() throws Exception {
		return dialog.new KachleOflinerSwingWorker(false).doInBackground();
	}

	@Test(timeout = 60000)
	public void stahujePresneDlazdiceVyrezuVeVsechMeritkach() throws Exception {
		final Set<String> ocekavane = ocekavane(STRED, MAX, MAX, MIN);
		Assert.assertTrue("výřez má víc dlaždic, ať test něco říká", ocekavane.size() > 20);
		Assert.assertEquals(ocekavane.size(), spocitej());
		final DavkaStahovani davka = stahni(ocekavane.size());
		Assert.assertEquals(ocekavane, new TreeSet<>(pozadavky));
		Assert.assertEquals("žádná dlaždice dvakrát", ocekavane.size(), pozadavky.size());
		Assert.assertTrue(davka.popis(), davka.popis().startsWith("Hotovo: v cache je " + ocekavane.size() + " z " + ocekavane.size() + " dlaždic, chyb 0."));
	}

	@Test(timeout = 60000)
	public void posunVyrezuPoOtevreniDialoguSeProjevi() throws Exception {
		final Wgs jinde = new Wgs(50.2, 14.7);
		udalost(() -> dialog.onEvent(new VyrezChangedEvent(coord(jinde, MAX))));
		final Set<String> ocekavane = ocekavane(jinde, MAX, MAX, MIN);
		Assert.assertNotEquals(ocekavane(STRED, MAX, MAX, MIN), ocekavane);
		Assert.assertEquals(ocekavane.size(), spocitej());
		stahni(ocekavane.size());
		Assert.assertEquals("stahuje se výřez z posledního stavu, ne z otevření dialogu", ocekavane, new TreeSet<>(pozadavky));
	}

	@Test(timeout = 60000)
	public void oddaleniPoOtevreniDialoguPokryjeStejneUzemi() throws Exception {
		udalost(() -> dialog.onEvent(new VyrezChangedEvent(coord(STRED, MAX - 1))));
		final Set<String> ocekavane = ocekavane(STRED, MAX - 1, MAX, MIN);
		Assert.assertEquals(ocekavane.size(), spocitej());
		final DavkaStahovani davka = stahni(ocekavane.size());
		final Set<String> pozadovane = new TreeSet<>(pozadavky);
		final Set<String> chybi = new TreeSet<>(ocekavane);
		chybi.removeAll(pozadovane);
		final Set<String> navic = new TreeSet<>(pozadovane);
		navic.removeAll(ocekavane);
		Assert.assertEquals("dávka: " + davka.popis() + "; chybí požadavky " + chybi + ", navíc " + navic, ocekavane, pozadovane);
	}

	@Test(timeout = 60000)
	public void priblizeniPoOtevreniDialoguNestahujeNadNabizeneMaximum() throws Exception {
		udalost(() -> dialog.onEvent(new VyrezChangedEvent(coord(STRED, MAX + 2))));
		spocitej();
		stahni(spocitej());
		for (final String p : pozadavky) {
			Assert.assertTrue(p, Integer.parseInt(p.substring(1).split("/")[0]) <= MAX);
		}
	}

	@Test(timeout = 60000)
	public void omezeniServeruDavkuZastavi() throws Exception {
		// Výřez tak velký, aby dlaždic bylo o hodně víc, než kolik jich je po omezení už rozjetých ve vláknech a frontě stahování.
		final Dimension velky = new Dimension(6000, 4000);
		udalost(() -> dialog.onEvent(new VyrezChangedEvent(new Coord(MAX, STRED.toMou(), velky, 0.0))));
		final int vsech = ocekavane(velky, STRED, MAX, MAX, MIN).size();
		limit.set(5);
		final DavkaStahovani davka = stahni(spocitej());
		Assert.assertTrue(davka.jeZastavena());
		Assert.assertTrue(davka.popis(), davka.popis().contains("HTTP 429"));
		Assert.assertTrue("po omezení se další dlaždice nežádají: " + pozadavky.size() + " z " + vsech, pozadavky.size() < vsech / 2);
	}
}
