package cz.geokuk.plugins.challenge;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.OceanTheme;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cz.geokuk.core.lookandfeel.CharcoalTheme;

/** Popup plánovače ve světlém a tmavém vzhledu a při zvětšení 100, 125 a 150 %: pozadí přes celou plochu, rámeček, nic se neořízne, upozornění je čitelné. */
public class JPopupPlanovaceVzhledTest {

	private LookAndFeel puvodniLaf;
	private MetalTheme puvodniTema;

	@Before
	public void uloz() {
		puvodniLaf = UIManager.getLookAndFeel();
		puvodniTema = MetalLookAndFeel.getCurrentTheme();
	}

	@After
	public void obnov() throws Exception {
		MetalLookAndFeel.setCurrentTheme(puvodniTema == null ? new OceanTheme() : puvodniTema);
		UIManager.setLookAndFeel(puvodniLaf);
	}

	private static void vzhled(final MetalTheme tema) throws Exception {
		MetalLookAndFeel.setCurrentTheme(tema);
		UIManager.setLookAndFeel(new MetalLookAndFeel());
	}

	private static JPopupPlanovace.Obsah obsah() {
		final JPopupPlanovace.Obsah o = new JPopupPlanovace.Obsah();
		final int[] chybi = new int[82];
		for (int i = 0; i < chybi.length; i++) {
			chybi[i] = 200 + i;
		}
		o.nastav(Sablony.cesko360(), TextyPopupuPlanovaceTest.vysledek(chybi, new int[] { 200, 201, 202 }, new double[][] { { 213, 2800, 4000, 5000, 6000, 7000 }, { 214, 3400 } }),
				StavPredpoctu.NEBEZI, false);
		o.setSize(o.getPreferredSize());
		o.doLayout();
		rozloz(o);
		return o;
	}

	private static void rozloz(final Container c) {
		c.doLayout();
		for (final Component d : c.getComponents()) {
			if (d instanceof Container) {
				rozloz((Container) d);
			}
		}
	}

	private static BufferedImage vykresli(final JComponent c, final double meritko) {
		final BufferedImage img = new BufferedImage((int) Math.ceil(c.getWidth() * meritko), (int) Math.ceil(c.getHeight() * meritko), BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = img.createGraphics();
		try {
			g.scale(meritko, meritko);
			c.paint(g);
		} finally {
			g.dispose();
		}
		return img;
	}

	/** Popisky a tlačítka mají aspoň tolik místa, kolik chtějí: text se nezkrátí na „…“. */
	private static void nicNeorizne(final Container c, final String kde) {
		for (final Component d : c.getComponents()) {
			final Rectangle b = d.getBounds();
			if (!d.isVisible() || b.isEmpty()) {
				continue;
			}
			Assert.assertTrue(kde + ": " + d.getClass().getSimpleName() + " " + b + " mimo " + c.getSize(), new Rectangle(c.getSize()).contains(b));
			if (d instanceof JLabel || d instanceof AbstractButton) {
				Assert.assertTrue(kde + ": " + ((d instanceof JLabel) ? ((JLabel) d).getText() : ((AbstractButton) d).getText()) + " " + b.width + " < " + d.getPreferredSize().width,
						b.width >= d.getPreferredSize().width);
			}
			if (d instanceof Container && !(d instanceof JScrollPane)) {
				nicNeorizne((Container) d, kde);
			}
		}
	}

	private static double jas(final Color c) {
		return (c.getRed() * 299 + c.getGreen() * 587 + c.getBlue() * 114) / 1000.0;
	}

	private static void zkontroluj(final String kde, final boolean tmavy) {
		final JPopupPlanovace.Obsah o = obsah();
		nicNeorizne(o, kde);
		final Color pozadi = o.getBackground();
		Assert.assertEquals(kde + ": pozadí podle vzhledu", UIManager.getColor("Panel.background").getRGB(), pozadi.getRGB());
		Assert.assertEquals(kde + ": tmavé pozadí", tmavy, JPopupPlanovace.Obsah.jeTmave(pozadi));
		Assert.assertFalse(kde + ": procenta jsou ve shrnutí, ne přes výplň", o.postup.isStringPainted());
		final double kontrast = Math.abs(jas(o.nejiste.getForeground()) - jas(pozadi));
		Assert.assertTrue(kde + ": upozornění čitelné, rozdíl jasu " + kontrast, kontrast > 90);
		for (final double m : new double[] { 1.0, 1.25, 1.5 }) {
			final BufferedImage img = vykresli(o, m);
			final String k = kde + " " + Math.round(m * 100) + " %";
			// Uvnitř rámečku u všech čtyř rohů je pozadí: plocha je vyplněná celá i při zvětšení.
			final int okraj = (int) Math.ceil(4 * m);
			for (final int[] bod : new int[][] { { okraj, okraj }, { img.getWidth() - 1 - okraj, okraj }, { okraj, img.getHeight() - 1 - okraj }, { img.getWidth() - 1 - okraj, img.getHeight() - 1 - okraj } }) {
				Assert.assertEquals(k + ": pozadí u " + bod[0] + "," + bod[1], pozadi.getRGB(), img.getRGB(bod[0], bod[1]));
			}
			Assert.assertNotEquals(k + ": rámeček u okraje", pozadi.getRGB(), img.getRGB(img.getWidth() / 2, 0));
			// Shrnutí je opravdu nakreslené: v jeho obdélníku jsou body výrazně jiné než pozadí.
			final Rectangle s = SwingUtilities.convertRectangle(o.souhrn.getParent(), o.souhrn.getBounds(), o);
			int textu = 0;
			for (int x = (int) (s.x * m); x < (int) ((s.x + s.width) * m); x++) {
				for (int y = (int) (s.y * m); y < (int) ((s.y + s.height) * m); y++) {
					if (Math.abs(jas(new Color(img.getRGB(x, y))) - jas(pozadi)) > 60) {
						textu++;
					}
				}
			}
			Assert.assertTrue(k + ": text shrnutí je vidět (" + textu + " bodů)", textu > 40 * m * m);
			// Ukazatel postupu (77 %): výplň se zřetelně liší od dráhy a přes ni není text.
			final Rectangle u = SwingUtilities.convertRectangle(o.postup.getParent(), o.postup.getBounds(), o);
			final int stredY = (int) ((u.y + u.height / 2) * m);
			final Color vypln = new Color(img.getRGB((int) ((u.x + u.width * 0.3) * m), stredY));
			final Color draha = new Color(img.getRGB((int) ((u.x + u.width * 0.92) * m), stredY));
			Assert.assertTrue(k + ": výplň " + vypln + " × dráha " + draha, Math.abs(jas(vypln) - jas(draha)) > 60);
		}
	}

	@Test
	public void svetlyVzhled() throws Exception {
		vzhled(new OceanTheme());
		zkontroluj("Ocean", false);
		vzhled(new DefaultMetalTheme());
		zkontroluj("Steel", false);
	}

	@Test
	public void tmavyVzhled() throws Exception {
		vzhled(new CharcoalTheme());
		zkontroluj("Charcoal", true);
	}

	/** Po přepnutí vzhledu za běhu (skin v menu) se přizpůsobí i už vytvořený popup. */
	@Test
	public void prepnutiVzhleduZaBehu() throws Exception {
		vzhled(new OceanTheme());
		final JPopupPlanovace.Obsah o = obsah();
		final Color svetla = o.nejiste.getForeground();
		vzhled(new CharcoalTheme());
		SwingUtilities.updateComponentTreeUI(o);
		Assert.assertTrue(JPopupPlanovace.Obsah.jeTmave(o.getBackground()));
		Assert.assertNotEquals("barva upozornění podle pozadí", svetla, o.nejiste.getForeground());
	}
}
