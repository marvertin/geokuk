package cz.geokuk.plugins.kesoidkruhy;

import org.junit.*;

import cz.geokuk.framework.MyPreferences;

/** Jednotkové kruhy jsou jen podoba zvýrazňovacích kruhů, jejich zapnutím se kruhy i zobrazí. */
public class JednotkoveKruhyActionTest {

	private final MyPreferences pref = MyPreferences.current().node("test-jednotkove-kruhy");
	private final KruhyModel model = new KruhyModel() {
		@Override
		protected MyPreferences currPrefe() {
			return pref;
		}
	};
	private final JednotkoveKruhyAction akce = new JednotkoveKruhyAction();

	@Before
	public void priprav() {
		model.inject(udalost -> {});
		model.reloadPreferences();
		akce.inject(model);
	}

	@After
	public void uklid() throws Exception {
		pref.removeNode();
	}

	@Test
	public void zapnutiJednotkovychZapneZvyraznovaciKruhy() {
		Assert.assertFalse(model.getData().isOnoff());
		akce.onSlectedChange(true);
		Assert.assertTrue(model.getData().isJednotkovaVelikost());
		Assert.assertTrue(model.getData().isOnoff());
	}

	@Test
	public void vypnutiJednotkovychNechaKruhyZapnute() {
		akce.onSlectedChange(true);
		akce.onSlectedChange(false);
		Assert.assertFalse(model.getData().isJednotkovaVelikost());
		Assert.assertTrue(model.getData().isOnoff());
	}
}
