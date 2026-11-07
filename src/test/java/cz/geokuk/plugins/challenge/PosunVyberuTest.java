package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class PosunVyberuTest {

	private static VysledekSektoru sektory(final int... sSKandidaty) {
		final VysledekSektoru.Kandidati[] k = new VysledekSektoru.Kandidati[360];
		for (final int s : sSKandidaty) {
			k[s] = new VysledekSektoru.Kandidati(s);
			k[s].pridej("GC" + s, 100);
		}
		return new VysledekSektoru(new BitSet(360), new BitSet(360), k, 0);
	}

	private static VysledekPolicek policka(final String[] chybejici, final String... sKandidaty) {
		final List<KandidatPolicka> k = new ArrayList<>();
		for (final String s : sKandidaty) {
			k.add(new KandidatPolicka("GC_" + s, 50, 14, s, s, null));
		}
		return new VysledekPolicek(81, Arrays.asList(chybejici), k);
	}

	@Test
	public void sektoryVpredVzestupneADokolaPresKonec() {
		final VysledekSektoru v = sektory(5, 120, 359);
		Assert.assertEquals(120, PosunVyberu.dalsiSektor(v, 5, true));
		Assert.assertEquals(359, PosunVyberu.dalsiSektor(v, 120, true));
		Assert.assertEquals("přes konec na začátek", 5, PosunVyberu.dalsiSektor(v, 359, true));
	}

	@Test
	public void sektoryZpetSestupneADokolaPresZacatek() {
		final VysledekSektoru v = sektory(0, 120, 359);
		Assert.assertEquals(120, PosunVyberu.dalsiSektor(v, 359, false));
		Assert.assertEquals(0, PosunVyberu.dalsiSektor(v, 120, false));
		Assert.assertEquals("přes začátek na konec", 359, PosunVyberu.dalsiSektor(v, 0, false));
	}

	@Test
	public void bezVyberuZacinaPrvnimVpredAPoslednimZpetVcetneOkraju() {
		Assert.assertEquals(7, PosunVyberu.dalsiSektor(sektory(7, 200), PosunVyberu.ZADNY_SEKTOR, true));
		Assert.assertEquals(200, PosunVyberu.dalsiSektor(sektory(7, 200), PosunVyberu.ZADNY_SEKTOR, false));
		Assert.assertEquals("sektor 0 je první", 0, PosunVyberu.dalsiSektor(sektory(0, 200), PosunVyberu.ZADNY_SEKTOR, true));
		Assert.assertEquals("sektor 359 je poslední", 359, PosunVyberu.dalsiSektor(sektory(7, 359), PosunVyberu.ZADNY_SEKTOR, false));
	}

	@Test
	public void sektorBezKandidatuSePreskoci() {
		final VysledekSektoru v = sektory(10, 300);
		Assert.assertEquals(300, PosunVyberu.dalsiSektor(v, 10, true));
		Assert.assertEquals("výchozí sektor bez kandidátů je jen výchozí bod", 300, PosunVyberu.dalsiSektor(v, 100, true));
		Assert.assertEquals(10, PosunVyberu.dalsiSektor(v, 100, false));
	}

	@Test
	public void jedinySektorSeVraciVzdyAPrazdnoDaMinusJedna() {
		Assert.assertEquals(42, PosunVyberu.dalsiSektor(sektory(42), 42, true));
		Assert.assertEquals(42, PosunVyberu.dalsiSektor(sektory(42), 42, false));
		Assert.assertEquals(-1, PosunVyberu.dalsiSektor(sektory(), PosunVyberu.ZADNY_SEKTOR, true));
		Assert.assertEquals(-1, PosunVyberu.dalsiSektor(sektory(), 5, false));
	}

	@Test
	public void sektorSPrazdnymiKandidatySePreskoci() {
		final VysledekSektoru.Kandidati[] k = new VysledekSektoru.Kandidati[360];
		k[10] = new VysledekSektoru.Kandidati(10);
		k[20] = new VysledekSektoru.Kandidati(20);
		k[20].pridej("GC20", 1);
		final VysledekSektoru v = new VysledekSektoru(new BitSet(360), new BitSet(360), k, 0);
		Assert.assertEquals(20, PosunVyberu.dalsiSektor(v, PosunVyberu.ZADNY_SEKTOR, true));
		Assert.assertEquals(20, PosunVyberu.dalsiSektor(v, 5, true));
		Assert.assertEquals(20, PosunVyberu.dalsiSektor(v, 30, false));
	}

	@Test
	public void sektorMimoRozsahJeChyba() {
		for (final int s : new int[] { -2, 360, 1000 }) {
			try {
				PosunVyberu.dalsiSektor(sektory(1), s, true);
				Assert.fail(String.valueOf(s));
			} catch (final IllegalArgumentException e) {
				// očekáváno
			}
		}
	}

	private static final String[] RADKY = { "D1/T1", "D1/T1,5", "D1/T2", "D1,5/T1", "D5/T5" };

	@Test
	public void polickaPoRadcichADokola() {
		final VysledekPolicek v = policka(RADKY, RADKY);
		Assert.assertEquals("D1/T1,5", PosunVyberu.dalsiPolicko(v, "D1/T1", true));
		Assert.assertEquals("D5/T5", PosunVyberu.dalsiPolicko(v, "D1,5/T1", true));
		Assert.assertEquals("přes konec", "D1/T1", PosunVyberu.dalsiPolicko(v, "D5/T5", true));
		Assert.assertEquals("přes začátek", "D5/T5", PosunVyberu.dalsiPolicko(v, "D1/T1", false));
		Assert.assertEquals("D1/T1", PosunVyberu.dalsiPolicko(v, "D1/T1,5", false));
	}

	@Test
	public void bezVyberuANeznamyStitekZacinaPrvnimVpredAPoslednimZpet() {
		final VysledekPolicek v = policka(RADKY, RADKY);
		Assert.assertEquals("D1/T1", PosunVyberu.dalsiPolicko(v, null, true));
		Assert.assertEquals("D5/T5", PosunVyberu.dalsiPolicko(v, null, false));
		Assert.assertEquals("D1/T1", PosunVyberu.dalsiPolicko(v, "D3/T3", true));
		Assert.assertEquals("D5/T5", PosunVyberu.dalsiPolicko(v, "D3/T3", false));
	}

	@Test
	public void polickaBezKandidatuSePreskoci() {
		final VysledekPolicek v = policka(RADKY, "D1/T1", "D1,5/T1");
		Assert.assertEquals("D1,5/T1", PosunVyberu.dalsiPolicko(v, "D1/T1", true));
		Assert.assertEquals("D1/T1", PosunVyberu.dalsiPolicko(v, "D1,5/T1", true));
		Assert.assertEquals("D1,5/T1", PosunVyberu.dalsiPolicko(v, "D1/T1", false));
	}

	@Test
	public void jedinePolickoSeVraciAPrazdnoDaNull() {
		final VysledekPolicek jedno = policka(new String[] { "D1/T1", "D2/T2" }, "D2/T2");
		Assert.assertEquals("D2/T2", PosunVyberu.dalsiPolicko(jedno, "D2/T2", true));
		Assert.assertEquals("D2/T2", PosunVyberu.dalsiPolicko(jedno, "D2/T2", false));
		Assert.assertNull("nikde kandidáti", PosunVyberu.dalsiPolicko(policka(RADKY), "D1/T1", true));
		Assert.assertNull("nic nechybí", PosunVyberu.dalsiPolicko(policka(new String[0]), null, true));
	}
}
