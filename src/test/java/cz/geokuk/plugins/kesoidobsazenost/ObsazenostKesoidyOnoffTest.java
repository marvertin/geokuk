package cz.geokuk.plugins.kesoidobsazenost;

import java.lang.reflect.Constructor;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.plugins.kesoid.mvc.KesoidOnoffEvent;

/** F5 skryje obsazenost; zapnutí kešoidů vrátí stav podle vlastního vypínače F9. */
public class ObsazenostKesoidyOnoffTest {

	private static KesoidOnoffEvent kesoidy(final boolean zapnute) throws Exception {
		final Constructor<KesoidOnoffEvent> k = KesoidOnoffEvent.class.getDeclaredConstructor(boolean.class);
		k.setAccessible(true);
		return k.newInstance(zapnute);
	}

	private static ObsazenostOnoffEvent obsazenost(final boolean zapnuto) {
		final ObsazenostOnoffEvent e = new ObsazenostOnoffEvent();
		e.onoff = zapnuto;
		return e;
	}

	@Test
	public void f5SkryjeAVratiObsazenost() throws Exception {
		final JObsazenost s = new JObsazenost();
		s.onEvent(obsazenost(true));
		Assert.assertTrue(s.isVisible());
		s.onEvent(kesoidy(false));
		Assert.assertFalse(s.isVisible());
		s.onEvent(kesoidy(true));
		Assert.assertTrue(s.isVisible());
	}

	@Test
	public void vypnutaObsazenostZustaneVypnuta() throws Exception {
		final JObsazenost s = new JObsazenost();
		s.onEvent(kesoidy(false));
		s.onEvent(obsazenost(false));
		s.onEvent(kesoidy(true));
		Assert.assertFalse(s.isVisible());
	}

	@Test
	public void startSVypnutymiKesoidy() throws Exception {
		final JObsazenost s = new JObsazenost();
		s.onEvent(kesoidy(false));
		s.onEvent(obsazenost(true));
		Assert.assertFalse(s.isVisible());
	}
}
