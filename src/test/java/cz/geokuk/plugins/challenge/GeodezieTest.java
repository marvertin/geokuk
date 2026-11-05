package cz.geokuk.plugins.challenge;

import org.junit.Assert;
import org.junit.Test;

public class GeodezieTest {

	private static final double TOL = 1e-6;

	@Test
	public void azimutKouleSouhlasiSReferencnimVypoctem() {
		Assert.assertEquals(0, Geodezie.azimutKoule(50, 14, 51, 14), TOL);
		Assert.assertEquals(89.616973761, Geodezie.azimutKoule(50, 14, 50, 15), TOL);
		Assert.assertEquals(98.101655529, Geodezie.azimutKoule(50.0875, 14.4208, 49.9, 16.3), TOL);
		Assert.assertEquals(145.218077320, Geodezie.azimutKoule(50.0875, 14.4208, 48.2, 16.37), TOL);
		Assert.assertEquals(180, Geodezie.azimutKoule(50, 14, 49, 14), TOL);
		Assert.assertEquals(270.383026239, Geodezie.azimutKoule(50, 14, 50, 13), TOL);
		Assert.assertEquals(44.561451413, Geodezie.azimutKoule(0, 0, 10, 10), TOL);
		Assert.assertEquals(230.815825274, Geodezie.azimutKoule(-33.9, 151.2, -37.8, 144.9), TOL);
	}

	@Test
	public void azimutElipsoidSouhlasiSReferencnimVypoctem() {
		Assert.assertEquals(0, Geodezie.azimutElipsoid(50, 14, 51, 14), TOL);
		Assert.assertEquals(89.616973750, Geodezie.azimutElipsoid(50, 14, 50, 15), TOL);
		Assert.assertEquals(98.077536325, Geodezie.azimutElipsoid(50.0875, 14.4208, 49.9, 16.3), TOL);
		Assert.assertEquals(145.141531421, Geodezie.azimutElipsoid(50.0875, 14.4208, 48.2, 16.37), TOL);
		Assert.assertEquals(44.751910171, Geodezie.azimutElipsoid(0, 0, 10, 10), TOL);
		Assert.assertEquals(230.937724902, Geodezie.azimutElipsoid(-33.9, 151.2, -37.8, 144.9), TOL);
		Assert.assertEquals("shodné body", 0, Geodezie.azimutElipsoid(50, 14, 50, 14), TOL);
	}

	@Test
	public void azimutJeVzdyVRozsahu0Az360() {
		for (double lat = -80; lat <= 80; lat += 20) {
			for (double lon = -170; lon <= 170; lon += 35) {
				final double a = Geodezie.azimutKoule(10, 20, lat, lon);
				Assert.assertTrue("koule " + a, a >= 0 && a < 360);
				final double e = Geodezie.azimutElipsoid(10, 20, lat, lon);
				Assert.assertTrue("elipsoid " + e, e >= 0 && e < 360);
			}
		}
	}

	@Test
	public void vzdalenostKoule() {
		Assert.assertEquals(111195.080, Geodezie.vzdalenostKoule(50, 14, 51, 14), 0.01);
		Assert.assertEquals(253263.684, Geodezie.vzdalenostKoule(50.0875, 14.4208, 48.2, 16.37), 0.01);
		Assert.assertEquals(0, Geodezie.vzdalenostKoule(50, 14, 50, 14), 1e-9);
	}

	@Test
	public void sektorSeUrezavaDolu() {
		Assert.assertEquals(0, Geodezie.sektor(0));
		Assert.assertEquals(0, Geodezie.sektor(0.999));
		Assert.assertEquals(1, Geodezie.sektor(1.0));
		Assert.assertEquals(89, Geodezie.sektor(89.9999));
		Assert.assertEquals(359, Geodezie.sektor(359.9999));
	}

	@Test
	public void wgsAzimutSeNeZmenilAMaJinyVyznam() {
		// Směr v mercatorově rovině se od počátečního směru po kouli liší; challenge ho nesmí používat.
		final double mercator = new cz.geokuk.core.coordinates.Wgs(50.0875, 14.4208).azimut(new cz.geokuk.core.coordinates.Wgs(48.2, 16.37));
		Assert.assertNotEquals(Geodezie.azimutKoule(50.0875, 14.4208, 48.2, 16.37), mercator, 0.01);
	}

	@Test
	public void nejistotaUHranicSektoru() {
		Assert.assertTrue(Geodezie.jeNejisty(12.0));
		Assert.assertTrue(Geodezie.jeNejisty(12.049));
		Assert.assertFalse(Geodezie.jeNejisty(12.051));
		Assert.assertFalse(Geodezie.jeNejisty(12.5));
		Assert.assertFalse(Geodezie.jeNejisty(12.949));
		Assert.assertTrue(Geodezie.jeNejisty(12.951));
		Assert.assertTrue(Geodezie.jeNejisty(359.99));
	}

	@Test
	public void azimutyZPrikladuRuian() throws Exception {
		int radku = 0;
		try (java.io.BufferedReader r = new java.io.BufferedReader(
				new java.io.InputStreamReader(GeodezieTest.class.getResourceAsStream("azimuty.txt"), java.nio.charset.StandardCharsets.UTF_8))) {
			for (String radek = r.readLine(); radek != null; radek = r.readLine()) {
				if (radek.isEmpty() || radek.startsWith("#")) {
					continue;
				}
				final String[] c = radek.trim().split("\\s+");
				final double az = Geodezie.azimutKoule(Double.parseDouble(c[0]), Double.parseDouble(c[1]), Double.parseDouble(c[2]), Double.parseDouble(c[3]));
				Assert.assertEquals(radek, Double.parseDouble(c[4]), az, 0.0006);
				Assert.assertEquals(radek, Integer.parseInt(c[5]), Geodezie.sektor(az));
				radku++;
			}
		}
		Assert.assertEquals(7, radku);
	}
}
