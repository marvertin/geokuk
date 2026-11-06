package cz.geokuk.core.program;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import java.awt.Color;
import java.awt.event.MouseEvent;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.plugins.kesoid.mvc.KesoidOnoffEvent;

/** Skryté kešoidy (F5) ve stavovém řádku: jantarový podklad počtů, vysvětlení a klik, který kešoidy zobrazí. */
public class JStatusBarSkryteKesoidyTest {

	@Before
	public void jenSDisplejem() {
		Assume.assumeFalse("bez displeje", java.awt.GraphicsEnvironment.isHeadless());
	}

	private static void klik(final JStatusBar sb) {
		for (final java.awt.event.MouseListener l : sb.poctyKesiPanel.getMouseListeners()) {
			l.mouseClicked(new MouseEvent(sb.poctyKesiPanel, MouseEvent.MOUSE_CLICKED, 0, 0, 5, 5, 1, false));
		}
	}

	@Test
	public void skryteKesoidyMajiJantarPodkladATooltipAZobrazeneZase() {
		final JStatusBar sb = new JStatusBar();
		final Color puvodni = sb.poctyKesiPanel.getBackground();
		sb.onEvent(new KesoidOnoffEvent(false));
		Assert.assertEquals(JStatusBar.BARVA_SKRYTYCH_KESOIDU, sb.poctyKesiPanel.getBackground());
		Assert.assertEquals(JStatusBar.TEXT_SKRYTYCH_KESOIDU, sb.poctyKesiPanel.getToolTipText());
		Assert.assertTrue(sb.poctyKesiPanel.getToolTipText().contains("F5"));
		sb.onEvent(new KesoidOnoffEvent(true));
		Assert.assertNotEquals(JStatusBar.BARVA_SKRYTYCH_KESOIDU, sb.poctyKesiPanel.getBackground());
		Assert.assertEquals("barva podkladu se vrátí", puvodni, sb.poctyKesiPanel.getBackground());
		Assert.assertNull(sb.poctyKesiPanel.getToolTipText());
	}

	@Test
	public void klikNaSkrytePoctyKesoidyZobrazi() {
		final JStatusBar sb = new JStatusBar();
		final KesoidModel model = mock(KesoidModel.class);
		when(model.isOnoff()).thenReturn(false);
		sb.kesoidModel = model;
		sb.onEvent(new KesoidOnoffEvent(false));
		klik(sb);
		verify(model).setOnoff(true);
	}

	@Test
	public void klikKdyzSeKesoidyZobrazujiNicNedela() {
		final JStatusBar sb = new JStatusBar();
		final KesoidModel model = mock(KesoidModel.class);
		when(model.isOnoff()).thenReturn(true);
		sb.kesoidModel = model;
		klik(sb);
		verify(model, never()).setOnoff(true);
	}

	@Test
	public void jantarMaCitelnyKontrastSCernymTextem() {
		final Color a = JStatusBar.BARVA_SKRYTYCH_KESOIDU;
		final double jas = 0.2126 * lin(a.getRed()) + 0.7152 * lin(a.getGreen()) + 0.0722 * lin(a.getBlue());
		Assert.assertTrue("kontrast černého textu: " + (jas + 0.05) / 0.05, (jas + 0.05) / 0.05 >= 7);
	}

	private static double lin(final int v) {
		final double c = v / 255.0;
		return c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
	}
}
