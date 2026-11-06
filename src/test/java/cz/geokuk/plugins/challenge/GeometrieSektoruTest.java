package cz.geokuk.plugins.challenge;

import java.util.BitSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.plugins.challenge.GeometrieSektoru.Bod;
import cz.geokuk.plugins.challenge.GeometrieSektoru.Model;
import cz.geokuk.plugins.challenge.GeometrieSektoru.Stav;
import cz.geokuk.plugins.challenge.GeometrieSektoru.Usek;

public class GeometrieSektoruTest {

	private static final double LAT = 50.0875;
	private static final double LON = 14.4208;

	private static VysledekSektoru vysledek(final BitSet obsazene, final BitSet nejiste) {
		return new VysledekSektoru(obsazene, nejiste, new VysledekSektoru.Kandidati[360], obsazene.cardinality());
	}

	private static BitSet bity(final int... sektory) {
		final BitSet b = new BitSet(360);
		for (final int s : sektory) {
			b.set(s);
		}
		return b;
	}

	/** Nejmenší úhlový rozdíl dvou azimutů ve stupních. */
	private static double rozdil(final double a, final double b) {
		final double d = Math.abs(a - b) % 360;
		return Math.min(d, 360 - d);
	}

	@Test
	public void bodNaKouliMaZadanouVzdalenostAAzimut() {
		for (final double az : new double[] { 0, 45, 90, 133.3, 180, 270.5, 359.9 }) {
			for (final double m : new double[] { 1_000, 10_000, 250_000 }) {
				final Bod b = GeometrieSektoru.bod(LAT, LON, az, m, Model.KOULE);
				Assert.assertEquals(m, Geodezie.vzdalenostKoule(LAT, LON, b.lat, b.lon), m * 1e-9 + 1e-6);
				Assert.assertEquals(0, rozdil(az, Geodezie.azimutKoule(LAT, LON, b.lat, b.lon)), 1e-7);
			}
		}
	}

	@Test
	public void bodNaElipsoiduSedisVincentymVReferencniUloze() {
		// Flinders Peak → Buninyong (Vincenty 1975): azimut 306°52'05,37", vzdálenost 54 972,271 m.
		final double az = 306 + 52 / 60.0 + 5.37 / 3600;
		final Bod b = GeometrieSektoru.bod(-37.95103341, 144.42486789, az, 54_972.271, Model.ELIPSOID);
		Assert.assertEquals(-37.65282113, b.lat, 2e-7);
		Assert.assertEquals(143.92649552, b.lon, 2e-7);
	}

	@Test
	public void bodNaElipsoiduMaAzimutShodnyseVstupem() {
		for (final double lat : new double[] { 0, 50.0875, -33.9, 70 }) {
			for (final double az : new double[] { 0.3, 45, 90.7, 180, 213.4, 359.97 }) {
				for (final double m : new double[] { 2_000, 10_000, 100_000 }) {
					final Bod b = GeometrieSektoru.bod(lat, 14.4, az, m, Model.ELIPSOID);
					Assert.assertEquals(lat + " " + az + " " + m, az, Geodezie.azimutElipsoid(lat, 14.4, b.lat, b.lon), 1e-6);
				}
			}
		}
	}

	@Test
	public void bodVNulovejVzdalenostiJeStred() {
		final Bod b = GeometrieSektoru.bod(LAT, LON, 123, 0, Model.KOULE);
		Assert.assertEquals(LAT, b.lat, 1e-12);
		Assert.assertEquals(LON, b.lon, 1e-12);
	}

	@Test
	public void delkaSePrecteZaAntimeridian() {
		final Bod b = GeometrieSektoru.bod(10, 179.99, 90, 10_000, Model.KOULE);
		Assert.assertTrue(b.lon + "", b.lon < -179 && b.lon >= -180);
	}

	@Test
	public void obloukJdePoSmeruHodinPresSever() {
		final List<Bod> o = GeometrieSektoru.oblouk(LAT, LON, 359, 361, 10_000, Model.KOULE);
		Assert.assertEquals(3, o.size());
		Assert.assertEquals(359, Geodezie.azimutKoule(LAT, LON, o.get(0).lat, o.get(0).lon), 1e-7);
		Assert.assertEquals(0, rozdil(0, Geodezie.azimutKoule(LAT, LON, o.get(1).lat, o.get(1).lon)), 1e-7);
		Assert.assertEquals(1, Geodezie.azimutKoule(LAT, LON, o.get(2).lat, o.get(2).lon), 1e-7);
	}

	@Test
	public void obloukSKratkouSirkouMaAsponDvaBody() {
		Assert.assertEquals(2, GeometrieSektoru.oblouk(LAT, LON, 10, 10.2, 5_000, Model.KOULE).size());
		Assert.assertEquals(1 + 3, GeometrieSektoru.oblouk(LAT, LON, 10, 12.5, 5_000, Model.KOULE).size());
		final List<Bod> cela = GeometrieSektoru.oblouk(LAT, LON, 0, 360, 5_000, Model.KOULE);
		Assert.assertEquals(361, cela.size());
		Assert.assertEquals(cela.get(0).lat, cela.get(360).lat, 1e-9);
		Assert.assertEquals(cela.get(0).lon, cela.get(360).lon, 1e-9);
	}

	@Test
	public void klinMaVnejsiAVnitrniObloukAUzaviraSe() {
		final List<Bod> k = GeometrieSektoru.klin(LAT, LON, 213, 8_000, 12_000, Model.KOULE);
		Assert.assertEquals(4, k.size());
		Assert.assertEquals(8_000, Geodezie.vzdalenostKoule(LAT, LON, k.get(2).lat, k.get(2).lon), 1e-3);
		Assert.assertEquals(12_000, Geodezie.vzdalenostKoule(LAT, LON, k.get(0).lat, k.get(0).lon), 1e-3);
		Assert.assertEquals(213, Geodezie.azimutKoule(LAT, LON, k.get(0).lat, k.get(0).lon), 1e-7);
		Assert.assertEquals(214, Geodezie.azimutKoule(LAT, LON, k.get(1).lat, k.get(1).lon), 1e-7);
		Assert.assertEquals(214, Geodezie.azimutKoule(LAT, LON, k.get(2).lat, k.get(2).lon), 1e-7);
		Assert.assertEquals(213, Geodezie.azimutKoule(LAT, LON, k.get(3).lat, k.get(3).lon), 1e-7);
	}

	@Test
	public void klinOdStreduKonciVeStredu() {
		final List<Bod> k = GeometrieSektoru.klin(LAT, LON, 0, 0, 10_000, Model.KOULE);
		final Bod posledni = k.get(k.size() - 1);
		Assert.assertEquals(LAT, posledni.lat, 1e-12);
		Assert.assertEquals(LON, posledni.lon, 1e-12);
		Assert.assertEquals(3, k.size());
	}

	@Test
	public void klinSektoru359KonciNaSeveru() {
		final List<Bod> k = GeometrieSektoru.klin(LAT, LON, 359, 0, 10_000, Model.KOULE);
		Assert.assertEquals(359, Geodezie.azimutKoule(LAT, LON, k.get(0).lat, k.get(0).lon), 1e-7);
		final Bod b = k.get(1);
		Assert.assertEquals(0, rozdil(0, Geodezie.azimutKoule(LAT, LON, b.lat, b.lon)), 1e-7);
	}

	@Test
	public void hraniceSousednichKlinuSeShoduji() {
		final List<Bod> a = GeometrieSektoru.klin(LAT, LON, 10, 5_000, 6_000, Model.ELIPSOID);
		final List<Bod> b = GeometrieSektoru.klin(LAT, LON, 11, 5_000, 6_000, Model.ELIPSOID);
		Assert.assertEquals(a.get(1).lat, b.get(0).lat, 1e-12);
		Assert.assertEquals(a.get(1).lon, b.get(0).lon, 1e-12);
	}

	@Test
	public void svetoveStranyJsouNa0_90_180_270() {
		final List<Bod> s = GeometrieSektoru.svetoveStrany(LAT, LON, 10_000, Model.KOULE);
		Assert.assertEquals(4, s.size());
		Assert.assertTrue(s.get(0).lat > LAT);
		Assert.assertEquals(LON, s.get(0).lon, 1e-9);
		Assert.assertTrue(s.get(1).lon > LON);
		Assert.assertTrue(s.get(2).lat < LAT);
		Assert.assertTrue(s.get(3).lon < LON);
	}

	@Test
	public void paprsekMaDvaBodyVPozadovanychPoloměrech() {
		final List<Bod> p = GeometrieSektoru.paprsek(LAT, LON, 45, 1_000, 9_000, Model.KOULE);
		Assert.assertEquals(2, p.size());
		Assert.assertEquals(1_000, Geodezie.vzdalenostKoule(LAT, LON, p.get(0).lat, p.get(0).lon), 1e-3);
		Assert.assertEquals(9_000, Geodezie.vzdalenostKoule(LAT, LON, p.get(1).lat, p.get(1).lon), 1e-3);
	}

	// ---- prstenec

	private static int soucet(final List<Usek> u) {
		int n = 0;
		for (final Usek x : u) {
			n += x.pocetSektoru();
		}
		return n;
	}

	@Test
	public void vseStejneDaJedenUsekSCelouKruznici() {
		final BitSet vse = new BitSet(360);
		vse.set(0, 360);
		final List<Usek> u = GeometrieSektoru.prstenec(vysledek(vse, new BitSet(360)), LAT, LON, 10_000, Model.KOULE);
		Assert.assertEquals(1, u.size());
		Assert.assertEquals(360, u.get(0).pocetSektoru());
		Assert.assertEquals(Stav.SPLNENY, u.get(0).stav());
		Assert.assertEquals(361, u.get(0).body().size());
		final List<Usek> zadne = GeometrieSektoru.prstenec(vysledek(new BitSet(360), new BitSet(360)), LAT, LON, 10_000, Model.KOULE);
		Assert.assertEquals(Stav.CHYBI, zadne.get(0).stav());
	}

	@Test
	public void chybejiciSektorySeSlouciDoUseku() {
		final BitSet obs = new BitSet(360);
		obs.set(0, 360);
		obs.clear(100, 103);
		obs.clear(200);
		final List<Usek> u = GeometrieSektoru.prstenec(vysledek(obs, new BitSet(360)), LAT, LON, 10_000, Model.KOULE);
		Assert.assertEquals(360, soucet(u));
		int chybi = 0;
		for (final Usek x : u) {
			if (x.stav() == Stav.CHYBI) {
				chybi++;
				Assert.assertTrue(x.zacatekSektor() == 100 && x.pocetSektoru() == 3 || x.zacatekSektor() == 200 && x.pocetSektoru() == 1);
			}
		}
		Assert.assertEquals(2, chybi);
		Assert.assertEquals(4, u.size());
	}

	@Test
	public void usekPresSeverJeJedenAMaVsechnyBody() {
		final BitSet obs = new BitSet(360);
		obs.set(0, 360);
		obs.clear(358, 360);
		obs.clear(0, 2);
		final List<Usek> u = GeometrieSektoru.prstenec(vysledek(obs, new BitSet(360)), LAT, LON, 10_000, Model.KOULE);
		Assert.assertEquals(2, u.size());
		Usek chybi = null;
		for (final Usek x : u) {
			if (x.stav() == Stav.CHYBI) {
				chybi = x;
			}
		}
		Assert.assertNotNull(chybi);
		Assert.assertEquals(358, chybi.zacatekSektor());
		Assert.assertEquals(4, chybi.pocetSektoru());
		Assert.assertEquals(5, chybi.body().size());
	}

	@Test
	public void nejistySektorMaSvujStav() {
		final BitSet obs = new BitSet(360);
		obs.set(0, 360);
		final BitSet nej = bity(50);
		final List<Usek> u = GeometrieSektoru.prstenec(vysledek(obs, nej), LAT, LON, 10_000, Model.KOULE);
		boolean nasel = false;
		for (final Usek x : u) {
			if (x.stav() == Stav.NEJISTY) {
				nasel = true;
				Assert.assertEquals(50, x.zacatekSektor());
				Assert.assertEquals(1, x.pocetSektoru());
			}
		}
		Assert.assertTrue(nasel);
		Assert.assertEquals(360, soucet(u));
		Assert.assertEquals(Stav.NEJISTY, GeometrieSektoru.stavSektoru(vysledek(obs, nej), 50));
	}

	// ---- výběr sektoru

	@Test
	public void sektorBoduPodleAzimutuAPoloměru() {
		for (final Model m : Model.values()) {
			for (final int s : new int[] { 0, 1, 90, 213, 359 }) {
				final Bod b = GeometrieSektoru.bod(LAT, LON, s + 0.5, 6_000, m);
				Assert.assertEquals(m + " " + s, s, GeometrieSektoru.sektorBodu(LAT, LON, b.lat, b.lon, 1_000, 10_000, m));
			}
		}
	}

	@Test
	public void mimoPrstenecJeMinusJedna() {
		final Bod blizko = GeometrieSektoru.bod(LAT, LON, 45.5, 500, Model.KOULE);
		final Bod daleko = GeometrieSektoru.bod(LAT, LON, 45.5, 20_000, Model.KOULE);
		Assert.assertEquals(-1, GeometrieSektoru.sektorBodu(LAT, LON, blizko.lat, blizko.lon, 1_000, 10_000, Model.KOULE));
		Assert.assertEquals(-1, GeometrieSektoru.sektorBodu(LAT, LON, daleko.lat, daleko.lon, 1_000, 10_000, Model.KOULE));
		Assert.assertEquals(-1, GeometrieSektoru.sektorBodu(LAT, LON, 99, 0, 1_000, 10_000, Model.KOULE));
	}

	@Test
	public void bodUvnitrKlinuPatriDoStejnehoSektoru() {
		final List<Bod> k = GeometrieSektoru.klin(LAT, LON, 77, 3_000, 9_000, Model.KOULE);
		double lat = 0;
		double lon = 0;
		for (final Bod b : k) {
			lat += b.lat / k.size();
			lon += b.lon / k.size();
		}
		Assert.assertEquals(77, GeometrieSektoru.sektorBodu(LAT, LON, lat, lon, 3_000, 9_000, Model.KOULE));
	}

	@Test
	public void rezimSeVybereZPravidel() {
		Assert.assertEquals(Model.KOULE, Model.zPravidel(Challenge.ZdrojPravidel.PGC));
		Assert.assertEquals(Model.ELIPSOID, Model.zPravidel(Challenge.ZdrojPravidel.STATOR));
	}

	@Test
	public void neplatnyVstupSeOdmitne() {
		final Runnable[] chyby = { () -> GeometrieSektoru.bod(99, 0, 0, 1, Model.KOULE), () -> GeometrieSektoru.bod(LAT, LON, 0, -1, Model.KOULE),
				() -> GeometrieSektoru.klin(LAT, LON, 360, 0, 1, Model.KOULE), () -> GeometrieSektoru.klin(LAT, LON, -1, 0, 1, Model.KOULE),
				() -> GeometrieSektoru.klin(LAT, LON, 5, 10, 10, Model.KOULE), () -> GeometrieSektoru.klin(LAT, LON, 5, -1, 10, Model.KOULE),
				() -> GeometrieSektoru.paprsek(LAT, LON, 0, 5, 1, Model.KOULE), () -> GeometrieSektoru.stavSektoru(vysledek(new BitSet(360), new BitSet(360)), 360) };
		for (int i = 0; i < chyby.length; i++) {
			try {
				chyby[i].run();
				Assert.fail("mělo selhat č. " + i);
			} catch (final IllegalArgumentException e) {
				// očekáváno
			}
		}
	}

	// ---- oblouk: okrajové šířky

	@Test
	public void obloukBezRozdiluJeJedenBod() {
		final List<Bod> o = GeometrieSektoru.oblouk(LAT, LON, 10, 10, 5_000, Model.KOULE);
		Assert.assertEquals(1, o.size());
		Assert.assertEquals(10, Geodezie.azimutKoule(LAT, LON, o.get(0).lat, o.get(0).lon), 1e-7);
	}

	@Test
	public void viceNezJednaKruhVMultipluDavaCelouKruznici() {
		Assert.assertEquals(361, GeometrieSektoru.oblouk(LAT, LON, 10, 730, 5_000, Model.KOULE).size());
		Assert.assertEquals(361, GeometrieSektoru.oblouk(LAT, LON, 10, -350, 5_000, Model.KOULE).size());
		Assert.assertEquals(361, GeometrieSektoru.oblouk(LAT, LON, 0, 360, 5_000, Model.KOULE).size());
	}

	@Test
	public void zapornyRozdilSeBereJakoObchazkaPoSmeruHodin() {
		final List<Bod> o = GeometrieSektoru.oblouk(LAT, LON, 10, 5, 5_000, Model.KOULE);
		Assert.assertEquals(356, o.size());
		Assert.assertEquals(0, rozdil(10, Geodezie.azimutKoule(LAT, LON, o.get(0).lat, o.get(0).lon)), 1e-7);
		Assert.assertEquals(0, rozdil(5, Geodezie.azimutKoule(LAT, LON, o.get(355).lat, o.get(355).lon)), 1e-7);
		Assert.assertEquals(0, rozdil(190, Geodezie.azimutKoule(LAT, LON, o.get(180).lat, o.get(180).lon)), 1e-7);
	}

	// ---- antimeridián

	private static double nejvetsiSkokDelky(final List<Bod> body) {
		double max = 0;
		for (int i = 1; i < body.size(); i++) {
			max = Math.max(max, Math.abs(body.get(i).lon - body.get(i - 1).lon));
		}
		return max;
	}

	@Test
	public void obloukPresAntimeridianMaSkokAOdvinutiHoVyrovna() {
		final List<Bod> o = GeometrieSektoru.oblouk(0, 179.9, 0, 360, 50_000, Model.KOULE);
		Assert.assertTrue("skok " + nejvetsiSkokDelky(o), nejvetsiSkokDelky(o) > 300);
		final List<Bod> v = GeometrieSektoru.odvinuteDelky(o);
		Assert.assertEquals(o.size(), v.size());
		Assert.assertTrue("skok " + nejvetsiSkokDelky(v), nejvetsiSkokDelky(v) < 1);
		double max = -1000;
		for (final Bod b : v) {
			max = Math.max(max, b.lon);
		}
		Assert.assertTrue("délka přes 180: " + max, max > 180);
		for (int i = 0; i < o.size(); i++) {
			Assert.assertEquals(o.get(i).lat, v.get(i).lat, 0);
			Assert.assertEquals(0, rozdil(o.get(i).lon, v.get(i).lon), 1e-9);
		}
	}

	@Test
	public void odvinutiZapadniStranyAntimeridianuJdeDruhymSmerem() {
		final List<Bod> o = GeometrieSektoru.oblouk(0, -179.9, 0, 360, 50_000, Model.KOULE);
		final List<Bod> v = GeometrieSektoru.odvinuteDelky(o);
		Assert.assertTrue(nejvetsiSkokDelky(v) < 1);
	}

	@Test
	public void odvinutiNeMeniSouvisleBodyAZvladaPrazdnySeznam() {
		final List<Bod> o = GeometrieSektoru.oblouk(LAT, LON, 0, 360, 10_000, Model.KOULE);
		final List<Bod> v = GeometrieSektoru.odvinuteDelky(o);
		for (int i = 0; i < o.size(); i++) {
			Assert.assertEquals(o.get(i).lon, v.get(i).lon, 0);
		}
		Assert.assertTrue(GeometrieSektoru.odvinuteDelky(new java.util.ArrayList<Bod>()).isEmpty());
	}

	// ---- sektorBodu: model, hrana, střed

	@Test
	public void bodVeStreduNemaSektor() {
		Assert.assertEquals(-1, GeometrieSektoru.sektorBodu(LAT, LON, LAT, LON, 0, 10_000, Model.KOULE));
		Assert.assertEquals(-1, GeometrieSektoru.sektorBodu(LAT, LON, LAT, LON, 0, 10_000, Model.ELIPSOID));
	}

	@Test
	public void hranicePoloměruPatriDovnitr() {
		for (final Model m : Model.values()) {
			final Bod b = GeometrieSektoru.bod(LAT, LON, 100.5, 7_000, m);
			final double d = m == Model.ELIPSOID ? Geodezie.vzdalenostElipsoid(LAT, LON, b.lat, b.lon) : Geodezie.vzdalenostKoule(LAT, LON, b.lat, b.lon);
			Assert.assertEquals(m + " dolní hrana", 100, GeometrieSektoru.sektorBodu(LAT, LON, b.lat, b.lon, d, d + 1, m));
			Assert.assertEquals(m + " horní hrana", 100, GeometrieSektoru.sektorBodu(LAT, LON, b.lat, b.lon, d - 1, d, m));
			Assert.assertEquals(m + " těsně mimo", -1, GeometrieSektoru.sektorBodu(LAT, LON, b.lat, b.lon, d + 0.01, d + 1, m));
			Assert.assertEquals(m + " těsně mimo", -1, GeometrieSektoru.sektorBodu(LAT, LON, b.lat, b.lon, d - 1, d - 0.01, m));
		}
	}

	@Test
	public void poloměrSeMeriVModeluBoduNaElipsoidu() {
		// Na elipsoidu je bod 50 km od středu po kouli jen ~49,91 km, rozdíl asi 86 m.
		final Bod b = GeometrieSektoru.bod(LAT, LON, 100.5, 50_000, Model.ELIPSOID);
		Assert.assertEquals(100, GeometrieSektoru.sektorBodu(LAT, LON, b.lat, b.lon, 49_950, 50_050, Model.ELIPSOID));
		Assert.assertEquals(-1, GeometrieSektoru.sektorBodu(LAT, LON, b.lat, b.lon, 49_950, 50_050, Model.KOULE));
		Assert.assertEquals(-1, GeometrieSektoru.sektorBodu(LAT, LON, b.lat, b.lon, 0.5, 49_990, Model.ELIPSOID));
		Assert.assertEquals(100, GeometrieSektoru.sektorBodu(LAT, LON, b.lat, b.lon, 0.5, 49_990, Model.KOULE));
	}

	@Test
	public void azimutSeMeriVModeluSektorBoduRozlisujeKouliAElipsoid() {
		int rozlisenych = 0;
		for (int s = 0; s < 360; s++) {
			final Bod b = GeometrieSektoru.bod(LAT, LON, s + 0.01, 200_000, Model.ELIPSOID);
			final int koule = Geodezie.sektor(Geodezie.azimutKoule(LAT, LON, b.lat, b.lon));
			if (koule != s) {
				rozlisenych++;
				Assert.assertEquals("elipsoid " + s, s, GeometrieSektoru.sektorBodu(LAT, LON, b.lat, b.lon, 1_000, 300_000, Model.ELIPSOID));
				Assert.assertEquals("koule " + s, koule, GeometrieSektoru.sektorBodu(LAT, LON, b.lat, b.lon, 1_000, 300_000, Model.KOULE));
			}
		}
		Assert.assertTrue("žádný bod, kde se koule a elipsoid liší: " + rozlisenych, rozlisenych > 0);
	}

	@Test
	public void bodNaElipsoiduSeShodujeSInverznimVypoctemNaDlouheVzdalenosti() {
		final double[] vzdalenosti = { 100_000, 1_000_000, 5_000_000, 12_000_000 };
		final double[][] stredy = { { 50.0875, 14.4208 }, { -33.9, 151.2 }, { 5.0, -70.0 }, { 70.0, 25.0 } };
		for (final double[] st : stredy) {
			for (final double metru : vzdalenosti) {
				for (int az = 0; az < 360; az += 37) {
					final Bod b = GeometrieSektoru.bod(st[0], st[1], az, metru, Model.ELIPSOID);
					final double d = Geodezie.vzdalenostElipsoid(st[0], st[1], b.lat, b.lon);
					if (Double.isNaN(d)) {
						continue; // téměř protilehlé body, inverzní úloha nekonverguje
					}
					Assert.assertEquals(st[0] + "," + st[1] + " az " + az + " " + metru + " m", metru, d, 0.001);
					Assert.assertEquals(st[0] + "," + st[1] + " az " + az + " " + metru + " m", 0, rozdil(az, Geodezie.azimutElipsoid(st[0], st[1], b.lat, b.lon)), 1e-6);
				}
			}
		}
	}

	@Test
	public void delkaBoduSeVraciDoRozsahuAPriMeziMinus180ZustaneMinus180() {
		Assert.assertEquals(-180, GeometrieSektoru.bod(10, -180, 0, 0, Model.KOULE).lon, 1e-9);
		final Bod vychod = GeometrieSektoru.bod(10, 179.9, 90, 100_000, Model.KOULE);
		Assert.assertTrue("přes antimeridián na východ " + vychod, vychod.lon < -179 && vychod.lon >= -180);
		final Bod zapad = GeometrieSektoru.bod(10, -179.9, 270, 100_000, Model.KOULE);
		Assert.assertTrue("přes antimeridián na západ " + zapad, zapad.lon > 179 && zapad.lon <= 180);
		final Bod vychodElipsoid = GeometrieSektoru.bod(10, 179.9, 90, 100_000, Model.ELIPSOID);
		Assert.assertTrue(vychodElipsoid.toString(), vychodElipsoid.lon < -179 && vychodElipsoid.lon >= -180);
		final Bod zapadElipsoid = GeometrieSektoru.bod(10, -179.9, 270, 100_000, Model.ELIPSOID);
		Assert.assertTrue(zapadElipsoid.toString(), zapadElipsoid.lon > 179 && zapadElipsoid.lon <= 180);
	}

	@Test
	public void pocetBoduObloukuZavisiNaCelemNasobkuKroku() {
		Assert.assertEquals("3° = 3 úseky", 4, GeometrieSektoru.oblouk(LAT, LON, 10, 13, 1000, Model.KOULE).size());
		Assert.assertEquals("3,5° = 4 úseky", 5, GeometrieSektoru.oblouk(LAT, LON, 10, 13.5, 1000, Model.KOULE).size());
		Assert.assertEquals("1° = 1 úsek", 2, GeometrieSektoru.oblouk(LAT, LON, 359, 360, 1000, Model.KOULE).size());
		Assert.assertEquals("kružnice 360° = 360 úseků", 361, GeometrieSektoru.oblouk(LAT, LON, 0, 360, 1000, Model.KOULE).size());
	}

	@Test
	public void odvinutiDelekNechaSkokPresne180AleVyrovnaVetsi() {
		final List<Bod> ctyri = java.util.Arrays.asList(new Bod(0, 0), new Bod(0, 180), new Bod(0, -180), new Bod(0, 0));
		final List<Bod> v = GeometrieSektoru.odvinuteDelky(ctyri);
		Assert.assertEquals(0, v.get(0).lon, 0);
		Assert.assertEquals("rozdíl přesně 180 se nemění", 180, v.get(1).lon, 0);
		Assert.assertEquals("-180 je od 180 vzdáleno 360, posune se o 360", 180, v.get(2).lon, 0);
		Assert.assertEquals("rozdíl -180 od 180 se nemění", 0, v.get(3).lon, 0);
		final List<Bod> opacne = GeometrieSektoru.odvinuteDelky(java.util.Arrays.asList(new Bod(0, 0), new Bod(0, -180), new Bod(0, 179)));
		Assert.assertEquals("rozdíl přesně -180 se nemění", -180, opacne.get(1).lon, 0);
		Assert.assertEquals(-181, opacne.get(2).lon, 0);
	}

	@Test
	public void prvniBodSeNeodvinuje() {
		final List<Bod> v = GeometrieSektoru.odvinuteDelky(java.util.Arrays.asList(new Bod(0, 270), new Bod(0, 275)));
		Assert.assertEquals(270, v.get(0).lon, 0);
		Assert.assertEquals(275, v.get(1).lon, 0);
		Assert.assertTrue(GeometrieSektoru.odvinuteDelky(java.util.Collections.<Bod> emptyList()).isEmpty());
	}

	@Test
	public void splnenyJedinySektorNulaDaDvaUsekyAProstrencePresSeverSpravneSpocitaDelku() {
		final List<Usek> u = GeometrieSektoru.prstenec(vysledek(bity(0), new BitSet(360)), LAT, LON, 10_000, Model.KOULE);
		Assert.assertEquals(2, u.size());
		Assert.assertEquals(0, u.get(0).zacatekSektor());
		Assert.assertEquals(1, u.get(0).pocetSektoru());
		Assert.assertEquals(Stav.SPLNENY, u.get(0).stav());
		Assert.assertEquals(1, u.get(1).zacatekSektor());
		Assert.assertEquals(359, u.get(1).pocetSektoru());
		Assert.assertEquals(Stav.CHYBI, u.get(1).stav());
		Assert.assertEquals(360, u.get(1).body().size());
	}

	@Test
	public void splnenyUsekPresSeverZacinaVePrvnimSektoruAMaCelouDelku() {
		final BitSet obs = new BitSet(360);
		obs.set(350, 360);
		obs.set(0, 10);
		final List<Usek> u = GeometrieSektoru.prstenec(vysledek(obs, new BitSet(360)), LAT, LON, 10_000, Model.KOULE);
		Assert.assertEquals(2, u.size());
		Assert.assertEquals(10, u.get(0).zacatekSektor());
		Assert.assertEquals(340, u.get(0).pocetSektoru());
		Assert.assertEquals(Stav.CHYBI, u.get(0).stav());
		Assert.assertEquals(341, u.get(0).body().size());
		Assert.assertEquals(350, u.get(1).zacatekSektor());
		Assert.assertEquals(20, u.get(1).pocetSektoru());
		Assert.assertEquals(Stav.SPLNENY, u.get(1).stav());
		Assert.assertEquals(21, u.get(1).body().size());
	}

	@Test
	public void sektorBoduOdmitneNeplatnyStredAPoloměry() {
		final double[][] spatne = { { 91, 14, 0, 1000 }, { 50, 181, 0, 1000 }, { 50, 14, -1, 1000 }, { 50, 14, 1000, 1000 }, { 50, 14, 0, Double.POSITIVE_INFINITY } };
		for (final double[] a : spatne) {
			try {
				GeometrieSektoru.sektorBodu(a[0], a[1], 50.1, 14.1, a[2], a[3], Model.KOULE);
				Assert.fail(java.util.Arrays.toString(a));
			} catch (final IllegalArgumentException e) {
				// očekávané
			}
		}
	}

	@Test
	public void bodMaPopisSeSouradnicemVRetezci() {
		final Bod b = GeometrieSektoru.bod(10, 20, 0, 0, Model.KOULE);
		Assert.assertEquals("10.0, 20.0", b.toString());
	}

	private static Bod b(final double lat, final double lon) {
		return new Bod(lat, lon);
	}

	@Test
	public void odvinutiRozlisujeSkokOd180Do190() {
		final List<Bod> dopredu = java.util.Arrays.asList(b(0, 0), b(0, 185));
		Assert.assertEquals(-175, GeometrieSektoru.odvinuteDelky(dopredu).get(1).lon, 1e-9);
		final List<Bod> dozadu = java.util.Arrays.asList(b(0, 0), b(0, -185));
		Assert.assertEquals(175, GeometrieSektoru.odvinuteDelky(dozadu).get(1).lon, 1e-9);
		final List<Bod> presne = java.util.Arrays.asList(b(0, 0), b(0, 180));
		Assert.assertEquals(180, GeometrieSektoru.odvinuteDelky(presne).get(1).lon, 1e-9);
		final List<Bod> presneZpet = java.util.Arrays.asList(b(0, 0), b(0, -180));
		Assert.assertEquals(-180, GeometrieSektoru.odvinuteDelky(presneZpet).get(1).lon, 1e-9);
	}

	@Test
	public void odvinutiObloukuJdouciehoNaVychodPresAntimeridian() {
		final List<Bod> vychod = java.util.Arrays.asList(b(0, 179.0), b(0, -179.0), b(0, -178.0));
		final List<Bod> v = GeometrieSektoru.odvinuteDelky(vychod);
		Assert.assertEquals(179, v.get(0).lon, 1e-9);
		Assert.assertEquals(181, v.get(1).lon, 1e-9);
		Assert.assertEquals(182, v.get(2).lon, 1e-9);
		final List<Bod> zapad = java.util.Arrays.asList(b(0, -179.0), b(0, 179.0), b(0, 178.0));
		final List<Bod> z = GeometrieSektoru.odvinuteDelky(zapad);
		Assert.assertEquals(-179, z.get(0).lon, 1e-9);
		Assert.assertEquals(-181, z.get(1).lon, 1e-9);
		Assert.assertEquals(-182, z.get(2).lon, 1e-9);
	}

	@Test
	public void protilehlyBodElipsoiduMaNaNAKouleSeVezmeZaloha() {
		Assert.assertTrue(Double.isNaN(Geodezie.vzdalenostElipsoid(0, 0, 0, 179.5)));
		final double azimutKoule = Geodezie.azimutKoule(0, 0, 0, 179.5);
		Assert.assertEquals(Geodezie.sektor(azimutKoule), GeometrieSektoru.sektorBodu(0, 0, 0, 179.5, 1_000, 25_000_000, Model.ELIPSOID));
		Assert.assertEquals(-1, GeometrieSektoru.sektorBodu(0, 0, 0, 179.5, 1_000, 10_000_000, Model.ELIPSOID));
	}

	@Test
	public void hraniceDovnitrJeNaMilimetr() {
		final Bod bod = GeometrieSektoru.bod(LAT, LON, 100.5, 50_000, Model.ELIPSOID);
		Assert.assertEquals(100, GeometrieSektoru.sektorBodu(LAT, LON, bod.lat, bod.lon, 1_000, 50_000, Model.ELIPSOID));
		Assert.assertEquals(100, GeometrieSektoru.sektorBodu(LAT, LON, bod.lat, bod.lon, 50_000, 60_000, Model.ELIPSOID));
		Assert.assertEquals(-1, GeometrieSektoru.sektorBodu(LAT, LON, bod.lat, bod.lon, 1_000, 49_999.99, Model.ELIPSOID));
		Assert.assertEquals(-1, GeometrieSektoru.sektorBodu(LAT, LON, bod.lat, bod.lon, 50_000.01, 60_000, Model.ELIPSOID));
	}
}
