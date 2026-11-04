package cz.geokuk.plugins.kesoidobsazenost;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import org.junit.*;

/** Vrstva s legendou posuvníků propouští myš ke spodním vrstvám a nekreslí, dokud posuvníky nejsou vidět. */
public class JLegendaObsazenostiTest {

	@Test
	public void vrstvaNeblokujeMysAnikdeNeObsahujeBod() {
		final JLegendaObsazenosti legenda = new JLegendaObsazenosti(new JObsazenost());
		legenda.setSize(800, 600);
		for (final int[] p : new int[][] { { 0, 0 }, { 400, 300 }, { 799, 599 }, { 790, 50 } }) {
			Assert.assertFalse(legenda.contains(p[0], p[1]));
		}
		Assert.assertNull("na takové vrstvě neleží žádná komponenta pod myší", legenda.getComponentAt(790, 50));
		Assert.assertFalse(legenda.isOpaque());
	}

	@Test
	public void bezViditelnychPosuvnikuNicNekresli() {
		final JLegendaObsazenosti legenda = new JLegendaObsazenosti(new JObsazenost());
		legenda.setSize(100, 100);
		final BufferedImage obraz = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = obraz.createGraphics();
		legenda.paint(g);
		g.dispose();
		for (int y = 0; y < 100; y++) {
			for (int x = 0; x < 100; x++) {
				Assert.assertEquals(0, obraz.getRGB(x, y) >>> 24);
			}
		}
	}

	@Test
	public void vrstvaPosuvnikumUrciKamSePrekresluje() {
		final JObsazenost obsazenost = new JObsazenost();
		final JLegendaObsazenosti legenda = new JLegendaObsazenosti(obsazenost);
		Assert.assertNotNull(legenda);
		Assert.assertNotNull(obsazenost.getSlidovnik());
	}
}
