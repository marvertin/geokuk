package cz.geokuk.plugins.challenge;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.OceanTheme;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.framework.Event0;
import cz.geokuk.framework.EventFirer;
import cz.geokuk.plugins.challenge.NastaveniChallengeTest.MapoveUloziste;

/** Dlouhé názvy šablon (GC kód a anglický název, ~58 znaků) v seznamu dialogu: zkrácené „…“ podle šířky seznamu, přípona zůstane, plný název je v tooltipu. */
public class PolozkaChallengeRendererTest {

	private static ChallengeModel model() {
		final ChallengeModel m = new ChallengeModel();
		m.inject(new EventFirer() {
			@Override
			public void fire(final Event0<?> event) {}
		});
		final MapoveUloziste u = new MapoveUloziste();
		m.setUloziste(() -> u);
		m.initAfterInject();
		return m;
	}

	private static JLabel polozka(final ChallengeModel m, final Challenge c, final int sirkaSeznamu) {
		final DefaultListModel<Challenge> lm = new DefaultListModel<>();
		lm.addElement(c);
		final JList<Challenge> l = new JList<>(lm);
		l.setSize(sirkaSeznamu, 300);
		return (JLabel) new PolozkaChallengeRenderer(() -> m).getListCellRendererComponent(l, c, 0, false, false);
	}

	@Test
	public void dlouhyNazevSablonySeVSirceSeznamuZkratiPriponaZustane() {
		final LookAndFeel puvodni = UIManager.getLookAndFeel();
		final MetalTheme puvodniTema = MetalLookAndFeel.getCurrentTheme();
		try {
			for (final MetalTheme tema : new MetalTheme[] { new DefaultMetalTheme(), new OceanTheme(), new cz.geokuk.core.lookandfeel.CharcoalTheme() }) {
				MetalLookAndFeel.setCurrentTheme(tema);
				UIManager.setLookAndFeel(new MetalLookAndFeel());
				final ChallengeModel m = model();
				for (final Challenge c : Sablony.vse()) {
					for (final int sirka : new int[] { 200, 290, 400 }) {
						final JLabel s = polozka(m, c, sirka);
						final BufferedImage obraz = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
						final Graphics2D g = obraz.createGraphics();
						final FontMetrics fm = g.getFontMetrics(s.getFont());
						g.dispose();
						Assert.assertTrue(tema.getName() + " " + sirka + ": " + s.getText(), fm.stringWidth(s.getText()) <= sirka - s.getInsets().left - s.getInsets().right || s.getText().length() < 8);
						Assert.assertTrue(s.getText(), s.getText().endsWith("(šablona)"));
						Assert.assertEquals("šířku seznamu určuje okno, ne název", 1, s.getPreferredSize().width);
						Assert.assertTrue("tooltip má plný název: " + s.getToolTipText(), s.getToolTipText().contains(c.nazev().replace("&", "&amp;")));
						if (fm.stringWidth(c.nazev() + (m.jeAktivni(c) ? "  (aktivní)" : "") + "  (šablona)") > sirka - s.getInsets().left - s.getInsets().right) {
							Assert.assertTrue(s.getText(), s.getText().contains("…"));
						} else {
							Assert.assertTrue("vejde se: " + s.getText(), s.getText().contains(c.nazev()));
						}
					}
				}
			}
		} catch (final Exception e) {
			throw new AssertionError(e);
		} finally {
			MetalLookAndFeel.setCurrentTheme(puvodniTema);
			try {
				UIManager.setLookAndFeel(puvodni);
			} catch (final Exception e) {
				// beze změny
			}
		}
	}

	@Test
	public void nazvySablonJsouDlouheAleVRozumnemRozsahADelsiNezSeznam() {
		int nejdelsi = 0;
		for (final Challenge c : Sablony.vse()) {
			nejdelsi = Math.max(nejdelsi, c.nazev().length());
		}
		Assert.assertTrue("nejdelší název šablony: " + nejdelsi, nejdelsi >= 40 && nejdelsi <= 80);
	}

	@Test
	public void aktivniAVlastniPolozkaMaPopisekAKratkyNazevBezZkraceni() {
		final ChallengeModel m = model();
		final Challenge c = m.ulozVlastni(new Challenge("Moje", Challenge.Typ.SEKTORY_360, Challenge.Stred.domov(), Challenge.OblastDef.zadna(), Challenge.ZdrojPravidel.PGC));
		m.aktivuj(c);
		final JLabel s = polozka(m, c, 290);
		Assert.assertEquals("Moje  (aktivní)", s.getText());
		Assert.assertFalse(s.getText().contains("…"));
	}
}
