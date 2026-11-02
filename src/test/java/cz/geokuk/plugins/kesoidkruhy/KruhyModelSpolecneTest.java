package cz.geokuk.plugins.kesoidkruhy;

import org.junit.*;

import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.MyPreferences;
import cz.geokuk.plugins.mapy.kachle.data.EKaType;

/** Zapnutí zvýrazňovacích a jednotkových kruhů platí pro všechny podklady map; velikost se pamatuje podle podkladu. */
public class KruhyModelSpolecneTest {

	private final MyPreferences pref = MyPreferences.current().node("test-kruhy-spolecne");

	private KruhyModel model() {
		final KruhyModel m = new KruhyModel() {
			@Override
			protected MyPreferences currPrefe() {
				return pref;
			}
		};
		m.inject(udalost -> {});
		m.reloadPreferences();
		return m;
	}

	@After
	public void uklid() throws Exception {
		pref.removeNode();
	}

	@Test
	public void zapnutiPlatiPoPrepnutiPodkladu() {
		final KruhyModel m = model();
		m.setPodkladMap(EKaType.OFFLINE_MF);
		final KruhySettings d = m.getData();
		d.setOnoff(true);
		d.setJednotkovaVelikost(true);
		m.setData(d);
		m.setPodkladMap(EKaType.OPEN_STREET);
		Assert.assertTrue(m.getData().isOnoff());
		Assert.assertTrue(m.getData().isJednotkovaVelikost());
		final KruhySettings vyp = m.getData();
		vyp.setOnoff(false);
		m.setData(vyp);
		m.setPodkladMap(EKaType.OFFLINE_MF);
		Assert.assertFalse(m.getData().isOnoff());
		Assert.assertTrue(m.getData().isJednotkovaVelikost());
	}

	@Test
	public void velikostZustavaPodlePodkladu() {
		final KruhyModel m = model();
		m.setPodkladMap(EKaType.OFFLINE_MF);
		final KruhySettings d = m.getData();
		d.setVelikost(35);
		m.setData(d);
		m.setPodkladMap(EKaType.OPEN_STREET);
		Assert.assertEquals(20, m.getData().getVelikost());
		m.setPodkladMap(EKaType.OFFLINE_MF);
		Assert.assertEquals(35, m.getData().getVelikost());
	}

	/** Uložené nastavení, kde se zapnutí pamatovalo podle podkladu: převezme se z podkladu, na kterém program začne. */
	@Test
	public void prevzetiZUlozenehoPodkladu() {
		final KruhySettings ulozene = new KruhySettings();
		ulozene.setOnoff(true);
		pref.node(FPref.ZVYRAZNOVACI_KRUHY_node).putStructure(EKaType.OFFLINE_MF.name(), ulozene);
		final KruhyModel m = model();
		Assert.assertFalse("bez podkladu se nic nepřevezme", m.getData().isOnoff());
		m.setPodkladMap(EKaType.OFFLINE_MF);
		Assert.assertTrue(m.getData().isOnoff());
		m.setPodkladMap(EKaType.OPEN_STREET);
		Assert.assertTrue(m.getData().isOnoff());
		Assert.assertTrue(pref.node(FPref.ZVYRAZNOVACI_KRUHY_node).getBoolean(KruhyModel.ZAPNUTO_value, false));
		Assert.assertTrue("další start vezme společnou volbu", model().getData().isOnoff());
	}

	/** Změna kruhů uživatelem hned po startu, ještě před určením podkladu, se uloží jako společná volba. */
	@Test
	public void zmenaPoStartuBezPodkladuSeUlozi() {
		final KruhyModel m = model();
		final KruhySettings d = m.getData();
		d.setOnoff(true);
		d.setJednotkovaVelikost(true);
		m.setData(d);
		Assert.assertTrue(pref.node(FPref.ZVYRAZNOVACI_KRUHY_node).getBoolean(KruhyModel.ZAPNUTO_value, false));
		Assert.assertTrue(pref.node(FPref.ZVYRAZNOVACI_KRUHY_node).getBoolean(KruhyModel.JEDNOTKOVE_value, false));
		m.setPodkladMap(EKaType.OPEN_STREET);
		Assert.assertTrue(m.getData().isOnoff());
		Assert.assertTrue(m.getData().isJednotkovaVelikost());
	}
}
