package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.plugins.challenge.GeometrieSektoru.Bod;
import cz.geokuk.plugins.challenge.GeometrieSektoru.Model;

public class SkokVeSmeruTest {

	private static final double LAT = 50.0;
	private static final double LON = 14.0;
	private static final double SEVER = 0;
	private static final double VYCHOD = 90;
	private static final double JIH = 180;
	private static final double ZAPAD = 270;

	/** Kandidát ve zadaném azimutu a vzdálenosti od výchozí polohy. */
	private static KandidatPolicka kp(final String id, final double azimut, final double metru) {
		final Bod b = GeometrieSektoru.bod(LAT, LON, azimut, metru, Model.KOULE);
		return new KandidatPolicka(id, b.lat, b.lon, "D1/T1", "D1/T1", null);
	}

	private static VysledekPolicek vp(final KandidatPolicka... k) {
		return new VysledekPolicek(81, Collections.singletonList("D1/T1"), java.util.Arrays.asList(k));
	}

	private static VysledekSektoru.KandidatKes ks(final String id, final int sektor, final double azimut, final double metru) {
		final Bod b = GeometrieSektoru.bod(LAT, LON, azimut, metru, Model.KOULE);
		return new VysledekSektoru.KandidatKes(id, b.lat, b.lon, sektor);
	}

	private static VysledekSektoru vs(final VysledekSektoru.KandidatKes... k) {
		return new VysledekSektoru(new BitSet(360), new BitSet(360), new VysledekSektoru.Kandidati[360], 0, java.util.Arrays.asList(k));
	}

	private static String id(final VybranyKandidat k) {
		return k == null ? null : k.id();
	}

	private static String policka(final VysledekPolicek v, final double lat, final double lon, final double smer, final String vynechat) {
		return id(SkokVeSmeru.proPolicka(v, lat, lon, smer, vynechat));
	}

	private static String sektory(final VysledekSektoru v, final double lat, final double lon, final double smer, final String vynechat) {
		return id(SkokVeSmeru.proSektory(v, lat, lon, smer, vynechat));
	}

	private static String doPolicek(final VysledekPolicek v, final double smer) {
		return policka(v, LAT, LON, smer, null);
	}

	@Test
	public void vybereNejblizsihoVeSmeruZeCtyrStran() {
		final VysledekPolicek v = vp(kp("N", 5, 3000), kp("N2", 5, 5000), kp("E", 95, 2000), kp("J", 180, 1000), kp("Z", 270, 4000));
		Assert.assertEquals("N", doPolicek(v, SEVER));
		Assert.assertEquals("E", doPolicek(v, VYCHOD));
		Assert.assertEquals("J", doPolicek(v, JIH));
		Assert.assertEquals("Z", doPolicek(v, ZAPAD));
	}

	@Test
	public void nejblizsiMimoKuzelNeVyhraje() {
		final VysledekPolicek v = vp(kp("BLIZKO_VYCHODNE", 90, 500), kp("DALEKO_SEVERNE", 10, 8000));
		Assert.assertEquals("DALEKO_SEVERNE", doPolicek(v, SEVER));
		Assert.assertEquals("BLIZKO_VYCHODNE", doPolicek(v, VYCHOD));
	}

	@Test
	public void hranicePetaCtyricetiStupnu() {
		for (final double smer : new double[] { SEVER, VYCHOD, JIH, ZAPAD }) {
			Assert.assertEquals("uvnitř kuželu " + smer, "UVNITR+", doPolicek(vp(kp("UVNITR+", smer + 44.9, 3000)), smer));
			Assert.assertEquals("uvnitř kuželu " + smer, "UVNITR-", doPolicek(vp(kp("UVNITR-", smer - 44.9, 3000)), smer));
			Assert.assertNull("mimo kužel " + smer, doPolicek(vp(kp("VNE+", smer + 45.1, 3000)), smer));
			Assert.assertNull("mimo kužel " + smer, doPolicek(vp(kp("VNE-", smer - 45.1, 3000)), smer));
		}
	}

	@Test
	public void kuzelPresPrechodNula360() {
		final VysledekPolicek v = vp(kp("P359", 359, 4000), kp("P1", 1, 3000), kp("P316", 316, 2000), kp("P314", 314, 1000), kp("P44", 44, 500));
		Assert.assertEquals("sever vidí 316 až 44, nejbližší P44", "P44", doPolicek(v, SEVER));
		Assert.assertEquals("směr 350 vidí i 5 stupňů za nulou", "P1", doPolicek(vp(kp("P1", 1, 3000), kp("P100", 100, 100)), 350));
		Assert.assertEquals("směr 10 vidí 355", "P355", doPolicek(vp(kp("P355", 355, 3000), kp("P100", 100, 100)), 10));
		Assert.assertEquals("směr -10 je 350", "P1", doPolicek(vp(kp("P1", 1, 3000)), -10));
		Assert.assertEquals("směr 370 je 10", "P355", doPolicek(vp(kp("P355", 355, 3000)), 370));
		Assert.assertEquals("P359", doPolicek(vp(kp("P359", 359, 4000)), SEVER));
		Assert.assertNull("P314 leží 46° od severu", doPolicek(vp(kp("P314", 314, 1000)), SEVER));
		Assert.assertEquals("P316 leží 44° od severu", "P316", doPolicek(vp(kp("P316", 316, 1000)), SEVER));
	}

	@Test
	public void prazdnoANikdoVKuzeluDaNull() {
		Assert.assertNull(doPolicek(vp(), SEVER));
		Assert.assertNull(sektory(vs(), LAT, LON, SEVER, null));
		Assert.assertNull(doPolicek(vp(kp("J", 180, 1000)), SEVER));
		Assert.assertNull(sektory(vs(ks("J", 180, 180, 1000)), LAT, LON, SEVER, null));
	}

	@Test
	public void vybranyKandidatSeNevybereZnovuAKandidatNaMisteSeVynecha() {
		final KandidatPolicka a = kp("A", 0, 1000);
		final KandidatPolicka b = kp("B", 0, 3000);
		final VysledekPolicek v = vp(a, b);
		Assert.assertEquals("A", policka(v, LAT, LON, SEVER, null));
		Assert.assertEquals("stojí se na A, pokračuje se dál na sever", "B", policka(v, a.lat(), a.lon(), SEVER, "A"));
		Assert.assertEquals("kód se vynechá i bez shody polohy", "B", policka(v, LAT, LON, SEVER, "A"));
		Assert.assertNull("na B už nikdo severně není", policka(v, b.lat(), b.lon(), SEVER, "B"));
		Assert.assertEquals("zpět na jih od B je A", "A", policka(v, b.lat(), b.lon(), JIH, "B"));
		Assert.assertEquals("keš přesně na poloze se nebere (azimut není definován)", "B", policka(v, a.lat(), a.lon(), SEVER, null));
	}

	@Test
	public void priShodeVzdalenostiRozhodujeSektorPakKod() {
		final Bod b = GeometrieSektoru.bod(LAT, LON, 0, 3000, Model.KOULE);
		final VysledekSektoru v = vs(new VysledekSektoru.KandidatKes("B", b.lat, b.lon, 40), new VysledekSektoru.KandidatKes("A", b.lat, b.lon, 41),
				new VysledekSektoru.KandidatKes("C", b.lat, b.lon, 40));
		Assert.assertEquals("nižší sektor, při shodě nižší kód", "B", sektory(v, LAT, LON, SEVER, null));
		final VysledekSektoru obracene = vs(new VysledekSektoru.KandidatKes("C", b.lat, b.lon, 40), new VysledekSektoru.KandidatKes("A", b.lat, b.lon, 41),
				new VysledekSektoru.KandidatKes("B", b.lat, b.lon, 40));
		Assert.assertEquals("pořadí kandidátů nerozhoduje", "B", sektory(obracene, LAT, LON, SEVER, null));
		final VysledekPolicek p = vp(new KandidatPolicka("Z", b.lat, b.lon, "D1/T1", "D1/T1", null), new KandidatPolicka("M", b.lat, b.lon, "D1/T1", "D1/T1", null));
		Assert.assertEquals("u políček podle kódu", "M", doPolicek(p, SEVER));
	}

	@Test
	public void sektory360() {
		final VysledekSektoru v = vs(ks("S10", 10, 10, 4000), ks("V100", 100, 100, 1000), ks("J170", 170, 170, 2000), ks("Z280", 280, 280, 3000));
		Assert.assertEquals("S10", sektory(v, LAT, LON, SEVER, null));
		Assert.assertEquals("V100", sektory(v, LAT, LON, VYCHOD, null));
		Assert.assertEquals("J170", sektory(v, LAT, LON, JIH, null));
		Assert.assertEquals("Z280", sektory(v, LAT, LON, ZAPAD, null));
		Assert.assertNull("vybraný se vynechá", sektory(v, LAT, LON, JIH, "J170"));
	}

	@Test
	public void ztrojeniNejblizsiVeSmeruBezOhleduNaPoradi() {
		final List<KandidatPolicka> k = new ArrayList<>();
		for (int i = 0; i < 200; i++) {
			k.add(kp("K" + i, i * 1.5 % 360, 1000 + (i * 37) % 5000));
		}
		final VysledekPolicek dopredu = new VysledekPolicek(81, Collections.singletonList("x"), k);
		final List<KandidatPolicka> opacne = new ArrayList<>(k);
		Collections.reverse(opacne);
		final VysledekPolicek dozadu = new VysledekPolicek(81, Collections.singletonList("x"), opacne);
		for (double smer = 0; smer < 360; smer += 7.5) {
			Assert.assertEquals("směr " + smer, doPolicek(dopredu, smer), doPolicek(dozadu, smer));
		}
	}

	@Test
	public void rozdilUhluJeSymetrickyAVRozsahu0Az180() {
		Assert.assertEquals(0, SkokVeSmeru.rozdilUhlu(0, 360), 0);
		Assert.assertEquals(10, SkokVeSmeru.rozdilUhlu(355, 5), 0);
		Assert.assertEquals(10, SkokVeSmeru.rozdilUhlu(5, 355), 0);
		Assert.assertEquals(180, SkokVeSmeru.rozdilUhlu(0, 180), 0);
		Assert.assertEquals(180, SkokVeSmeru.rozdilUhlu(90, 270), 0);
		Assert.assertEquals(170, SkokVeSmeru.rozdilUhlu(0, 190), 0);
		Assert.assertEquals(20, SkokVeSmeru.rozdilUhlu(-10, 10), 0);
		Assert.assertEquals(0, SkokVeSmeru.rozdilUhlu(10, 10 + 720), 1e-9);
	}

	@Test
	public void neplatnyVstupJeChyba() {
		final VysledekPolicek v = vp(kp("A", 0, 1000));
		for (final double[] bod : new double[][] { { Double.NaN, 14 }, { 50, Double.NaN }, { 91, 14 }, { 50, 181 } }) {
			try {
				policka(v, bod[0], bod[1], SEVER, null);
				Assert.fail(bod[0] + ", " + bod[1]);
			} catch (final IllegalArgumentException e) {
				// očekáváno
			}
		}
		for (final double smer : new double[] { Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY }) {
			try {
				sektory(vs(), LAT, LON, smer, null);
				Assert.fail(String.valueOf(smer));
			} catch (final IllegalArgumentException e) {
				// očekáváno
			}
		}
	}

	@Test
	public void vysledekNeseSouradniceSektorStitekAVzdalenost() {
		final KandidatPolicka k = new KandidatPolicka("GC9", 50.01, 14.0, "D4/T1", "D4/T1", null);
		final VybranyKandidat p = SkokVeSmeru.proPolicka(vp(k), LAT, LON, SEVER, null);
		Assert.assertEquals(50.01, p.lat(), 0);
		Assert.assertEquals(14.0, p.lon(), 0);
		Assert.assertEquals(-1, p.sektor());
		Assert.assertEquals("D4/T1", p.stitek());
		Assert.assertEquals(Geodezie.vzdalenostKoule(LAT, LON, 50.01, 14.0), p.vzdalenost(), 0);
		final VybranyKandidat s = SkokVeSmeru.proSektory(vs(new VysledekSektoru.KandidatKes("GC8", 50.01, 14.0, 123)), LAT, LON, SEVER, null);
		Assert.assertEquals(123, s.sektor());
		Assert.assertNull(s.stitek());
		Assert.assertEquals(p.vzdalenost(), s.vzdalenost(), 0);
	}

	@Test
	public void smerSkokuMaAzimutAText() {
		Assert.assertEquals(0, SmerSkoku.SEVER.azimut(), 0);
		Assert.assertEquals(90, SmerSkoku.VYCHOD.azimut(), 0);
		Assert.assertEquals(180, SmerSkoku.JIH.azimut(), 0);
		Assert.assertEquals(270, SmerSkoku.ZAPAD.azimut(), 0);
		Assert.assertEquals("na sever", SmerSkoku.SEVER.text());
		Assert.assertEquals("na východ", SmerSkoku.VYCHOD.text());
		Assert.assertEquals("na jih", SmerSkoku.JIH.text());
		Assert.assertEquals("na západ", SmerSkoku.ZAPAD.text());
	}

	@Test
	public void presnaHranicePetaCtyricetiStupnuPatriDovnitr() {
		Assert.assertTrue(SkokVeSmeru.jeVKuzelu(45, 0));
		Assert.assertTrue(SkokVeSmeru.jeVKuzelu(315, 0));
		Assert.assertTrue(SkokVeSmeru.jeVKuzelu(135, 90));
		Assert.assertTrue(SkokVeSmeru.jeVKuzelu(0, 315));
		Assert.assertEquals(45, SkokVeSmeru.rozdilUhlu(315, 0), 0);
		Assert.assertFalse(SkokVeSmeru.jeVKuzelu(45.000001, 0));
		Assert.assertFalse(SkokVeSmeru.jeVKuzelu(314.999999, 0));
		Assert.assertTrue(SkokVeSmeru.jeVKuzelu(44.999999, 0));
	}

	@Test
	public void vPolickuSeHledaJenMeziKandidatyPolicka() {
		final Bod bliz = GeometrieSektoru.bod(LAT, LON, 0, 1000, Model.KOULE);
		final Bod dal = GeometrieSektoru.bod(LAT, LON, 10, 3000, Model.KOULE);
		final VysledekPolicek v = new VysledekPolicek(81, java.util.Arrays.asList("D1/T1", "D2/T2"),
				java.util.Arrays.asList(new KandidatPolicka("BLIZ", bliz.lat, bliz.lon, "D1/T1", "D1/T1", null), new KandidatPolicka("DAL", dal.lat, dal.lon, "D2/T2", "D2/T2", null)));
		Assert.assertEquals("BLIZ", SkokVeSmeru.proPolicka(v, null, LAT, LON, SEVER, null).id());
		Assert.assertEquals("DAL", SkokVeSmeru.proPolicka(v, "D2/T2", LAT, LON, SEVER, null).id());
		Assert.assertEquals("BLIZ", SkokVeSmeru.proPolicka(v, "D1/T1", LAT, LON, SEVER, null).id());
		Assert.assertNull("v políčku nikdo v kuželu není", SkokVeSmeru.proPolicka(v, "D2/T2", LAT, LON, JIH, null));
		Assert.assertNull("políčko bez kandidátů", SkokVeSmeru.proPolicka(v, "D9/T9", LAT, LON, SEVER, null));
	}
}
