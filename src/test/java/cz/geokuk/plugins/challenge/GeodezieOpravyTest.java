package cz.geokuk.plugins.challenge;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.plugins.challenge.Challenge.ZdrojPravidel;
import cz.geokuk.plugins.challenge.GeometrieSektoru.Model;

/** Neplatné souřadnice a azimuty, keš ve středu, nenormalizovaný azimut a délka ±180°. */
public class GeodezieOpravyTest {

	private static final double[][] NEPLATNE = { { Double.NaN, 14 }, { 50, Double.NaN }, { Double.POSITIVE_INFINITY, 14 }, { 50, Double.NEGATIVE_INFINITY }, { 95, 14 }, { -95, 14 },
			{ 50, 200 }, { 50, -200 }, { 90.0001, 0 }, { 0, 180.0001 } };

	private static VysledekSektoru spocti(final ZdrojPravidel zdroj, final VstupKes... kese) {
		return VypocetSektoru.spocti(zdroj, 50, 14, null, Collections.<String> emptySet(), Arrays.asList(kese));
	}

	@Test
	public void kesSNeplatnymiSouradnicemiNeniNalezemAniKandidatem() {
		for (final ZdrojPravidel zdroj : ZdrojPravidel.values()) {
			for (final double[] s : NEPLATNE) {
				final VysledekSektoru v = spocti(zdroj, new VypocetSektoruTest.Kes("GC1", s[0], s[1], true), new VypocetSektoruTest.Kes("GC2", s[0], s[1], false));
				final String kde = zdroj + " " + s[0] + ", " + s[1];
				Assert.assertEquals(kde, 0, v.pocetObsazenych());
				Assert.assertEquals(kde, 0, v.pocetZapocitanychNalezu());
				Assert.assertTrue(kde, v.vsichniKandidati().isEmpty());
			}
		}
	}

	@Test
	public void kesPresneVeStreduNeniNalezemAniKandidatem() {
		for (final ZdrojPravidel zdroj : ZdrojPravidel.values()) {
			final VysledekSektoru v = spocti(zdroj, new VypocetSektoruTest.Kes("GC1", 50, 14, true), new VypocetSektoruTest.Kes("GC2", 50, 14, false));
			Assert.assertEquals(zdroj.name(), 0, v.pocetObsazenych());
			Assert.assertEquals(zdroj.name(), 0, v.pocetZapocitanychNalezu());
			Assert.assertTrue(zdroj.name(), v.vsichniKandidati().isEmpty());
		}
	}

	@Test
	public void kesVeStreduNaDatoveCareJeStejnaJakoStred() {
		final VysledekSektoru v = VypocetSektoru.spocti(ZdrojPravidel.PGC, 0, 180, null, Collections.<String> emptySet(),
				Arrays.<VstupKes> asList(new VypocetSektoruTest.Kes("GC1", 0, -180, true)));
		Assert.assertEquals(0, v.pocetObsazenych());
		Assert.assertEquals(0, v.pocetZapocitanychNalezu());
	}

	@Test
	public void kesNaPoluJeVeStreduBezOhleduNaDelku() {
		for (final double pol : new double[] { 90, -90 }) {
			for (final ZdrojPravidel zdroj : ZdrojPravidel.values()) {
				final VysledekSektoru v = VypocetSektoru.spocti(zdroj, pol, 0, null, Collections.<String> emptySet(),
						Arrays.<VstupKes> asList(new VypocetSektoruTest.Kes("GC1", pol, 50, true), new VypocetSektoruTest.Kes("GC2", pol, 50, false)));
				Assert.assertEquals(zdroj + " " + pol, 0, v.pocetObsazenych());
				Assert.assertTrue(zdroj + " " + pol, v.vsichniKandidati().isEmpty());
			}
		}
	}

	@Test
	public void kesVBlizkostiStredu() {
		final VysledekSektoru v = spocti(ZdrojPravidel.PGC, new VypocetSektoruTest.Kes("GC1", 50.001, 14, true));
		Assert.assertTrue("0° sever", v.jeObsazeny(0));
		Assert.assertEquals(1, v.pocetObsazenych());
	}

	@Test
	public void sektorNormalizujeAzimut() {
		Assert.assertEquals(359, Geodezie.sektor(-0.5));
		Assert.assertEquals(359, Geodezie.sektor(-360.5));
		Assert.assertEquals(0, Geodezie.sektor(-360.0));
		Assert.assertEquals(0, Geodezie.sektor(0));
		Assert.assertEquals(0, Geodezie.sektor(0.999));
		Assert.assertEquals(1, Geodezie.sektor(1));
		Assert.assertEquals(359, Geodezie.sektor(359.999));
		Assert.assertEquals(0, Geodezie.sektor(360));
		Assert.assertEquals(359, Geodezie.sektor(719.5));
		Assert.assertEquals(90, Geodezie.sektor(450));
	}

	@Test
	public void sektorNanANekonecnoJeChyba() {
		for (final double a : new double[] { Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY }) {
			try {
				Geodezie.sektor(a);
				Assert.fail(String.valueOf(a));
			} catch (final IllegalArgumentException e) {
				// očekáváno
			}
		}
	}

	@Test
	public void neplatnyAzimutJeChybaVeVsechFunkcichGeometrie() {
		for (final Model model : Model.values()) {
			for (final double a : new double[] { Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY }) {
				try {
					GeometrieSektoru.bod(50, 14, a, 1000, model);
					Assert.fail("bod " + a);
				} catch (final IllegalArgumentException e) {
					// očekáváno
				}
				try {
					GeometrieSektoru.paprsek(50, 14, a, 100, 1000, model);
					Assert.fail("paprsek " + a);
				} catch (final IllegalArgumentException e) {
					// očekáváno
				}
				try {
					GeometrieSektoru.oblouk(50, 14, a, 10, 1000, model);
					Assert.fail("oblouk od " + a);
				} catch (final IllegalArgumentException e) {
					// očekáváno
				}
				try {
					GeometrieSektoru.oblouk(50, 14, 10, a, 1000, model);
					Assert.fail("oblouk do " + a);
				} catch (final IllegalArgumentException e) {
					// očekáváno
				}
			}
		}
	}

	@Test
	public void bodSAzimutemVeVelkychCislechJeKonecny() {
		for (final Model model : Model.values()) {
			final GeometrieSektoru.Bod b = GeometrieSektoru.bod(50, 14, 1e6, 1000, model);
			Assert.assertFalse(Double.isNaN(b.lat) || Double.isNaN(b.lon));
		}
	}

	@Test
	public void staBodVDelcePlusMinus180MaNulovouVzdalenostAZadnySektor() {
		for (final Model model : Model.values()) {
			Assert.assertEquals(model.name(), -1, GeometrieSektoru.sektorBodu(0, 180, 0, -180, 0, 1000, model));
			Assert.assertEquals(model.name(), -1, GeometrieSektoru.sektorBodu(0, -180, 0, 180, 0, 1000, model));
			Assert.assertEquals(model.name(), -1, GeometrieSektoru.sektorBodu(45, 180, 45, -180, 0, 1000, model));
		}
		Assert.assertEquals(0, Geodezie.vzdalenostKoule(0, 180, 0, -180), 0);
		Assert.assertEquals(0, Geodezie.vzdalenostKoule(0, -180, 0, 180), 0);
		Assert.assertEquals(0, Geodezie.vzdalenostElipsoid(0, 180, 0, -180), 0);
		Assert.assertEquals(0, Geodezie.vzdalenostElipsoid(45, -180, 45, 180), 0);
	}

	@Test
	public void vzdalenostAAzimutJsouPeriodickeVDelce() {
		Assert.assertEquals(Geodezie.vzdalenostKoule(10, 179.9, 10, 180.1), Geodezie.vzdalenostKoule(10, 179.9, 10, -179.9), 1e-6);
		Assert.assertEquals(Geodezie.azimutKoule(10, 179.9, 10, 180.1), Geodezie.azimutKoule(10, 179.9, 10, -179.9), 1e-9);
		Assert.assertEquals(Geodezie.vzdalenostElipsoid(10, 179.9, 10, 180.1), Geodezie.vzdalenostElipsoid(10, 179.9, 10, -179.9), 1e-6);
		Assert.assertEquals(Geodezie.azimutElipsoid(10, 179.9, 10, 180.1), Geodezie.azimutElipsoid(10, 179.9, 10, -179.9), 1e-9);
		Assert.assertEquals(Geodezie.vzdalenostKoule(10, -179.9, 10, 179.9), Geodezie.vzdalenostKoule(10, 180.1, 10, 179.9), 1e-6);
		Assert.assertEquals("na východ přes datovou čáru", 89, GeometrieSektoru.sektorBodu(10, 179.9, 10, -179.9, 0, 100_000, Model.KOULE));
		Assert.assertEquals("na západ přes datovou čáru", 270, GeometrieSektoru.sektorBodu(10, -179.9, 10, 179.9, 0, 100_000, Model.KOULE));
	}
}
