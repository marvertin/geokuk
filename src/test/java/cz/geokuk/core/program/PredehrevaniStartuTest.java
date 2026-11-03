package cz.geokuk.core.program;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class PredehrevaniStartuTest {

	/** Předehřátí běží vedle EDT a nesmí vyhodit výjimku ani při opakování. */
	@Test
	public void predehrejDobehneOpakovane() {
		PredehrevaniStartu.predehrej();
		PredehrevaniStartu.predehrej();
	}

	@Test
	public void znakyObsahujiCislicePismenaADiakritiku() {
		for (final char znak : "09%aZáčěřšžůÝ".toCharArray()) {
			assertTrue("chybí " + znak, PredehrevaniStartu.ZNAKY.indexOf(znak) >= 0);
		}
	}

	@Test
	public void kontextBezHintuPlochyMaVyhlazeniVypnute() {
		final List<FontRenderContext> kontexty = PredehrevaniStartu.kontextyMereni(null);
		assertEquals(1, kontexty.size());
		assertEquals(RenderingHints.VALUE_TEXT_ANTIALIAS_OFF, kontexty.get(0).getAntiAliasingHint());
		assertEquals(RenderingHints.VALUE_FRACTIONALMETRICS_OFF, kontexty.get(0).getFractionalMetricsHint());
	}

	@Test
	public void kontextSHinteryPlochyPrebiraVyhlazeniAZlomkoveMetriky() {
		final Map<Object, Object> hinty = new HashMap<>();
		hinty.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		final List<FontRenderContext> kontexty = PredehrevaniStartu.kontextyMereni(hinty);
		assertEquals(2, kontexty.size());
		assertEquals(RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB, kontexty.get(1).getAntiAliasingHint());
		assertEquals(RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT, kontexty.get(1).getFractionalMetricsHint());
		hinty.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		assertEquals(RenderingHints.VALUE_FRACTIONALMETRICS_ON, PredehrevaniStartu.kontextyMereni(hinty).get(1).getFractionalMetricsHint());
	}

	@Test
	public void hintyPlochyBezVyhlazeniDajiJedenKontext() {
		assertEquals(1, PredehrevaniStartu.kontextyMereni(new HashMap<>()).size());
		assertEquals(1, PredehrevaniStartu.kontextyMereni("neco jineho").size());
	}

	/** PNG loga načítání se dekóduje už v předehřátí, ne až při prvním vykreslení mapy na EDT. */
	@Test
	public void predehratiDekodujeLogo() throws Exception {
		final java.lang.reflect.Field zdroj = cz.geokuk.plugins.mapy.kachle.gui.LogoNacitani.class.getDeclaredField("zdroj");
		final java.lang.reflect.Field nacten = cz.geokuk.plugins.mapy.kachle.gui.LogoNacitani.class.getDeclaredField("zdrojNacten");
		zdroj.setAccessible(true);
		nacten.setAccessible(true);
		synchronized (cz.geokuk.plugins.mapy.kachle.gui.LogoNacitani.class) {
			zdroj.set(null, null);
			nacten.setBoolean(null, false);
		}
		PredehrevaniStartu.predehrej();
		assertTrue(nacten.getBoolean(null));
		assertTrue(zdroj.get(null) instanceof java.awt.image.BufferedImage);
	}
}
