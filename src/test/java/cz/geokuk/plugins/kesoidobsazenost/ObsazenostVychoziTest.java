package cz.geokuk.plugins.kesoidobsazenost;

import org.junit.*;

import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.MyPreferences;

/** Obsazenost je na nové instalaci vypnutá; uložená volba uživatele platí dál. */
public class ObsazenostVychoziTest {

	private final MyPreferences pref = MyPreferences.current().node("test-obsazenost-vychozi");
	private final class Model extends ObsazenostModel {
		@Override
		protected MyPreferences currPrefe() {
			return pref;
		}

		void nacti() {
			reloadPreferences();
		}
	}

	private final Model model = new Model();

	@Before
	public void priprav() {
		model.inject(udalost -> {});
	}

	@After
	public void uklid() throws Exception {
		pref.removeNode();
	}

	@Test
	public void bezUlozeneVolbyJeVypnuta() {
		model.nacti();
		Assert.assertFalse(model.visible.isOnoff());
	}

	@Test
	public void ulozeneZapnutiZustane() {
		pref.node(FPref.MRIZKA_node).putBoolean(FPref.ZOBRAZ_OBSAZENOST_value, true);
		model.nacti();
		Assert.assertTrue(model.visible.isOnoff());
	}

	@Test
	public void volbaSeUlozi() {
		model.nacti();
		model.visible.setOnoff(true);
		Assert.assertTrue(pref.node(FPref.MRIZKA_node).getBoolean(FPref.ZOBRAZ_OBSAZENOST_value, false));
	}
}
