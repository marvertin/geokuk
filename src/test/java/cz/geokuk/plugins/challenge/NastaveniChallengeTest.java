package cz.geokuk.plugins.challenge;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.plugins.challenge.Challenge.OblastDef;
import cz.geokuk.plugins.challenge.Challenge.Stred;
import cz.geokuk.plugins.challenge.Challenge.Typ;
import cz.geokuk.plugins.challenge.Challenge.ZdrojPravidel;
import cz.geokuk.plugins.challenge.Challenge.ZdrojeNalezu;

public class NastaveniChallengeTest {

	static class MapoveUloziste implements ChallengeUloziste {
		final Map<String, String> m = new LinkedHashMap<>();

		@Override
		public String get(final String klic) {
			return m.get(klic);
		}

		@Override
		public void put(final String klic, final String hodnota) {
			m.put(klic, hodnota);
		}

		@Override
		public void remove(final String klic) {
			m.remove(klic);
		}

		@Override
		public Set<String> klice() {
			return new java.util.LinkedHashSet<>(m.keySet());
		}
	}

	private static Challenge vlastni(final String nazev, final ZdrojPravidel zdroj, final ZdrojeNalezu nalezy) {
		return new Challenge(nazev, Typ.SEKTORY_360, Stred.souradnice(50.123456789, 14.987654321), OblastDef.sada("okres-a", "okres-b"), zdroj, nalezy);
	}

	@Test
	public void prazdneUlozisteDaVychoziStav() {
		final NastaveniChallenge.Nacteno n = NastaveniChallenge.nacti(new MapoveUloziste());
		Assert.assertFalse(n.zapnuto);
		Assert.assertEquals(0, n.preskoceno);
		Assert.assertEquals(4, n.seznam.vsechny().size());
		Assert.assertTrue(n.seznam.aktivni().isEmpty());
	}

	@Test
	public void ulozeniANacteniVratiVsechnoVcetnePoradiAktivnich() {
		final MapoveUloziste u = new MapoveUloziste();
		final ChallengeSeznam s = new ChallengeSeznam();
		s.ulozVlastni(vlastni("Moje A", ZdrojPravidel.STATOR, ZdrojeNalezu.VSECHNY));
		s.ulozVlastni(vlastni("Moje B", ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE));
		s.kopieSablony("360° of Prague");
		s.aktivuj("Moje B");
		s.aktivuj("360° of Prague");
		s.aktivuj("Moje A");
		NastaveniChallenge.uloz(s, true, u);
		final NastaveniChallenge.Nacteno n = NastaveniChallenge.nacti(u);
		Assert.assertTrue(n.zapnuto);
		Assert.assertEquals(0, n.preskoceno);
		Assert.assertEquals(s.vlastni(), n.seznam.vlastni());
		Assert.assertEquals(Arrays.asList("Moje B", "360° of Prague", "Moje A"), Arrays.asList(n.seznam.aktivni().get(0).nazev(), n.seznam.aktivni().get(1).nazev(), n.seznam.aktivni().get(2).nazev()));
		Assert.assertEquals(ZdrojeNalezu.VSECHNY, n.seznam.najdi("Moje A").nalezy());
		Assert.assertEquals(50.123456789, n.seznam.najdi("Moje A").stred().lat(), 0);
		Assert.assertFalse("šablony se neukládají", u.m.toString().contains("360° of the Czech Republic"));
	}

	@Test
	public void novyZapisSmazeDefiniceKteryUzNejsou() {
		final MapoveUloziste u = new MapoveUloziste();
		final ChallengeSeznam s = new ChallengeSeznam();
		s.ulozVlastni(vlastni("A", ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE));
		s.ulozVlastni(vlastni("B", ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE));
		NastaveniChallenge.uloz(s, false, u);
		s.smazVlastni("A");
		NastaveniChallenge.uloz(s, false, u);
		final NastaveniChallenge.Nacteno n = NastaveniChallenge.nacti(u);
		Assert.assertEquals(1, n.seznam.vlastni().size());
		Assert.assertEquals("B", n.seznam.vlastni().get(0).nazev());
		Assert.assertFalse(u.m.keySet().toString(), u.m.keySet().stream().anyMatch(k -> k.startsWith("definice.1.")));
	}

	@Test
	public void cizieKlicenesmazeNicNavic() {
		final MapoveUloziste u = new MapoveUloziste();
		u.put("jineNastaveni", "zůstane");
		NastaveniChallenge.uloz(new ChallengeSeznam(), false, u);
		Assert.assertEquals("zůstane", u.get("jineNastaveni"));
	}

	@Test
	public void chybnaDefiniceSePreskociAOstatniSeNactou() {
		final MapoveUloziste u = new MapoveUloziste();
		final ChallengeSeznam s = new ChallengeSeznam();
		s.ulozVlastni(vlastni("Dobrá", ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE));
		NastaveniChallenge.uloz(s, true, u);
		u.put("definice.1.7.nazev", "Rozbitá");
		u.put("definice.1.7.typ", "NEZNAMY");
		u.put("definice.1.8.nazev", "Bez dalšího");
		u.put("definice.1.x.nazev", "Nečíslovaná");
		u.put("definice.1.nazev", "Bez čísla");
		u.put("definice.1.9.nazev", "360° of Prague");
		u.put("definice.1.9.typ", "SEKTORY_360");
		u.put("definice.1.9.zdroj", "PGC");
		u.put("definice.1.9.stred", "DOMOV");
		u.put("definice.1.9.oblast", "ZADNA");
		final NastaveniChallenge.Nacteno n = NastaveniChallenge.nacti(u);
		Assert.assertEquals(1, n.seznam.vlastni().size());
		Assert.assertEquals("Dobrá", n.seznam.vlastni().get(0).nazev());
		Assert.assertEquals("rozbitá, neúplná, nečíslovaná, bez čísla a kolize se šablonou", 5, n.preskoceno);
		Assert.assertTrue(n.zapnuto);
	}

	@Test
	public void aktivniKteraNeexistujeSePreskociADuplicityNicNezpusobi() {
		final MapoveUloziste u = new MapoveUloziste();
		u.put("format", "1");
		u.put("aktivni", "360° of Prague\nNeexistuje\n360° of Prague\n\n360° od domova");
		final NastaveniChallenge.Nacteno n = NastaveniChallenge.nacti(u);
		Assert.assertEquals(2, n.seznam.aktivni().size());
		Assert.assertEquals(1, n.preskoceno);
	}

	@Test
	public void aktivniNadStropSePreskociSeZpravou() {
		final MapoveUloziste u = new MapoveUloziste();
		final ChallengeSeznam velky = new ChallengeSeznam(Sablony.vse(), 3);
		for (int i = 1; i <= 3; i++) {
			velky.ulozVlastni(vlastni("V" + i, ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE));
		}
		NastaveniChallenge.uloz(velky, false, u);
		u.put("aktivni", "V1\nV2\nV3\n360° od domova\n360° of Prague\n360° of the Czech Republic");
		final NastaveniChallenge.Nacteno n = NastaveniChallenge.nacti(u);
		Assert.assertEquals(ChallengeSeznam.NEJVIC_AKTIVNICH, n.seznam.aktivni().size());
		Assert.assertEquals(1, n.preskoceno);
	}

	@Test
	public void neznamyFormatDaVychoziHodnotyANicNePrepise() {
		final MapoveUloziste u = new MapoveUloziste();
		u.put("format", "9");
		u.put("zapnuto", "true");
		final NastaveniChallenge.Nacteno n = NastaveniChallenge.nacti(u);
		Assert.assertFalse(n.zapnuto);
		Assert.assertEquals(4, n.seznam.vsechny().size());
		Assert.assertEquals("9", u.get("format"));
	}

	@Test
	public void definiceBezPoleNalezyMaVychoziZapnute() {
		final MapoveUloziste u = new MapoveUloziste();
		u.put("verze", "1");
		u.put("definice.1.0.nazev", "Starší");
		u.put("definice.1.0.typ", "SEKTORY_360");
		u.put("definice.1.0.zdroj", "PGC");
		u.put("definice.1.0.stred", "DOMOV");
		u.put("definice.1.0.oblast", "ZADNA");
		final NastaveniChallenge.Nacteno n = NastaveniChallenge.nacti(u);
		Assert.assertEquals(ZdrojeNalezu.ZAPNUTE, n.seznam.najdi("Starší").nalezy());
	}

	@Test
	public void poskozenaHodnotaStredu() {
		final MapoveUloziste u = new MapoveUloziste();
		u.put("verze", "1");
		u.put("definice.1.0.nazev", "Rozbitá souřadnice");
		u.put("definice.1.0.typ", "SEKTORY_360");
		u.put("definice.1.0.zdroj", "PGC");
		u.put("definice.1.0.stred", "SOURADNICE");
		u.put("definice.1.0.stred.lat", "99");
		u.put("definice.1.0.stred.lon", "14");
		u.put("definice.1.0.oblast", "ZADNA");
		final NastaveniChallenge.Nacteno n = NastaveniChallenge.nacti(u);
		Assert.assertEquals(0, n.seznam.vlastni().size());
		Assert.assertEquals(1, n.preskoceno);
	}

	/** Úložiště, které po zadaném počtu zápisů selže jako preference při nepovolené hodnotě. */
	private static final class SelhavajiciUloziste extends MapoveUloziste {
		int zbyva = Integer.MAX_VALUE;

		@Override
		public void put(final String klic, final String hodnota) {
			if (zbyva-- <= 0) {
				throw new IllegalArgumentException("Value too long");
			}
			super.put(klic, hodnota);
		}
	}

	@Test
	public void selhaniZapisuUprostredNechaPuvodniDefinice() {
		final SelhavajiciUloziste u = new SelhavajiciUloziste();
		final ChallengeSeznam s = new ChallengeSeznam();
		s.ulozVlastni(vlastni("A", ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE));
		s.ulozVlastni(vlastni("B", ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE));
		NastaveniChallenge.uloz(s, true, u);
		s.ulozVlastni(vlastni("C", ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE));
		for (int zbyva = 0; zbyva < 40; zbyva += 3) {
			u.zbyva = zbyva;
			try {
				NastaveniChallenge.uloz(s, true, u);
			} catch (final IllegalArgumentException e) {
				final NastaveniChallenge.Nacteno n = NastaveniChallenge.nacti(u);
				Assert.assertEquals("po selhání po " + zbyva + " zápisech", 2, n.seznam.vlastni().size());
				Assert.assertEquals("A", n.seznam.vlastni().get(0).nazev());
			}
		}
		u.zbyva = Integer.MAX_VALUE;
		NastaveniChallenge.uloz(s, true, u);
		Assert.assertEquals(3, NastaveniChallenge.nacti(u).seznam.vlastni().size());
	}

	@Test
	public void neznamyFormatSeNactePoznacenyJakoNepriepisovatelny() {
		final MapoveUloziste u = new MapoveUloziste();
		u.put("format", "2");
		u.put("verze", "5");
		u.put("definice.5.0.nazev", "Z budoucna");
		Assert.assertTrue(NastaveniChallenge.nacti(u).neznamyFormat);
	}

	@Test
	public void nazevSNulovymZnakemAPrilisDlouhaDefiniceSeOdmitnou() {
		final ChallengeSeznam s = new ChallengeSeznam();
		try {
			s.ulozVlastni(vlastni("A\u0000B", ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE));
			Assert.fail();
		} catch (final ChallengeChyba e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains("řídicí"));
		}
		final String[] ids = new String[800];
		for (int i = 0; i < ids.length; i++) {
			ids[i] = "oblast-" + i;
		}
		try {
			s.ulozVlastni(new Challenge("Velká", Typ.SEKTORY_360, Stred.domov(), OblastDef.sada(ids), ZdrojPravidel.PGC));
			Assert.fail();
		} catch (final ChallengeChyba e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains("rozsáhlá"));
		}
		Assert.assertTrue(s.vlastni().isEmpty());
	}

	private static int cislo(final String text) {
		try {
			return Integer.parseInt(text);
		} catch (final NumberFormatException e) {
			throw new AssertionError("není číslo: " + text, e);
		}
	}

	@Test
	public void druhaInstanceNezapisujeDoSadyPrvni() {
		final MapoveUloziste u = new MapoveUloziste();
		final ChallengeSeznam a = new ChallengeSeznam();
		a.ulozVlastni(vlastni("A", ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE));
		NastaveniChallenge.uloz(a, false, u);
		final int verzeA = cislo(u.get("verze"));
		// Druhá instance načetla stejný stav a zatím první uložila novější sadu.
		final ChallengeSeznam b = new ChallengeSeznam();
		b.ulozVlastni(vlastni("B", ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE));
		u.put("definice." + (verzeA + 3) + ".0.nazev", "Cizí");
		NastaveniChallenge.uloz(b, false, u);
		Assert.assertEquals(Integer.toString(verzeA + 4), u.get("verze"));
		final NastaveniChallenge.Nacteno n = NastaveniChallenge.nacti(u);
		Assert.assertEquals(1, n.seznam.vlastni().size());
		Assert.assertEquals("B", n.seznam.vlastni().get(0).nazev());
	}

	@Test
	public void verzeZa2147483647NepreteceAUlozeniFunguje() {
		final MapoveUloziste u = new MapoveUloziste();
		final ChallengeSeznam s = new ChallengeSeznam();
		s.ulozVlastni(vlastni("A", ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE));
		NastaveniChallenge.uloz(s, false, u);
		u.put("verze", "2147483647");
		u.put("definice.2147483647.0.nazev", "Stará");
		NastaveniChallenge.uloz(s, false, u);
		Assert.assertEquals("2147483648", u.get("verze"));
		Assert.assertEquals(1, NastaveniChallenge.nacti(u).seznam.vlastni().size());
		Assert.assertFalse(u.m.keySet().toString(), u.m.keySet().stream().anyMatch(k -> k.startsWith("definice.2147483647.")));
	}

	@Test
	public void nesmyslneVelkaVerzeSeBereJakoNulaAUlozeniZacneZnovu() {
		final MapoveUloziste u = new MapoveUloziste();
		u.put("verze", "9223372036854775807");
		u.put("definice.9223372036854775807.0.nazev", "Nesmysl");
		final ChallengeSeznam s = new ChallengeSeznam();
		s.ulozVlastni(vlastni("A", ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE));
		NastaveniChallenge.uloz(s, false, u);
		Assert.assertEquals("1", u.get("verze"));
		Assert.assertEquals("A", NastaveniChallenge.nacti(u).seznam.vlastni().get(0).nazev());
	}

	@Test
	public void zapornaUlozenaVerzeSeBereJakoNula() {
		final MapoveUloziste u = new MapoveUloziste();
		u.put("verze", "-5");
		final ChallengeSeznam s = new ChallengeSeznam();
		s.ulozVlastni(vlastni("A", ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE));
		NastaveniChallenge.uloz(s, false, u);
		Assert.assertEquals("1", u.get("verze"));
		Assert.assertEquals(1, NastaveniChallenge.nacti(u).seznam.vlastni().size());
	}

	@Test
	public void verzeBezDefinicNovaJeOJednuVyssi() {
		final MapoveUloziste u = new MapoveUloziste();
		u.put("verze", "7");
		final ChallengeSeznam s = new ChallengeSeznam();
		s.ulozVlastni(vlastni("A", ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE));
		NastaveniChallenge.uloz(s, false, u);
		Assert.assertEquals("8", u.get("verze"));
		Assert.assertEquals("A", NastaveniChallenge.nacti(u).seznam.vlastni().get(0).nazev());
	}

	@Test
	public void hranice1e15JeJeste_platna_ADalsiUz_ne() {
		final String hranice = "1000000000000000";
		final MapoveUloziste u = new MapoveUloziste();
		u.put("verze", hranice);
		final ChallengeSeznam s = new ChallengeSeznam();
		s.ulozVlastni(vlastni("A", ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE));
		NastaveniChallenge.uloz(s, false, u);
		Assert.assertEquals("1000000000000001", u.get("verze"));
		final MapoveUloziste v = new MapoveUloziste();
		v.put("verze", "1000000000000001");
		NastaveniChallenge.uloz(s, false, v);
		Assert.assertEquals("1", v.get("verze"));
	}

	@Test
	public void ulozenyStavMaPresneKlicePoradiAktivnichAFormat() {
		final MapoveUloziste u = new MapoveUloziste();
		final ChallengeSeznam s = new ChallengeSeznam();
		s.ulozVlastni(vlastni("A", ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE));
		s.ulozVlastni(vlastni("B", ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE));
		s.aktivuj("B");
		s.aktivuj("A");
		NastaveniChallenge.uloz(s, true, u);
		Assert.assertEquals("B\nA", u.get("aktivni"));
		Assert.assertEquals("1", u.get("format"));
		Assert.assertEquals("true", u.get("zapnuto"));
		Assert.assertEquals("1", u.get("verze"));
	}

	@Test
	public void bezAktivnichSeUlozenySeznamAktivnichSmaze() {
		final MapoveUloziste u = new MapoveUloziste();
		final ChallengeSeznam s = new ChallengeSeznam();
		s.ulozVlastni(vlastni("A", ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE));
		s.aktivuj("A");
		NastaveniChallenge.uloz(s, true, u);
		Assert.assertNotNull(u.get("aktivni"));
		s.deaktivuj("A");
		NastaveniChallenge.uloz(s, false, u);
		Assert.assertNull(u.get("aktivni"));
		Assert.assertEquals("false", u.get("zapnuto"));
		Assert.assertTrue(NastaveniChallenge.nacti(u).seznam.aktivni().isEmpty());
	}

	@Test
	public void dveUlozeneDefiniceSeStejnymNazvemSeNactouJakoJednaAJednaSePreskoci() {
		final MapoveUloziste u = new MapoveUloziste();
		final ChallengeSeznam s = new ChallengeSeznam();
		s.ulozVlastni(vlastni("Dvojice", ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE));
		NastaveniChallenge.uloz(s, false, u);
		for (final String k : new java.util.ArrayList<>(u.m.keySet())) {
			if (k.startsWith("definice.1.0.")) {
				u.put("definice.1.1." + k.substring("definice.1.0.".length()), u.get(k));
			}
		}
		final NastaveniChallenge.Nacteno n = NastaveniChallenge.nacti(u);
		Assert.assertEquals(1, n.seznam.vlastni().size());
		Assert.assertEquals(1, n.preskoceno);
	}

	@Test
	public void prilisRozsahlaDefiniceSeNeulozi() {
		final MapoveUloziste u = new MapoveUloziste();
		final ChallengeSeznam s = new ChallengeSeznam();
		final String[] kody = new String[200];
		for (int i = 0; i < kody.length; i++) {
			kody[i] = "oblast-s-dlouhym-kodem-" + i;
		}
		try {
			s.ulozVlastni(new Challenge("Velká", Typ.SEKTORY_360, Stred.souradnice(50, 14), OblastDef.sada(kody), ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE));
		} catch (final ChallengeChyba e) {
			return; // odmítnuto už při uložení definice do seznamu
		}
		try {
			NastaveniChallenge.uloz(s, false, u);
			Assert.fail();
		} catch (final ChallengeChyba e) {
			Assert.assertTrue(e.getMessage(), e.getMessage().contains("rozsáhlá"));
			Assert.assertTrue("nic se nezapsalo", u.m.isEmpty());
		}
	}

	private static MapoveUloziste sDefinici(final String klic, final String hodnota) {
		final MapoveUloziste u = new MapoveUloziste();
		final ChallengeSeznam s = new ChallengeSeznam();
		s.ulozVlastni(vlastni("Dobrá", ZdrojPravidel.PGC, ZdrojeNalezu.ZAPNUTE));
		NastaveniChallenge.uloz(s, true, u);
		u.put("definice.1.7.nazev", "Budoucí");
		u.put("definice.1.7.typ", "SEKTORY_360");
		u.put("definice.1.7.zdroj", "PGC");
		u.put("definice.1.7.stred", "DOMOV");
		u.put("definice.1.7.oblast", "ZADNA");
		u.put("definice.1.7." + klic, hodnota);
		return u;
	}

	@Test
	public void definiceSNeznamouHodnotouVyctuNeniDoNastaveniPrepsatelna() {
		for (final String[] kv : new String[][] { { "typ", "BUDOUCI_TYP" }, { "zdroj", "BUDOUCI" }, { "stred", "BUDOUCI" }, { "oblast", "BUDOUCI" }, { "nalezy", "BUDOUCI" } }) {
			final NastaveniChallenge.Nacteno n = NastaveniChallenge.nacti(sDefinici(kv[0], kv[1]));
			Assert.assertTrue(kv[0], n.neznamyFormat);
			Assert.assertEquals(kv[0], 1, n.preskoceno);
			Assert.assertEquals("čitelná definice se načte", "Dobrá", n.seznam.vlastni().get(0).nazev());
		}
	}

	@Test
	public void obycejneChybnaDefiniceUkladaniNezakazuje() {
		for (final String[] kv : new String[][] { { "stred.lat", "abc" }, { "nazev", "" } }) {
			final MapoveUloziste u = sDefinici(kv[0], kv[1]);
			u.put("definice.1.7.stred", "SOURADNICE");
			final NastaveniChallenge.Nacteno n = NastaveniChallenge.nacti(u);
			Assert.assertFalse(kv[0], n.neznamyFormat);
			Assert.assertEquals(kv[0], 1, n.preskoceno);
		}
	}

	@Test
	public void kopieSablonyMaticeDTSeUpravenaUlozyANacte() {
		final MapoveUloziste u = new MapoveUloziste();
		final ChallengeSeznam s = new ChallengeSeznam();
		final Challenge kopie = s.kopieSablony("Matice D/T");
		final Challenge upravena = new Challenge(kopie.nazev(), kopie.typ(), Stred.souradnice(49.5, 15.25), OblastDef.zadna(), ZdrojPravidel.STATOR, ZdrojeNalezu.VSECHNY);
		s.ulozVlastni(upravena);
		s.aktivuj("Matice D/T");
		s.aktivuj(upravena.nazev());
		NastaveniChallenge.uloz(s, true, u);
		final NastaveniChallenge.Nacteno n = NastaveniChallenge.nacti(u);
		Assert.assertEquals(0, n.preskoceno);
		Assert.assertFalse(n.neznamyFormat);
		Assert.assertEquals(Arrays.asList(upravena), n.seznam.vlastni());
		Assert.assertEquals(Challenge.Typ.DT_MATICE, n.seznam.vlastni().get(0).typ());
		Assert.assertEquals(Arrays.asList(Sablony.maticeDT(), upravena), n.seznam.aktivni());
		Assert.assertTrue(n.zapnuto);
	}
}
