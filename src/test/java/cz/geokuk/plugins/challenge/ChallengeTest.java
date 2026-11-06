package cz.geokuk.plugins.challenge;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.plugins.challenge.Challenge.DruhOblasti;
import cz.geokuk.plugins.challenge.Challenge.DruhStredu;
import cz.geokuk.plugins.challenge.Challenge.OblastDef;
import cz.geokuk.plugins.challenge.Challenge.Stred;
import cz.geokuk.plugins.challenge.Challenge.Typ;
import cz.geokuk.plugins.challenge.Challenge.ZdrojPravidel;

public class ChallengeTest {

	@Test
	public void sablonyJsouCtyriAVsechnyPrejdouPresMapu() {
		final List<Challenge> vse = Sablony.vse();
		Assert.assertEquals(4, vse.size());
		for (final Challenge c : vse) {
			Assert.assertEquals(c.nazev(), c, Challenge.zMapy(c.naMapu()));
		}
	}

	@Test
	public void sablona360OdDomova() {
		final Challenge c = Sablony.od360Domova();
		Assert.assertEquals(Typ.SEKTORY_360, c.typ());
		Assert.assertEquals(DruhStredu.DOMOV, c.stred().druh());
		Assert.assertEquals(DruhOblasti.ZADNA, c.oblast().druh());
		Assert.assertEquals(ZdrojPravidel.PGC, c.zdroj());
		Assert.assertEquals("360° od domova", c.nazev());
	}

	@Test
	public void sablonaCesko() {
		final Challenge c = Sablony.cesko360();
		Assert.assertEquals(Sablony.NAZEV_CESKO, c.nazev());
		Assert.assertEquals(DruhStredu.LISTING, c.stred().druh());
		Assert.assertEquals("GC4PCQB", c.stred().kodListingu());
		Assert.assertEquals(DruhOblasti.STAT, c.oblast().druh());
		Assert.assertEquals(java.util.Collections.singletonList("stat-cz"), c.oblast().idOblasti());
	}

	@Test
	public void sablonaPraha() {
		final Challenge c = Sablony.praha360();
		Assert.assertEquals(Sablony.NAZEV_PRAHA, c.nazev());
		Assert.assertEquals("GC56R49", c.stred().kodListingu());
		Assert.assertEquals(DruhOblasti.SADA, c.oblast().druh());
		Assert.assertEquals(java.util.Collections.singletonList("praha"), c.oblast().idOblasti());
	}

	@Test
	public void souradnicovyStredPresMapuZachovaPresnost() {
		final Challenge c = new Challenge("Vlastní", Typ.SEKTORY_360, Stred.souradnice(50.123456789, 14.987654321), OblastDef.zadna(), ZdrojPravidel.STATOR);
		final Challenge zpet = Challenge.zMapy(c.naMapu());
		Assert.assertEquals(c, zpet);
		Assert.assertEquals(50.123456789, zpet.stred().lat(), 0);
		Assert.assertEquals(14.987654321, zpet.stred().lon(), 0);
		Assert.assertEquals(ZdrojPravidel.STATOR, zpet.zdroj());
	}

	@Test
	public void chybejiciUdajJeChyba() {
		final Map<String, String> m = new LinkedHashMap<>(Sablony.cesko360().naMapu());
		m.remove("stred.kod");
		try {
			Challenge.zMapy(m);
			Assert.fail();
		} catch (final IllegalArgumentException e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains("stred.kod"));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void neznamyTypJeChyba() {
		final Map<String, String> m = new LinkedHashMap<>(Sablony.od360Domova().naMapu());
		m.put("typ", "BUDOUCI_TYP");
		Challenge.zMapy(m);
	}

	@Test(expected = IllegalArgumentException.class)
	public void neplatneSouradniceJsouChyba() {
		final Map<String, String> m = new LinkedHashMap<>(
				new Challenge("x", Typ.SEKTORY_360, Stred.souradnice(1, 2), OblastDef.zadna(), ZdrojPravidel.PGC).naMapu());
		m.put("stred.lat", "padesat");
		Challenge.zMapy(m);
	}

	@Test(expected = IllegalArgumentException.class)
	public void sadaBezOblastiNeni() {
		OblastDef.sada();
	}

	@Test
	public void sablonyJsouNezavisleHodnoty() {
		Assert.assertNotSame(Sablony.praha360(), Sablony.praha360());
		Assert.assertEquals(Sablony.praha360(), Sablony.praha360());
		Assert.assertNotEquals(Sablony.praha360(), Sablony.cesko360());
	}

	@Test
	public void sablonaMaticeDT() {
		final Challenge c = Sablony.maticeDT();
		Assert.assertEquals("Matice D/T", c.nazev());
		Assert.assertEquals(Typ.DT_MATICE, c.typ());
		Assert.assertEquals(DruhStredu.DOMOV, c.stred().druh());
		Assert.assertEquals(DruhOblasti.ZADNA, c.oblast().druh());
		Assert.assertEquals(ZdrojPravidel.PGC, c.zdroj());
		Assert.assertEquals(c, Challenge.zMapy(c.naMapu()));
		Assert.assertNotSame(Sablony.maticeDT(), Sablony.maticeDT());
		Assert.assertEquals(Sablony.maticeDT(), Sablony.maticeDT());
	}

	@Test
	public void sablonyNemajiKalendar() {
		for (final Challenge c : Sablony.vse()) {
			Assert.assertNotEquals(c.nazev(), Typ.KALENDAR, c.typ());
		}
		Assert.assertTrue(Sablony.vse().contains(Sablony.maticeDT()));
	}

	@Test
	public void sablonyMajiUnikatniNazvy() {
		final java.util.Set<String> nazvy = new java.util.HashSet<>();
		for (final Challenge c : Sablony.vse()) {
			Assert.assertTrue(c.nazev(), nazvy.add(c.nazev().toLowerCase(java.util.Locale.ROOT)));
		}
	}

	@Test
	public void nazvySablonSKodemAOriginalemKese() {
		Assert.assertEquals("GC4PCQB – Challenge - 360 degrees of the Czech Republic", Sablony.cesko360().nazev());
		Assert.assertEquals("GC56R49 – Challenge - 360 Degrees of Prague", Sablony.praha360().nazev());
		Assert.assertEquals("360° od domova", Sablony.od360Domova().nazev());
		Assert.assertEquals("Matice D/T", Sablony.maticeDT().nazev());
		Assert.assertTrue(Sablony.cesko360().nazev().startsWith(Sablony.cesko360().stred().kodListingu() + " – "));
		Assert.assertTrue(Sablony.praha360().nazev().startsWith(Sablony.praha360().stred().kodListingu() + " – "));
	}

	@Test
	public void nazvySablonSeVejdouDoLimituNazvu() {
		for (final Challenge c : Sablony.vse()) {
			Assert.assertTrue(c.nazev(), c.nazev().length() <= ChallengeSeznam.NEJDELSI_NAZEV - " (vlastní 99)".length());
		}
	}
}
