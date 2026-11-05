package cz.geokuk.plugins.mrizky;

import java.awt.*;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.util.gui.CornerLayoutManager;
import cz.geokuk.util.gui.ERoh;

/** Měřítko na obrazovce je uprostřed dole a nezasahuje pod detail v levém dolním rohu. */
public class JMeritkoSlideTest {

	private static JMeritko najdi(final Container c) {
		for (final Component k : c.getComponents()) {
			if (k instanceof JMeritko) {
				return (JMeritko) k;
			}
			if (k instanceof Container) {
				final JMeritko m = najdi((Container) k);
				if (m != null) {
					return m;
				}
			}
		}
		return null;
	}

	@Test
	public void meritkoJeUprostredMimoDetailVRohu() {
		for (final int sirka : new int[] { 800, 1000, 1400, 1900, 2560 }) {
			for (final int zoom : new int[] { 6, 11, 13, 16, 18 }) {
				final JPanel srohama = new JPanel(new CornerLayoutManager());
				final JPanel detail = new JPanel();
				detail.setMinimumSize(new Dimension(100, 100));
				detail.setPreferredSize(new Dimension(200, 200));
				detail.setMaximumSize(new Dimension(300, 300));
				srohama.add(detail, ERoh.JZ);
				final JMeritkoSlide slide = new JMeritkoSlide();
				srohama.add(slide);
				final JMeritko m = najdi(slide);
				m.setMaximalniSirkaMeritka(JMeritkoSlide.maximalniSirka(sirka));
				m.setPixluNaMetr(256 * Math.pow(2, zoom) / (40_075_016.686 * Math.cos(Math.toRadians(50))));
				srohama.setSize(sirka, 900);
				srohama.doLayout();
				slide.doLayout();

				final int levyOkraj = SwingUtilities.convertPoint(m, (m.getWidth() - m.getPreferredSize().width) / 2, 0, srohama).x;
				final int pravyOkraj = levyOkraj + m.getPreferredSize().width;
				final String kde = sirka + " px z" + zoom + ": " + levyOkraj + "–" + pravyOkraj + ", detail do " + detail.getWidth();
				Assert.assertEquals(kde, sirka, levyOkraj + pravyOkraj, 1);
				Assert.assertTrue(kde, levyOkraj > detail.getX() + detail.getWidth());
				Assert.assertEquals(kde, 900, slide.getY() + m.getY() + m.getHeight());
			}
		}
	}

	@Test
	public void meritkoSeVejdeDoVyhrazeneVysky() {
		Assert.assertTrue(new JMeritkoSlide().zabranaVyska() + " > " + cz.geokuk.framework.RozmisteniPrekryvu.VYSKA_MERITKA,
				new JMeritkoSlide().zabranaVyska() <= cz.geokuk.framework.RozmisteniPrekryvu.VYSKA_MERITKA);
	}
}
