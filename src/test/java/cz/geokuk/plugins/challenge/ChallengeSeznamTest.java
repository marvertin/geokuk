package cz.geokuk.plugins.challenge;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.plugins.challenge.Challenge.OblastDef;
import cz.geokuk.plugins.challenge.Challenge.Stred;
import cz.geokuk.plugins.challenge.Challenge.Typ;
import cz.geokuk.plugins.challenge.Challenge.ZdrojPravidel;

public class ChallengeSeznamTest {

	private static Challenge vlastni(final String nazev) {
		return new Challenge(nazev, Typ.SEKTORY_360, Stred.souradnice(50, 14), OblastDef.zadna(), ZdrojPravidel.PGC);
	}

	private static void chyba(final Runnable akce, final String ocekavanaZprava) {
		try {
			akce.run();
			Assert.fail("očekávána chyba " + ocekavanaZprava);
		} catch (final ChallengeChyba e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains(ocekavanaZprava));
		}
	}

	@Test
	public void zacinaSeCtyrmiSablonamiBezVlastnichAktivnich() {
		final ChallengeSeznam s = new ChallengeSeznam();
		Assert.assertEquals(4, s.sablony().size());
		Assert.assertTrue(s.vlastni().isEmpty());
		Assert.assertEquals(4, s.vsechny().size());
		Assert.assertTrue(s.aktivni().isEmpty());
		Assert.assertEquals(1, s.strop());
		Assert.assertTrue(s.jeSablona(Sablony.NAZEV_PRAHA));
		Assert.assertTrue("bez ohledu na velikost písmen", s.jeSablona(Sablony.NAZEV_PRAHA.toUpperCase(java.util.Locale.ROOT)));
	}

	@Test
	public void sablonuNejdePrepsatSmazatAniPrejmenovat() {
		final ChallengeSeznam s = new ChallengeSeznam();
		chyba(() -> s.ulozVlastni(vlastni(Sablony.NAZEV_PRAHA)), "kopii");
		chyba(() -> s.ulozVlastni(vlastni("  " + Sablony.NAZEV_PRAHA.toUpperCase(java.util.Locale.ROOT) + " ")), "kopii");
		chyba(() -> s.smazVlastni(Sablony.NAZEV_PRAHA), "Šablonu nejde smazat");
		chyba(() -> s.prejmenuj(Sablony.NAZEV_PRAHA, "Jiné"), "Šablonu nejde přejmenovat");
		Assert.assertEquals(Sablony.praha360(), s.najdi(Sablony.NAZEV_PRAHA));
	}

	@Test
	public void kopieSablonyMaJinyNazevAJeVlastni() {
		final ChallengeSeznam s = new ChallengeSeznam();
		final Challenge k1 = s.kopieSablony(Sablony.NAZEV_PRAHA);
		Assert.assertEquals(Sablony.NAZEV_PRAHA + " (vlastní)", k1.nazev());
		Assert.assertEquals(Sablony.praha360().oblast(), k1.oblast());
		Assert.assertEquals(Sablony.praha360().stred(), k1.stred());
		final Challenge k2 = s.kopieSablony(Sablony.NAZEV_PRAHA);
		Assert.assertEquals(Sablony.NAZEV_PRAHA + " (vlastní 2)", k2.nazev());
		Assert.assertEquals(2, s.vlastni().size());
		Assert.assertEquals("šablona zůstala beze změny", Sablony.praha360(), s.najdi(Sablony.NAZEV_PRAHA));
		chyba(() -> s.kopieSablony("Neexistuje"), "neexistuje");
		chyba(() -> s.kopieSablony(Sablony.NAZEV_PRAHA + " (vlastní)"), "neexistuje");
	}

	@Test
	public void vlastniDefiniceSeUkladaNahrazujeAMaJednoznacnyNazev() {
		final ChallengeSeznam s = new ChallengeSeznam();
		s.ulozVlastni(vlastni("Moje"));
		final Challenge upravena = new Challenge("MOJE", Typ.SEKTORY_360, Stred.souradnice(51, 15), OblastDef.zadna(), ZdrojPravidel.STATOR);
		s.ulozVlastni(upravena);
		Assert.assertEquals(1, s.vlastni().size());
		Assert.assertEquals(ZdrojPravidel.STATOR, s.najdi("moje").zdroj());
		Assert.assertEquals("MOJE", s.najdi("moje").nazev());
	}

	@Test
	public void nazevJePlatnyJenKdyzNeniPrazdnyDlouhyAniViceradkovy() {
		final ChallengeSeznam s = new ChallengeSeznam();
		chyba(() -> s.ulozVlastni(vlastni("   ")), "musí mít název");
		chyba(() -> s.ulozVlastni(vlastni("")), "musí mít název");
		chyba(() -> s.ulozVlastni(vlastni(new String(new char[81]).replace('\0', 'a'))), "nejvýš 80");
		chyba(() -> s.ulozVlastni(vlastni("a\nb")), "zalomení");
		s.ulozVlastni(vlastni(new String(new char[80]).replace('\0', 'a')));
		Assert.assertEquals("název se ořízne o okraje", "Okrajový", s.ulozVlastni(vlastni("  Okrajový ")).nazev());
	}

	@Test
	public void prejmenovaniZachovaAktivituAPoradi() {
		final ChallengeSeznam s = new ChallengeSeznam();
		s.ulozVlastni(vlastni("A"));
		s.ulozVlastni(vlastni("B"));
		s.ulozVlastni(vlastni("C"));
		s.aktivuj("B");
		s.prejmenuj("B", "Béčko");
		Assert.assertTrue(s.jeAktivni("Béčko"));
		Assert.assertFalse(s.jeAktivni("B"));
		Assert.assertNull(s.najdi("B"));
		Assert.assertEquals(Arrays.asList("A", "Béčko", "C"), nazvy(s.vlastni()));
		chyba(() -> s.prejmenuj("A", "c"), "už existuje");
		chyba(() -> s.prejmenuj("A", Sablony.NAZEV_PRAHA), "už existuje");
		chyba(() -> s.prejmenuj("Neexistuje", "X"), "neexistuje");
		s.prejmenuj("A", "a");
		Assert.assertEquals("změna jen velikosti písmen projde", "a", s.najdi("A").nazev());
	}

	private static List<String> nazvy(final List<Challenge> v) {
		final List<String> n = new java.util.ArrayList<>();
		for (final Challenge c : v) {
			n.add(c.nazev());
		}
		return n;
	}

	@Test
	public void zapnutiDalsiVypneDosavadniAVratiJi() {
		final ChallengeSeznam s = new ChallengeSeznam();
		for (int i = 1; i <= 3; i++) {
			s.ulozVlastni(vlastni("V" + i));
		}
		Assert.assertNull("první nic nevypíná", s.aktivuj("V1"));
		Assert.assertNull("už aktivní zůstává", s.aktivuj("V1"));
		Assert.assertEquals(Arrays.asList("V1"), nazvy(s.aktivni()));
		Assert.assertEquals("V1", s.aktivuj("V2").nazev());
		Assert.assertEquals(Arrays.asList("V2"), nazvy(s.aktivni()));
		Assert.assertFalse(s.jeAktivni("V1"));
		Assert.assertEquals("V2", s.aktivuj("360° od domova").nazev());
		Assert.assertEquals(Arrays.asList("360° od domova"), nazvy(s.aktivni()));
		s.deaktivuj("360° od domova");
		Assert.assertNull("nic není vypnuto, když nic neběží", s.aktivuj("V3"));
	}

	@Test
	public void stropJdeNastavitAVypinaSeNejdelZapnuta() {
		final ChallengeSeznam s = new ChallengeSeznam(Sablony.vse(), 2);
		Assert.assertNull(s.aktivuj("360° od domova"));
		Assert.assertNull(s.aktivuj(Sablony.NAZEV_PRAHA));
		Assert.assertEquals("360° od domova", s.aktivuj(Sablony.NAZEV_CESKO).nazev());
		Assert.assertEquals(Arrays.asList(Sablony.NAZEV_PRAHA, Sablony.NAZEV_CESKO), nazvy(s.aktivni()));
		Assert.assertNull("už aktivní nevytlačí nic", s.aktivuj(Sablony.NAZEV_PRAHA));
		Assert.assertEquals(2, s.aktivni().size());
	}

	@Test(expected = IllegalArgumentException.class)
	public void stropMusiBytAspon1() {
		new ChallengeSeznam(Collections.<Challenge> emptyList(), 0);
	}

	@Test
	public void smazaniVypneAktivniAAktivaceNeexistujiciJeChyba() {
		final ChallengeSeznam s = new ChallengeSeznam();
		s.ulozVlastni(vlastni("A"));
		s.aktivuj("A");
		s.smazVlastni("A");
		Assert.assertFalse(s.jeAktivni("A"));
		Assert.assertTrue(s.aktivni().isEmpty());
		chyba(() -> s.aktivuj("A"), "neexistuje");
		chyba(() -> s.smazVlastni("A"), "neexistuje");
	}

	@Test
	public void aktivniSablonaAUpravenaVlastniZustavaAktivni() {
		final ChallengeSeznam s = new ChallengeSeznam(Sablony.vse(), 2);
		s.aktivuj(Sablony.NAZEV_PRAHA);
		s.ulozVlastni(vlastni("X"));
		s.aktivuj("x");
		s.ulozVlastni(new Challenge("X", Typ.SEKTORY_360, Stred.souradnice(1, 2), OblastDef.zadna(), ZdrojPravidel.STATOR));
		Assert.assertEquals(Arrays.asList(Sablony.NAZEV_PRAHA, "X"), nazvy(s.aktivni()));
		Assert.assertEquals(ZdrojPravidel.STATOR, s.aktivni().get(1).zdroj());
	}

	@Test
	public void kopieSablonyMaticeDTMaStejnyTypAJeVlastni() {
		final ChallengeSeznam s = new ChallengeSeznam();
		final Challenge k = s.kopieSablony("Matice D/T");
		Assert.assertEquals("Matice D/T (vlastní)", k.nazev());
		Assert.assertEquals(Challenge.Typ.DT_MATICE, k.typ());
		Assert.assertEquals(Sablony.maticeDT().stred(), k.stred());
		Assert.assertTrue(s.vlastni().contains(k));
		Assert.assertEquals("šablona zůstala beze změny", Sablony.maticeDT(), s.najdi("Matice D/T"));
		Assert.assertTrue(s.jeSablona("matice d/t"));
	}

	@Test
	public void sablonuMaticeDTLzeAktivovatAVlastniNazevSeSablonouSeNesmiShodovat() {
		final ChallengeSeznam s = new ChallengeSeznam();
		s.aktivuj("Matice D/T");
		Assert.assertEquals(Arrays.asList(Sablony.maticeDT()), s.aktivni());
		chyba(() -> s.ulozVlastni(Sablony.maticeDT()), "nejde přepsat");
	}

	@Test
	public void sablonyPoSKodemSeRozeznajiAKopieMajiRozlisitelnyNazev() {
		final ChallengeSeznam s = new ChallengeSeznam();
		Assert.assertTrue(s.jeSablona(Sablony.NAZEV_CESKO));
		Assert.assertTrue(s.jeSablona(Sablony.NAZEV_PRAHA.toLowerCase(java.util.Locale.ROOT)));
		Assert.assertFalse("starý název už šablonu neoznačuje", s.jeSablona("360° of Prague"));
		Assert.assertEquals(Sablony.NAZEV_CESKO + " (vlastní)", s.kopieSablony(Sablony.NAZEV_CESKO).nazev());
		chyba(() -> s.ulozVlastni(vlastni(Sablony.NAZEV_CESKO)), "kopii");
	}

	@Test
	public void kopieVlastniZachovaDefiniciAJeNeaktivni() {
		final ChallengeSeznam s = new ChallengeSeznam();
		final Challenge puvodni = s.ulozVlastni(new Challenge("Moje 360", Challenge.Typ.SEKTORY_360, Challenge.Stred.souradnice(50.1, 14.5), Challenge.OblastDef.stat("CZ"), ZdrojPravidel.STATOR, Challenge.ZdrojeNalezu.VSECHNY));
		s.aktivuj("Moje 360");
		final Challenge k = s.kopieVlastni("Moje 360");
		Assert.assertEquals("Moje 360 (kopie)", k.nazev());
		Assert.assertEquals(puvodni.typ(), k.typ());
		Assert.assertEquals(puvodni.stred(), k.stred());
		Assert.assertEquals(puvodni.oblast(), k.oblast());
		Assert.assertEquals(puvodni.zdroj(), k.zdroj());
		Assert.assertEquals(puvodni.nalezy(), k.nalezy());
		Assert.assertFalse("kopie není aktivní", s.jeAktivni(k.nazev()));
		Assert.assertTrue("původní zůstala aktivní", s.jeAktivni("Moje 360"));
		Assert.assertFalse(s.jeSablona(k.nazev()));
	}

	@Test
	public void kopieVlastniMaticeDTZachovaTyp() {
		final ChallengeSeznam s = new ChallengeSeznam();
		final Challenge v = s.kopieSablony("Matice D/T");
		final Challenge k = s.kopieVlastni(v.nazev());
		Assert.assertEquals(Challenge.Typ.DT_MATICE, k.typ());
		Assert.assertEquals("Matice D/T (vlastní) (kopie)", k.nazev());
	}

	@Test
	public void kopieVlastniMaJedinecnyNazevAKopieKopieSeNeNarusta() {
		final ChallengeSeznam s = new ChallengeSeznam();
		s.ulozVlastni(Sablony.praha360().sNazvem("A"));
		Assert.assertEquals("A (kopie)", s.kopieVlastni("A").nazev());
		Assert.assertEquals("A (kopie 2)", s.kopieVlastni("A").nazev());
		Assert.assertEquals("A (kopie 3)", s.kopieVlastni("A (kopie)").nazev());
		Assert.assertEquals(4, s.vlastni().size());
		chyba(() -> s.kopieVlastni("Neexistuje"), "neexistuje");
		chyba(() -> s.kopieVlastni(Sablony.NAZEV_PRAHA), "neexistuje");
	}

	@Test
	public void kopieVlastniNeprekrociNejdelsiNazev() {
		final ChallengeSeznam s = new ChallengeSeznam();
		final StringBuilder dlouhy = new StringBuilder();
		while (dlouhy.length() < ChallengeSeznam.NEJDELSI_NAZEV) {
			dlouhy.append('x');
		}
		s.ulozVlastni(Sablony.praha360().sNazvem(dlouhy.toString()));
		final Challenge k1 = s.kopieVlastni(dlouhy.toString());
		final Challenge k2 = s.kopieVlastni(dlouhy.toString());
		Assert.assertEquals(ChallengeSeznam.NEJDELSI_NAZEV, k1.nazev().length());
		Assert.assertTrue(k1.nazev().endsWith(" (kopie)"));
		Assert.assertTrue(k2.nazev().endsWith(" (kopie 2)"));
		Assert.assertTrue(k2.nazev().length() <= ChallengeSeznam.NEJDELSI_NAZEV);
		Assert.assertNotEquals(k1.nazev(), k2.nazev());
	}
}
