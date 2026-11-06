package cz.geokuk.util.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.Assert;
import org.junit.Test;

/** Zkracovaný popisek: „…“ podle šířky, tooltip s plným textem jen při zkrácení, nejvyšší požadovaná šířka, kreslení jen v mezích. */
public class JZkracenyLabelTest {

	private static final String DLOUHY = "Challenge GC4PCQB – Challenge - 360 degrees of the Czech Republic";

	private static JZkracenyLabel label(final int nejvetsi, final int sirka) {
		final JZkracenyLabel l = new JZkracenyLabel(nejvetsi);
		l.setPlnyText(DLOUHY);
		l.setSize(sirka, 20);
		return l;
	}

	@Test
	public void pozadovanaSirkaJeNejvyseNejvetsi() {
		Assert.assertEquals(200, label(200, 100).getPreferredSize().width);
		Assert.assertTrue("krátký text je užší než strop", new JZkracenyLabel(500) {
			private static final long serialVersionUID = 1L;
			{
				setPlnyText("Krátký");
			}
		}.getPreferredSize().width < 100);
	}

	@Test
	public void bezZkraceniMaCelyTextABezTooltipu() {
		final JZkracenyLabel l = label(1000, 1000);
		Assert.assertEquals(DLOUHY, l.zobrazeny());
		Assert.assertNull(l.getToolTipText(null));
	}

	@Test
	public void spoZkraceniMa3TeckyATooltipSPlnymNazvem() {
		final JZkracenyLabel l = label(1000, 200);
		Assert.assertTrue(l.zobrazeny(), l.zobrazeny().endsWith("…"));
		Assert.assertTrue(DLOUHY.startsWith(l.zobrazeny().substring(0, l.zobrazeny().length() - 1)));
		Assert.assertEquals(DLOUHY, l.getToolTipText(null));
		Assert.assertEquals("getText zůstává plný (testy a čtečky)", DLOUHY, l.getText());
		Assert.assertTrue(l.getFontMetrics(l.getFont()).stringWidth(l.zobrazeny()) <= 200);
	}

	@Test
	public void nekreslíMimoSvouSirku() {
		final JZkracenyLabel l = label(1000, 150);
		l.setForeground(Color.BLACK);
		final BufferedImage obraz = new BufferedImage(400, 20, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = obraz.createGraphics();
		l.paint(g);
		g.dispose();
		boolean vlevo = false;
		for (int y = 0; y < 20; y++) {
			for (int x = 0; x < 400; x++) {
				final boolean nakresleno = (obraz.getRGB(x, y) >>> 24) != 0;
				if (x >= 150) {
					Assert.assertFalse("pixel mimo šířku popisku: " + x, nakresleno);
				} else if (nakresleno) {
					vlevo = true;
				}
			}
		}
		Assert.assertTrue("text se nakreslil", vlevo);
	}
}
