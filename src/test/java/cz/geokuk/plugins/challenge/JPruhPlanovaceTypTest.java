package cz.geokuk.plugins.challenge;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;

import javax.swing.JLabel;

import org.junit.Assert;
import org.junit.Test;

/** Popisek typu v pruhu se vejde do svého místa celý (Kalendář menším písmem), kratší typy mají výchozí písmo. */
public class JPruhPlanovaceTypTest {

	private static FontMetrics metriky(final Font f) {
		return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB).getGraphics().getFontMetrics(f);
	}

	@Test
	public void kazdyTypSeVejdeCely() {
		final Font zaklad = new JLabel().getFont().deriveFont(Font.BOLD, 12f);
		final int sirka = JPruhPlanovace.rozloz(267)[JPruhPlanovace.TYP].width;
		for (final Challenge.Typ t : Challenge.Typ.values()) {
			final String text = StitkyPruhu.typ(t);
			final Font f = JPruhPlanovace.pismoNaSirku(zaklad, text, sirka, JPruhPlanovaceTypTest::metriky);
			Assert.assertTrue(text + ": " + metriky(f).stringWidth(text) + " > " + sirka, metriky(f).stringWidth(text) <= sirka);
			Assert.assertTrue(text + ": písmo aspoň 9 bodů", f.getSize2D() >= 9f);
		}
		Assert.assertEquals("360° výchozím písmem", 12f, JPruhPlanovace.pismoNaSirku(zaklad, "360°", sirka, JPruhPlanovaceTypTest::metriky).getSize2D(), 0.01);
		Assert.assertTrue("Kalendář menším", JPruhPlanovace.pismoNaSirku(zaklad, "Kalendář", sirka, JPruhPlanovaceTypTest::metriky).getSize2D() < 12f);
		Assert.assertEquals("nejmenší 9 bodů", 9f, JPruhPlanovace.pismoNaSirku(zaklad, "Velmi dlouhý typ challenge", sirka, JPruhPlanovaceTypTest::metriky).getSize2D(), 0.01);
	}

	/** Skutečný pruh s Kalendářem: popisek typu se po rozložení vejde do svého obdélníku. */
	@Test
	public void pruhSKalendaremUkazePopisekCely() throws Exception {
		final ChallengeModel model = new ChallengeModel();
		final JPruhPlanovace pruh = new JPruhPlanovace();
		model.inject(new cz.geokuk.framework.EventFirer() {
			@Override
			public void fire(final cz.geokuk.framework.Event0<?> event) {
				pruh.onEvent((ChallengeZmenenyEvent) event);
			}
		});
		final NastaveniChallengeTest.MapoveUloziste u = new NastaveniChallengeTest.MapoveUloziste();
		model.setUloziste(() -> u);
		model.initAfterInject();
		model.setKalendarVUi(true);
		pruh.inject(model);
		pruh.setSize(1920, 1000);
		final Challenge c = model.ulozVlastni(new Challenge("Kalendář test", Challenge.Typ.KALENDAR, Challenge.Stred.domov(), Challenge.OblastDef.zadna(), Challenge.ZdrojPravidel.PGC));
		model.aktivuj(c);
		model.setZobrazena(c);
		model.setZapnuto(true);
		javax.swing.SwingUtilities.invokeAndWait(() -> {});
		pruh.doLayout();
		JLabel typ = null;
		for (final java.awt.Component k : pruh.getComponents()) {
			if (k instanceof JLabel) {
				typ = (JLabel) k;
			}
		}
		Assert.assertNotNull(typ);
		Assert.assertEquals("Kalendář", typ.getText());
		Assert.assertTrue(typ.getFont() + ": " + typ.getFontMetrics(typ.getFont()).stringWidth("Kalendář") + " > " + typ.getWidth(),
				typ.getFontMetrics(typ.getFont()).stringWidth("Kalendář") <= typ.getWidth());
		Assert.assertEquals("Typ challenge: Kalendář", typ.getToolTipText());
	}
}
