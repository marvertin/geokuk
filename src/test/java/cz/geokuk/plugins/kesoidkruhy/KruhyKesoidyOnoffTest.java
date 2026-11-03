package cz.geokuk.plugins.kesoidkruhy;

import java.lang.reflect.Constructor;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.plugins.kesoid.mvc.KesoidOnoffEvent;

/** F5 skryje zvýrazňovací kruhy; zapnutí kešoidů vrátí stav podle vlastního vypínače F8. */
public class KruhyKesoidyOnoffTest {

	static KesoidOnoffEvent kesoidy(final boolean zapnute) throws Exception {
		final Constructor<KesoidOnoffEvent> k = KesoidOnoffEvent.class.getDeclaredConstructor(boolean.class);
		k.setAccessible(true);
		return k.newInstance(zapnute);
	}

	private static KruhyPreferencesChangeEvent kruhy(final boolean zapnuto) {
		final KruhySettings s = new KruhySettings();
		s.setOnoff(zapnuto);
		return new KruhyPreferencesChangeEvent(s);
	}

	@Test
	public void f5SkryjeAVratiKruhy() throws Exception {
		final JZvyraznovaciKruhySlide s = new JZvyraznovaciKruhySlide();
		s.onEvent(kruhy(true));
		Assert.assertTrue(s.isVisible());
		s.onEvent(kesoidy(false));
		Assert.assertFalse(s.isVisible());
		s.onEvent(kesoidy(true));
		Assert.assertTrue(s.isVisible());
	}

	@Test
	public void vypnuteKruhyZustanouVypnute() throws Exception {
		final JZvyraznovaciKruhySlide s = new JZvyraznovaciKruhySlide();
		s.onEvent(kesoidy(false));
		s.onEvent(kruhy(false));
		s.onEvent(kesoidy(true));
		Assert.assertFalse(s.isVisible());
	}

	@Test
	public void startSVypnutymiKesoidy() throws Exception {
		final JZvyraznovaciKruhySlide s = new JZvyraznovaciKruhySlide();
		s.onEvent(kesoidy(false));
		s.onEvent(kruhy(true));
		Assert.assertFalse(s.isVisible());
	}
}
