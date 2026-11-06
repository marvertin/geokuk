package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.plugins.challenge.Challenge.ZdrojPravidel;
import cz.geokuk.plugins.challenge.GeometrieSektoru.Bod;
import cz.geokuk.plugins.challenge.GeometrieSektoru.Model;
import cz.geokuk.plugins.challenge.GeometrieSektoru.Usek;
import cz.geokuk.plugins.challenge.OblastPolygony.Cast;
import cz.geokuk.plugins.challenge.OblastPolygony.Okraj;

/** Mnohoúhelníky oblastí, geometrie sektorů a výpočet sektorů na pólech, u datové čáry, na extrémních vzdálenostech a s neplatnými čísly. */
public class GeometrieOdolnostTest {

	private static final double NEPLATNA[] = { Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 90.0001, -90.0001 };

	private static OblastPolygony oblast(final Okraj... okraje) {
		final List<Cast> casti = new ArrayList<>();
		for (final Okraj o : okraje) {
			casti.add(new Cast(o, Collections.<Okraj> emptyList()));
		}
		return new OblastPolygony("t", "v", casti);
	}

	private static Okraj okraj(final double[] lat, final double[] lon) {
		return new Okraj(lat, lon);
	}

	// ---- OblastPolygony

	@Test
	public void okrajSNaNNekonecnemNeboNevalidnimiSouradnicemSeOdmitne() {
		for (final double zla : new double[] { Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 90.1, -90.1 }) {
			try {
				okraj(new double[] { 10, zla, 12 }, new double[] { 1, 2, 3 });
				Assert.fail("šířka " + zla);
			} catch (final IllegalArgumentException e) {
				// očekávané
			}
		}
		for (final double zla : new double[] { Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 180.1, -180.1 }) {
			try {
				okraj(new double[] { 10, 11, 12 }, new double[] { 1, zla, 3 });
				Assert.fail("délka " + zla);
			} catch (final IllegalArgumentException e) {
				// očekávané
			}
		}
	}

	@Test
	public void hranaSRozdilemDelekNad180StupnuSeOdmitneUzOKousekAHraniceSePripousti() {
		final double[][] spatne = { { -100, 80.0001, -100 }, { -90.25, 90.25, -90.25 }, { -90.5, 90.5, -90.5 }, { -90.5001, 90.5001, -90.5001 }, { -120, 60.5, -120 }, { -179, 179, -179 } };
		for (final double[] lon : spatne) {
			try {
				okraj(new double[] { 0, 1, 2 }, lon);
				Assert.fail("délky " + Arrays.toString(lon));
			} catch (final IllegalArgumentException e) {
				Assert.assertTrue(e.getMessage(), e.getMessage().contains("180"));
			}
		}
		final double[][] dobre = { { -90, 90, -90 }, { -100, 80, -100 }, { -179.5, 0.5, -179.5 }, { 0, 180, 0 }, { -180, 0, -180 } };
		for (final double[] lon : dobre) {
			Assert.assertEquals(3, okraj(new double[] { 0, 1, 2 }, lon).pocetBodu());
		}
	}

	@Test
	public void okrajSPrazdnymiNeboNestejnymiPolemiSeOdmitne() {
		for (final int n : new int[] { 0, 1, 2 }) {
			try {
				okraj(new double[n], new double[n]);
				Assert.fail("bodů " + n);
			} catch (final IllegalArgumentException e) {
				// očekávané
			}
		}
		try {
			okraj(new double[] { 1, 2, 3 }, new double[] { 1, 2 });
			Assert.fail();
		} catch (final IllegalArgumentException e) {
			// očekávané
		}
	}

	@Test
	public void okrajSeKopirujeAZmenaVstupnihoPoleHoNeovlivni() {
		final double[] lat = { 10, 10, 11, 11 };
		final double[] lon = { 20, 21, 21, 20 };
		final OblastPolygony o = oblast(okraj(lat, lon));
		Assert.assertTrue(o.obsahuje(10.5, 20.5));
		Arrays.fill(lat, 0);
		Arrays.fill(lon, 0);
		Assert.assertTrue("oblast má vlastní kopii", o.obsahuje(10.5, 20.5));
		Assert.assertFalse(o.obsahuje(0.5, 0.5));
	}

	@Test
	public void degenerovanyMnohouhelnikNicNeobsahujeAnineSelhavaDotaz() {
		final OblastPolygony[] degenerovane = {
				oblast(okraj(new double[] { 10, 10, 10 }, new double[] { 20, 20, 20 })),
				oblast(okraj(new double[] { 10, 11, 12 }, new double[] { 20, 21, 22 })),
				oblast(okraj(new double[] { 10, 10, 10, 10 }, new double[] { 20, 21, 22, 23 })),
				oblast(okraj(new double[] { 10, 12, 10, 12 }, new double[] { 20, 20, 20, 20 })),
				oblast(okraj(new double[] { 10, 10, 11 }, new double[] { 20, 20, 20 })) };
		for (final OblastPolygony o : degenerovane) {
			for (double lat = 9.5; lat <= 12.5; lat += 0.25) {
				for (double lon = 19.5; lon <= 23.5; lon += 0.25) {
					Assert.assertFalse(lat + "," + lon, o.obsahuje(lat, lon));
				}
			}
		}
	}

	@Test
	public void dotazSNaNNekonecnemNeboMimoRozsahVracíFalseAnizVyjimky() {
		final OblastPolygony o = oblast(okraj(new double[] { 10, 10, 11, 11 }, new double[] { 20, 21, 21, 20 }));
		final double[] cisla = { Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 1e300, -1e300, Double.MAX_VALUE, Double.MIN_VALUE };
		for (final double a : cisla) {
			Assert.assertFalse(Double.toString(a), o.obsahuje(a, 20.5));
			Assert.assertFalse(Double.toString(a), o.obsahuje(10.5, a));
			for (final double b : cisla) {
				Assert.assertFalse(a + "," + b, o.obsahuje(a, b));
			}
		}
	}

	@Test
	public void oblastSDvemaVrcholyNaSebeAOpakovanymPosladnimBodemFunguje() {
		final OblastPolygony o = oblast(okraj(new double[] { 10, 10, 10, 11, 11, 10 }, new double[] { 20, 21, 21, 21, 20, 20 }));
		Assert.assertTrue(o.obsahuje(10.5, 20.5));
		Assert.assertFalse(o.obsahuje(11.5, 20.5));
		Assert.assertFalse(o.obsahuje(10.5, 21.5));
	}

	@Test
	public void mnohouhelnikSNejdelsiSouradnicemNaMezichRozsahuFunguje() {
		final OblastPolygony pol = oblast(okraj(new double[] { 85, 85, 90, 90 }, new double[] { 0, 90, 90, 0 }));
		Assert.assertTrue(pol.obsahuje(87, 45));
		Assert.assertFalse(pol.obsahuje(90, 45));
		Assert.assertFalse(pol.obsahuje(84.99, 45));
		final OblastPolygony jih = oblast(okraj(new double[] { -90, -90, -85, -85 }, new double[] { 0, 90, 90, 0 }));
		Assert.assertTrue(jih.obsahuje(-90, 45));
		Assert.assertTrue(jih.obsahuje(-87, 45));
		Assert.assertFalse(jih.obsahuje(-84, 45));
		final OblastPolygony vychod = oblast(okraj(new double[] { 0, 0, 10, 10 }, new double[] { 170, 180, 180, 170 }));
		Assert.assertTrue(vychod.obsahuje(5, 179.999));
		Assert.assertFalse(vychod.obsahuje(5, -179.999));
		final OblastPolygony zapad = oblast(okraj(new double[] { 0, 0, 10, 10 }, new double[] { -180, -170, -170, -180 }));
		Assert.assertTrue(zapad.obsahuje(5, -179.999));
		Assert.assertTrue("levý okraj patří dovnitř", zapad.obsahuje(5, -180));
		Assert.assertFalse(zapad.obsahuje(5, 179.999));
	}

	@Test
	public void mnohouhelnikSeSamoprusecikemSeRovnaPravidluSudehoPoctuPrusecikuJakoPrimyVypocet() {
		// Přesýpací hodiny: dva trojúhelníky protínající se ve středu.
		final Okraj hodiny = okraj(new double[] { 10, 12, 10, 12 }, new double[] { 20, 20, 22, 22 });
		final OblastPolygony o = oblast(hodiny);
		final Random r = new Random(5);
		for (int i = 0; i < 5000; i++) {
			final double la = 9.5 + 3 * r.nextDouble();
			final double lo = 19.5 + 3 * r.nextDouble();
			boolean ocekavano = false;
			for (int a = 0, b = hodiny.pocetBodu() - 1; a < hodiny.pocetBodu(); b = a++) {
				if (hodiny.lat(a) > la != hodiny.lat(b) > la && lo < (hodiny.lon(b) - hodiny.lon(a)) * (la - hodiny.lat(a)) / (hodiny.lat(b) - hodiny.lat(a)) + hodiny.lon(a)) {
					ocekavano = !ocekavano;
				}
			}
			Assert.assertEquals(la + "," + lo, ocekavano, o.obsahuje(la, lo));
		}
		Assert.assertTrue("levý cíp", o.obsahuje(11.5, 20.2));
		Assert.assertTrue("pravý cíp", o.obsahuje(11.5, 21.8));
		Assert.assertFalse("mezi cípy", o.obsahuje(11.5, 21));
		Assert.assertFalse("mimo", o.obsahuje(9.5, 21));
	}

	@Test(timeout = 20000)
	public void mnohouhelnikSDeseticiTisicBoduSeVytvoriARychleVyhodnoti() {
		final int n = 100_000;
		final double[] lat = new double[n];
		final double[] lon = new double[n];
		for (int i = 0; i < n; i++) {
			final double uhel = 2 * Math.PI * i / n;
			lat[i] = 50 + 2 * Math.sin(uhel);
			lon[i] = 14 + 3 * Math.cos(uhel);
		}
		final OblastPolygony o = oblast(okraj(lat, lon));
		Assert.assertTrue(o.obsahuje(50, 14));
		Assert.assertFalse(o.obsahuje(53, 14));
		Assert.assertFalse(o.obsahuje(50, 18));
		for (int i = 0; i < 20_000; i++) {
			o.obsahuje(48 + (i % 400) * 0.01, 11 + (i % 600) * 0.01);
		}
	}

	@Test
	public void oblastSDirouVneVnejsihoOkrajeVracíSudyPocetPrusecikuPodlePravidla() {
		final Okraj vnejsi = okraj(new double[] { 10, 10, 12, 12 }, new double[] { 20, 22, 22, 20 });
		final Okraj dira = okraj(new double[] { 14, 14, 15, 15 }, new double[] { 20, 22, 22, 20 });
		final OblastPolygony o = new OblastPolygony("t", "v", Collections.singletonList(new Cast(vnejsi, Collections.singletonList(dira))));
		Assert.assertTrue(o.obsahuje(11, 21));
		Assert.assertFalse("díra mimo vnější okraj nic nepřidává do výsledku mimo něj", o.obsahuje(13, 21));
	}

	// ---- GeometrieSektoru: póly a datová čára

	private static void jePlatnyBod(final String co, final Bod b) {
		Assert.assertFalse(co + " " + b, Double.isNaN(b.lat) || Double.isNaN(b.lon) || Double.isInfinite(b.lat) || Double.isInfinite(b.lon));
		Assert.assertTrue(co + " " + b, b.lat >= -90 && b.lat <= 90 && b.lon >= -180 && b.lon <= 180);
	}

	@Test
	public void bodKolemPoluMaSpravnouVzdalenostAPlatneSouradnice() {
		for (final Model m : Model.values()) {
			for (final double lat : new double[] { 90, -90, 89.999999, -89.999999 }) {
				for (int az = 0; az < 360; az += 45) {
					for (final double metru : new double[] { 0, 1, 1000, 100_000, 2_000_000 }) {
						final Bod b = GeometrieSektoru.bod(lat, 10, az, metru, m);
						jePlatnyBod(m + " " + lat + " az " + az + " " + metru, b);
						final double d = m == Model.KOULE ? Geodezie.vzdalenostKoule(lat, 10, b.lat, b.lon) : Geodezie.vzdalenostElipsoid(lat, 10, b.lat, b.lon);
						// Těsně u pólu ztrácí asin přesnost (asi 2 cm při 1e-6° od pólu).
						Assert.assertEquals(m + " " + lat + " az " + az + " " + metru, metru, d, Math.abs(lat) > 89.9999 ? 0.1 : m == Model.KOULE ? 1e-3 : 0.05);
					}
				}
			}
		}
	}

	@Test
	public void bodPresPolPokracujeNaProtejsiDelce() {
		for (final Model m : Model.values()) {
			final Bod b = GeometrieSektoru.bod(89.99999, 10, 0, 5000, m);
			Assert.assertTrue(b.toString(), b.lat < 90 && b.lat > 89.9);
			Assert.assertEquals(m + " " + b, -170, b.lon, 0.01);
		}
	}

	@Test
	public void bodPresDatovouCaruAObracene() {
		for (final Model m : Model.values()) {
			final Bod vychod = GeometrieSektoru.bod(0, 179.99, 90, 5000, m);
			Assert.assertTrue(m + " " + vychod, vychod.lon < -179.9 && vychod.lon >= -180);
			final Bod zapad = GeometrieSektoru.bod(0, -179.99, 270, 5000, m);
			Assert.assertTrue(m + " " + zapad, zapad.lon > 179.9 && zapad.lon <= 180);
			jePlatnyBod("na čáře", GeometrieSektoru.bod(10, 180, 45, 100_000, m));
			jePlatnyBod("na čáře", GeometrieSektoru.bod(10, -180, 45, 100_000, m));
		}
	}

	@Test
	public void sektorBoduPresDatovouCaruAKolemPolu() {
		for (final Model m : Model.values()) {
			Assert.assertEquals(90, GeometrieSektoru.sektorBodu(0, 179.9, 0, -179.9, 0, 100_000, m));
			Assert.assertEquals(270, GeometrieSektoru.sektorBodu(0, -179.9, 0, 179.9, 0, 100_000, m));
			Assert.assertEquals(90, GeometrieSektoru.sektorBodu(0, 180, 0, -179.9, 0, 100_000, m));
			Assert.assertEquals(270, GeometrieSektoru.sektorBodu(0, -180, 0, 179.9, 0, 100_000, m));
			Assert.assertEquals("ze severního pólu míří všechny směry na jih podle konvence délky", 170, GeometrieSektoru.sektorBodu(90, 0, 80, 10, 0, 1_500_000, m));
			final int zJihu = GeometrieSektoru.sektorBodu(-90, 0, -80, 10, 0, 1_500_000, m);
			Assert.assertTrue("z jižního pólu míří všechny směry na sever: " + zJihu, zJihu >= 0 && zJihu < 360);
		}
	}

	@Test
	public void bodKtervyjdeZeStreduVAzimutuSektoruSeVratiDoStejnehoSektoruVsudePoZemi() {
		final double[] sirky = { -89, -60, -33.9, 0, 45, 50.0875, 70, 89 };
		final double[] delky = { -180, -179.5, -90, 0, 14.4208, 90, 179.5, 180 };
		final double[] vzdalenosti = { 1_000, 50_000, 500_000, 3_000_000 };
		for (final Model m : Model.values()) {
			for (final double lat : sirky) {
				for (final double lon : delky) {
					for (final double metru : vzdalenosti) {
						for (int sektor = 0; sektor < 360; sektor += 29) {
							final Bod b = GeometrieSektoru.bod(lat, lon, sektor + 0.5, metru, m);
							final int zpet = GeometrieSektoru.sektorBodu(lat, lon, b.lat, b.lon, 0, metru + 1, m);
							Assert.assertEquals(m + " " + lat + "," + lon + " sektor " + sektor + " " + metru + " m", sektor, zpet);
						}
					}
				}
			}
		}
	}

	@Test
	public void priameAInverzniUlohaNaElipsoiduSeShodujiNaDlouheVzdalenostiNaMilimetr() {
		final double[][] stredy = { { 50.0875, 14.4208 }, { -33.9, 151.2 }, { 5.0, -70.0 }, { 70.0, 25.0 }, { 0.0, 0.0 }, { 89.0, 0.0 }, { 30.0, 179.5 } };
		for (final double[] st : stredy) {
			for (final double metru : new double[] { 10, 12_345, 1_000_000, 3_000_000, 6_000_000, 10_000_000, 15_000_000 }) {
				for (int az = 0; az < 360; az += 23) {
					final Bod b = GeometrieSektoru.bod(st[0], st[1], az + 0.37, metru, Model.ELIPSOID);
					final double d = Geodezie.vzdalenostElipsoid(st[0], st[1], b.lat, b.lon);
					if (Double.isNaN(d)) {
						continue;
					}
					final String co = Arrays.toString(st) + " az " + (az + 0.37) + " " + metru + " m";
					Assert.assertEquals(co, metru, d, 1e-6);
					final double zpet = Geodezie.azimutElipsoid(st[0], st[1], b.lat, b.lon);
					double rozdil = Math.abs(zpet - (az + 0.37)) % 360;
					rozdil = Math.min(rozdil, 360 - rozdil);
					Assert.assertEquals(co, 0, rozdil, metru >= 1_000_000 ? 1e-8 : 1e-5);
				}
			}
		}
	}

	@Test
	public void znameVzdalenostiAAzimutyNaElipsoiduWgs84() {
		// Rovník o 1° délky a poledník o 1° šířky (délky oblouků na elipsoidu WGS84).
		Assert.assertEquals(111_319.4908, Geodezie.vzdalenostElipsoid(0, 0, 0, 1), 1e-3);
		Assert.assertEquals(90, Geodezie.azimutElipsoid(0, 0, 0, 1), 1e-9);
		Assert.assertEquals(110_574.3886, Geodezie.vzdalenostElipsoid(0, 0, 1, 0), 1e-3);
		Assert.assertEquals(0, Geodezie.azimutElipsoid(0, 0, 1, 0), 1e-9);
		Assert.assertEquals(180, Geodezie.azimutElipsoid(1, 0, 0, 0), 1e-9);
		Assert.assertEquals(270, Geodezie.azimutElipsoid(0, 1, 0, 0), 1e-9);
		// Čtvrtina poledníku od rovníku k pólu: 10 001 965,729 m.
		Assert.assertEquals(10_001_965.729, Geodezie.vzdalenostElipsoid(0, 0, 90, 0), 1e-2);
	}

	@Test
	public void vzdalenostNaPulObvoduAZaNimSeSklada() {
		final double obvod = 2 * Math.PI * Geodezie.POLOMER_ZEME;
		final Bod pul = GeometrieSektoru.bod(10, 20, 30, obvod / 2, Model.KOULE);
		Assert.assertEquals(-10, pul.lat, 1e-6);
		Assert.assertEquals(-160, pul.lon, 1e-6);
		final Bod za = GeometrieSektoru.bod(10, 20, 30, 30_000_000, Model.KOULE);
		jePlatnyBod("za polovinou", za);
		Assert.assertEquals(obvod - 30_000_000, Geodezie.vzdalenostKoule(10, 20, za.lat, za.lon), 1e-3);
		final Bod celyObvod = GeometrieSektoru.bod(10, 20, 30, obvod, Model.KOULE);
		Assert.assertEquals(10, celyObvod.lat, 1e-6);
		Assert.assertEquals(20, celyObvod.lon, 1e-6);
		for (final Model m : Model.values()) {
			jePlatnyBod("elipsoid", GeometrieSektoru.bod(10, 20, 30, 20_037_000, m));
			jePlatnyBod("elipsoid", GeometrieSektoru.bod(10, 20, 30, 1e12, m));
		}
	}

	@Test
	public void kolizniBodyTesneUProtinoziceDavajiPlatnySektorBezVyjimky() {
		for (final Model m : Model.values()) {
			final Random r = new Random(3);
			for (int i = 0; i < 2000; i++) {
				final double lat = r.nextDouble() * 20 - 10;
				final double lon = r.nextDouble() * 360 - 180;
				final double protiLat = -lat + (r.nextDouble() - 0.5) * 0.4;
				double protiLon = lon + 180 + (r.nextDouble() - 0.5) * 0.4;
				if (protiLon > 180) {
					protiLon -= 360;
				}
				final int s = GeometrieSektoru.sektorBodu(lat, lon, protiLat, protiLon, 0, 25_000_000, m);
				Assert.assertTrue(m + " " + lat + "," + lon + " → " + protiLat + "," + protiLon + ": " + s, s >= 0 && s < 360);
			}
		}
	}

	// ---- GeometrieSektoru: neplatné vstupy

	@Test
	public void neplatnyStredSeOdmitneVeVsechVerejnychFunkcich() {
		for (final double zla : NEPLATNA) {
			for (final double[] stred : new double[][] { { zla, 10 }, { 10, zla } }) {
				if (stred[1] == 90.0001 || stred[1] == -90.0001) {
					continue; // takové délky jsou platné
				}
				final double lat = stred[0];
				final double lon = stred[1];
				odmitne("bod " + lat + "," + lon, () -> GeometrieSektoru.bod(lat, lon, 0, 1000, Model.KOULE));
				odmitne("oblouk " + lat + "," + lon, () -> GeometrieSektoru.oblouk(lat, lon, 0, 10, 1000, Model.KOULE));
				odmitne("klin " + lat + "," + lon, () -> GeometrieSektoru.klin(lat, lon, 1, 0, 1000, Model.KOULE));
				odmitne("paprsek " + lat + "," + lon, () -> GeometrieSektoru.paprsek(lat, lon, 1, 0, 1000, Model.KOULE));
				odmitne("strany " + lat + "," + lon, () -> GeometrieSektoru.svetoveStrany(lat, lon, 1000, Model.KOULE));
				odmitne("sektorBodu " + lat + "," + lon, () -> GeometrieSektoru.sektorBodu(lat, lon, 10, 10, 0, 1000, Model.KOULE));
				odmitne("prstenec " + lat + "," + lon, () -> GeometrieSektoru.prstenec(new VysledekSektoru(new BitSet(360), new BitSet(360), new VysledekSektoru.Kandidati[360], 0), lat, lon, 1000, Model.KOULE));
			}
		}
	}

	@Test
	public void neplatnaVzdalenostAPoloměrySeOdmitnou() {
		for (final double zla : new double[] { Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, -1, -1e-9 }) {
			odmitne("bod " + zla, () -> GeometrieSektoru.bod(10, 10, 0, zla, Model.KOULE));
			odmitne("oblouk " + zla, () -> GeometrieSektoru.oblouk(10, 10, 0, 10, zla, Model.KOULE));
			odmitne("strany " + zla, () -> GeometrieSektoru.svetoveStrany(10, 10, zla, Model.KOULE));
			odmitne("klin max " + zla, () -> GeometrieSektoru.klin(10, 10, 1, 0, zla, Model.KOULE));
			odmitne("klin min " + zla, () -> GeometrieSektoru.klin(10, 10, 1, zla, 1000, Model.KOULE));
			odmitne("paprsek max " + zla, () -> GeometrieSektoru.paprsek(10, 10, 1, 0, zla, Model.KOULE));
			odmitne("sektorBodu min " + zla, () -> GeometrieSektoru.sektorBodu(10, 10, 11, 11, zla, 1000, Model.KOULE));
			odmitne("sektorBodu max " + zla, () -> GeometrieSektoru.sektorBodu(10, 10, 11, 11, 0, zla, Model.KOULE));
		}
		odmitne("min = max", () -> GeometrieSektoru.klin(10, 10, 1, 1000, 1000, Model.KOULE));
		odmitne("min > max", () -> GeometrieSektoru.paprsek(10, 10, 1, 2000, 1000, Model.KOULE));
		odmitne("sektor -1", () -> GeometrieSektoru.klin(10, 10, -1, 0, 1000, Model.KOULE));
		odmitne("sektor 360", () -> GeometrieSektoru.klin(10, 10, 360, 0, 1000, Model.KOULE));
		odmitne("stavSektoru 360", () -> GeometrieSektoru.stavSektoru(new VysledekSektoru(new BitSet(360), new BitSet(360), new VysledekSektoru.Kandidati[360], 0), 360));
	}

	@Test
	public void bodMimoRozsahVSektoruBoduVracíMinusJedna() {
		for (final Model m : Model.values()) {
			for (final double zla : new double[] { Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 91, -91, 1e9 }) {
				Assert.assertEquals(-1, GeometrieSektoru.sektorBodu(50, 14, zla, 14, 0, 1_000_000, m));
				Assert.assertEquals(-1, GeometrieSektoru.sektorBodu(50, 14, 50, zla, 0, 1_000_000, m));
			}
			Assert.assertEquals("bod ve středu", -1, GeometrieSektoru.sektorBodu(50, 14, 50, 14, 0, 1_000_000, m));
		}
	}

	@Test
	public void prstencePodObriNebonulovyPolomerNemaNaNBody() {
		final VysledekSektoru prazdny = new VysledekSektoru(new BitSet(360), new BitSet(360), new VysledekSektoru.Kandidati[360], 0);
		for (final Model m : Model.values()) {
			for (final double[] stred : new double[][] { { 90, 0 }, { -90, 0 }, { 0, 180 }, { 0, -180 }, { 89.999, 179.999 } }) {
				for (final double polomer : new double[] { 0, 1, 1_000_000, 20_000_000, 40_000_000 }) {
					final List<Usek> useky = GeometrieSektoru.prstenec(prazdny, stred[0], stred[1], polomer, m);
					Assert.assertEquals(1, useky.size());
					Assert.assertEquals(361, useky.get(0).body().size());
					for (final Bod b : useky.get(0).body()) {
						jePlatnyBod(m + " " + Arrays.toString(stred) + " " + polomer, b);
					}
				}
				final List<Bod> klin = GeometrieSektoru.klin(stred[0], stred[1], 359, 0, 5_000_000, m);
				for (final Bod b : klin) {
					jePlatnyBod("klin", b);
				}
			}
		}
	}

	@Test
	public void obloukPresDatovouCaruMaPoOdvinutiSouvislouDelku() {
		for (final Model m : Model.values()) {
			final List<Bod> oblouk = GeometrieSektoru.oblouk(0, 179.5, 0, 360, 200_000, m);
			final List<Bod> v = GeometrieSektoru.odvinuteDelky(oblouk);
			double nejvetsi = 0;
			for (int i = 1; i < v.size(); i++) {
				nejvetsi = Math.max(nejvetsi, Math.abs(v.get(i).lon - v.get(i - 1).lon));
			}
			Assert.assertTrue(m + " skok " + nejvetsi, nejvetsi < 1.5);
			Assert.assertTrue("oblouk opravdu přešel datovou čáru", oblouk.stream().anyMatch(b -> b.lon < -150) && oblouk.stream().anyMatch(b -> b.lon > 150));
		}
	}

	private interface Volani {
		void zavolej();
	}

	private static void odmitne(final String co, final Volani v) {
		try {
			v.zavolej();
			Assert.fail("mělo se odmítnout: " + co);
		} catch (final IllegalArgumentException e) {
			// očekávané
		}
	}

	// ---- VypocetSektoru

	@Test
	public void vypocetKolemPoluADatoveCaryDavaPlatnySektorBezNaN() {
		for (final ZdrojPravidel z : ZdrojPravidel.values()) {
			for (final double[] stred : new double[][] { { 90, 0 }, { -90, 0 }, { 0, 180 }, { 0, -180 }, { 89.99999, 179.99999 }, { -89.99999, -179.99999 } }) {
				final List<VypocetSektoruTest.Kes> kese = new ArrayList<>();
				final Random r = new Random(1);
				for (int i = 0; i < 400; i++) {
					kese.add(new VypocetSektoruTest.Kes("K" + i, Math.max(-90, Math.min(90, stred[0] + (r.nextDouble() - 0.5) * 6)), Math.max(-180, Math.min(180, stred[1] + (r.nextDouble() - 0.5) * 6)), i % 3 != 0));
				}
				final VysledekSektoru v = VypocetSektoru.spocti(z, stred[0], stred[1], null, Collections.<String> emptySet(), kese);
				Assert.assertTrue(z + " " + Arrays.toString(stred), v.pocetObsazenych() > 0 && v.pocetObsazenych() <= 360);
				Assert.assertEquals(360, v.pocetObsazenych() + v.chybejici().size());
				for (final VysledekSektoru.KandidatKes k : v.vsichniKandidati()) {
					Assert.assertTrue(k.sektor() >= 0 && k.sektor() < 360);
				}
			}
		}
	}

	@Test
	public void kesPoObouStranachDatoveCaryJeVeSpravnychSektorech() {
		for (final ZdrojPravidel z : ZdrojPravidel.values()) {
			final List<VypocetSektoruTest.Kes> kese = Arrays.asList(new VypocetSektoruTest.Kes("V", 0, -179.9, true), new VypocetSektoruTest.Kes("Z", 0, 179.8, true));
			final VysledekSektoru v = VypocetSektoru.spocti(z, 0, 179.9, null, Collections.<String> emptySet(), kese);
			Assert.assertEquals(2, v.pocetObsazenych());
			Assert.assertTrue(z + " východ", v.jeObsazeny(90));
			Assert.assertTrue(z + " západ", v.jeObsazeny(270));
			final VysledekSektoru opacne = VypocetSektoru.spocti(z, 0, -179.9, null, Collections.<String> emptySet(), Arrays.asList(new VypocetSektoruTest.Kes("V", 0, -179.8, true), new VypocetSektoruTest.Kes("Z", 0, 179.9, true)));
			Assert.assertTrue(opacne.jeObsazeny(90) && opacne.jeObsazeny(270));
		}
	}

	@Test
	public void prazdnyVyctesAVyctySamymiVyloucenymiKesemiDavaVsechnoChybi() {
		for (final ZdrojPravidel z : ZdrojPravidel.values()) {
			Assert.assertEquals(360, VypocetSektoru.spocti(z, 50, 14, null, Collections.<String> emptySet(), Collections.<VstupKes> emptyList()).chybejici().size());
			final VypocetSektoruTest.Kes lab = new VypocetSektoruTest.Kes("L", 50.1, 14.1, true);
			lab.lab = true;
			final VypocetSektoruTest.Kes bezPolohy = new VypocetSektoruTest.Kes("B", 50.1, 14.1, true);
			bezPolohy.locationless = true;
			final VysledekSektoru v = VypocetSektoru.spocti(z, 50, 14, null, Collections.<String> emptySet(), Arrays.asList(lab, bezPolohy));
			Assert.assertEquals(z + " " + v.pocetObsazenych(), z == ZdrojPravidel.STATOR ? 1 : 0, v.pocetObsazenych());
			Assert.assertEquals(z == ZdrojPravidel.STATOR ? 1 : 0, v.pocetZapocitanychNalezu());
		}
	}

	@Test
	public void neplatnyStredVeVypoctuSeOdmitne() {
		for (final double zla : new double[] { Double.NaN, Double.POSITIVE_INFINITY, 91, -91 }) {
			odmitne("střed lat " + zla, () -> VypocetSektoru.spocti(ZdrojPravidel.PGC, zla, 14, null, Collections.<String> emptySet(), Collections.<VstupKes> emptyList()));
			odmitne("střed lon " + zla, () -> VypocetSektoru.spocti(ZdrojPravidel.STATOR, 50, zla == 91 || zla == -91 ? 181 : zla, null, Collections.<String> emptySet(), Collections.<VstupKes> emptyList()));
		}
	}
}
