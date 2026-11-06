package cz.geokuk.plugins.kesoid.filtr;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.GraphicsEnvironment;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.plugins.kesoid.mvc.KesoidOnoffEvent;

/** Dialog Filtr říká, že skryté kešoidy (F5) filtr na mapě neuplatní, a nabízí je zobrazit; potřebuje displej. */
public class JFiltrIkonyDialogSkryteKesoidyTest {

	@Test
	public void pruhJeVidetJenKdyzJsouKesoidySkryteATlacitkoJeZobrazi() throws Exception {
		Assume.assumeFalse("bez displeje", GraphicsEnvironment.isHeadless());
		SwingUtilities.invokeAndWait(() -> {
			final JFiltrIkonyDialog d = new JFiltrIkonyDialog();
			try {
				final KesoidModel model = mock(KesoidModel.class);
				when(model.isOnoff()).thenReturn(false);
				d.inject(model);
				Assert.assertTrue(d.skryteKesoidy.isVisible());
				final JButton tlacitko = (JButton) d.skryteKesoidy.getComponent(1);
				Assert.assertEquals("Zobrazit kešoidy", tlacitko.getText());
				tlacitko.doClick();
				verify(model).setOnoff(true);
				d.onEvent(new KesoidOnoffEvent(true));
				Assert.assertFalse("po zapnutí kešoidů pruh zmizí", d.skryteKesoidy.isVisible());
				d.onEvent(new KesoidOnoffEvent(false));
				Assert.assertTrue(d.skryteKesoidy.isVisible());
			} finally {
				d.dispose();
			}
		});
	}

	@Test
	public void pruhJeSkrytyKdyzSeKesoidyZobrazuji() throws Exception {
		Assume.assumeFalse("bez displeje", GraphicsEnvironment.isHeadless());
		SwingUtilities.invokeAndWait(() -> {
			final JFiltrIkonyDialog d = new JFiltrIkonyDialog();
			try {
				final KesoidModel model = mock(KesoidModel.class);
				when(model.isOnoff()).thenReturn(true);
				d.inject(model);
				Assert.assertFalse(d.skryteKesoidy.isVisible());
			} finally {
				d.dispose();
			}
		});
	}
}
