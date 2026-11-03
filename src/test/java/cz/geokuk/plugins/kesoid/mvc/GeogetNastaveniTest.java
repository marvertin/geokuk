package cz.geokuk.plugins.kesoid.mvc;

import java.io.File;
import java.util.*;

import org.junit.*;

import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.MyPreferences;
import cz.geokuk.util.file.FileAndTime;
import cz.geokuk.util.file.KeFile;
import cz.geokuk.util.file.Root;

/** „Nové databáze načíst až po vybrání“ u GeoGetu: uložení bez změny starých profilů a nezávislost na GSAKu a OpenSAKu. */
public class GeogetNastaveniTest {

	private final MyPreferences koren = MyPreferences.current().node("test-geoget-nastaveni");
	private GsakParametryNacitani parametry = new GsakParametryNacitani();
	private final KesoidModel model = new KesoidModel() {
		@Override
		protected MyPreferences currPrefe() {
			return koren;
		}

		@Override
		public GsakParametryNacitani getGsakParametryNacitani() {
			return parametry;
		}

		@Override
		public void fire(final cz.geokuk.framework.Event0<?> event) {
			// události tu nikdo neposlouchá
		}
	};

	private final File a = new File("/tmp/geoget/Cesko.db3").getAbsoluteFile();
	private final File b = new File("/tmp/geoget/Slovensko.db3").getAbsoluteFile();

	@After
	public void uklid() throws Exception {
		koren.removeNode();
	}

	@Test
	public void bezUlozenehoNastaveniSeNacitaHned() {
		Assert.assertTrue(model.loadGsakParametryNacitani().isNoveDatabazeGeogetuHned());
	}

	@Test
	public void staryProfilBezUzluGeogetSeNezmeniPriUlozeni() {
		koren.node(FPref.GSAK_node).putBoolean(FPref.NOVE_DATABAZE_HNED_value, false);
		koren.node(FPref.OPENSAK_node).putBoolean(FPref.NOVE_DATABAZE_HNED_value, false);
		final GsakParametryNacitani g = model.loadGsakParametryNacitani();
		Assert.assertTrue(g.isNoveDatabazeGeogetuHned());
		model.setGsakParametryNacitani(g);
		Assert.assertFalse(koren.node(FPref.GSAK_node).getBoolean(FPref.NOVE_DATABAZE_HNED_value, true));
		Assert.assertFalse(koren.node(FPref.OPENSAK_node).getBoolean(FPref.NOVE_DATABAZE_HNED_value, true));
		Assert.assertTrue(koren.node(FPref.GEOGET_node).getBoolean(FPref.NOVE_DATABAZE_HNED_value, false));
	}

	@Test
	public void volbaSeUkladaPodUzlemGeogetSeStejnymKlicem() {
		final GsakParametryNacitani g = new GsakParametryNacitani();
		g.setNoveDatabazeGeogetuHned(false);
		model.setGsakParametryNacitani(g);
		Assert.assertEquals("geoget", FPref.GEOGET_node);
		Assert.assertFalse(koren.node("geoget").getBoolean("nacitatHnedVsechnyDatabaze", true));
		Assert.assertFalse(model.loadGsakParametryNacitani().isNoveDatabazeGeogetuHned());
	}

	@Test
	public void novaDatabazeGeogetuSeNenacte() {
		parametry.setNoveDatabazeGeogetuHned(false);
		model.zaradGeogetDatabaze(set(a));
		Assert.assertTrue("první prohledání nic nezakáže", nacte(a));
		model.zaradGeogetDatabaze(set(a, b));
		Assert.assertTrue(nacte(a));
		Assert.assertFalse(nacte(b));
	}

	@Test
	public void novaDatabazeGeogetuSeNacteHnedKdyzJeVolbaVypnuta() {
		parametry.setNoveDatabazeGeogetuHned(true);
		model.zaradGeogetDatabaze(set(a));
		model.zaradGeogetDatabaze(set(a, b));
		Assert.assertTrue(nacte(b));
	}

	@Test
	public void volbyGsakuAOpensakuGeogetNeblokuji() {
		parametry.setNoveDatabazeHned(false);
		parametry.setNoveDatabazeOpensakuHned(false);
		parametry.setNoveDatabazeGeogetuHned(true);
		model.zaradGeogetDatabaze(set(a));
		model.zaradGeogetDatabaze(set(a, b));
		Assert.assertTrue(nacte(b));
	}

	@Test
	public void volbaGeogetuGsakANeblokuje() {
		parametry.setNoveDatabazeHned(true);
		parametry.setNoveDatabazeGeogetuHned(false);
		model.zaradGsakDatabaze(set(a));
		model.zaradGsakDatabaze(set(a, b));
		Assert.assertTrue(nacte(b));
	}

	@Test
	public void znameDatabazeVNedostupneSlozceZustanouZname() {
		parametry.setNoveDatabazeGeogetuHned(false);
		model.zaradGeogetDatabaze(set(a));
		model.zaradGeogetDatabaze(set(), set(a.getParentFile()));
		model.zaradGeogetDatabaze(set(a, b));
		Assert.assertTrue("a zůstala známá", nacte(a));
		Assert.assertFalse(nacte(b));
	}

	private boolean nacte(final File f) {
		return model.maSeNacist(new KeFile(new FileAndTime(f, 0), new Root(f.getParentFile(), new Root.Def(1, null, null))));
	}

	private static Set<File> set(final File... f) {
		return new HashSet<>(Arrays.asList(f));
	}
}
