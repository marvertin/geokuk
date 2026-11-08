package cz.geokuk.core.napoveda;

import java.awt.EventQueue;
import java.awt.SecondaryLoop;
import java.awt.Toolkit;
import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cz.geokuk.core.napoveda.Vykon.Souhrn;
import cz.geokuk.core.napoveda.Vykon.Velicina;

/** Klouzavé okno měření: medián, p95, přetečení, vynulování a měření událostí na EDT. */
public class VykonTest {

	@Before
	@After
	public void vynuluj() {
		Vykon.vynuluj();
	}

	/**
	 * Provede kontrolu v jediné události na EDT: dokud běží, žádná jiná událost nezapíše měření (fronta měřící EDT zůstává po jiných testech v systémové frontě a zapisuje každou událost; kreslení
	 * zapisuje překreslení mapy).
	 */
	private static void naEdt(final Runnable kontrola) throws Exception {
		final Throwable[] chyba = new Throwable[1];
		EventQueue.invokeAndWait(() -> {
			try {
				kontrola.run();
			} catch (final RuntimeException | Error e) {
				chyba[0] = e;
			}
		});
		if (chyba[0] instanceof Error) {
			throw (Error) chyba[0];
		}
		if (chyba[0] instanceof RuntimeException) {
			throw (RuntimeException) chyba[0];
		}
	}

	@Test
	public void medianAP95() throws Exception {
		naEdt(() -> {
			Vykon.vynuluj();
			for (int ms = 1; ms <= 100; ms++) {
				Vykon.zaznamenej(Velicina.PREKRESLENI, ms * 1_000_000L);
			}
			final Souhrn s = Vykon.souhrn(Velicina.PREKRESLENI);
			Assert.assertEquals(100, s.pocet);
			Assert.assertEquals(50.0, s.median, 0.001);
			Assert.assertEquals(95.0, s.p95, 0.001);
			Assert.assertEquals(100.0, s.max, 0.001);
			Assert.assertEquals(100, s.zaPoslednich10s);
		});
	}

	@Test
	public void oknoDrziJenPosledniHodnoty() {
		for (int i = 0; i < Vykon.VELIKOST_OKNA; i++) {
			Vykon.zaznamenej(Velicina.DLAZDICE_ONLINE, 1_000_000_000L);
		}
		for (int i = 0; i < Vykon.VELIKOST_OKNA; i++) {
			Vykon.zaznamenej(Velicina.DLAZDICE_ONLINE, 2_000_000L);
		}
		final Souhrn s = Vykon.souhrn(Velicina.DLAZDICE_ONLINE);
		Assert.assertEquals(2 * Vykon.VELIKOST_OKNA, s.pocet);
		Assert.assertEquals("medián jen z posledních hodnot", 2.0, s.p95, 0.001);
		Assert.assertEquals("max od vynulování", 1000.0, s.max, 0.001);
	}

	@Test
	public void prazdneAVynulovani() throws Exception {
		naEdt(() -> {
			Vykon.vynuluj();
			Assert.assertEquals(0, Vykon.souhrn(Velicina.EDT).pocet);
			Assert.assertEquals(0.0, Vykon.souhrn(Velicina.EDT).median, 0);
			Vykon.zaznamenej(Velicina.EDT, (Vykon.PRAH_EDT_MS + 1) * 1_000_000);
			Assert.assertEquals(1, Vykon.edtNadPrahem());
			Vykon.vynuluj();
			Assert.assertEquals(0, Vykon.souhrn(Velicina.EDT).pocet);
			Assert.assertEquals(0, Vykon.edtNadPrahem());
		});
	}

	@Test
	public void textAJson() throws Exception {
		naEdt(() -> {
			Vykon.vynuluj();
			Vykon.zaznamenej(Velicina.PREKRESLENI, 12_000_000);
			Vykon.zaznamenej(Velicina.DLAZDICE_OFFLINE, 3_000_000);
			final String text = Vykon.text();
			Assert.assertTrue(text, text.contains("Překreslení mapy: medián 12.0 ms"));
			Assert.assertTrue(text, text.contains("Získání dlaždice online: bez záznamu"));
			Assert.assertTrue(text, text.contains("Dlaždic za sekundu (posledních 10 s): 0.1"));
			final String json = Vykon.json();
			Assert.assertTrue(json, json.contains("\"prekresleni\":{\"pocet\":1,\"medianMs\":12.000"));
			Assert.assertTrue(json, json.contains("\"dlazdicZaSekundu\":0.10"));
		});
	}

	/** Událost, jejíž obsluha vyhodí výjimku. */
	private static final class ChybnaUdalost extends java.awt.AWTEvent implements java.awt.ActiveEvent {
		private static final long serialVersionUID = 1L;

		ChybnaUdalost() {
			super(new Object(), java.awt.AWTEvent.RESERVED_ID_MAX + 1);
		}

		@Override
		public void dispatch() {
			throw new IllegalStateException("zkouška");
		}
	}

	/** invokeAndWait se vrátí už z dispatch(), měření se zapíše až po něm; další událost počká na zápis. */
	private static void pockejNaZapis() throws Exception {
		EventQueue.invokeAndWait(() -> {
		});
	}

	@Test
	public void mericiFrontaMeriAPropoustiVyjimky() throws Exception {
		final Vykon.MericiFronta fronta = new Vykon.MericiFronta();
		Toolkit.getDefaultToolkit().getSystemEventQueue().push(fronta);
		EventQueue.invokeAndWait(() -> {
			try {
				Thread.sleep(25);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
		pockejNaZapis();
		Assert.assertTrue(Vykon.souhrn(Velicina.EDT).max >= 20.0);
		final RuntimeException[] zachycena = new RuntimeException[1];
		final long pred = Vykon.souhrn(Velicina.EDT).pocet;
		EventQueue.invokeAndWait(() -> {
			try {
				fronta.dispatchEvent(new ChybnaUdalost());
			} catch (final RuntimeException e) {
				zachycena[0] = e;
			}
		});
		Assert.assertTrue("výjimka projde beze změny", zachycena[0] instanceof IllegalStateException);
		Assert.assertEquals("zkouška", zachycena[0].getMessage());
		Assert.assertTrue("vnořená událost se změří i s výjimkou", Vykon.souhrn(Velicina.EDT).pocet > pred);
	}

	@Test
	public void modalniSmyckaVeVnoreneUdalostiSeNemeri() throws Exception {
		Toolkit.getDefaultToolkit().getSystemEventQueue().push(new Vykon.MericiFronta());
		EventQueue.invokeAndWait(() -> {
			final SecondaryLoop vnejsi = Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();
			EventQueue.invokeLater(() -> {
				final SecondaryLoop vnitrni = Toolkit.getDefaultToolkit().getSystemEventQueue().createSecondaryLoop();
				final Thread zavri = new Thread(() -> {
					try {
						Thread.sleep(300);
					} catch (final InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					vnitrni.exit();
				});
				zavri.start();
				vnitrni.enter();
				vnejsi.exit();
			});
			vnejsi.enter();
		});
		pockejNaZapis();
		final Souhrn edt = Vykon.souhrn(Velicina.EDT);
		Assert.assertTrue("EDT max " + edt.max, edt.max < 250);
	}

	@Test
	public void popisZaseku() {
		Assert.assertEquals("Zásek EDT 1500 ms: ChybnaUdalost (Object)", Vykon.popisZaseku(new ChybnaUdalost(), 1_500_000_000L));
	}

	@Test
	public void zasekNadSekunduJeVPoslednichUdalostechIPoStartu() throws Exception {
		Vykon.cekejNaMapu();
		Toolkit.getDefaultToolkit().getSystemEventQueue().push(new Vykon.MericiFronta());
		EventQueue.invokeAndWait(() -> {
			try {
				Thread.sleep(Vykon.PRAH_ZASEKU_MS + 50);
			} catch (final InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});
		pockejNaZapis();
		final String text = Diagnostika.text();
		final String udalosti = text.substring(text.indexOf("Poslední události"));
		Assert.assertTrue(text, udalosti.contains("Zásek EDT ") && udalosti.contains(": InvocationEvent"));
		Assert.assertTrue("zásek při startu se do statistik nepočítá: " + Vykon.text(), Vykon.souhrn(Velicina.EDT).max < Vykon.PRAH_ZASEKU_MS);
	}

	@Test
	public void sbiraSeAzPoOdkladuPoPrvniMape() throws Exception {
		naEdt(this::sbiraSeAzPoOdkladuPoPrvniMapeNaEdt);
	}

	private void sbiraSeAzPoOdkladuPoPrvniMapeNaEdt() {
		Vykon.vynuluj();
		Vykon.cekejNaMapu();
		Assert.assertTrue(Vykon.text(), Vykon.text().contains("(měření začne 10 s po zobrazení mapy; medián a p95 z posledních 256 záznamů)"));
		Vykon.zaznamenej(Velicina.EDT, 5_000_000_000L);
		Vykon.zaznamenej(Velicina.PREKRESLENI, 3_000_000_000L);
		Vykon.zaznamenej(Velicina.EDT, 4_000_000_000L);
		Assert.assertEquals(0, Vykon.souhrn(Velicina.EDT).pocet);
		Assert.assertEquals(0, Vykon.souhrn(Velicina.PREKRESLENI).pocet);
		Assert.assertTrue(Vykon.text(), Vykon.text().matches("(?s).*\\(měřeno od \\d\\d:\\d\\d:\\d\\d; .*"));
		Vykon.vynuluj();
		Vykon.zaznamenej(Velicina.EDT, 1_000_000L);
		Assert.assertEquals("po vynulování se měří hned", 1, Vykon.souhrn(Velicina.EDT).pocet);
	}

	@Test
	public void kratkyZasobnikHorniRamceAPrvniGeokuk() {
		final StackTraceElement[] st = { new StackTraceElement("sun.font.SunFontManager", "loadFonts", "S.java", 10), new StackTraceElement("sun.font.X", "y", "X.java", 2),
				new StackTraceElement("java.awt.Z", "w", "Z.java", 3), new StackTraceElement("javax.swing.A", "b", "A.java", 4),
				new StackTraceElement("cz.geokuk.plugins.mapy.kachle.gui.JKachle", "kresli", "JKachle.java", 223), new StackTraceElement("cz.geokuk.Jiny", "x", "Jiny.java", 1) };
		Assert.assertEquals("SunFontManager.loadFonts:10 ← X.y:2 ← Z.w:3 ← … ← JKachle.kresli:223", Vykon.kratce(st));
		Assert.assertEquals("JKachle.kresli:223 ← Jiny.x:1", Vykon.kratce(Arrays.copyOfRange(st, 4, 6)));
	}

	private static void spiNaEdt() {
		try {
			Thread.sleep(Vykon.PRAH_ZASEKU_MS + 600);
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	@Test
	public void hlidacPridaZasobnikKZaseku() throws Exception {
		final Vykon.MericiFronta fronta = new Vykon.MericiFronta();
		Toolkit.getDefaultToolkit().getSystemEventQueue().push(fronta);
		fronta.spustHlidace();
		EventQueue.invokeAndWait(VykonTest::spiNaEdt);
		pockejNaZapis();
		final String text = Diagnostika.text();
		final String udalosti = text.substring(text.indexOf("Poslední události"));
		Assert.assertTrue(udalosti, udalosti.matches("(?s).*Zásek EDT \\d+ ms: InvocationEvent \\([^)]*\\) – [^\\n]*VykonTest\\.spiNaEdt:\\d+.*"));
	}

	private static final long S = 1_000_000_000L;

	@Test
	public void probuzeniPoUspani() {
		Assert.assertEquals("Probuzení po 45 s", Vykon.popisProbuzeni(45 * S));
		Assert.assertEquals("Probuzení po 635 min", Vykon.popisProbuzeni(38_074 * S));
		final Vykon.MericiFronta fronta = new Vykon.MericiFronta();
		final long t0 = 1_000 * S;
		fronta.tik(t0);
		fronta.tik(t0 + 30 * 60 * S);
		final String text = Diagnostika.text();
		Assert.assertTrue(text, text.substring(text.indexOf("Poslední události")).contains("Probuzení po 30 min"));
	}

	/** Událost přes uspání počítače se nepočítá; skutečně dlouhá událost, během které hlídač tikal, ano. */
	@Test
	public void udalostPresUspani() {
		final Vykon.MericiFronta fronta = new Vykon.MericiFronta();
		final long t0 = 1_000 * S;
		Assert.assertFalse("bez hlídače se nic nepozná", fronta.presUspani(t0, t0 + 60 * S));
		fronta.tik(t0);
		for (long t = t0; t <= t0 + 20 * S; t += S / 4) {
			fronta.tik(t);
		}
		Assert.assertFalse("hlídač tikal, EDT opravdu stál", fronta.presUspani(t0, t0 + 20 * S));
		Assert.assertTrue("hlídač po probuzení ještě netikl", fronta.presUspani(t0 + 20 * S, t0 + 20 * S + 3_600 * S));
		fronta.tik(t0 + 20 * S + 3_600 * S);
		Assert.assertTrue("hlídač mezeru zjistil dřív, než událost skončila", fronta.presUspani(t0 + 20 * S, t0 + 20 * S + 3_600 * S + S / 10));
		Assert.assertFalse("krátká událost", fronta.presUspani(t0 + 3_621 * S, t0 + 3_621 * S + S / 2));
		for (long t = t0 + 3_620 * S; t <= t0 + 3_633 * S; t += S / 4) {
			fronta.tik(t);
		}
		Assert.assertFalse("dlouhá událost až po probuzení se počítá", fronta.presUspani(t0 + 3_621 * S, t0 + 3_633 * S));
	}

	/** Zásek EDT přes uspání (hlídač mezitím netikal) se do Diagnostiky ani do statistik nezapíše. */
	@Test
	public void zasekPresUspaniSeNezapise() throws Exception {
		final Vykon.MericiFronta fronta = new Vykon.MericiFronta();
		fronta.mezeraUspaniNs = Vykon.PRAH_ZASEKU_MS * 1_000_000;
		fronta.tik(System.nanoTime());
		Toolkit.getDefaultToolkit().getSystemEventQueue().push(fronta);
		final String pred = Diagnostika.text();
		final int zaseku = pred.split("Zásek EDT", -1).length;
		EventQueue.invokeAndWait(VykonTest::spiNaEdt);
		pockejNaZapis();
		Assert.assertEquals(zaseku, Diagnostika.text().split("Zásek EDT", -1).length);
		Assert.assertTrue("do statistiky EDT se nezapočítá", Vykon.souhrn(Velicina.EDT).max < Vykon.PRAH_ZASEKU_MS);
	}
}
