package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class VypocetMaticeTest {

	private static final class K implements VstupDT {
		String id = "GC1";
		int d;
		int t;
		boolean nalezena;
		boolean vlastni;
		boolean neaktivni;
		boolean lab;
		boolean locationless;
		StavLogu log = StavLogu.NEZNAMY;
		double lat = 50;
		double lon = 14;

		K(final int d, final int t, final boolean nalezena) {
			this.d = d;
			this.t = t;
			this.nalezena = nalezena;
		}

		K misto(final double nova) {
			lat = nova;
			return this;
		}

		K id(final String i) {
			id = i;
			return this;
		}

		@Override
		public String id() {
			return id;
		}

		@Override
		public double lat() {
			return lat;
		}

		@Override
		public double lon() {
			return lon;
		}

		@Override
		public boolean nalezena() {
			return nalezena;
		}

		@Override
		public boolean lab() {
			return lab;
		}

		@Override
		public boolean locationless() {
			return locationless;
		}

		@Override
		public boolean vlastni() {
			return vlastni;
		}

		@Override
		public boolean neaktivni() {
			return neaktivni;
		}

		@Override
		public StavLogu logNaGc() {
			return log;
		}

		@Override
		public int obtiznost() {
			return d;
		}

		@Override
		public int teren() {
			return t;
		}
	}

	private static VysledekPolicek spocti(final K... k) {
		return VypocetMatice.spocti(PravidlaTypu.proMatici(), 50, 14, Arrays.asList(k));
	}

	@Test
	public void prazdneMaticeChybiVsech81() {
		final VysledekPolicek v = spocti();
		Assert.assertEquals(81, v.celkem());
		Assert.assertEquals(81, v.pocetChybejicich());
		Assert.assertEquals("D1/T1", v.stitkyChybejicich().get(0));
		Assert.assertEquals("D1/T1,5", v.stitkyChybejicich().get(1));
		Assert.assertEquals("D5/T5", v.stitkyChybejicich().get(80));
	}

	@Test
	public void stitkyPoPulkach() {
		Assert.assertEquals("D4/T1", VypocetMatice.stitek(6, 0));
		Assert.assertEquals("D1,5/T2,5", VypocetMatice.stitek(1, 3));
		Assert.assertEquals("D5/T5", VypocetMatice.stitek(8, 8));
	}

	@Test
	public void nalezPolickoVyplni() {
		final VysledekPolicek v = spocti(new K(6, 0, true), new K(6, 0, true), new K(8, 8, true));
		Assert.assertEquals(79, v.pocetChybejicich());
		Assert.assertFalse(v.stitkyChybejicich().contains("D4/T1"));
		Assert.assertFalse(v.stitkyChybejicich().contains("D5/T5"));
	}

	@Test
	public void plnaMaticeNemaChybejici() {
		final List<K> vse = new ArrayList<>();
		for (int d = 0; d < 9; d++) {
			for (int t = 0; t < 9; t++) {
				vse.add(new K(d, t, true));
			}
		}
		final VysledekPolicek v = VypocetMatice.spocti(PravidlaTypu.proMatici(), 50, 14, vse);
		Assert.assertEquals(0, v.pocetChybejicich());
		Assert.assertTrue(v.kandidati().isEmpty());
	}

	@Test
	public void neplatneDTSeIgnoruje() {
		final VysledekPolicek v = spocti(new K(-1, 3, true), new K(3, -1, true), new K(9, 3, true), new K(3, 9, true), new K(-1, 3, false), new K(9, 9, false));
		Assert.assertEquals(81, v.pocetChybejicich());
		Assert.assertTrue(v.kandidati().isEmpty());
	}

	@Test
	public void hranicePlatnychHodnot() {
		final VysledekPolicek v = spocti(new K(0, 0, true), new K(8, 8, true));
		Assert.assertEquals(79, v.pocetChybejicich());
	}

	@Test
	public void kandidatJeNenalezenaKesVChybejicimPolicku() {
		final VysledekPolicek v = spocti(new K(6, 0, true), new K(6, 0, false).id("GCA"), new K(2, 4, false).id("GCB"));
		Assert.assertEquals(1, v.kandidati().size());
		Assert.assertEquals("GCB", v.kandidati().get(0).id());
		Assert.assertEquals("D2/T3", v.kandidati().get(0).stitek());
		Assert.assertNull(v.kandidati().get(0).dniDo());
		Assert.assertEquals(50, v.kandidati().get(0).lat(), 0);
		Assert.assertEquals(14, v.kandidati().get(0).lon(), 0);
	}

	@Test
	public void vlastniAneaktivniNejsouKandidati() {
		final K vlastni = new K(2, 2, false);
		vlastni.vlastni = true;
		final K neaktivni = new K(3, 3, false);
		neaktivni.neaktivni = true;
		Assert.assertTrue(spocti(vlastni, neaktivni).kandidati().isEmpty());
	}

	@Test
	public void nalezBezLoguNaGcSeNepocita() {
		final K ne = new K(2, 2, true);
		ne.log = VstupKes.StavLogu.NE;
		final K ano = new K(4, 4, true);
		ano.log = VstupKes.StavLogu.ANO;
		final VysledekPolicek v = spocti(ne, ano);
		Assert.assertEquals(80, v.pocetChybejicich());
		Assert.assertTrue(v.stitkyChybejicich().contains("D2/T2"));
		Assert.assertFalse(v.stitkyChybejicich().contains("D3/T3"));
	}

	@Test
	public void labALocationlessPodlePravidel() {
		final K lab = new K(2, 2, true);
		lab.lab = true;
		final K loc = new K(4, 4, true);
		loc.locationless = true;
		final List<K> kese = Arrays.asList(lab, loc);
		Assert.assertEquals(80, VypocetMatice.spocti(PravidlaTypu.proMatici(), 50, 14, kese).pocetChybejicich());
		Assert.assertEquals(79, VypocetMatice.spocti(PravidlaTypu.proMatici().sLab(true), 50, 14, kese).pocetChybejicich());
		Assert.assertEquals(81, VypocetMatice.spocti(PravidlaTypu.proMatici().sLocationless(false), 50, 14, kese).pocetChybejicich());
		Assert.assertEquals(80, VypocetMatice.spocti(PravidlaTypu.proMatici().sLocationless(false).sLab(true), 50, 14, kese).pocetChybejicich());
	}

	@Test
	public void vysledekJeNemenny() {
		final VysledekPolicek v = spocti(new K(2, 2, false));
		try {
			v.stitkyChybejicich().add("x");
			Assert.fail();
		} catch (final UnsupportedOperationException e) {
			// očekáváno
		}
		try {
			v.kandidati().clear();
			Assert.fail();
		} catch (final UnsupportedOperationException e) {
			// očekáváno
		}
	}

	@Test
	public void kandidatuJeNejvicNaPolickoAVybereSePrvniVPoradi() {
		final VysledekPolicek v = VypocetMatice.spocti(PravidlaTypu.proMatici().sMaxKandidatu(2), 50, 14, Arrays.asList(new K(2, 2, false).id("A"), new K(2, 2, false).id("B"), new K(2, 2, false).id("C"), new K(3, 3, false).id("D")));
		final List<String> id = new ArrayList<>();
		for (final KandidatPolicka k : v.kandidati()) {
			id.add(k.id());
		}
		Assert.assertEquals(Arrays.asList("A", "B", "D"), id);
		Assert.assertEquals("D2/T2", v.kandidati().get(0).stitek());
		Assert.assertEquals("D2/T2", v.kandidati().get(0).popis());
	}

	@Test
	public void priUplneShodePriOrezuRozhodujeKodKese() {
		final PravidlaTypu p = PravidlaTypu.proMatici().sMaxKandidatu(1);
		Assert.assertEquals("A", VypocetMatice.spocti(p, 50, 14, Arrays.asList(new K(2, 2, false).id("B"), new K(2, 2, false).id("A"))).kandidati().get(0).id());
		Assert.assertEquals("A", VypocetMatice.spocti(p, 50, 14, Arrays.asList(new K(2, 2, false).id("A"), new K(2, 2, false).id("B"))).kandidati().get(0).id());
	}

	@Test
	public void kandidatSNeplatnymiSouradnicemiSeNenabizi() {
		for (final double lat : new double[] { Double.NaN, Double.POSITIVE_INFINITY, 95, -91 }) {
			Assert.assertTrue(String.valueOf(lat), spocti(new K(2, 2, false).misto(lat)).kandidati().isEmpty());
		}
		Assert.assertEquals("nález bez souřadnic vyplní políčko", 80, spocti(new K(2, 2, true).misto(Double.NaN)).pocetChybejicich());
		Assert.assertEquals(1, spocti(new K(2, 2, false).misto(50)).kandidati().size());
	}

	@Test
	public void kandidatPoOrezuNeniVNalezenemPolicku() {
		final VysledekPolicek v = VypocetMatice.spocti(PravidlaTypu.proMatici().sMaxKandidatu(1), 50, 14, Arrays.asList(new K(2, 2, false).id("A"), new K(2, 2, true)));
		Assert.assertTrue(v.kandidati().isEmpty());
	}

	@Test(expected = IllegalArgumentException.class)
	public void maxKandidatuMusiBytKladne() {
		PravidlaTypu.proMatici().sMaxKandidatu(0);
	}

	@Test
	public void maxKandidatuJeJedna() {
		Assert.assertEquals(1, PravidlaTypu.proMatici().sMaxKandidatu(1).maxKandidatu());
	}

	@Test(expected = NullPointerException.class)
	public void nullPravidla() {
		VypocetMatice.spocti(null, 50, 14, new ArrayList<K>());
	}

	@Test(expected = NullPointerException.class)
	public void nullKese() {
		VypocetMatice.spocti(PravidlaTypu.proMatici(), 50, 14, null);
	}

	@Test(expected = NullPointerException.class)
	public void nullPrvekKesi() {
		VypocetMatice.spocti(PravidlaTypu.proMatici(), 50, 14, Arrays.asList(new K(1, 1, true), null));
	}

	@Test
	public void kandidatiVPolickuSeVybiraNejblizsiBoduZVelkehoPoctu() {
		final List<K> kese = new ArrayList<>();
		for (int i = 0; i < 200_000; i++) {
			kese.add(new K(2, 2, false).id("GC" + i).misto(50 + i * 0.00005));
		}
		final double lat = 50 + 199_999 * 0.00005;
		final VysledekPolicek v = VypocetMatice.spocti(PravidlaTypu.proMatici().sMaxKandidatu(3), lat, 14, kese);
		final List<String> id = new ArrayList<>();
		for (final KandidatPolicka k : v.kandidati()) {
			id.add(k.id());
		}
		Assert.assertEquals(Arrays.asList("GC199999", "GC199998", "GC199997"), id);
		final DalsiPolicko dalsi = new DalsiPolicko();
		Assert.assertEquals("GC199999", dalsi.dalsi(v, lat, 14).id());
	}

	@Test
	public void kandidatiVPolickuJsouSerazeniOdNejblizsiho() {
		final VysledekPolicek v = spocti(new K(2, 2, false).id("DALEKO").misto(52), new K(2, 2, false).id("BLIZKO").misto(50.1), new K(2, 2, false).id("STREDNI").misto(51));
		final List<String> id = new ArrayList<>();
		for (final KandidatPolicka k : v.kandidati()) {
			id.add(k.id());
		}
		Assert.assertEquals(Arrays.asList("BLIZKO", "STREDNI", "DALEKO"), id);
	}

	@Test
	public void referencniBodMimoRozsahJeChyba() {
		final List<K> kese = new ArrayList<>();
		for (final double[] bod : new double[][] { { 90.1, 0 }, { -90.1, 0 }, { 0, 180.1 }, { 0, -180.1 }, { Double.NaN, 0 }, { 0, Double.NaN } }) {
			try {
				VypocetMatice.spocti(PravidlaTypu.proMatici(), bod[0], bod[1], kese);
				Assert.fail(bod[0] + ", " + bod[1]);
			} catch (final IllegalArgumentException e) {
				// očekáváno
			}
		}
		VypocetMatice.spocti(PravidlaTypu.proMatici(), 90, 180, kese);
		VypocetMatice.spocti(PravidlaTypu.proMatici(), -90, -180, kese);
	}
}
