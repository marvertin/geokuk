package cz.geokuk.plugins.challenge;

import java.time.Instant;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class VypocetKalendareTest {

	private static final LocalDate DNES = LocalDate.of(2026, 11, 5);

	private static final class K implements VstupKalendar {
		String id = "GC1";
		LocalDate nalez;
		LocalDate datum;
		boolean event;
		boolean nalezena;
		boolean vlastni;
		boolean neaktivni;
		boolean lab;
		boolean locationless;
		StavLogu log = StavLogu.NEZNAMY;
		double lat = 50;

		K misto(final double nova) {
			lat = nova;
			return this;
		}

		static K nalez(final LocalDate d) {
			final K k = new K();
			k.nalezena = true;
			k.nalez = d;
			return k;
		}

		static K event(final String id, final LocalDate d) {
			final K k = new K();
			k.id = id;
			k.event = true;
			k.datum = d;
			return k;
		}

		static K keska(final String id, final LocalDate d) {
			final K k = new K();
			k.id = id;
			k.datum = d;
			return k;
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
			return 14;
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
		public StavLogu logNaGc() {
			return log;
		}

		@Override
		public LocalDate datumNalezu() {
			return nalez;
		}

		@Override
		public LocalDate datum() {
			return datum;
		}

		@Override
		public boolean event() {
			return event;
		}
	}

	private static VysledekPolicek spocti(final PravidlaKalendare p, final K... k) {
		return VypocetKalendare.spocti(p, DNES, 50, 14, Arrays.asList(k));
	}

	private static VysledekPolicek spocti(final K... k) {
		return spocti(PravidlaKalendare.vychozi(), k);
	}

	@Test
	public void bezNalezuChybi365ARazenoOdDnesDoPristihoRoku() {
		final VysledekPolicek v = spocti();
		Assert.assertEquals(365, v.celkem());
		Assert.assertEquals(365, v.pocetChybejicich());
		Assert.assertEquals("5. 11. (dnes)", v.stitkyChybejicich().get(0));
		Assert.assertEquals("6. 11. (za 1 den)", v.stitkyChybejicich().get(1));
		Assert.assertEquals("4. 11. (za 364 dní)", v.stitkyChybejicich().get(364));
	}

	@Test
	public void stitekPocitaDnyAPrechodRoku() {
		Assert.assertEquals("14. 3. (za 131 dní)", VypocetKalendare.stitek(MonthDay.of(3, 14), 131));
		Assert.assertEquals(129, VypocetKalendare.dniDoDalsiho(MonthDay.of(3, 14), DNES));
	}

	@Test
	public void dniDoPrelomRoku() {
		Assert.assertEquals(1, VypocetKalendare.dniDoDalsiho(MonthDay.of(1, 1), LocalDate.of(2026, 12, 31)));
		Assert.assertEquals(0, VypocetKalendare.dniDoDalsiho(MonthDay.of(12, 31), LocalDate.of(2026, 12, 31)));
		Assert.assertEquals(364, VypocetKalendare.dniDoDalsiho(MonthDay.of(12, 30), LocalDate.of(2026, 12, 31)));
	}

	@Test
	public void sklonovaniDnu() {
		Assert.assertEquals("1. 1. (dnes)", VypocetKalendare.stitek(MonthDay.of(1, 1), 0));
		Assert.assertEquals("1. 1. (za 1 den)", VypocetKalendare.stitek(MonthDay.of(1, 1), 1));
		Assert.assertEquals("1. 1. (za 2 dny)", VypocetKalendare.stitek(MonthDay.of(1, 1), 2));
		Assert.assertEquals("1. 1. (za 4 dny)", VypocetKalendare.stitek(MonthDay.of(1, 1), 4));
		Assert.assertEquals("1. 1. (za 5 dní)", VypocetKalendare.stitek(MonthDay.of(1, 1), 5));
	}

	@Test
	public void nalezVyplniDenVLibovolnemRoce() {
		final VysledekPolicek v = spocti(K.nalez(LocalDate.of(2019, 3, 14)), K.nalez(LocalDate.of(2024, 3, 14)));
		Assert.assertEquals(364, v.pocetChybejicich());
		for (final String s : v.stitkyChybejicich()) {
			Assert.assertFalse(s, s.startsWith("14. 3."));
		}
	}

	@Test
	public void prestupnyDenSeVychozePozadovatNebo() {
		final K prestupny = K.nalez(LocalDate.of(2024, 2, 29));
		Assert.assertEquals("29. 2. nevyplňuje žádný z 365 dní", 365, spocti(prestupny).pocetChybejicich());
		final PravidlaKalendare p = PravidlaKalendare.vychozi().sPrestupnym(true);
		Assert.assertEquals(366, spocti(p).celkem());
		Assert.assertEquals(366, spocti(p).pocetChybejicich());
		Assert.assertEquals(365, spocti(p, prestupny).pocetChybejicich());
		final List<String> stitky = spocti(p).stitkyChybejicich();
		Assert.assertEquals("29. 2. (za 481 dní)", stitky.get(stitky.size() - 1));
	}

	@Test
	public void prestupnyDenPoctyDni() {
		Assert.assertEquals(481, VypocetKalendare.dniDoDalsiho(MonthDay.of(2, 29), LocalDate.of(2026, 11, 5)));
		Assert.assertEquals(0, VypocetKalendare.dniDoDalsiho(MonthDay.of(2, 29), LocalDate.of(2028, 2, 29)));
		Assert.assertEquals(1, VypocetKalendare.dniDoDalsiho(MonthDay.of(2, 29), LocalDate.of(2028, 2, 28)));
		Assert.assertEquals(java.time.temporal.ChronoUnit.DAYS.between(LocalDate.of(2096, 3, 1), LocalDate.of(2104, 2, 29)),
				VypocetKalendare.dniDoDalsiho(MonthDay.of(2, 29), LocalDate.of(2096, 3, 1)));
	}

	@Test
	public void nalezBezDataNeboBezLoguSeNepocita() {
		final K bezData = K.nalez(null);
		final K ne = K.nalez(LocalDate.of(2020, 5, 5));
		ne.log = VstupKes.StavLogu.NE;
		Assert.assertEquals(365, spocti(bezData, ne).pocetChybejicich());
	}

	@Test
	public void labALocationlessSePodlePravidelNepocita() {
		final K lab = K.nalez(LocalDate.of(2020, 5, 5));
		lab.lab = true;
		final K loc = K.nalez(LocalDate.of(2020, 5, 6));
		loc.locationless = true;
		Assert.assertEquals(365, spocti(lab, loc).pocetChybejicich());
		Assert.assertEquals(364, spocti(PravidlaKalendare.vychozi().sTypy(PravidlaTypu.proKalendar().sLab(true)), lab, loc).pocetChybejicich());
		Assert.assertEquals(364, spocti(PravidlaKalendare.vychozi().sTypy(PravidlaTypu.proKalendar().sLocationless(true)), lab, loc).pocetChybejicich());
	}

	@Test
	public void kandidatEventVChybejicimDniAJenBudouci() {
		final VysledekPolicek v = spocti(K.event("GCE1", LocalDate.of(2026, 12, 24)), K.event("GCE2", LocalDate.of(2026, 11, 4)), K.event("GCE3", LocalDate.of(2026, 11, 5)),
				K.event("GCE4", LocalDate.of(2027, 3, 14)));
		Assert.assertEquals(3, v.kandidati().size());
		Assert.assertEquals("GCE3", v.kandidati().get(0).id());
		Assert.assertEquals(Integer.valueOf(0), v.kandidati().get(0).dniDo());
		Assert.assertEquals("GCE1", v.kandidati().get(1).id());
		Assert.assertEquals(Integer.valueOf(49), v.kandidati().get(1).dniDo());
		Assert.assertEquals("24. 12. (za 49 dní)", v.kandidati().get(1).stitek());
		Assert.assertEquals("GCE4", v.kandidati().get(2).id());
		Assert.assertEquals(Integer.valueOf(129), v.kandidati().get(2).dniDo());
	}

	@Test
	public void eventVNalezenemDniNeniKandidat() {
		final VysledekPolicek v = spocti(K.nalez(LocalDate.of(2020, 12, 24)), K.event("GCE1", LocalDate.of(2026, 12, 24)));
		Assert.assertTrue(v.kandidati().isEmpty());
	}

	@Test
	public void eventNa29UnoraJeKandidatJenKdyzSePozaduje() {
		final K e = K.event("GCE", LocalDate.of(2028, 2, 29));
		Assert.assertTrue(spocti(e).kandidati().isEmpty());
		final VysledekPolicek v = spocti(PravidlaKalendare.vychozi().sPrestupnym(true), e);
		Assert.assertEquals(1, v.kandidati().size());
		Assert.assertEquals(Integer.valueOf(481), v.kandidati().get(0).dniDo());
	}

	@Test
	public void bezDataNebezHledatelnostiNeniKandidat() {
		final K bezData = K.event("A", null);
		final K vlastni = K.event("B", LocalDate.of(2026, 12, 24));
		vlastni.vlastni = true;
		final K neaktivni = K.event("C", LocalDate.of(2026, 12, 24));
		neaktivni.neaktivni = true;
		final K nalezeny = K.event("D", LocalDate.of(2026, 12, 24));
		nalezeny.nalezena = true;
		Assert.assertTrue(spocti(bezData, vlastni, neaktivni, nalezeny).kandidati().isEmpty());
	}

	@Test
	public void obycejnaKesJeKandidatJenPodleNastaveni() {
		final K keska = K.keska("GCK", LocalDate.of(2010, 12, 24));
		Assert.assertTrue(spocti(keska).kandidati().isEmpty());
		final VysledekPolicek v = spocti(PravidlaKalendare.vychozi().sKandidaty(PravidlaKalendare.Kandidati.EVENTY_A_VYROCI), keska);
		Assert.assertEquals(1, v.kandidati().size());
		Assert.assertEquals(Integer.valueOf(49), v.kandidati().get(0).dniDo());
		Assert.assertEquals("24. 12. (za 49 dní)", v.kandidati().get(0).stitek());
	}

	@Test
	public void minulyEventSeZVyrociNepocitaAniPriRozsirenem() {
		final K e = K.event("GCE", LocalDate.of(2026, 11, 4));
		final VysledekPolicek v = spocti(PravidlaKalendare.vychozi().sKandidaty(PravidlaKalendare.Kandidati.EVENTY_A_VYROCI), e);
		Assert.assertTrue(v.kandidati().isEmpty());
	}

	@Test
	public void lokalniDatumPodlePasma() {
		final Instant i = Instant.parse("2026-03-14T23:30:00Z");
		Assert.assertEquals(LocalDate.of(2026, 3, 15), VypocetKalendare.lokalniDatum(i, ZoneId.of("Europe/Prague")));
		Assert.assertEquals(LocalDate.of(2026, 3, 14), VypocetKalendare.lokalniDatum(i, ZoneId.of("UTC")));
		Assert.assertEquals(LocalDate.of(2026, 3, 14), VypocetKalendare.lokalniDatum(i, ZoneId.of("America/New_York")));
		Assert.assertEquals("letní čas, přelom roku", LocalDate.of(2027, 1, 1), VypocetKalendare.lokalniDatum(Instant.parse("2026-12-31T23:00:00Z"), ZoneId.of("Europe/Prague")));
	}

	@Test
	public void dalsiPodleCasuPakVzdalenosti() {
		final List<K> kese = new ArrayList<>();
		kese.add(K.event("POZDEJI", LocalDate.of(2027, 1, 10)));
		kese.add(K.event("DRIVE", LocalDate.of(2026, 12, 1)));
		final VysledekPolicek v = VypocetKalendare.spocti(PravidlaKalendare.vychozi(), DNES, 50, 14, kese);
		final DalsiPolicko d = new DalsiPolicko();
		Assert.assertEquals("DRIVE", d.dalsi(v, 50, 14).id());
		Assert.assertEquals("POZDEJI", d.dalsi(v, 50, 14).id());
		Assert.assertEquals("kolo začne znovu", "DRIVE", d.dalsi(v, 50, 14).id());
	}

	@Test
	public void vzdalenyEventMaPopisSeSkutecnymPoctemDniAStitekDne() {
		final VysledekPolicek v = spocti(K.event("GCE", LocalDate.of(2028, 3, 14)));
		final KandidatPolicka k = v.kandidati().get(0);
		Assert.assertEquals("14. 3. (za 129 dní)", k.stitek());
		Assert.assertTrue(v.stitkyChybejicich().contains(k.stitek()));
		Assert.assertEquals(Integer.valueOf(495), k.dniDo());
		Assert.assertEquals("14. 3. (za 495 dní)", k.popis());
	}

	@Test
	public void kandidatuJeNejvicNaDenAVybiraSeNejblizsiDatum() {
		final PravidlaKalendare p = PravidlaKalendare.vychozi().sTypy(PravidlaTypu.proKalendar().sMaxKandidatu(2));
		final VysledekPolicek v = spocti(p, K.event("E2028", LocalDate.of(2028, 12, 24)), K.event("E2026", LocalDate.of(2026, 12, 24)), K.event("E2030", LocalDate.of(2030, 12, 24)),
				K.event("E2027", LocalDate.of(2027, 12, 24)), K.event("JINY", LocalDate.of(2026, 12, 25)));
		final List<String> id = new ArrayList<>();
		for (final KandidatPolicka k : v.kandidati()) {
			id.add(k.id());
		}
		Assert.assertEquals(Arrays.asList("E2026", "E2027", "JINY"), id);
	}

	@Test
	public void vyrociMaStejnyPopisJakoStitek() {
		final VysledekPolicek v = spocti(PravidlaKalendare.vychozi().sKandidaty(PravidlaKalendare.Kandidati.EVENTY_A_VYROCI), K.keska("GCK", LocalDate.of(2010, 12, 24)));
		Assert.assertEquals(v.kandidati().get(0).stitek(), v.kandidati().get(0).popis());
	}

	@Test
	public void kandidatSNeplatnymiSouradnicemiSeNenabizi() {
		final PravidlaKalendare p = PravidlaKalendare.vychozi().sKandidaty(PravidlaKalendare.Kandidati.EVENTY_A_VYROCI);
		for (final double lat : new double[] { Double.NaN, Double.POSITIVE_INFINITY, 95, -91 }) {
			Assert.assertTrue(String.valueOf(lat), spocti(p, K.event("E", LocalDate.of(2026, 12, 24)).misto(lat), K.keska("K", LocalDate.of(2010, 12, 24)).misto(lat)).kandidati().isEmpty());
		}
		Assert.assertEquals(1, spocti(p, K.event("E", LocalDate.of(2026, 12, 24)).misto(50)).kandidati().size());
	}

	@Test(expected = NullPointerException.class)
	public void nullPravidla() {
		VypocetKalendare.spocti(null, DNES, 50, 14, new ArrayList<K>());
	}

	@Test(expected = NullPointerException.class)
	public void nullDnes() {
		VypocetKalendare.spocti(PravidlaKalendare.vychozi(), null, 50, 14, new ArrayList<K>());
	}

	@Test(expected = NullPointerException.class)
	public void nullKese() {
		VypocetKalendare.spocti(PravidlaKalendare.vychozi(), DNES, 50, 14, null);
	}

	@Test(expected = NullPointerException.class)
	public void nullPrvekKesi() {
		VypocetKalendare.spocti(PravidlaKalendare.vychozi(), DNES, 50, 14, Arrays.asList(K.nalez(LocalDate.of(2020, 1, 1)), null));
	}

	private static List<String> idKandidatu(final VysledekPolicek v) {
		final List<String> id = new ArrayList<>();
		for (final KandidatPolicka k : v.kandidati()) {
			id.add(k.id());
		}
		return id;
	}

	@Test
	public void kandidatiDneSerazeniPodleDataPakId() {
		final VysledekPolicek v = spocti(K.event("Z", LocalDate.of(2026, 12, 24)), K.event("B", LocalDate.of(2027, 12, 24)), K.event("A", LocalDate.of(2027, 12, 24)));
		Assert.assertEquals(Arrays.asList("Z", "A", "B"), idKandidatu(v));
	}

	@Test
	public void priOrezuSeNejblizsiDatumDrziBezZreteleNaPoradiVstupu() {
		final PravidlaKalendare p = PravidlaKalendare.vychozi().sTypy(PravidlaTypu.proKalendar().sMaxKandidatu(2));
		final VysledekPolicek v = spocti(p, K.event("E2028", LocalDate.of(2028, 12, 24)), K.event("E2030", LocalDate.of(2030, 12, 24)), K.event("E2029", LocalDate.of(2029, 12, 24)),
				K.event("E2027", LocalDate.of(2027, 12, 24)));
		Assert.assertEquals(Arrays.asList("E2027", "E2028"), idKandidatu(v));
	}

	@Test
	public void priUplneShodePriOrezuRozhodujeKodKese() {
		final PravidlaKalendare p = PravidlaKalendare.vychozi().sTypy(PravidlaTypu.proKalendar().sMaxKandidatu(1));
		Assert.assertEquals(Arrays.asList("A"), idKandidatu(spocti(p, K.event("B", LocalDate.of(2026, 12, 24)), K.event("A", LocalDate.of(2026, 12, 24)))));
		Assert.assertEquals(Arrays.asList("A"), idKandidatu(spocti(p, K.event("A", LocalDate.of(2026, 12, 24)), K.event("B", LocalDate.of(2026, 12, 24)))));
	}

	@Test
	public void kandidatiSeStejnymDatemSeOrezouPodleVzdalenostiOdBodu() {
		final List<K> kese = new ArrayList<>();
		for (int i = 0; i < 200_000; i++) {
			kese.add(K.event("GC" + i, LocalDate.of(2026, 12, 24)).misto(50 + i * 0.00005));
		}
		final double lat = 50 + 199_999 * 0.00005;
		final PravidlaKalendare p = PravidlaKalendare.vychozi().sTypy(PravidlaTypu.proKalendar().sMaxKandidatu(3));
		final VysledekPolicek v = VypocetKalendare.spocti(p, DNES, lat, 14, kese);
		Assert.assertEquals(Arrays.asList("GC199999", "GC199998", "GC199997"), idKandidatu(v));
		Assert.assertEquals("GC199999", new DalsiPolicko().dalsi(v, lat, 14).id());
	}

	@Test
	public void vyrociSeOrezePodleVzdalenostiOdBodu() {
		final PravidlaKalendare p = PravidlaKalendare.vychozi().sKandidaty(PravidlaKalendare.Kandidati.EVENTY_A_VYROCI).sTypy(PravidlaTypu.proKalendar().sMaxKandidatu(1));
		final VysledekPolicek v = VypocetKalendare.spocti(p, DNES, 51, 14,
				Arrays.asList(K.keska("DALEKO", LocalDate.of(2010, 12, 24)).misto(55), K.keska("BLIZKO", LocalDate.of(2011, 12, 24)).misto(51.01)));
		Assert.assertEquals(Arrays.asList("BLIZKO"), idKandidatu(v));
	}

	@Test
	public void datumMaPrednostPredVzdalenosti() {
		final PravidlaKalendare p = PravidlaKalendare.vychozi().sTypy(PravidlaTypu.proKalendar().sMaxKandidatu(1));
		final VysledekPolicek v = VypocetKalendare.spocti(p, DNES, 50, 14,
				Arrays.asList(K.event("POZDEJI_BLIZKO", LocalDate.of(2027, 12, 24)).misto(50), K.event("DRIVE_DALEKO", LocalDate.of(2026, 12, 24)).misto(55)));
		Assert.assertEquals(Arrays.asList("DRIVE_DALEKO"), idKandidatu(v));
	}

	@Test
	public void referencniBodMimoRozsahJeChyba() {
		for (final double[] bod : new double[][] { { 90.1, 0 }, { -90.1, 0 }, { 0, 180.1 }, { 0, -180.1 }, { Double.NaN, 0 }, { 0, Double.NaN } }) {
			try {
				VypocetKalendare.spocti(PravidlaKalendare.vychozi(), DNES, bod[0], bod[1], new ArrayList<K>());
				Assert.fail(bod[0] + ", " + bod[1]);
			} catch (final IllegalArgumentException e) {
				// očekáváno
			}
		}
		VypocetKalendare.spocti(PravidlaKalendare.vychozi(), DNES, 90, 180, new ArrayList<K>());
		VypocetKalendare.spocti(PravidlaKalendare.vychozi(), DNES, -90, -180, new ArrayList<K>());
	}
}
