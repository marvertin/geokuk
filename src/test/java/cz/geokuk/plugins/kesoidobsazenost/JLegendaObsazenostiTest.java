package cz.geokuk.plugins.kesoidobsazenost;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import org.junit.*;
import org.mockito.Mockito;

import cz.geokuk.util.gui.JBarvovyDvojSlider;

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

	private static final class ViditelnySlider extends JBarvovyDvojSlider {
		private static final long serialVersionUID = 1L;
		Component kam;

		@Override
		public boolean isShowing() {
			return true;
		}

		@Override
		public Rectangle kresliNadMapou(final Graphics2D g, final Component kam) {
			this.kam = kam;
			return null;
		}
	}

	@Test
	public void viditelnePosuvnikyKresliDoVrstvyLegendy() {
		final ViditelnySlider slider = new ViditelnySlider();
		final JObsazenost obsazenost = Mockito.mock(JObsazenost.class);
		Mockito.when(obsazenost.getSlidovnik()).thenReturn(slider);
		final JLegendaObsazenosti legenda = new JLegendaObsazenosti(obsazenost);
		legenda.setSize(100, 100);
		final Graphics2D g = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB).createGraphics();
		legenda.paint(g);
		g.dispose();
		Assert.assertSame(legenda, slider.kam);
	}

	@Test
	public void zmenaPosuvnikuPrekresliVrstvuLegendy() {
		final JObsazenost obsazenost = new JObsazenost();
		final int[] prekresleni = { 0 };
		final JLegendaObsazenosti legenda = new JLegendaObsazenosti(obsazenost) {
			private static final long serialVersionUID = 1L;

			@Override
			public void repaint(final long tm, final int x, final int y, final int width, final int height) {
				prekresleni[0]++;
			}
		};
		Assert.assertNotNull(legenda);
		obsazenost.getSlidovnik().getBarvovnik().setValue(10);
		Assert.assertTrue(prekresleni[0] > 0);
	}
}
