package cz.geokuk.plugins.kesoid.mvc;

import java.util.Arrays;

import org.junit.*;

import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.MyPreferences;

/** Uložení nastavení GSAKu a OpenSAKu: klíč „nacitatHnedVsechnyDatabaze“ zůstává pod oběma uzly, zrušený Čas nenálezu se ze starých profilů ztratí bez chyby. */
public class GsakNastaveniUlozeniTest {

	private final MyPreferences koren = MyPreferences.current().node("test-gsak-nastaveni-ulozeni");
	private final KesoidModel model = new KesoidModel() {
		@Override
		protected MyPreferences currPrefe() {
			return koren;
		}

		@Override
		public void fire(final cz.geokuk.framework.Event0<?> event) {
			// události tu nikdo neposlouchá
		}
	};

	@After
	public void uklid() throws Exception {
		koren.removeNode();
	}

	@Test
	public void staryProfilSCasemNenalezuSeNacteBezChybyAPriUlozeniKlicZmizi() {
		final MyPreferences gsak = koren.node(FPref.GSAK_node);
		gsak.putStringSet(FPref.GSAK_CAS_NENALEZU_value, new java.util.LinkedHashSet<>(Arrays.asList("UserData")));
		gsak.putStringSet(FPref.GSAK_CAS_NALEZU_value, new java.util.LinkedHashSet<>(Arrays.asList("Cas")));
		gsak.putBoolean(FPref.NOVE_DATABAZE_HNED_value, false);
		koren.node(FPref.OPENSAK_node).putBoolean(FPref.NOVE_DATABAZE_HNED_value, true);

		final GsakParametryNacitani g = model.loadGsakParametryNacitani();
		Assert.assertEquals(Arrays.asList("Cas"), Arrays.asList(g.getCasNalezu().toArray()));
		Assert.assertFalse(g.isNoveDatabazeHned());
		Assert.assertTrue(g.isNoveDatabazeOpensakuHned());

		model.setGsakParametryNacitani(g);
		Assert.assertNull("zrušené nastavení se odstranilo", gsak.get(FPref.GSAK_CAS_NENALEZU_value, null));
		Assert.assertFalse("volba GSAKu zůstala", gsak.getBoolean(FPref.NOVE_DATABAZE_HNED_value, true));
		Assert.assertTrue("volba OpenSAKu zůstala", koren.node(FPref.OPENSAK_node).getBoolean(FPref.NOVE_DATABAZE_HNED_value, false));
	}

	@Test
	public void volbyGsakuAOpensakuSeUkladajiNezavisleSeStejnymKlicem() {
		final GsakParametryNacitani g = new GsakParametryNacitani();
		g.setNoveDatabazeHned(true);
		g.setNoveDatabazeOpensakuHned(false);
		model.setGsakParametryNacitani(g);
		Assert.assertEquals("nacitatHnedVsechnyDatabaze", FPref.NOVE_DATABAZE_HNED_value);
		Assert.assertTrue(koren.node(FPref.GSAK_node).getBoolean(FPref.NOVE_DATABAZE_HNED_value, false));
		Assert.assertFalse(koren.node(FPref.OPENSAK_node).getBoolean(FPref.NOVE_DATABAZE_HNED_value, true));

		final GsakParametryNacitani nacteno = model.loadGsakParametryNacitani();
		Assert.assertTrue(nacteno.isNoveDatabazeHned());
		Assert.assertFalse(nacteno.isNoveDatabazeOpensakuHned());
	}

	@Test
	public void bezUlozenehoNastaveniJsouVychoziHodnotyHnedProObaZdroje() {
		final GsakParametryNacitani g = model.loadGsakParametryNacitani();
		Assert.assertTrue(g.isNoveDatabazeHned());
		Assert.assertTrue(g.isNoveDatabazeOpensakuHned());
	}
}
