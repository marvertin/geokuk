package cz.geokuk.core.coord;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JComponent;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/** První načítání mapy: jeden příznak pro logo i vrstvy nad mapou; vrstva se před koncem nekreslí, na obrazovce ani při rendrování. */
public class PrvniNacitaniMapyTest {

	@Before
	public void obnov() {
		PrvniNacitaniMapy.obnovProTesty();
	}

	@After
	public void obnovPo() {
		PrvniNacitaniMapy.obnovProTesty();
	}

	/** Vrstva nad mapou, která vyplní celou plochu červeně. */
	private static class Vrstva extends JSingleSlide0 {
		private static final long serialVersionUID = 1L;
		final AtomicInteger prekresleni = new AtomicInteger();

		Vrstva() {
			setSize(10, 10);
		}

		@Override
		protected boolean jeNadMapouKNacitani() {
			return true;
		}

		@Override
		protected void paintComponent(final Graphics g) {
			g.setColor(Color.RED);
			g.fillRect(0, 0, getWidth(), getHeight());
		}

		@Override
		public void repaint() {
			prekresleni.incrementAndGet();
		}

		BufferedImage naObrazovku() {
			final BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
			final Graphics g = img.createGraphics();
			try {
				paint(g);
			} finally {
				g.dispose();
			}
			return img;
		}
	}

	private static boolean nakreslena(final BufferedImage img) {
		return img.getRGB(5, 5) == Color.RED.getRGB();
	}

	@Test
	public void vrstvaSeNekresliPredNactenim() {
		Assert.assertFalse(nakreslena(new Vrstva().naObrazovku()));
	}

	@Test
	public void vrstvaSeKresliPoUkonceniAPrekresliSe() {
		final Vrstva v = new Vrstva();
		Assert.assertFalse(nakreslena(v.naObrazovku()));
		Assert.assertEquals(0, v.prekresleni.get());
		PrvniNacitaniMapy.ukonci();
		Assert.assertEquals("po konci se překreslí sama", 1, v.prekresleni.get());
		Assert.assertTrue(nakreslena(v.naObrazovku()));
	}

	@Test
	public void poPrvnimNacteniSeVrstvaUzNeskryje() {
		final Vrstva v = new Vrstva();
		PrvniNacitaniMapy.ukonci();
		Assert.assertTrue(nakreslena(v.naObrazovku()));
		// Další čekání na dlaždice po posunu nebo zoomu nemá jak příznak zapnout znovu.
		Assert.assertTrue(nakreslena(v.naObrazovku()));
		Assert.assertFalse(PrvniNacitaniMapy.probiha());
	}

	@Test
	public void poLimituSeVrstvaKresli() {
		final long[] cas = { 1_000 };
		PrvniNacitaniMapy.nastavHodiny(() -> cas[0]);
		final Vrstva v = new Vrstva();
		Assert.assertFalse(nakreslena(v.naObrazovku()));
		cas[0] = PrvniNacitaniMapy.LIMIT_MS;
		Assert.assertFalse("přesně na limitu ještě ne", nakreslena(v.naObrazovku()));
		cas[0] = PrvniNacitaniMapy.LIMIT_MS + 1;
		Assert.assertTrue(nakreslena(v.naObrazovku()));
		Assert.assertEquals(1, v.prekresleni.get());
	}

	@Test
	public void rendrovaniKresliVzdy() throws InterruptedException {
		final Vrstva v = new Vrstva();
		final BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		final Graphics g = img.createGraphics();
		try {
			v.render(g);
		} finally {
			g.dispose();
		}
		Assert.assertTrue("tisk a export obsahují body i při prvním načítání", nakreslena(img));
	}

	@Test
	public void obycejnaVrstvaSeNeskryva() {
		final JSingleSlide0 v = new JSingleSlide0() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(final Graphics g) {
				g.setColor(Color.RED);
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		};
		v.setSize(10, 10);
		final BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
		final Graphics g = img.createGraphics();
		try {
			v.paint(g);
		} finally {
			g.dispose();
		}
		Assert.assertTrue("ovládací prvky a měřítko se kreslí vždy", nakreslena(img));
	}

	@Test
	public void prahJeCtyricetProcentDlazdic() {
		Assert.assertFalse(PrvniNacitaniMapy.jeDostNactenych(0, 0));
		Assert.assertFalse(PrvniNacitaniMapy.jeDostNactenych(1, 3));
		Assert.assertFalse(PrvniNacitaniMapy.jeDostNactenych(3, 8));
		Assert.assertTrue(PrvniNacitaniMapy.jeDostNactenych(2, 5));
		Assert.assertTrue(PrvniNacitaniMapy.jeDostNactenych(4, 9));
	}

	@Test
	public void vrstvySeZobraziPoPrahuALogoJesteTrva() {
		final Vrstva v = new Vrstva();
		Assert.assertFalse(nakreslena(v.naObrazovku()));
		PrvniNacitaniMapy.zobrazVrstvy();
		Assert.assertEquals("vrstva se po prahu překreslí", 1, v.prekresleni.get());
		Assert.assertTrue(nakreslena(v.naObrazovku()));
		Assert.assertTrue("logo trvá do úplného načtení", PrvniNacitaniMapy.probiha());
		PrvniNacitaniMapy.ukonci();
		Assert.assertFalse(PrvniNacitaniMapy.probiha());
		Assert.assertTrue(nakreslena(v.naObrazovku()));
	}
}
