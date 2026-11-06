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
		Assert.assertEquals(20, new OkoKesoiduIkona().getIconWidth());
	}
}
