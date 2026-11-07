package cz.geokuk.plugins.kesoid;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.JToggleButton;

import org.junit.Assert;
import org.junit.Test;

/** Ikona „Zobrazovat kešoidy (F5)“: červená přeškrtnutí jen u skrytých kešoidů. */
public class OkoKesoiduIkonaTest {

	private static int cervenych(final boolean zobrazuji) {
		final JToggleButton b = new JToggleButton();
		b.setSelected(zobrazuji);
		b.setForeground(Color.BLACK);
		final OkoKesoiduIkona ikona = new OkoKesoiduIkona();
		final BufferedImage obraz = new BufferedImage(ikona.getIconWidth(), ikona.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		final java.awt.Graphics2D g = obraz.createGraphics();
		ikona.paintIcon(b, g, 0, 0);
		g.dispose();
		int n = 0;
		for (int y = 0; y < obraz.getHeight(); y++) {
			for (int x = 0; x < obraz.getWidth(); x++) {
				final int argb = obraz.getRGB(x, y);
				if ((argb >>> 24) > 128 && ((argb >> 16) & 0xff) > 150 && ((argb >> 8) & 0xff) < 90) {
					n++;
				}
			}
		}
		return n;
	}

	private static int nakreslenych(final boolean zobrazuji) {
		final JToggleButton b = new JToggleButton();
		b.setSelected(zobrazuji);
		b.setForeground(Color.BLACK);
		final OkoKesoiduIkona ikona = new OkoKesoiduIkona();
		final BufferedImage obraz = new BufferedImage(ikona.getIconWidth(), ikona.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		final java.awt.Graphics2D g = obraz.createGraphics();
		ikona.paintIcon(b, g, 0, 0);
		g.dispose();
		int n = 0;
		for (int y = 0; y < obraz.getHeight(); y++) {
			for (int x = 0; x < obraz.getWidth(); x++) {
				if ((obraz.getRGB(x, y) >>> 24) > 0) {
					n++;
				}
			}
		}
		return n;
	}

	@Test
	public void skrytaOkoMaCervenouCaruZobrazeneNe() {
		Assert.assertEquals("zobrazené oko je bez červené", 0, cervenych(true));
		Assert.assertTrue("skryté oko má červenou čáru: " + cervenych(false), cervenych(false) > 10);
	}

	@Test
	public void ikonaSeKresliVObouStavech() {
		Assert.assertTrue(nakreslenych(true) > 40);
		Assert.assertTrue(nakreslenych(false) > nakreslenych(true));
		Assert.assertEquals(16, new OkoKesoiduIkona().getIconWidth());
	}

	private static BufferedImage nakresli(final boolean zobrazuji, final boolean povoleno) {
		final JToggleButton b = new JToggleButton();
		b.setSelected(zobrazuji);
		b.setEnabled(povoleno);
		b.setForeground(Color.BLACK);
		final OkoKesoiduIkona ikona = new OkoKesoiduIkona();
		final BufferedImage obraz = new BufferedImage(ikona.getIconWidth(), ikona.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		final java.awt.Graphics2D g = obraz.createGraphics();
		ikona.paintIcon(b, g, 0, 0);
		g.dispose();
		return obraz;
	}

	private static int pixelu(final BufferedImage obraz) {
		int n = 0;
		for (int y = 0; y < obraz.getHeight(); y++) {
			for (int x = 0; x < obraz.getWidth(); x++) {
				if ((obraz.getRGB(x, y) >>> 24) > 128) {
					n++;
				}
			}
		}
		return n;
	}

	@Test
	public void ikonaMaRozmer20x20AKresliSeVeVsechStavech() {
		for (final boolean zobrazuji : new boolean[] { false, true }) {
			for (final boolean povoleno : new boolean[] { false, true }) {
				final BufferedImage o = nakresli(zobrazuji, povoleno);
				Assert.assertEquals(16, o.getWidth());
				Assert.assertEquals(16, o.getHeight());
				Assert.assertTrue("kreslí se: " + zobrazuji + povoleno, pixelu(o) > 20);
			}
		}
	}

	@Test
	public void zakazaneOkoJeSede() {
		final BufferedImage o = nakresli(true, false);
		for (int y = 0; y < o.getHeight(); y++) {
			for (int x = 0; x < o.getWidth(); x++) {
				final int argb = o.getRGB(x, y);
				if ((argb >>> 24) > 200) {
					Assert.assertEquals(128, (argb >> 16) & 0xff, 2);
				}
			}
		}
	}

	@Test
	public void cestaZeSvgSeNacteZMoveLineCurveClose() {
		final java.awt.geom.Rectangle2D r = OkoKesoiduIkona.cesta("M0 0L10 0L10 5Z").getBounds2D();
		Assert.assertEquals(10, r.getWidth(), 1e-9);
		Assert.assertEquals(5, r.getHeight(), 1e-9);
	}

	@Test
	public void skryteOkoJeZesvetleneAZobrazeneJeCelistve() {
		Assert.assertTrue("zobrazené oko má plné pixely", maxAlfa(nakresli(true, true), 0, 6) > 250);
		Assert.assertTrue("skryté oko je vlevo průsvitné", maxAlfa(nakresli(false, true), 0, 6) < 200);
	}

	/** Největší krytí pixelů v levém horním rohu ikony (mimo přeškrtnutí). */
	private static int maxAlfa(final BufferedImage o, final int odX, final int doX) {
		int m = 0;
		for (int y = 0; y < 8; y++) {
			for (int x = odX; x < doX; x++) {
				m = Math.max(m, o.getRGB(x, y) >>> 24);
			}
		}
		return m;
	}

	private static boolean jeZelena(final int rgb) {
		return Math.abs(((rgb >> 16) & 0xff) - 10) < 12 && Math.abs(((rgb >> 8) & 0xff) - 200) < 12 && Math.abs((rgb & 0xff) - 10) < 12;
	}

	private static BufferedImage nakresliSkryte(final JToggleButton b) {
		b.setSelected(false);
		final OkoKesoiduIkona ikona = new OkoKesoiduIkona();
		final BufferedImage obraz = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		final java.awt.Graphics2D g = obraz.createGraphics();
		ikona.paintIcon(b, g, 0, 0);
		g.dispose();
		return obraz;
	}

	private static int pixeluBarvy(final BufferedImage o, final java.util.function.IntPredicate p) {
		int n = 0;
		for (int y = 0; y < o.getHeight(); y++) {
			for (int x = 0; x < o.getWidth(); x++) {
				if ((o.getRGB(x, y) >>> 24) > 60 && p.test(o.getRGB(x, y))) {
					n++;
				}
			}
		}
		return n;
	}

	@Test
	public void naTmavemTlacitkuJeCaraSvetlejsi() {
		final JToggleButton tmave = new JToggleButton();
		tmave.setForeground(Color.WHITE);
		tmave.setBackground(new Color(40, 40, 40));
		Assert.assertTrue(pixeluBarvy(nakresliSkryte(tmave), rgb -> ((rgb >> 16) & 0xff) > 240 && ((rgb >> 8) & 0xff) > 100 && ((rgb >> 8) & 0xff) < 125) > 5);
		final JToggleButton svetle = new JToggleButton();
		svetle.setForeground(Color.BLACK);
		svetle.setBackground(new Color(238, 238, 238));
		Assert.assertEquals(0, pixeluBarvy(nakresliSkryte(svetle), rgb -> ((rgb >> 8) & 0xff) > 100 && ((rgb >> 16) & 0xff) > 240 && ((rgb >> 8) & 0xff) < 125));
	}

	@Test
	public void skryteOkoNemaPodkladCaryVBarveTlacitka() {
		final JToggleButton b = new JToggleButton();
		b.setForeground(Color.BLACK);
		b.setBackground(new Color(10, 200, 10));
		final BufferedImage o = nakresliSkryte(b);
		Assert.assertEquals("kolem čáry zůstává průhledno, ne barva tlačítka", 0, pixeluBarvy(o, rgb -> jeZelena(rgb)));
		Assert.assertEquals("a oko je jen částečně krycí", 0, pixeluBarvy(o, rgb -> ((rgb >> 16) & 0xff) < 100 && ((rgb >> 8) & 0xff) < 100 && (rgb & 0xff) < 100 && (rgb >>> 24) > 200));
		final int zbyva = pixeluBarvy(o, rgb -> ((rgb >> 16) & 0xff) < 100);
		Assert.assertTrue("pod čárou je oko vyříznuté, zbývá " + zbyva + " z " + pixelu(nakresli(true, true)), zbyva + 12 < pixelu(nakresli(true, true)));
		Assert.assertTrue("oko zůstalo vedle čáry", pixeluBarvy(nakresliSkryte(b), rgb -> (rgb >>> 24) > 60 && (rgb >>> 24) < 160) > 20);
	}

	@Test
	public void tlacitkoOkaMaVelikostTlacitkaTisk() throws Exception {
		for (final javax.swing.plaf.metal.MetalTheme tema : new javax.swing.plaf.metal.MetalTheme[] { new javax.swing.plaf.metal.OceanTheme(), new javax.swing.plaf.metal.DefaultMetalTheme() }) {
			final javax.swing.LookAndFeel puvodni = javax.swing.UIManager.getLookAndFeel();
			final javax.swing.plaf.metal.MetalTheme puvodniTema = javax.swing.plaf.metal.MetalLookAndFeel.getCurrentTheme();
			try {
				javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(tema);
				javax.swing.UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
				final javax.swing.JToolBar tb = new javax.swing.JToolBar();
				final javax.swing.Icon tisk16 = new javax.swing.ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB));
				final javax.swing.JButton tisk = tb.add(new javax.swing.AbstractAction("Tisk", tisk16) {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(final java.awt.event.ActionEvent e) {
						// jen rozměr
					}
				});
				final JToggleButton oko = new JToggleButton(new OkoKesoiduIkona());
				oko.setFocusable(false);
				tb.add(oko);
				Assert.assertEquals(tisk.getPreferredSize(), oko.getPreferredSize());
				Assert.assertEquals(tisk.getBorder().getBorderInsets(tisk), oko.getBorder().getBorderInsets(oko));
			} finally {
				javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(puvodniTema);
				javax.swing.UIManager.setLookAndFeel(puvodni);
			}
		}
	}
}
