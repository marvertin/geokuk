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
		final int sirka = JPruhPlanovace.rozloz(cz.geokuk.framework.RozmisteniPrekryvu.PRUH_PLNY)[JPruhPlanovace.VYBER].width - 8;
		for (final Challenge.Typ t : Challenge.Typ.values()) {
			final String text = StitkyPruhu.typ(t) + " ▾";
			final Font f = JPruhPlanovace.pismoNaSirku(zaklad, text, sirka, JPruhPlanovaceTypTest::metriky);
			Assert.assertTrue(text + ": " + metriky(f).stringWidth(text) + " > " + sirka, metriky(f).stringWidth(text) <= sirka);
			Assert.assertTrue(text + ": písmo aspoň 9 bodů", f.getSize2D() >= 9f);
		}
		Assert.assertEquals("360° výchozím písmem", 12f, JPruhPlanovace.pismoNaSirku(zaklad, "360° ▾", sirka, JPruhPlanovaceTypTest::metriky).getSize2D(), 0.01);
		Assert.assertTrue("Kalendář menším", JPruhPlanovace.pismoNaSirku(zaklad, "Kalendář ▾", sirka, JPruhPlanovaceTypTest::metriky).getSize2D() < 12f);
		Assert.assertEquals("nejmenší 9 bodů", 9f, JPruhPlanovace.pismoNaSirku(zaklad, "Velmi dlouhý typ challenge", sirka, JPruhPlanovaceTypTest::metriky).getSize2D(), 0.01);
	}

	/** Skutečná lišta s Kalendářem: typ v tlačítku výběru se po rozložení vejde celý, název challenge je jen v tooltipu. */
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
		final javax.swing.AbstractButton vyber = (javax.swing.AbstractButton) pruh.getComponent(JPruhPlanovace.VYBER);
		Assert.assertEquals("Kalendář ▾", vyber.getText());
		final int text = vyber.getFontMetrics(vyber.getFont()).stringWidth(vyber.getText());
		Assert.assertTrue(vyber.getFont() + ": " + text + " > " + vyber.getWidth(), text <= vyber.getWidth() - 8);
		Assert.assertEquals("Challenge: Kalendář test – vybrat jinou", vyber.getToolTipText());
	}
}
