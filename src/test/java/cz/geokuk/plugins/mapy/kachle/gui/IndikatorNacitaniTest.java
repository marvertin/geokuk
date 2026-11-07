package cz.geokuk.plugins.mapy.kachle.gui;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import cz.geokuk.core.coord.PrvniNacitaniMapy;

/** Kolečko načítání se ukáže až po prodlevě, plynule se objeví a po poslední dlaždici zmizí. */
public class IndikatorNacitaniTest {

	@After
	public void obnovPo() {
		PrvniNacitaniMapy.obnovProTesty();
	}

	@Test
	public void kratkeCekaniKolecko_neukaze() {
		final IndikatorNacitani i = new IndikatorNacitani();
		Assert.assertFalse(i.aktualizuj(1000, 5));
		Assert.assertFalse(i.aktualizuj(1000 + IndikatorNacitani.PRODLEVA_MS - 1, 3));
		Assert.assertFalse(i.aktualizuj(1100, 0));
		Assert.assertFalse(i.beziAnimace());
		Assert.assertEquals(0, i.getPruhlednost(), 0);
	}

	@Test
	public void dlouheCekaniKoleckoUkaze() {
		final IndikatorNacitani i = new IndikatorNacitani();
		i.aktualizuj(1000, 5);
		Assert.assertTrue(i.aktualizuj(1000 + IndikatorNacitani.PRODLEVA_MS, 2));
		Assert.assertEquals(1, i.getPruhlednost(), 0);
		Assert.assertFalse("po doručení vše zmizí", i.aktualizuj(5000, 0));
		Assert.assertFalse("nové čekání začíná prodlevou znovu", i.aktualizuj(5100, 4));
	}

	@Test
	public void poPosledniDlazdiciMiziPlynule() {
		final IndikatorNacitani i = new IndikatorNacitani();
		i.aktualizuj(1000, 5);
		i.aktualizuj(1300, 2);
		Assert.assertTrue("hned po doručení ještě mizí", i.aktualizuj(1340, 0));
		final double po40 = i.getPruhlednost();
		Assert.assertTrue(po40 > 0 && po40 < 1);
		Assert.assertTrue(i.beziAnimace());
		Assert.assertTrue(i.aktualizuj(1380, 0));
		Assert.assertTrue("průhlednost klesá", i.getPruhlednost() < po40);
		Assert.assertFalse("po době zmizení je pryč", i.aktualizuj(1340 + IndikatorNacitani.ZMIZENI_MS, 0));
		Assert.assertFalse("a není co animovat", i.beziAnimace());
	}

	@Test
	public void objeveniJePlynuleAMonotonni() {
		final IndikatorNacitani i = new IndikatorNacitani();
		double predtim = 0;
		for (long t = 0; t <= 1000; t += 40) {
			i.aktualizuj(t, 3);
			Assert.assertTrue("průhlednost neklesá při čekání: " + t, i.getPruhlednost() >= predtim);
			if (t < IndikatorNacitani.PRODLEVA_MS) {
				Assert.assertEquals("před prodlevou je kolečko neviditelné: " + t, 0, i.getPruhlednost(), 0);
			}
			predtim = i.getPruhlednost();
		}
		Assert.assertEquals(1, predtim, 0);
		final IndikatorNacitani j = new IndikatorNacitani();
		double nejvetsiSkok = 0;
		double p = 0;
		for (long t = 0; t <= 600; t += 40) {
			j.aktualizuj(t, 3);
			nejvetsiSkok = Math.max(nejvetsiSkok, j.getPruhlednost() - p);
			p = j.getPruhlednost();
		}
		Assert.assertTrue("objevení není skokem při tiku po 40 ms", nejvetsiSkok < 0.5);
	}

	@Test
	public void kolecko_jeVpravoNahoreVOblastiMapy() {
		for (final int sirka : new int[] { 320, 800, 1920 }) {
			final Rectangle r = IndikatorNacitani.ramecek(sirka);
			Assert.assertEquals(sirka - IndikatorNacitani.ODSTUP, r.x + r.width);
			Assert.assertEquals(IndikatorNacitani.ODSTUP, r.y);
			Assert.assertEquals(IndikatorNacitani.PRUMER, r.width);
			Assert.assertEquals(IndikatorNacitani.PRUMER, r.height);
		}
		Assert.assertTrue("nenápadná velikost", IndikatorNacitani.PRUMER >= 20 && IndikatorNacitani.PRUMER <= 28);
	}

	@Test
	public void uhelSePlynuleOtaciAOpakuje() {
		Assert.assertEquals(0, IndikatorNacitani.uhel(0), 0);
		Assert.assertEquals(0, IndikatorNacitani.uhel(3 * IndikatorNacitani.PERIODA_MS), 0);
		Assert.assertEquals(180, IndikatorNacitani.uhel(IndikatorNacitani.PERIODA_MS / 2), 1e-9);
		double predtim = -1;
		for (long t = 0; t < IndikatorNacitani.PERIODA_MS; t += 25) {
			final double u = IndikatorNacitani.uhel(t);
			Assert.assertTrue(u > predtim && u < 360);
			Assert.assertTrue("krok otáčení je malý", u - predtim < 20 || predtim < 0);
			predtim = u;
		}
	}

	@Test
	public void slideKresliJenPoProdleveAPoDoruceniPostupneZmizi() throws Exception {
		Assume.assumeFalse("bez displeje", GraphicsEnvironment.isHeadless());
		// Při prvním načítání mapy kreslí čekání logo, kolečko se až pak.
		PrvniNacitaniMapy.ukonci();
		try {
			final AtomicInteger cekajici = new AtomicInteger(3);
			final AtomicLong cas = new AtomicLong(10_000);
			final JIndikatorNacitaniSlide slide = new JIndikatorNacitaniSlide(cekajici::get, cas::get);
			slide.tik();
			Assert.assertFalse(slide.isKresli());
			cas.addAndGet(IndikatorNacitani.PRODLEVA_MS - 1);
			slide.tik();
			Assert.assertFalse("před uplynutím prodlevy ne", slide.isKresli());
			cas.addAndGet(1);
			slide.tik();
			Assert.assertTrue(slide.isKresli());
			cas.addAndGet(IndikatorNacitani.OBJEVENI_MS);
			slide.tik();
			cekajici.set(0);
			cas.addAndGet(40);
			slide.tik();
			Assert.assertTrue("po doručení poslední dlaždice ještě chvíli mizí", slide.isKresli());
			cas.addAndGet(IndikatorNacitani.ZMIZENI_MS);
			slide.tik();
			Assert.assertFalse(slide.isKresli());
		} finally {
			PrvniNacitaniMapy.obnovProTesty();
		}
	}

	@Test
	public void priLoguPrvnihoNacitaniSeKoleckoNeukaze() throws Exception {
		Assume.assumeFalse("bez displeje", GraphicsEnvironment.isHeadless());
		PrvniNacitaniMapy.obnovProTesty();
		Assert.assertTrue("první načítání probíhá", PrvniNacitaniMapy.probiha());
		try {
			final AtomicLong cas = new AtomicLong(10_000);
			final JIndikatorNacitaniSlide slide = new JIndikatorNacitaniSlide(() -> 3, cas::get);
			slide.tik();
			cas.addAndGet(IndikatorNacitani.PRODLEVA_MS + 50);
			slide.tik();
			Assert.assertFalse("logo má přednost před kolečkem", slide.isKresli());
			PrvniNacitaniMapy.ukonci();
			cas.addAndGet(40);
			slide.tik();
			Assert.assertTrue("po skončení prvního načítání se kolečko ukáže", slide.isKresli());
		} finally {
			PrvniNacitaniMapy.obnovProTesty();
		}
	}

	@Test
	public void casovacSePoDokonceniCekaniAMizeniZastavi() throws Exception {
		Assume.assumeFalse("bez displeje", GraphicsEnvironment.isHeadless());
		PrvniNacitaniMapy.ukonci();
		try {
			final AtomicInteger cekajici = new AtomicInteger(2);
			final AtomicLong cas = new AtomicLong(10_000);
			final JIndikatorNacitaniSlide slide = new JIndikatorNacitaniSlide(cekajici::get, cas::get);
			Assert.assertFalse(slide.bezi());
			slide.probud();
			Assert.assertTrue("po nové zakázce časovač běží", slide.bezi());
			slide.tik();
			cas.addAndGet(IndikatorNacitani.PRODLEVA_MS);
			slide.tik();
			Assert.assertTrue("dokud se čeká, běží dál", slide.bezi());
			cekajici.set(0);
			cas.addAndGet(40);
			slide.tik();
			Assert.assertTrue("při mizení ještě běží", slide.bezi());
			cas.addAndGet(IndikatorNacitani.ZMIZENI_MS);
			slide.tik();
			Assert.assertFalse("po zmizení se zastaví, ať netiká věčně", slide.bezi());
		} finally {
			PrvniNacitaniMapy.obnovProTesty();
		}
	}

	@Test
	public void cekaniZacinajiciVNulovemCaseMaProdlevu() {
		final IndikatorNacitani i = new IndikatorNacitani();
		Assert.assertFalse(i.aktualizuj(0, 4));
		Assert.assertTrue("začátek čekání se nepřepisuje", i.aktualizuj(IndikatorNacitani.PRODLEVA_MS, 4));
	}

	@Test
	public void slideNezachytavaMys() {
		final JIndikatorNacitaniSlide slide = new JIndikatorNacitaniSlide(() -> 0);
		Assert.assertEquals(0, slide.getMouseListeners().length);
		Assert.assertEquals(0, slide.getMouseMotionListeners().length);
		Assert.assertEquals(0, slide.getMouseWheelListeners().length);
		Assert.assertFalse(slide.isOpaque());
	}

	@Test
	public void koleckoKresliJenKolemSvehoCtverceAPodlePruhlednosti() {
		final Rectangle r = IndikatorNacitani.ramecek(200);
		final BufferedImage plne = new BufferedImage(200, 60, BufferedImage.TYPE_INT_ARGB);
		JIndikatorNacitaniSlide.kresliKolecko(plne.createGraphics(), r, 1, 30);
		final BufferedImage polo = new BufferedImage(200, 60, BufferedImage.TYPE_INT_ARGB);
		JIndikatorNacitaniSlide.kresliKolecko(polo.createGraphics(), r, 0.5, 30);
		final BufferedImage nic = new BufferedImage(200, 60, BufferedImage.TYPE_INT_ARGB);
		JIndikatorNacitaniSlide.kresliKolecko(nic.createGraphics(), r, 0, 30);
		int uvnitr = 0;
		int nejvicPlne = 0;
		int nejvicPolo = 0;
		for (int y = 0; y < 60; y++) {
			for (int x = 0; x < 200; x++) {
				final int a = plne.getRGB(x, y) >>> 24;
				final boolean vOkoli = x >= r.x - 6 && x < r.x + r.width + 6 && y >= r.y - 6 && y < r.y + r.height + 6;
				if (!vOkoli) {
					Assert.assertEquals("mimo kolečko se nekreslí: " + x + "," + y, 0, a);
				} else if (a > 0) {
					uvnitr++;
				}
				nejvicPlne = Math.max(nejvicPlne, a);
				nejvicPolo = Math.max(nejvicPolo, polo.getRGB(x, y) >>> 24);
				Assert.assertEquals("při nulové průhlednosti nic", 0, nic.getRGB(x, y) >>> 24);
			}
		}
		Assert.assertTrue("kolečko je nakreslené", uvnitr > 100);
		Assert.assertTrue("poloviční průhlednost je slabší", nejvicPolo < nejvicPlne);
	}
}
