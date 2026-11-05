package cz.geokuk.plugins.mapy.kachle.podklady;

import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.*;

import com.sun.net.httpserver.HttpServer;

/** Spojení se po chybě a přesměrování znovupoužije a nastavení spojení odpovídá souběhu. */
public class KachloDownloaderSpojeniTest {

	private HttpServer server;

	@After
	public void tearDown() {
		if (server != null) {
			server.stop(0);
		}
	}

	private List<Integer> zavolejDvakrat(final int kod, final String cesta) throws Exception {
		final List<Integer> porty = Collections.synchronizedList(new ArrayList<>());
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/", ex -> {
			porty.add(ex.getRemoteAddress().getPort());
			final byte[] telo = "chyba".getBytes();
			if (kod / 100 == 3) {
				ex.getResponseHeaders().add("Location", "/cil");
			}
			ex.sendResponseHeaders(kod, telo.length);
			try (OutputStream out = ex.getResponseBody()) {
				out.write(telo);
			}
		});
		server.start();
		for (int i = 0; i < 2; i++) {
			try {
				new KachloDownloader().downloadImage(new URL("http://127.0.0.1:" + server.getAddress().getPort() + cesta));
			} catch (final java.io.IOException e) {
				// očekávané, jde o spojení
			}
		}
		return porty;
	}

	@Test
	public void chybovaOdpovedNezavreSpojeni() throws Exception {
		final List<Integer> porty = zavolejDvakrat(404, "/1/2/3.png");
		Assert.assertEquals(2, porty.size());
		Assert.assertEquals("Druhý požadavek měl použít stejné spojení", porty.get(0), porty.get(1));
	}

	@Test
	public void presmerovaniNezavreSpojeni() throws Exception {
		final List<Integer> porty = zavolejDvakrat(302, "/1/2/3.png");
		// každé volání: přesměrování + cíl (opět 302, do vyčerpání), všechny na jednom spojení
		Assert.assertEquals(1, new HashSet<>(porty).size());
	}

	@Test
	public void maxConnectionsOdpovidaSoubehu() {
		new KachloDownloader();
		Assert.assertEquals(String.valueOf(KachleZiskavac.NTHREADS_WEB_CORE_ONLINE + KachleZiskavac.NTHREADS_WEB_MAXIMUM_BACTH), System.getProperty("http.maxConnections"));
	}

	/** Velká chybová odpověď: server pošle 300 kB z deklarovaných 100 MB a dál mlčí, dokud klient spojení nezavře. */
	@Test(timeout = 20000)
	public void velkaChybovaOdpovedNeblokujeAConnectionSeZavre() throws Exception {
		final byte[] zacatek = new byte[300 * 1024];
		final AtomicInteger spojeni = new AtomicInteger();
		final CountDownLatch zavrena = new CountDownLatch(2);
		final List<Socket> otevrena = Collections.synchronizedList(new ArrayList<>());
		try (ServerSocket ss = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"))) {
			final Thread prijimac = new Thread(() -> {
				try {
					while (true) {
						final Socket so = ss.accept();
						otevrena.add(so);
						spojeni.incrementAndGet();
						final Thread obsluha = new Thread(() -> obsluz(so, zacatek, zavrena), "test-spojeni");
						obsluha.setDaemon(true);
						obsluha.start();
					}
				} catch (final java.io.IOException e) {
					// server se zavřel
				}
			}, "test-prijimac");
			prijimac.setDaemon(true);
			prijimac.start();
			try {
				for (int i = 0; i < 2; i++) {
					try {
						new KachloDownloader().downloadImage(new URL("http://127.0.0.1:" + ss.getLocalPort() + "/1/2/3.png"));
						Assert.fail();
					} catch (final KachloDownloader.ChybaServeru e) {
						Assert.assertEquals(500, e.getKod());
					}
				}
				Assert.assertTrue("klient oba rozečtené odpovědi zavřel", zavrena.await(10, TimeUnit.SECONDS));
				Assert.assertEquals("nedočtené spojení se nevrací do keep-alive, druhý požadavek otevřel nové", 2, spojeni.get());
			} finally {
				synchronized (otevrena) {
					for (final Socket so : otevrena) {
						so.close();
					}
				}
			}
		}
	}

	/** Přečte požadavek, odpoví začátkem těla a čeká, až klient spojení zavře. */
	private static void obsluz(final Socket so, final byte[] zacatek, final CountDownLatch zavrena) {
		try (Socket s = so) {
			final java.io.InputStream in = s.getInputStream();
			int konec = 0;
			int c;
			while (konec < 4 && (c = in.read()) >= 0) {
				konec = c == '\r' || c == '\n' ? konec + 1 : 0;
			}
			final OutputStream out = s.getOutputStream();
			out.write("HTTP/1.1 500 Chyba\r\nContent-Length: 104857600\r\n\r\n".getBytes(java.nio.charset.StandardCharsets.US_ASCII));
			out.write(zacatek);
			out.flush();
			while (in.read() >= 0) {
				// klient dál nic neposílá; čeká se na zavření
			}
		} catch (final java.io.IOException e) {
			// klient spojení zavřel
		} finally {
			zavrena.countDown();
		}
	}

	@Test
	public void osmDavkaSmiNaServerJenDveSpojeni() {
		final SoubehNaHostitele soubeh = new SoubehNaHostitele();
		Assert.assertEquals(2, soubeh.pro("tile.openstreetmap.org").availablePermits());
		Assert.assertEquals(2, soubeh.pro("mapy.cz").availablePermits());
		Assert.assertEquals(2, soubeh.pro("ags.cuzk.gov.cz").availablePermits());
		Assert.assertEquals(2, soubeh.pro("zbgis.skgeodesy.sk").availablePermits());
		Assert.assertNull("bez omezení", soubeh.pro("outdoor.tiles.freemap.sk"));
		Assert.assertNull("bez omezení", soubeh.pro("127.0.0.1"));
		Assert.assertSame(soubeh.pro("Tile.OpenStreetMap.org"), soubeh.pro("tile.openstreetmap.org"));
	}
}
