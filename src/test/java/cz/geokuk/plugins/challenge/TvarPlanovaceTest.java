package cz.geokuk.plugins.challenge;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.plugins.challenge.Challenge.OblastDef;
import cz.geokuk.plugins.challenge.Challenge.Stred;
import cz.geokuk.plugins.challenge.Challenge.Typ;
import cz.geokuk.plugins.challenge.Challenge.ZdrojPravidel;

/** Podklad kreslení plánovače na mapě a texty značek; bez displeje. */
public class TvarPlanovaceTest {

	static Challenge sektory() {
		return new Challenge("A", Typ.SEKTORY_360, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC);
	}

	/** Sektory 0 až 99 splněné, kandidáti GC1 (sektor 150) a GC2 (sektor 151, nejistý). */
	static VysledekSektoru vysledek() {
		final BitSet obsazene = new BitSet(360);
		obsazene.set(0, 100);
		final BitSet nejiste = new BitSet(360);
		nejiste.set(151);
		final List<VysledekSektoru.KandidatKes> k = new ArrayList<>();
		k.add(new VysledekSektoru.KandidatKes("GC1", 50.01, 14.0, 150, 1200, false));
		k.add(new VysledekSektoru.KandidatKes("GC2", 50.03, 14.0, 151, 3400, true));
		return new VysledekSektoru(obsazene, nejiste, new VysledekSektoru.Kandidati[360], 100, k);
	}

	@Test
	public void tvarSektoruMaStredKandidatyATextySektoru() {
		final TvarPlanovace t = TvarPlanovace.z(PrehledChallenge.zeSektoru(sektory(), vysledek(), 50.0, 14.0), null);
		Assert.assertNotNull(t.stred);
		Assert.assertEquals("domov", t.popisStredu);
		Assert.assertEquals(2, t.pozice.length);
		Assert.assertEquals("150°", t.texty[0]);
		Assert.assertEquals("151°", t.texty[1]);
		Assert.assertNull("bez výběru není vybraný sektor", t.popisVybraneho);
		Assert.assertEquals(-1, t.vybranySektor);
		Assert.assertNull(TvarPlanovace.z(null, null));
	}

	@Test
	public void tvarBezStreduNekresliStred() {
		final TvarPlanovace t = TvarPlanovace.z(PrehledChallenge.zeSektoru(sektory(), vysledek(), Double.NaN, Double.NaN), null);
		Assert.assertNull(t.stred);
		Assert.assertTrue(t.prstenec(1000).isEmpty());
	}

	@Test
	public void vybranySektorMaStitekPoctemAVzdalenosti() {
		final VybranyKandidat v = new VybranyKandidat("GC1", 50.01, 14.0, 150, 1200);
		final TvarPlanovace t = TvarPlanovace.z(PrehledChallenge.zeSektoru(sektory(), vysledek(), 50.0, 14.0), v);
		Assert.assertEquals(150, t.vybranySektor);
		Assert.assertNotNull(t.vybranyPozice);
		Assert.assertTrue(t.popisVybraneho, t.popisVybraneho.startsWith("Sektor 150° · 1 kandidát"));
		Assert.assertFalse(t.klin(2000).isEmpty());
	}

	@Test
	public void prstenecMaUsekyPodleStavuSektoru() {
		final TvarPlanovace t = TvarPlanovace.z(PrehledChallenge.zeSektoru(sektory(), vysledek(), 50.0, 14.0), null);
		final List<GeometrieSektoru.Usek> u = t.prstenec(5000);
		int splnene = 0;
		int chybi = 0;
		int nejiste = 0;
		for (final GeometrieSektoru.Usek x : u) {
			switch (x.stav()) {
			case SPLNENY:
				splnene += x.pocetSektoru();
				break;
			case NEJISTY:
				nejiste += x.pocetSektoru();
				break;
			default:
				chybi += x.pocetSektoru();
			}
		}
		Assert.assertEquals(100, splnene);
		Assert.assertEquals(1, nejiste);
		Assert.assertEquals(259, chybi);
		Assert.assertSame("tentýž poloměr tentýž seznam", u, t.prstenec(5000));
		Assert.assertNotSame(u, t.prstenec(2000));
	}

	@Test
	public void polomerPrstenceJeRadou125AZabereCtvrtAzPulkuOkna() {
		for (int moumer = 8; moumer <= 18; moumer++) {
			final Coord c = new Coord(moumer, new Wgs(50, 14).toMou(), new Dimension(1280, 720), 0);
			final double r = TvarPlanovace.polomerPrstence(c);
			final double mantisa = r / Math.pow(10, Math.floor(Math.log10(r)));
			Assert.assertTrue("1, 2 nebo 5: " + r, Math.abs(mantisa - 1) < 1e-9 || Math.abs(mantisa - 2) < 1e-9 || Math.abs(mantisa - 5) < 1e-9);
			final double px = r * c.getPixluNaMetr();
			Assert.assertTrue(moumer + ": " + px + " px", px >= 0.2 * 720 && px <= 0.62 * 720);
		}
	}

	@Test
	public void textyZnacek() {
		Assert.assertEquals("850 m", TextyKresleni.vzdalenost(850));
		Assert.assertEquals("2,8 km", TextyKresleni.vzdalenost(2800));
		Assert.assertEquals("25 km", TextyKresleni.vzdalenost(25_300));
		Assert.assertEquals("1 kandidát", TextyKresleni.kandidatu(1));
		Assert.assertEquals("3 kandidáti", TextyKresleni.kandidatu(3));
		Assert.assertEquals("5 kandidátů", TextyKresleni.kandidatu(5));
		Assert.assertEquals("Sektor 213° · 5 kandidátů · 2,8 km", TextyKresleni.vybranySektor(213, 5, 2800));
		Assert.assertEquals("Střed: GC56R49", TextyKresleni.stred("GC56R49"));
		Assert.assertEquals("Střed: domov · 25 km", TextyKresleni.stredMimoVyrez("domov", 25_300));
	}

	@Test
	public void souradniceSeZalamujiPresAntimeridianAOmezujiUPolu() {
		final cz.geokuk.core.coordinates.Mou a = PixelyMapy.mou(10, 190);
		final cz.geokuk.core.coordinates.Mou b = PixelyMapy.mou(10, -170);
		Assert.assertEquals(b.xx, a.xx);
		Assert.assertEquals(b.yy, a.yy);
		Assert.assertEquals(PixelyMapy.mou(85, 0).yy, PixelyMapy.mou(89.9, 0).yy);
		Assert.assertEquals(PixelyMapy.mou(-85, 0).yy, PixelyMapy.mou(-90, 0).yy);
	}
}
