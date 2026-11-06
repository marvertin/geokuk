package cz.geokuk.plugins.challenge;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/** Čtečka hranic RÚIAN proti prázdným, poškozeným, nezvykle zapsaným a extrémním vstupům: vždy buď oblasti, nebo {@link IOException}, nikdy jiná výjimka. */
public class RuianOdolnostTest {

	private static final String HLAVICKA = "# staženo 2026-11-05\n";
	private static final String CTVEREC = "oblast ctverec Čtverec\nokruh 4\n1400000 5000000\n100000 0\n0 100000\n-100000 0\nkonec\n";

	private static Map<String, OblastPolygony> nacti(final String text) throws IOException {
		return FormatRuian.nacti(new StringReader(text));
	}

	private static void odmitne(final String text, final String cast) {
		try {
			nacti(text);
			Assert.fail("mělo se odmítnout: " + cast);
		} catch (final IOException e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains(cast));
		}
	}

	// ---- Prázdné a téměř prázdné vstupy

	@Test
	public void prazdnyVstupNemaVerziAOdmitneSe() {
		odmitne("", "verzi dat");
		odmitne("\n\n   \n", "verzi dat");
		odmitne("# jen komentář\n", "verzi dat");
	}

	@Test
	public void souborJenSVerziDaPrazdnyVysledek() throws Exception {
		Assert.assertTrue(nacti(HLAVICKA).isEmpty());
		Assert.assertTrue(nacti("\n\n" + HLAVICKA + "\n\n").isEmpty());
	}

	@Test
	public void vysledekJeNemenitelnyAZachovavaPoradiOblasti() throws Exception {
		final String text = HLAVICKA + CTVEREC.replace("ctverec", "b") + CTVEREC.replace("ctverec", "a");
		final Map<String, OblastPolygony> m = nacti(text);
		Assert.assertEquals(java.util.Arrays.asList("b", "a"), new java.util.ArrayList<>(m.keySet()));
		try {
			m.clear();
			Assert.fail();
		} catch (final UnsupportedOperationException e) {
			// očekávané
		}
	}

	// ---- Zápis řádků

	@Test
	public void konceRadkuWindowsAStarePocitaceSeBeruJakoKonceRadku() throws Exception {
		Assert.assertEquals(1, nacti((HLAVICKA + CTVEREC).replace("\n", "\r\n")).size());
		Assert.assertEquals(1, nacti((HLAVICKA + CTVEREC).replace("\n", "\r")).size());
	}

	@Test
	public void tabulatoryMezeryNaKonciAOdsazeniNevadi() throws Exception {
		final String text = "  # staženo 2026-11-05  \n  oblast ctverec Čtverec \t \n  okruh 4  \n1400000\t5000000\n  100000   0  \n0 100000\n-100000 0 \n konec \n";
		Assert.assertEquals(1, nacti(text).size());
	}

	@Test
	public void souborBezPosledniNovehoRadkuSeNacte() throws Exception {
		final String bezKonce = (HLAVICKA + CTVEREC).substring(0, (HLAVICKA + CTVEREC).length() - 1);
		Assert.assertTrue(bezKonce.endsWith("konec"));
		Assert.assertEquals(1, nacti(bezKonce).size());
	}

	@Test
	public void znackaBomNaZacatkuSeOdmitneSrozumitelneAneJinouVyjimkou() {
		odmitne("﻿" + HLAVICKA + CTVEREC, "Řádek 1");
	}

	@Test
	public void ridiciZnakyANulovyBajtSeOdmitnou() {
		odmitne(HLAVICKA + "oblast a A\nokruh 3\n1 1\n1\u0000 1\n1 1\nkonec\n", "Neplatné číslo");
		odmitne(HLAVICKA + "\u0007abc\n", "Neznámý řádek");
	}

	@Test
	public void kodOblastiMusiBytMalaPismenaCislaAPomlcky() throws Exception {
		final String okruh = "okruh 3\n1400000 5000000\n100000 0\n0 100000\nkonec\n";
		Assert.assertEquals(1, nacti(HLAVICKA + "oblast a-1 N\n" + okruh).size());
		Assert.assertEquals(1, nacti(HLAVICKA + "oblast 9 N\n" + okruh).size());
		for (final String zly : new String[] { "A", "-a", "a_b", "á", "a.b", "a/b", "" }) {
			odmitne(HLAVICKA + "oblast " + zly + " N\n" + okruh, "Neplatná hlavička");
		}
		odmitne(HLAVICKA + "oblast jenKod\n" + okruh, "Neplatná hlavička");
	}

	// ---- Čísla

	@Test
	public void cislaMimoRozsahLongSeOdmitnouJakoNeplatnaNeJakoJinaVyjimka() {
		final String zacatek = HLAVICKA + "oblast a A\nokruh 3\n";
		odmitne(zacatek + "99999999999999999999 0\n1 1\n1 1\nkonec\n", "Neplatné číslo");
		odmitne(zacatek + "1 -99999999999999999999\n1 1\n1 1\nkonec\n", "Neplatné číslo");
		odmitne(HLAVICKA + "oblast a A\nokruh 99999999999999999999\n", "Neplatné číslo");
		odmitne(HLAVICKA + "oblast a A\nokruh 2147483648\n", "Neplatné číslo");
		odmitne(HLAVICKA + "oblast a A\nokruh -3\n", "mimo rozsah");
		odmitne(HLAVICKA + "oblast a A\nokruh 0\n", "mimo rozsah");
	}

	@Test
	public void necisloDesetinneCisloAExponentSeOdmitnou() {
		final String zacatek = HLAVICKA + "oblast a A\nokruh 3\n";
		for (final String zly : new String[] { "1.5 1", "1e3 1", "NaN 1", "Infinity 1", "0x10 1", "1,5 1", "-- 1", "+ 1", "1 a" }) {
			odmitne(zacatek + zly + "\n1 1\n1 1\nkonec\n", "Neplatné číslo");
		}
	}

	@Test
	public void bodSJednimNeboTremiCislySeOdmitne() {
		final String zacatek = HLAVICKA + "oblast a A\nokruh 3\n";
		odmitne(zacatek + "1\n1 1\n1 1\nkonec\n", "dvě čísla");
		odmitne(zacatek + "1 1 1\n1 1\n1 1\nkonec\n", "dvě čísla");
		odmitne(zacatek + "\n1 1\n1 1\nkonec\n", "dvě čísla");
	}

	@Test
	public void souradniceKterePosunemPresahnouRozsahSeOdmitnouIKdyzJednotliveDeltyJsouMale() {
		final StringBuilder b = new StringBuilder(HLAVICKA + "oblast a A\nokruh 4\n0 0\n");
		b.append("9000000 0\n9000000 0\n1 0\nkonec\n");
		odmitne(b.toString(), "mimo rozsah souřadnic");
		odmitne(HLAVICKA + "oblast a A\nokruh 3\n0 0\n0 9000000\n0 1\nkonec\n", "mimo rozsah souřadnic");
		odmitne(HLAVICKA + "oblast a A\nokruh 3\n0 0\n-18000000 0\n-1 0\nkonec\n", "mimo rozsah souřadnic");
	}

	// ---- Struktura

	@Test
	public void okruhKratsiNezHlasiAKonecSouboruUprostredOkruhuSeOdmitne() {
		odmitne(HLAVICKA + "oblast a A\nokruh 5\n1 1\n1 1\n1 1\n", "kratší");
		odmitne(HLAVICKA + "oblast a A\nokruh 3\n1 1\nkonec\n", "dvě čísla");
	}

	@Test
	public void chybejiciKonecAChybejiciOblastSeHlasi() {
		odmitne(HLAVICKA + "oblast a A\nokruh 3\n1 1\n1 1\n1 1\n", "Chybí konec oblasti a");
		odmitne(HLAVICKA + "oblast a A\nokruh 3\n1 1\n1 1\n1 1\noblast b B\n", "Chybí konec oblasti a");
		odmitne(HLAVICKA + "konec\n", "Konec bez oblasti");
		odmitne(HLAVICKA + "okruh 3\n1 1\n1 1\n1 1\n", "Okruh mimo oblast");
		odmitne(HLAVICKA + "oblast a A\nkonec\n", "oblast bez okruhu");
		odmitne(HLAVICKA + CTVEREC + "konec\n", "Konec bez oblasti");
	}

	@Test
	public void opakovanyKodOblastiSeOdmitne() {
		odmitne(HLAVICKA + CTVEREC + CTVEREC, "Opakovaná");
	}

	@Test
	public void neznamyNebezvyznamnyRadekSeOdmitneSCislemRadku() {
		odmitne(HLAVICKA + "oblast_a A\n", "Řádek 2: Neznámý řádek");
		odmitne(HLAVICKA + "OBLAST a A\n", "Neznámý řádek");
		odmitne(HLAVICKA + "oblastX a A\n", "Neznámý řádek");
		odmitne(HLAVICKA + "okruh\n", "Neznámý řádek");
		odmitne(HLAVICKA + "oblast a A\nokruh \n", "Neznámý řádek");
		odmitne(HLAVICKA + "konecx\n", "Neznámý řádek");
	}

	@Test
	public void datumVeVerziBereZPrvnihoKomentareAKomentarMeziDatyNevadi() throws Exception {
		final String text = "# staženo 2025-01-02\n# staženo 2026-03-04\n" + CTVEREC.replace("okruh 4\n", "okruh 4\n# komentář uvnitř? ne, je to bod\n").replace("# komentář uvnitř? ne, je to bod\n", "");
		final OblastPolygony o = nacti(text).get("ctverec");
		Assert.assertTrue(o.verze(), o.verze().startsWith("ruian-2025-01-02/"));
		odmitne("# staženo 2026-1-2\n" + CTVEREC, "verzi dat");
		odmitne("# stazeno 2026-01-02\n" + CTVEREC, "verzi dat");
	}

	// ---- Degenerované a extrémní oblasti

	@Test
	public void degenerovanyOkruhSeNactenaleNicNeobsahuje() throws Exception {
		final String text = HLAVICKA + "oblast bod B\nokruh 3\n1400000 5000000\n0 0\n0 0\nkonec\n" + "oblast primka P\nokruh 3\n1400000 5000000\n100000 0\n100000 0\nkonec\n";
		final Map<String, OblastPolygony> m = nacti(text);
		for (final OblastPolygony o : m.values()) {
			for (double lat = 49.9; lat <= 50.2; lat += 0.05) {
				for (double lon = 13.9; lon <= 14.3; lon += 0.05) {
					Assert.assertFalse(o.id() + " " + lat + "," + lon, o.obsahuje(lat, lon));
				}
			}
		}
	}

	@Test
	public void okruhNaPolechAAntimeridianuSeNacteAObsahuje() throws Exception {
		// Pás u severního pólu 85° až 90° a délek 0° až 90°, pás u datové čáry 170° až 180°.
		final String text = HLAVICKA + "oblast pol P\nokruh 4\n0 8500000\n9000000 0\n0 500000\n-9000000 0\nkonec\n"
				+ "oblast cara C\nokruh 4\n17000000 0\n1000000 0\n0 1000000\n-1000000 0\nkonec\n";
		final Map<String, OblastPolygony> m = nacti(text);
		Assert.assertTrue(m.get("pol").obsahuje(87, 45));
		Assert.assertFalse(m.get("pol").obsahuje(80, 45));
		Assert.assertFalse(m.get("pol").obsahuje(87, 100));
		Assert.assertTrue(m.get("cara").obsahuje(5, 175));
		Assert.assertTrue(m.get("cara").obsahuje(5, 179.99));
		Assert.assertFalse(m.get("cara").obsahuje(5, -179.99));
	}

	@Test
	public void okruhPresPolednik180SeOdmitne() {
		odmitne(HLAVICKA + "oblast a A\nokruh 4\n17900000 0\n200000 0\n0 100000\n-200000 0\nkonec\n", "mimo rozsah souřadnic");
		odmitne(HLAVICKA + "oblast a A\nokruh 3\n17900000 0\n-35800000 0\n0 100000\nkonec\n", "poledn");
	}

	@Test
	public void velkyJedenRadekBezKoncePrilisVelkySouborSeOdmitneBezJinevyjimky() {
		final StringBuilder b = new StringBuilder("# ");
		for (int i = 0; i < FormatRuian.NEJVIC_ZNAKU + 10; i++) {
			b.append('x');
		}
		odmitne(b.toString(), "velký");
	}

	@Test
	public void chybaCteniZdrojePrejdeJakoIOException() {
		final Reader rozbity = new Reader() {
			private int volani;

			@Override
			public int read(final char[] buf, final int off, final int len) throws IOException {
				if (volani++ == 0) {
					final String s = HLAVICKA + "oblast a A\n";
					s.getChars(0, s.length(), buf, off);
					return s.length();
				}
				throw new IOException("disk selhal");
			}

			@Override
			public void close() {}
		};
		try {
			FormatRuian.nacti(rozbity);
			Assert.fail();
		} catch (final IOException e) {
			Assert.assertEquals("disk selhal", e.getMessage());
		}
	}

	@Test
	public void zabudovanaDataSeNactouAVsechnyOblastiMajiPlatnouVerziAObsahujiSvujStred() throws Exception {
		final ZdrojovyPoskytovatelOblasti zdroj = new ZdrojovyPoskytovatelOblasti();
		for (final String id : new String[] { "stat-cz", "praha" }) {
			final Oblast o = zdroj.najdi(id).orElseThrow(() -> new AssertionError("chybí " + id));
			Assert.assertTrue(o.verze(), o.verze().startsWith("ruian-"));
		}
		Assert.assertTrue(zdroj.najdi("praha").get().obsahuje(50.0875, 14.4208));
		Assert.assertTrue(zdroj.najdi("stat-cz").get().obsahuje(49.8, 15.5));
		Assert.assertFalse(zdroj.najdi("stat-cz").get().obsahuje(Double.NaN, 15.5));
		Assert.assertFalse(zdroj.najdi("stat-cz").get().obsahuje(49.8, Double.NaN));
		Assert.assertFalse(zdroj.najdi("stat-cz").get().obsahuje(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY));
		Assert.assertFalse(zdroj.najdi("neexistuje").isPresent());
	}
}
