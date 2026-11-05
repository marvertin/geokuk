package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.plugins.challenge.Challenge.ZdrojPravidel;
import cz.geokuk.plugins.challenge.OblastPolygony.Cast;
import cz.geokuk.plugins.challenge.OblastPolygony.Okraj;

public class VypocetSektoruTest {

	private static final double LAT = 50.0;
	private static final double LON = 14.0;

	static final class Kes implements VstupKes {
		final String id;
		final double lat;
		final double lon;
		boolean nalezena;
		boolean lab;
		boolean locationless;
		boolean vlastni;
		boolean neaktivni;
		boolean mimoKandidaty;
		StavLogu log = StavLogu.NEZNAMY;

		Kes(final String id, final double lat, final double lon, final boolean nalezena) {
			this.id = id;
			this.lat = lat;
			this.lon = lon;
			this.nalezena = nalezena;
		}

		@Override
		public boolean mimoKandidaty() {
			return mimoKandidaty;
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
		public VstupKes.StavLogu logNaGc() {
			return log;
		}
	}

	/** Keš ve zadaném azimutu a vzdálenosti od středu (po kouli). */
	static Kes vAzimutu(final String id, final double azimut, final double metru, final boolean nalezena) {
		final double d = metru / Geodezie.POLOMER_ZEME;
		final double a = Math.toRadians(azimut);
		final double p1 = Math.toRadians(LAT);
		final double l1 = Math.toRadians(LON);
		final double p2 = Math.asin(Math.sin(p1) * Math.cos(d) + Math.cos(p1) * Math.sin(d) * Math.cos(a));
		final double l2 = l1 + Math.atan2(Math.sin(a) * Math.sin(d) * Math.cos(p1), Math.cos(d) - Math.sin(p1) * Math.sin(p2));
		return new Kes(id, Math.toDegrees(p2), Math.toDegrees(l2), nalezena);
	}

	private static List<Kes> vsechnySektory(final boolean nalezena) {
		final List<Kes> v = new ArrayList<>();
		for (int s = 0; s < 360; s++) {
			v.add(vAzimutu("GC" + s, s + 0.5, 20_000, nalezena));
		}
		return v;
	}

	private static VysledekSektoru pgc(final Iterable<? extends VstupKes> kese) {
		return VypocetSektoru.spocti(ZdrojPravidel.PGC, LAT, LON, null, Collections.<String> emptySet(), kese);
	}

	@Test
	public void testovaciKeseLeziVeSvemSektoru() {
		for (int s = 0; s < 360; s += 7) {
			final Kes k = vAzimutu("x", s + 0.5, 15_000, true);
			Assert.assertEquals(s, Geodezie.sektor(Geodezie.azimutKoule(LAT, LON, k.lat, k.lon)));
		}
	}

	@Test
	public void vsechnyNalezyDajiDokonceno() {
		final VysledekSektoru v = pgc(vsechnySektory(true));
		Assert.assertEquals(360, v.pocetObsazenych());
		Assert.assertTrue(v.chybejici().isEmpty());
		Assert.assertEquals(360, v.pocetZapocitanychNalezu());
	}

	@Test
	public void bezNalezuChybiVse() {
		final VysledekSektoru v = pgc(vsechnySektory(false));
		Assert.assertEquals(0, v.pocetObsazenych());
		Assert.assertEquals(360, v.chybejici().size());
	}

	@Test
	public void chybejiciSektoryAKandidati() {
		final List<Kes> kese = vsechnySektory(true);
		final Kes nenalezena1 = vAzimutu("CAND1", 100.5, 30_000, false);
		final Kes nenalezena2 = vAzimutu("CAND2", 100.3, 10_000, false);
		final Kes nenalezenaJinde = vAzimutu("CAND3", 200.5, 5_000, false);
		kese.set(100, vAzimutu("GC100", 100.5, 20_000, false));
		kese.set(250, vAzimutu("GC250", 250.5, 20_000, false));
		kese.add(nenalezena1);
		kese.add(nenalezena2);
		kese.add(nenalezenaJinde);
		final VysledekSektoru v = pgc(kese);
		Assert.assertEquals(Arrays.asList(100, 250), v.chybejici());
		Assert.assertEquals(358, v.pocetObsazenych());
		Assert.assertFalse(v.jeObsazeny(100));
		Assert.assertTrue(v.jeObsazeny(200));
		Assert.assertNull("obsazený sektor nemá kandidáty", v.kandidati(200));
		Assert.assertEquals(3, v.kandidati(100).pocet());
		Assert.assertEquals("GC100 nebo CAND2 jsou ve stejné vzdálenosti 10-20 km, nejblíž je CAND2", "CAND2", v.kandidati(100).nejblizsiId());
		Assert.assertEquals(10_000, v.kandidati(100).nejblizsiVzdalenost(), 5);
		Assert.assertEquals(1, v.kandidati(250).pocet());
		Assert.assertEquals("GC250", v.kandidati(250).nejblizsiId());
	}

	@Test
	public void pgcVynechaLabALocationlessAStatorLabPocita() {
		final List<Kes> kese = vsechnySektory(true);
		final Kes lab = vAzimutu("LAB", 10.5, 20_000, true);
		lab.lab = true;
		kese.set(10, lab);
		final Kes bez = vAzimutu("LOC", 20.5, 20_000, true);
		bez.locationless = true;
		kese.set(20, bez);
		Assert.assertEquals("PGC: bez Lab a Locationless", Arrays.asList(10, 20), pgc(kese).chybejici());
		final VysledekSektoru stator = VypocetSektoru.spocti(ZdrojPravidel.STATOR, LAT, LON, null, Collections.<String> emptySet(), kese);
		Assert.assertEquals("Stator: Lab počítá, Locationless ne", Collections.singletonList(20), stator.chybejici());
	}

	@Test
	public void statorVynechaIgnorovanePgcIgnorovanymNeRozumi() {
		final List<Kes> kese = vsechnySektory(true);
		final Set<String> ignorovane = new HashSet<>(Collections.singletonList("GC30"));
		Assert.assertEquals(Collections.singletonList(30), VypocetSektoru.spocti(ZdrojPravidel.STATOR, LAT, LON, null, ignorovane, kese).chybejici());
		Assert.assertTrue(VypocetSektoru.spocti(ZdrojPravidel.PGC, LAT, LON, null, ignorovane, kese).chybejici().isEmpty());
	}

	@Test
	public void logNaGcRozhodujeJenUPgcAJenKdyzJeZnamyJakoChybejici() {
		final List<Kes> kese = vsechnySektory(true);
		kese.get(40).log = VstupKes.StavLogu.NE;
		kese.get(41).log = VstupKes.StavLogu.ANO;
		kese.get(42).log = VstupKes.StavLogu.NEZNAMY;
		Assert.assertEquals("jen NE vyřadí nález", Collections.singletonList(40), pgc(kese).chybejici());
		Assert.assertTrue("Stator log neřeší", VypocetSektoru.spocti(ZdrojPravidel.STATOR, LAT, LON, null, Collections.<String> emptySet(), kese).chybejici().isEmpty());
	}

	@Test
	public void stejnaKesJeJenJedenSektorAleVicNalezuSektoruNezvysi() {
		final List<Kes> kese = new ArrayList<>();
		kese.add(vAzimutu("A", 45.5, 10_000, true));
		kese.add(vAzimutu("B", 45.7, 50_000, true));
		final VysledekSektoru v = pgc(kese);
		Assert.assertEquals(1, v.pocetObsazenych());
		Assert.assertEquals(2, v.pocetZapocitanychNalezu());
	}

	@Test
	public void oblastOmezujeNalezyIKandidaty() {
		final List<Kes> kese = new ArrayList<>();
		kese.add(vAzimutu("BLIZKO", 90.5, 10_000, true));
		kese.add(vAzimutu("DALEKO", 180.5, 500_000, true));
		kese.add(vAzimutu("KANDIDAT_V", 0.5, 10_000, false));
		kese.add(vAzimutu("KANDIDAT_MIMO", 270.5, 500_000, false));
		// Čtverec okolo středu o straně asi 100 km.
		final Oblast o = new OblastPolygony("o", "v1", Collections.singletonList(new Cast(OblastPolygonyTest.ctverec(LAT - 0.5, LON - 0.8, LAT + 0.5, LON + 0.8), Collections.<Okraj> emptyList())));
		final VysledekSektoru v = VypocetSektoru.spocti(ZdrojPravidel.PGC, LAT, LON, o, Collections.<String> emptySet(), kese);
		Assert.assertEquals(1, v.pocetObsazenych());
		Assert.assertEquals(1, v.pocetZapocitanychNalezu());
		Assert.assertTrue(v.jeObsazeny(90));
		Assert.assertFalse("nález mimo oblast nezaplnil sektor", v.jeObsazeny(180));
		Assert.assertEquals("KANDIDAT_V", v.kandidati(0).nejblizsiId());
		Assert.assertNull("kandidát mimo oblast se nenabízí", v.kandidati(270));
	}

	@Test
	public void sjednoceniOblastiPocitaBodVKterekoliOblasti() {
		final Oblast a = new OblastPolygony("a", "v1", Collections.singletonList(new Cast(OblastPolygonyTest.ctverec(49.9, 13.9, 50.1, 14.05), Collections.<Okraj> emptyList())));
		final Oblast b = new OblastPolygony("b", "v1", Collections.singletonList(new Cast(OblastPolygonyTest.ctverec(49.9, 14.05, 50.1, 14.3), Collections.<Okraj> emptyList())));
		final List<Kes> kese = new ArrayList<>();
		kese.add(vAzimutu("V_A", 270.5, 5_000, true));
		kese.add(vAzimutu("V_B", 90.5, 10_000, true));
		Assert.assertEquals(1, VypocetSektoru.spocti(ZdrojPravidel.PGC, LAT, LON, a, Collections.<String> emptySet(), kese).pocetObsazenych());
		Assert.assertEquals(2, VypocetSektoru.spocti(ZdrojPravidel.PGC, LAT, LON, new SjednoceniOblasti(Arrays.asList(a, b)), Collections.<String> emptySet(), kese).pocetObsazenych());
	}

	@Test
	public void nalezUHranicSektoruJeNejistyASousedChybiNejiste() {
		final List<Kes> kese = vsechnySektory(true);
		// Sektor 50 nahradíme nálezem těsně nad hranicí se sektorem 49 (azimut 50,01) a sektor 49 odebereme.
		kese.set(50, vAzimutu("HRANA", 50.01, 20_000, true));
		kese.remove(49);
		final VysledekSektoru v = pgc(kese);
		Assert.assertEquals(Collections.singletonList(49), v.chybejici());
		Assert.assertTrue("sektor 50 drží jen nález u hranice", v.jeNejisty(50));
		Assert.assertTrue("sektor 49 by mohl být obsazen", v.jeNejisty(49));
		Assert.assertFalse(v.jeNejisty(100));
	}

	@Test
	public void jistyNalezVSektoruZrusiNejistotu() {
		final List<Kes> kese = new ArrayList<>();
		kese.add(vAzimutu("HRANA", 50.01, 20_000, true));
		kese.add(vAzimutu("STRED", 50.5, 20_000, true));
		final VysledekSektoru v = pgc(kese);
		Assert.assertFalse(v.jeNejisty(50));
		Assert.assertTrue("soused nálezu u hranice zůstává nejistý", v.jeNejisty(49));
	}

	@Test
	public void zrusenyVypocetVraciNull() {
		final List<Kes> kese = new ArrayList<>();
		for (int i = 0; i < 40_000; i++) {
			kese.add(vAzimutu("K" + i, i % 360 + 0.5, 1_000, i % 2 == 0));
		}
		Assert.assertNull(VypocetSektoru.spocti(ZdrojPravidel.PGC, LAT, LON, null, Collections.<String> emptySet(), kese, () -> true));
		Assert.assertNotNull(VypocetSektoru.spocti(ZdrojPravidel.PGC, LAT, LON, null, Collections.<String> emptySet(), kese, () -> false));
	}

	@Test
	public void statorAPgcSeLiziUHraniceSektoruKvuliElipsoidu() {
		// Sektor je uříznutý azimut: koule a elipsoid dávají pro stejnou dvojici bodů azimuty lišící se o desetiny stupně.
		final double azKoule = Geodezie.azimutKoule(50.0875, 14.4208, 49.9, 16.3);
		final double azElips = Geodezie.azimutElipsoid(50.0875, 14.4208, 49.9, 16.3);
		Assert.assertEquals(98, Geodezie.sektor(azKoule));
		Assert.assertEquals(98, Geodezie.sektor(azElips));
		Assert.assertNotEquals(azKoule, azElips, 0.01);
	}

	private static VysledekSektoru sKandidaty(final List<Kes> kese) {
		return pgc(kese);
	}

	@Test
	public void kandidatemNeniVlastniZakazanaAniArchivovana() {
		final List<Kes> kese = vsechnySektory(true);
		kese.set(60, vAzimutu("GC60", 60.5, 20_000, false));
		final Kes vlastni = vAzimutu("OWN", 60.4, 5_000, false);
		vlastni.vlastni = true;
		final Kes neaktivni = vAzimutu("OFF", 60.3, 6_000, false);
		neaktivni.neaktivni = true;
		final Kes dobra = vAzimutu("OK", 60.6, 30_000, false);
		kese.add(vlastni);
		kese.add(neaktivni);
		kese.add(dobra);
		final VysledekSektoru v = sKandidaty(kese);
		Assert.assertEquals(2, v.kandidati(60).pocet());
		Assert.assertEquals("GC60", v.kandidati(60).nejblizsiId());
	}

	@Test
	public void ignorovanaNenalezenaKesJeKandidatemUPgcAleUStatoruNe() {
		final List<Kes> kese = vsechnySektory(true);
		kese.set(70, vAzimutu("GC70", 70.5, 20_000, false));
		final Set<String> ignorovane = new HashSet<>(Collections.singletonList("GC70"));
		Assert.assertEquals("GC70", VypocetSektoru.spocti(ZdrojPravidel.PGC, LAT, LON, null, ignorovane, kese).kandidati(70).nejblizsiId());
		Assert.assertNull(VypocetSektoru.spocti(ZdrojPravidel.STATOR, LAT, LON, null, ignorovane, kese).kandidati(70));
	}

	@Test
	public void stredNeplatnyJeChyba() {
		final double[][] spatne = { { Double.NaN, 14 }, { 50, Double.NaN }, { Double.POSITIVE_INFINITY, 14 }, { 50, Double.NEGATIVE_INFINITY }, { 90.001, 14 }, { -91, 14 }, { 50, 180.5 }, { 50, -181 } };
		for (final double[] s : spatne) {
			try {
				VypocetSektoru.spocti(ZdrojPravidel.PGC, s[0], s[1], null, Collections.<String> emptySet(), vsechnySektory(true));
				Assert.fail("měl být odmítnut střed " + s[0] + ", " + s[1]);
			} catch (final IllegalArgumentException e) {
				Assert.assertTrue(e.getMessage(), e.getMessage().contains("Střed"));
			}
			try {
				Challenge.Stred.souradnice(s[0], s[1]);
				Assert.fail("měl být odmítnut střed " + s[0] + ", " + s[1]);
			} catch (final IllegalArgumentException e) {
				Assert.assertTrue(e.getMessage(), e.getMessage().contains("Střed"));
			}
		}
		VypocetSektoru.spocti(ZdrojPravidel.PGC, 90, 180, null, Collections.<String> emptySet(), vsechnySektory(true));
		Challenge.Stred.souradnice(-90, -180);
	}

	@Test
	public void elipsoidKterySeNesejdeDavaNaN() {
		Assert.assertTrue(Double.isNaN(Geodezie.azimutElipsoid(50, 14, -50, -166)));
		Assert.assertTrue(Double.isNaN(Geodezie.azimutElipsoid(0, 0, 0, 179.9)));
		Assert.assertFalse(Double.isNaN(Geodezie.azimutElipsoid(50, 14, 51, 15)));
	}

	@Test
	public void statorUProtinozceSpadneNaKouliSNejistymSektorem() {
		// Střed 50 N, 14 E; nález téměř v protinožci (tam elipsoid nekonverguje).
		final Kes protinozec = new Kes("PROTI", -49.9, -166.2, true);
		Assert.assertTrue("elipsoid tu nekonverguje", Double.isNaN(Geodezie.azimutElipsoid(50, 14, -49.9, -166.2)));
		final VysledekSektoru v = VypocetSektoru.spocti(ZdrojPravidel.STATOR, 50, 14, null, Collections.<String> emptySet(), Collections.singletonList(protinozec));
		Assert.assertEquals(1, v.pocetObsazenych());
		final int s = Geodezie.sektor(Geodezie.azimutKoule(50, 14, -49.9, -166.2));
		Assert.assertTrue(v.jeObsazeny(s));
		Assert.assertTrue("sektor z náhradní koule je nejistý", v.jeNejisty(s));
	}

	@Test
	public void typMimoKandidatyNeniKandidatAleNalezSePocita() {
		final List<Kes> kese = new ArrayList<>();
		for (int s = 0; s < 360; s++) {
			if (s != 100 && s != 200) {
				kese.add(vAzimutu("N" + s, s + 0.5, 20_000, true));
			}
		}
		final Kes exhibit = vAzimutu("EXH", 100.5, 10_000, false);
		exhibit.mimoKandidaty = true;
		final Kes bezne = vAzimutu("OK", 200.5, 10_000, false);
		final Kes nalezenyGiga = vAzimutu("GIGA", 100.5, 15_000, true);
		nalezenyGiga.mimoKandidaty = true;
		kese.add(exhibit);
		kese.add(bezne);
		VysledekSektoru v = VypocetSektoru.spocti(ZdrojPravidel.PGC, LAT, LON, null, Collections.<String> emptySet(), kese);
		Assert.assertNull("exhibit není kandidát", v.kandidati(100));
		Assert.assertEquals("OK", v.kandidati(200).nejblizsiId());
		kese.add(nalezenyGiga);
		v = VypocetSektoru.spocti(ZdrojPravidel.PGC, LAT, LON, null, Collections.<String> emptySet(), kese);
		Assert.assertFalse("nález tohoto typu sektor zaplní", v.chybejici().contains(100));
	}
}
