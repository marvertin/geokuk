package cz.geokuk.util.gui;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.*;

/** Průběh bublinky s hodnotou: čistá funkce viditelnosti, řízení časovače a kreslení bez displeje. */
public class BublinkaHodnotyTest {

	private final AtomicLong cas = new AtomicLong(1000);
	private final AtomicInteger prekresleni = new AtomicInteger();
	private boolean bezi;
	private int spusteni;
	private int zastaveni;

	private final BublinkaHodnoty.Casovac casovac = new BublinkaHodnoty.Casovac() {
		@Override
		public void spust() {
			bezi = true;
			spusteni++;
		}

		@Override
		public void zastav() {
			bezi = false;
			zastaveni++;
		}

		@Override
		public boolean bezi() {
			return bezi;
		}
	};

	private BublinkaHodnoty bublinka() {
		return new BublinkaHodnoty(cas::get, casovac, prekresleni::incrementAndGet);
	}

	@Test
	public void casyJsouVeSpolecnychKonstantach() {
		Assert.assertEquals(140, BublinkaHodnoty.ZACATEK_MS);
		Assert.assertEquals(700, BublinkaHodnoty.DRZENI_MS);
		Assert.assertEquals(300, BublinkaHodnoty.ZANIKNUTI_MS);
	}

	@Test
	public void viditelnostPrubehTazeni() {
		Assert.assertEquals(0f, BublinkaHodnoty.viditelnost(0, -1), 0f);
		Assert.assertEquals(0.5f, BublinkaHodnoty.viditelnost(BublinkaHodnoty.ZACATEK_MS / 2, -1), 0.01f);
		Assert.assertEquals(1f, BublinkaHodnoty.viditelnost(BublinkaHodnoty.ZACATEK_MS, -1), 0f);
		Assert.assertEquals("při tažení zůstane", 1f, BublinkaHodnoty.viditelnost(60_000, -1), 0f);
	}

	@Test
	public void viditelnostPoPusteni() {
		final long dlouho = 10_000;
		Assert.assertEquals(1f, BublinkaHodnoty.viditelnost(dlouho, 0), 0f);
		Assert.assertEquals("drží se", 1f, BublinkaHodnoty.viditelnost(dlouho, BublinkaHodnoty.DRZENI_MS), 0f);
		Assert.assertEquals(0.5f, BublinkaHodnoty.viditelnost(dlouho, BublinkaHodnoty.DRZENI_MS + BublinkaHodnoty.ZANIKNUTI_MS / 2), 0.01f);
		Assert.assertEquals(0f, BublinkaHodnoty.viditelnost(dlouho, BublinkaHodnoty.DRZENI_MS + BublinkaHodnoty.ZANIKNUTI_MS), 0f);
		Assert.assertEquals(0f, BublinkaHodnoty.viditelnost(dlouho, 5_000), 0f);
	}

	@Test
	public void viditelnostJeSouvislaAMonotonniVJednotlivychFazich() {
		float predchozi = 0;
		for (long t = 0; t <= BublinkaHodnoty.ZACATEK_MS + 50; t++) {
			final float v = BublinkaHodnoty.viditelnost(t, -1);
			Assert.assertTrue(v >= predchozi);
			Assert.assertTrue("skok " + predchozi + " -> " + v, v - predchozi < 0.02f);
			predchozi = v;
		}
		predchozi = 1;
		for (long t = BublinkaHodnoty.DRZENI_MS; t <= BublinkaHodnoty.DRZENI_MS + BublinkaHodnoty.ZANIKNUTI_MS + 50; t++) {
			final float v = BublinkaHodnoty.viditelnost(10_000, t);
			Assert.assertTrue(v <= predchozi);
			Assert.assertTrue("skok " + predchozi + " -> " + v, predchozi - v < 0.02f);
			predchozi = v;
		}
	}

	@Test
	public void bublinkaSeObjeviDrziAZmizi() {
		final BublinkaHodnoty b = bublinka();
		Assert.assertEquals(0f, b.viditelnost(), 0f);
		b.tahne("50 %");
		Assert.assertTrue("časovač se spustil", b.casovacBezi());
		Assert.assertEquals(0f, b.viditelnost(), 0.001f);
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS);
		Assert.assertEquals(1f, b.viditelnost(), 0f);
		cas.addAndGet(5_000);
		Assert.assertEquals("při tažení se drží", 1f, b.viditelnost(), 0f);
		b.pust();
		cas.addAndGet(BublinkaHodnoty.DRZENI_MS - 1);
		Assert.assertEquals(1f, b.viditelnost(), 0f);
		cas.addAndGet(1 + BublinkaHodnoty.ZANIKNUTI_MS / 2);
		Assert.assertEquals(0.5f, b.viditelnost(), 0.02f);
		b.tik();
		Assert.assertTrue("časovač běží, dokud bublinka mizí", b.casovacBezi());
		cas.addAndGet(BublinkaHodnoty.ZANIKNUTI_MS);
		b.tik();
		Assert.assertEquals(0f, b.viditelnost(), 0f);
		Assert.assertFalse("časovač se na konci zastavil", b.casovacBezi());
		Assert.assertFalse(b.jeAktivni());
		Assert.assertEquals(1, zastaveni);
	}

	@Test
	public void casovacSeNespoustiZnovuPriDalsimTazeni() {
		final BublinkaHodnoty b = bublinka();
		b.tahne("a");
		b.tahne("b");
		b.tahne("c");
		Assert.assertEquals(1, spusteni);
		Assert.assertEquals("c", b.getText());
	}

	@Test
	public void novyTahBehemZanikuNaskociOdAktualniViditelnostiBezSkoku() {
		final BublinkaHodnoty b = bublinka();
		b.tahne("x");
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS);
		b.pust();
		cas.addAndGet(BublinkaHodnoty.DRZENI_MS + BublinkaHodnoty.ZANIKNUTI_MS / 2);
		final float pred = b.viditelnost();
		Assert.assertEquals(0.5f, pred, 0.02f);
		b.tahne("y");
		Assert.assertEquals("bez skoku", pred, b.viditelnost(), 0.02f);
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS);
		Assert.assertEquals(1f, b.viditelnost(), 0f);
	}

	@Test
	public void dotykBezTazeniSeKratkoUkazeAZmizi() {
		final BublinkaHodnoty b = bublinka();
		b.dotkni("10 %");
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS);
		Assert.assertEquals(1f, b.viditelnost(), 0f);
		cas.addAndGet(BublinkaHodnoty.DRZENI_MS + BublinkaHodnoty.ZANIKNUTI_MS);
		b.tik();
		Assert.assertEquals(0f, b.viditelnost(), 0f);
		Assert.assertFalse(b.casovacBezi());
	}

	@Test
	public void kazdyKrokPrekresli() {
		final BublinkaHodnoty b = bublinka();
		b.tahne("x");
		final int po = prekresleni.get();
		b.tik();
		Assert.assertEquals(po + 1, prekresleni.get());
	}

	@Test
	public void kresleniRespektujeViditelnost() {
		final BublinkaHodnoty b = bublinka();
		final BufferedImage obraz = new BufferedImage(300, 60, BufferedImage.TYPE_INT_ARGB);
		final Font font = new Font(Font.DIALOG, Font.BOLD, 12);
		Graphics2D g = obraz.createGraphics();
		Assert.assertNull("neviditelná se nekreslí", b.kresli(g, font, 250, 30));
		g.dispose();

		b.tahne("Průhlednost 73 %");
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS / 2);
		g = obraz.createGraphics();
		final Rectangle polovina = b.kresli(g, font, 250, 30);
		g.dispose();
		Assert.assertNotNull(polovina);
		final int alfaPolovina = alfa(obraz, polovina);

		final BufferedImage obraz2 = new BufferedImage(300, 60, BufferedImage.TYPE_INT_ARGB);
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS);
		g = obraz2.createGraphics();
		final Rectangle plna = b.kresli(g, font, 250, 30);
		g.dispose();
		Assert.assertEquals(polovina, plna);
		Assert.assertTrue("plná je krycí víc než poloviční", alfa(obraz2, plna) > alfaPolovina);
		Assert.assertEquals("pravý okraj na zadaném místě", 250, plna.x + plna.width);
		Assert.assertEquals("svisle vystředěná", 30, plna.y + plna.height / 2, 1);
		Assert.assertEquals("mimo bublinku nic", 0, obraz2.getRGB(2, 2) >>> 24);
	}

	private static int alfa(final BufferedImage i, final Rectangle r) {
		return i.getRGB(r.x + r.width / 2, r.y + 2) >>> 24;
	}

	@Test
	public void vlastniDrzeniPosouvaZanik() {
		Assert.assertEquals(1f, BublinkaHodnoty.viditelnost(5000, 2500, 2500), 0f);
		Assert.assertEquals("při výchozím držení už ubývá", true, BublinkaHodnoty.viditelnost(5000, 2500) < 1f);
		Assert.assertEquals(0.5f, BublinkaHodnoty.viditelnost(5000, 2500 + BublinkaHodnoty.ZANIKNUTI_MS / 2, 2500), 0.001f);
		Assert.assertEquals(0f, BublinkaHodnoty.viditelnost(5000, 2500 + BublinkaHodnoty.ZANIKNUTI_MS, 2500), 0f);
		Assert.assertEquals(BublinkaHodnoty.viditelnost(100, -1), BublinkaHodnoty.viditelnost(100, -1, 2500), 0f);
	}

	@Test
	public void zpravaSDlouhymDrzenimTrvaPresDveASPulSekundy() {
		final BublinkaHodnoty b = new BublinkaHodnoty(cas::get, casovac, prekresleni::incrementAndGet, 2500);
		b.dotkni("zpráva");
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS);
		Assert.assertEquals(1f, b.viditelnost(), 0f);
		cas.addAndGet(2500 - BublinkaHodnoty.ZACATEK_MS);
		b.tik();
		Assert.assertEquals("držení se počítá od doteku, tedy včetně náběhu", 1f, b.viditelnost(), 0f);
		Assert.assertTrue(bezi);
		cas.addAndGet(BublinkaHodnoty.ZANIKNUTI_MS + 1);
		b.tik();
		Assert.assertFalse("na konci se časovač zastaví", bezi);
		Assert.assertEquals(0f, b.viditelnost(), 0f);
	}

	@Test
	public void novaZpravaNahradiStaroubezSkokuDoNuly() {
		final BublinkaHodnoty b = new BublinkaHodnoty(cas::get, casovac, prekresleni::incrementAndGet, 2500);
		b.dotkni("první");
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS + 1000);
		b.dotkni("druhá");
		Assert.assertEquals("druhá", b.getText());
		Assert.assertTrue("zpráva zůstává viditelná", b.viditelnost() > 0.9f);
		Assert.assertEquals("časovač se nespouští znovu", 1, spusteni);
	}

	@Test
	public void bublinkaPustenaVNulovemCaseSePoDalsimPusteniNeprepise() {
		cas.set(0);
		final BublinkaHodnoty b = bublinka();
		b.tahne("x");
		b.pust();
		cas.set(100);
		b.pust();
		cas.set(BublinkaHodnoty.DRZENI_MS + BublinkaHodnoty.ZANIKNUTI_MS);
		b.tik();
		Assert.assertFalse(b.jeAktivni());
	}

	@Test
	public void bublinkaPustenaVNulovemCaseMaCasZanikuOdPusteni() {
		cas.set(0);
		final BublinkaHodnoty b = bublinka();
		b.tahne("x");
		b.pust();
		cas.set(BublinkaHodnoty.DRZENI_MS + BublinkaHodnoty.ZANIKNUTI_MS / 2);
		final float v = b.viditelnost();
		Assert.assertTrue("viditelnost " + v, v > 0f && v < 1f);
	}

	@Test
	public void novyTahPoPusteniVNulovemCaseZacinaOdAktualniViditelnosti() {
		cas.set(0);
		final BublinkaHodnoty b = bublinka();
		b.tahne("x");
		b.pust();
		cas.set(BublinkaHodnoty.DRZENI_MS + BublinkaHodnoty.ZANIKNUTI_MS / 2);
		final float pred = b.viditelnost();
		b.tahne("y");
		Assert.assertEquals("bez skoku", pred, b.viditelnost(), 0.01f);
	}

	@Test
	public void krokPresneNaKonciZanikuBublinkuSkryje() {
		final BublinkaHodnoty b = bublinka();
		b.dotkni("x");
		cas.addAndGet(BublinkaHodnoty.DRZENI_MS + BublinkaHodnoty.ZANIKNUTI_MS - 1);
		b.tik();
		Assert.assertTrue(b.jeAktivni());
		Assert.assertTrue(bezi);
		cas.addAndGet(1);
		b.tik();
		Assert.assertFalse(b.jeAktivni());
		Assert.assertFalse(bezi);
	}

	@Test
	public void zrusSkryjeBublinkuAZastaviCasovac() {
		final BublinkaHodnoty b = bublinka();
		b.tahne("x");
		b.zrus();
		Assert.assertFalse(b.jeAktivni());
		Assert.assertFalse(bezi);
		Assert.assertEquals(0f, b.viditelnost(), 0f);
	}

	@Test
	public void swingovyCasovacSpoustiZastavujeAVolaKrok() throws Exception {
		final CountDownLatch krok = new CountDownLatch(1);
		final BublinkaHodnoty.Casovac c = BublinkaHodnoty.swingovyCasovac(krok::countDown);
		try {
			Assert.assertFalse(c.bezi());
			c.spust();
			Assert.assertTrue(c.bezi());
			Assert.assertTrue("časovač zavolá krok", krok.await(5, TimeUnit.SECONDS));
		} finally {
			c.zastav();
		}
		Assert.assertFalse(c.bezi());
		BublinkaHodnoty.swingovyCasovac(null).spust();
	}

	@Test
	public void verejnyKonstruktorKresliPrekreslovanimZCasovace() throws Exception {
		final CountDownLatch dvakrat = new CountDownLatch(2);
		final BublinkaHodnoty b = new BublinkaHodnoty(dvakrat::countDown);
		try {
			b.tahne("x");
			Assert.assertTrue(b.casovacBezi());
			Assert.assertTrue("časovač po tažení zavolá překreslení", dvakrat.await(5, TimeUnit.SECONDS));
		} finally {
			b.zrus();
		}
		Assert.assertFalse(b.casovacBezi());
	}

	@Test
	public void bublinkaMaRozmeryPodleTextuAKresliBilyText() {
		final BublinkaHodnoty b = bublinka();
		b.tahne("Průhlednost 73 %");
		cas.addAndGet(BublinkaHodnoty.ZACATEK_MS);
		final BufferedImage obraz = new BufferedImage(400, 80, BufferedImage.TYPE_INT_ARGB);
		final Font font = new Font(Font.SERIF, Font.BOLD, 20);
		final Graphics2D g = obraz.createGraphics();
		final Rectangle r = b.kresli(g, font, 350, 40);
		final java.awt.FontMetrics fm = g.getFontMetrics(font);
		g.dispose();
		final int w = fm.stringWidth("Průhlednost 73 %") + 16;
		final int h = fm.getHeight() + 8;
		Assert.assertEquals(new Rectangle(350 - w, 40 - h / 2, w, h), r);
		Assert.assertEquals("pozadí", new java.awt.Color(20, 20, 20, 200).getRGB(), obraz.getRGB(r.x + r.width / 2, r.y + 2));
		int minX = Integer.MAX_VALUE, maxX = -1, minY = Integer.MAX_VALUE, maxY = -1;
		for (int y = r.y; y < r.y + r.height; y++) {
			for (int x = r.x; x < r.x + r.width; x++) {
				if (obraz.getRGB(x, y) == 0xFFFFFFFF) {
					minX = Math.min(minX, x);
					maxX = Math.max(maxX, x);
					minY = Math.min(minY, y);
					maxY = Math.max(maxY, y);
				}
			}
		}
		Assert.assertTrue("text je vidět", maxX >= 0);
		Assert.assertTrue("vlevo " + (minX - r.x), minX >= r.x + 8 && minX < r.x + 8 + fm.getMaxAdvance());
		Assert.assertTrue("vpravo " + (r.x + r.width - maxX), maxX <= r.x + r.width - 8);
		Assert.assertTrue("nahoře " + (minY - r.y), minY >= r.y + 4 && minY < r.y + 4 + fm.getAscent());
		Assert.assertTrue("dole " + (r.y + r.height - maxY), maxY <= r.y + 4 + fm.getAscent() + fm.getDescent());
	}
}
