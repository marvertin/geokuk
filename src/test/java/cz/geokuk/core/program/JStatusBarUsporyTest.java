package cz.geokuk.core.program;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTheme;
import javax.swing.plaf.metal.OceanTheme;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 * Stavový řádek při 100 %, 125 % a 150 % zvětšení obrazovky na FHD (okno v logických bodech 1920, 1536, 1280) ve třech tématech: vejde se do jednoho řádku a nic nepřesahuje okraj; potřebuje
 * displej.
 */
public class JStatusBarUsporyTest {

	private static final int[] SIRKY = { 1920, 1536, 1280 };

	private static MetalTheme[] temata() {
		return new MetalTheme[] { new DefaultMetalTheme(), new OceanTheme(), new cz.geokuk.core.lookandfeel.CharcoalTheme() };
	}

	private static JStatusBar lista(final int sirka) {
		final JStatusBar sb = new JStatusBar();
		final JPanel rodic = new JPanel(new BorderLayout());
		rodic.setSize(sirka, 100);
		rodic.add(sb, BorderLayout.SOUTH);
		for (int i = 0; i < 3; i++) {
			rodic.doLayout();
			sb.doLayout();
		}
		return sb;
	}

	private static int radku(final JStatusBar sb) {
		final TreeSet<Integer> ys = new TreeSet<>();
		for (final Component c : sb.getComponents()) {
			if (c.isVisible() && c.getWidth() > 0) {
				ys.add(c.getY());
			}
		}
		return ys.size();
	}

	private interface Kontrola {
		void proved(String tema, int sirka, JStatusBar sb) throws Exception;
	}

	private static void vsudy(final Kontrola k) throws Exception {
		Assume.assumeFalse("bez displeje", java.awt.GraphicsEnvironment.isHeadless());
		final LookAndFeel puvodni = UIManager.getLookAndFeel();
		final MetalTheme puvodniTema = MetalLookAndFeel.getCurrentTheme();
		try {
			for (final MetalTheme tema : temata()) {
				MetalLookAndFeel.setCurrentTheme(tema);
				UIManager.setLookAndFeel(new MetalLookAndFeel());
				for (final int sirka : SIRKY) {
					SwingUtilities.invokeAndWait(() -> {
						try {
							k.proved(tema.getName(), sirka, lista(sirka));
						} catch (final Exception e) {
							throw new RuntimeException(e);
						}
					});
				}
			}
		} finally {
			MetalLookAndFeel.setCurrentTheme(puvodniTema);
			UIManager.setLookAndFeel(puvodni);
		}
	}

	@Test
	public void vJednomRadkuPri100a125a150Procentech() throws Exception {
		vsudy((tema, sirka, sb) -> Assert.assertEquals(tema + " " + sirka + " stupeň " + sb.getStupenUspor(), 1, radku(sb)));
	}

	/** Stupeň úspor roste s užším oknem; které přesně okno potřebuje který stupeň, závisí na písmu platformy. */
	@Test
	public void stupenUsporRosteSUzsimOknem() throws Exception {
		final java.util.Map<String, int[]> stupne = new java.util.HashMap<>();
		vsudy((tema, sirka, sb) -> {
			final int[] s = stupne.computeIfAbsent(tema, t -> new int[SIRKY.length]);
			for (int i = 0; i < SIRKY.length; i++) {
				if (SIRKY[i] == sirka) {
					s[i] = sb.getStupenUspor();
				}
			}
		});
		for (final java.util.Map.Entry<String, int[]> e : stupne.entrySet()) {
			final int[] s = e.getValue();
			for (int i = 1; i < s.length; i++) {
				Assert.assertTrue(e.getKey() + " " + java.util.Arrays.toString(SIRKY) + " -> " + java.util.Arrays.toString(s), SIRKY[i] >= SIRKY[i - 1] ? s[i] <= s[i - 1] : s[i] >= s[i - 1]);
			}
		}
	}

	@Test
	public void nicNepresahujeOkrajListyAniSeNekryje() throws Exception {
		vsudy((tema, sirka, sb) -> {
			final BufferedImage obraz = new BufferedImage(sirka + 300, sb.getHeight(), BufferedImage.TYPE_INT_ARGB);
			final Graphics2D g = obraz.createGraphics();
			g.setColor(Color.MAGENTA);
			g.fillRect(0, 0, obraz.getWidth(), obraz.getHeight());
			sb.paint(g);
			g.dispose();
			for (int y = 0; y < obraz.getHeight(); y++) {
				for (int x = sirka; x < obraz.getWidth(); x++) {
					Assert.assertEquals(tema + " " + sirka + ": přesah pixelu " + x + "," + y, Color.MAGENTA.getRGB(), obraz.getRGB(x, y));
				}
			}
			final Rectangle lista = new Rectangle(0, 0, sirka, sb.getHeight());
			final java.util.List<Rectangle> viditelne = new java.util.ArrayList<>();
			for (final Component c : sb.getComponents()) {
				if (c.isVisible() && c.getWidth() > 0) {
					Assert.assertTrue(tema + " " + sirka + ": " + c.getBounds(), lista.contains(c.getBounds()));
					for (final Rectangle jiny : viditelne) {
						Assert.assertFalse(tema + " " + sirka + ": " + c.getBounds() + " × " + jiny, jiny.intersects(c.getBounds()));
					}
					viditelne.add(c.getBounds());
				}
			}
		});
	}

	@Test
	public void usporneStupneSeVybiraji() throws Exception {
		vsudy((tema, sirka, sb) -> {
			final int[] w = sb.sirkyStupnu();
			for (int s = 1; s < w.length; s++) {
				Assert.assertTrue(tema + ": stupeň " + s + " je užší než předchozí", w[s] < w[s - 1]);
			}
			Assert.assertTrue("plná podoba se vejde na 100 %", w[0] <= 1920);
			Assert.assertTrue("úsporná podoba se vejde na 150 %", w[w.length - 1] <= 1280);
		});
	}
}
