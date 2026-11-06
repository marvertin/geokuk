package cz.geokuk.plugins.challenge;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.plugins.challenge.Challenge.ZdrojPravidel;
import cz.geokuk.plugins.challenge.GeometrieSektoru.Model;

/** Neplatné souřadnice a neurčený azimut nesmějí ovlivnit sektory ani vracet NaN body. */
public class NeplatneSouradniceTest {

	private static final double[][] NEPLATNE = { { Double.NaN, 14 }, { 50, Double.NaN }, { Double.POSITIVE_INFINITY, 14 }, { 50, Double.NEGATIVE_INFINITY }, { 95, 200 }, { -91, 14 }, { 50, 181 } };

	@Test
	public void nalezenaKesSNeplatnymiSouradnicemSeNezapocita() {
		for (final ZdrojPravidel z : ZdrojPravidel.values()) {
			for (final double[] c : NEPLATNE) {
				final VysledekSektoru v = VypocetSektoru.spocti(z, 50, 14, null, Collections.<String> emptySet(), Arrays.asList(new VypocetSektoruTest.Kes("X", c[0], c[1], true)));
				Assert.assertEquals(z + " " + Arrays.toString(c), 0, v.pocetObsazenych());
				Assert.assertEquals(z + " " + Arrays.toString(c), 0, v.pocetZapocitanychNalezu());
			}
		}
	}

	@Test
	public void nenalezenaKesSNeplatnymiSouradnicemNeniKandidat() {
		for (final ZdrojPravidel z : ZdrojPravidel.values()) {
			for (final double[] c : NEPLATNE) {
				final VysledekSektoru v = VypocetSektoru.spocti(z, 50, 14, null, Collections.<String> emptySet(), Arrays.asList(new VypocetSektoruTest.Kes("X", c[0], c[1], false)));
				Assert.assertTrue(z + " " + Arrays.toString(c), v.vsichniKandidati().isEmpty());
			}
		}
	}

	@Test
	public void kesPresneVeStreduNemaSektor() {
		for (final ZdrojPravidel z : ZdrojPravidel.values()) {
			final VysledekSektoru v = VypocetSektoru.spocti(z, 50, 14, null, Collections.<String> emptySet(), Arrays.asList(new VypocetSektoruTest.Kes("X", 50, 14, true)));
			Assert.assertEquals(z.toString(), 0, v.pocetObsazenych());
		}
	}

	@Test
	public void neurcenyAzimutSeOdmitne() {
		for (final Model m : Model.values()) {
			for (final double az : new double[] { Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY }) {
				try {
					GeometrieSektoru.bod(10, 20, az, 1000, m);
					Assert.fail("bod " + az);
				} catch (final IllegalArgumentException e) {
					// očekávané
				}
				try {
					GeometrieSektoru.oblouk(10, 20, az, 5, 1000, m);
					Assert.fail("oblouk od " + az);
				} catch (final IllegalArgumentException e) {
					// očekávané
				}
				try {
					GeometrieSektoru.oblouk(10, 20, 0, az, 1000, m);
					Assert.fail("oblouk do " + az);
				} catch (final IllegalArgumentException e) {
					// očekávané
				}
				try {
					GeometrieSektoru.paprsek(10, 20, az, 0, 1000, m);
					Assert.fail("paprsek " + az);
				} catch (final IllegalArgumentException e) {
					// očekávané
				}
			}
		}
	}

	@Test
	public void sektorZNezapornehoIZapornehoAzimutu() {
		Assert.assertEquals(359, Geodezie.sektor(-0.5));
		Assert.assertEquals(0, Geodezie.sektor(-0.0));
		Assert.assertEquals(0, Geodezie.sektor(360.0));
		Assert.assertEquals(0, Geodezie.sektor(720.5));
		Assert.assertEquals(180, Geodezie.sektor(-179.5));
	}

	@Test
	public void bodNaStreduSDelkouPlusMinus180NemaSektor() {
		for (final Model m : Model.values()) {
			Assert.assertEquals(m.toString(), -1, GeometrieSektoru.sektorBodu(0, 180, 0, -180, 0, 1000, m));
			Assert.assertEquals(m.toString(), -1, GeometrieSektoru.sektorBodu(0, -180, 0, 180, 0, 1000, m));
			Assert.assertEquals(m.toString(), -1, GeometrieSektoru.sektorBodu(90, 0, 90, 50, 0, 1000, m));
			Assert.assertEquals(m.toString(), -1, GeometrieSektoru.sektorBodu(-90, 0, -90, 50, 0, 1000, m));
		}
	}
}
