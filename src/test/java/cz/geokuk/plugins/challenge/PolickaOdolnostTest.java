package cz.geokuk.plugins.challenge;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.plugins.kesoid.EKesVztah;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.kind.kes.Kes;

/** D/T matice, kalendář, čtení dat a výběr dalšího políčka proti neobvyklým, prázdným a extrémním vstupům a pořadí vstupu. */
public class PolickaOdolnostTest {

	/** Keš pro matici i kalendář s libovolnými příznaky. */
	private static final class V implements VstupDT, VstupKalendar {
		final String id;
		double lat = 50.1;
		double lon = 14.1;
		boolean nalezena;
		boolean lab;
		boolean locationless;
		boolean vlastni;
		boolean neaktivni;
		boolean mimo;
		StavLogu log = StavLogu.NEZNAMY;
		int d = 2;
		int t = 2;
		LocalDate datumNalezu;
		LocalDate datum;
		boolean event;

		V(final String id) {
			this.id = id;
		}

		V poloha(final double la, final double lo) {
			lat = la;
			lon = lo;
			return this;
		}

		V dt(final int obt, final int ter) {
			d = obt;
			t = ter;
			return this;
		}

		V nalez(final LocalDate den) {
			nalezena = true;
			datumNalezu = den;
			return this;
		}

		V eventDne(final LocalDate den) {
			event = true;
			datum = den;
			return this;
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
		public boolean mimoKandidaty() {
			return mimo;
		}

		@Override
		public StavLogu logNaGc() {
			return log;
		}

		@Override
		public int obtiznost() {
			return d;
		}

		@Override
		public int teren() {
			return t;
		}

		@Override
		public LocalDate datumNalezu() {
			return datumNalezu;
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

	private static List<String> idKandidatu(final VysledekPolicek v) {
		final List<String> id = new ArrayList<>();
		for (final KandidatPolicka k : v.kandidati()) {
			id.add(k.id());
		}
		return id;
	}

	private static VysledekPolicek matice(final List<V> kese) {
		return VypocetMatice.spocti(PravidlaTypu.proMatici(), 50, 14, kese);
	}

	private static VysledekPolicek kalendar(final LocalDate dnes, final List<V> kese) {
		return VypocetKalendare.spocti(PravidlaKalendare.vychozi(), dnes, 50, 14, kese);
	}

	// ---- DatumyKese

	@Test
	public void datumyZeZdrojuSNesmyslnymiHodnotamiDajiNullAneVyjimku() {
		final String[] spatne = { null, "", "   ", "abc", "2026", "2026-1-5", "2026-02-30", "2025-02-29", "2026-13-01", "2026-00-10", "2026-04-31", "20261301", "20260230", "00000000", "99999999",
				"２０２６-11-05", "-2026-11-05", "+2026-11-05", "2026/11/05", "05.11.2026", "2026-11", "T2026-11-05", "20261105x" };
		for (final String s : spatne) {
			for (final ZoneId z : new ZoneId[] { null, ZoneId.of("UTC"), ZoneId.of("Pacific/Kiritimati") }) {
				final LocalDate d = DatumyKese.datum(s, z);
				// Osmimístné číslo s příponou je zápis GeoGetu, jinak se čte jen platné datum.
				if ("20261105x".equals(s)) {
					Assert.assertNull(String.valueOf(s), d);
				} else {
					Assert.assertNull(s + " " + z, d);
				}
			}
		}
	}

	@Test
	public void platnaDataVeVsechZapisechSeCtou() {
		Assert.assertEquals(LocalDate.of(2024, 2, 29), DatumyKese.datum("2024-02-29", null));
		Assert.assertEquals(LocalDate.of(2024, 2, 29), DatumyKese.datum("20240229", null));
		Assert.assertEquals(LocalDate.of(2024, 2, 29), DatumyKese.datum("20240229123456", null));
		Assert.assertEquals(LocalDate.of(2024, 2, 29), DatumyKese.datum("  2024-02-29  ", ZoneId.of("UTC")));
		Assert.assertEquals(LocalDate.of(2024, 2, 29), DatumyKese.datum("2024-02-29T10:15", ZoneId.of("Asia/Tokyo")));
		Assert.assertEquals(LocalDate.of(2026, 11, 6), DatumyKese.datum("2026-11-05T23:30:00-05:00", ZoneId.of("Europe/Prague")));
		Assert.assertEquals(LocalDate.of(2026, 11, 5), DatumyKese.datum("2026-11-05T23:30:00-05:00", null));
		Assert.assertEquals(LocalDate.of(2026, 11, 5), DatumyKese.datum("2026-11-05T23:30:00+14:00", ZoneId.of("Pacific/Kiritimati")));
		Assert.assertEquals(LocalDate.of(2026, 11, 6), DatumyKese.datum("2026-11-05T23:30:00.123456789+00:00", ZoneId.of("Pacific/Kiritimati")));
	}

	@Test
	public void neplatnyCasSeZonouDaNullAbezPasmaSeVezmeJenDatum() {
		Assert.assertNull(DatumyKese.datum("2026-11-05T25:00:00Z", ZoneId.of("UTC")));
		Assert.assertNull(DatumyKese.datum("2026-11-05T12:61:00+01:00", ZoneId.of("UTC")));
		Assert.assertEquals(LocalDate.of(2026, 11, 5), DatumyKese.datum("2026-11-05T25:00:00Z", null));
	}

	@Test
	public void velmiDlouhyNesmyslneZnakyAHranicniRokySeNezhrouti() {
		final StringBuilder dlouhy = new StringBuilder("2026-11-05");
		for (int i = 0; i < 100_000; i++) {
			dlouhy.append('x');
		}
		Assert.assertEquals(LocalDate.of(2026, 11, 5), DatumyKese.datum(dlouhy.toString(), ZoneId.of("UTC")));
		Assert.assertEquals(LocalDate.of(1, 1, 1), DatumyKese.datum("0001-01-01", null));
		Assert.assertEquals(LocalDate.of(9999, 12, 31), DatumyKese.datum("9999-12-31", null));
		Assert.assertNull(DatumyKese.datum("0000-00-00", null));
		final StringBuilder cisla = new StringBuilder();
		for (int i = 0; i < 100_000; i++) {
			cisla.append('7');
		}
		Assert.assertNull(DatumyKese.datum(cisla.toString(), null));
	}

	// ---- Matice

	@Test
	public void nezname_zaporneAPresahujiciObtiznostAterenSePreskoci() {
		final List<V> kese = new ArrayList<>();
		final int[] spatne = { -1, 9, 10, Integer.MIN_VALUE, Integer.MAX_VALUE };
		for (final int s : spatne) {
			kese.add(new V("A" + s).dt(s, 2).nalez(null));
			kese.add(new V("B" + s).dt(2, s).nalez(null));
			kese.add(new V("C" + s).dt(s, s));
		}
		final VysledekPolicek v = matice(kese);
		Assert.assertEquals(81, v.celkem());
		Assert.assertEquals("nic se nezapočítá ani nenabídne", 81, v.pocetChybejicich());
		Assert.assertTrue(v.kandidati().isEmpty());
	}

	@Test
	public void kesNaMezichRozsahuDTSePocitaVNajednomPolicku() {
		final VysledekPolicek v = matice(Arrays.asList(new V("N1").dt(0, 0).nalez(null), new V("N2").dt(8, 8).nalez(null), new V("N3").dt(0, 8).nalez(null), new V("N4").dt(8, 0).nalez(null)));
		Assert.assertEquals(77, v.pocetChybejicich());
		Assert.assertFalse(v.stitkyChybejicich().contains("D1/T1"));
		Assert.assertFalse(v.stitkyChybejicich().contains("D5/T5"));
		Assert.assertFalse(v.stitkyChybejicich().contains("D1/T5"));
		Assert.assertFalse(v.stitkyChybejicich().contains("D5/T1"));
	}

	@Test
	public void pocetNalezuVJednomPolickuNicNemeni() {
		final List<V> kese = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			kese.add(new V("N" + i).dt(3, 4).nalez(null));
		}
		Assert.assertEquals(80, matice(kese).pocetChybejicich());
	}

	@Test
	public void nalezSLogemNeNaGcComSeNepocitaALabALocationlessPodlePravidel() {
		final V ne = new V("NE").dt(1, 1).nalez(null);
		ne.log = VstupKes.StavLogu.NE;
		final V lab = new V("LAB").dt(2, 2).nalez(null);
		lab.lab = true;
		final V loc = new V("LOC").dt(3, 3).nalez(null);
		loc.locationless = true;
		final List<V> kese = Arrays.asList(ne, lab, loc);
		Assert.assertEquals("jen Locationless se počítá", 80, matice(kese).pocetChybejicich());
		Assert.assertEquals(79, VypocetMatice.spocti(PravidlaTypu.proMatici().sLab(true), 50, 14, kese).pocetChybejicich());
		Assert.assertEquals(81, VypocetMatice.spocti(PravidlaTypu.proMatici().sLocationless(false), 50, 14, kese).pocetChybejicich());
	}

	@Test
	public void kandidatiSeNebereVlastniNeaktivniAMimoKandidaty() {
		final V vlastni = new V("A");
		vlastni.vlastni = true;
		final V neaktivni = new V("B");
		neaktivni.neaktivni = true;
		final V mimo = new V("C");
		mimo.mimo = true;
		final V dobry = new V("D");
		Assert.assertEquals(Collections.singletonList("D"), idKandidatu(matice(Arrays.asList(vlastni, neaktivni, mimo, dobry))));
	}

	@Test
	public void vysledekMaticeNezaleziNaPoradiKesiAVyberKandidatuJeUstaleny() {
		final Random r = new Random(9);
		final List<V> kese = new ArrayList<>();
		for (int i = 0; i < 3000; i++) {
			final V k = new V("GC" + i).poloha(49 + r.nextDouble() * 2, 13 + r.nextDouble() * 2).dt(r.nextInt(9), r.nextInt(9));
			k.nalezena = r.nextInt(3) == 0;
			kese.add(k);
		}
		final VysledekPolicek zaklad = VypocetMatice.spocti(PravidlaTypu.proMatici().sMaxKandidatu(3), 50, 14, kese);
		for (int pokus = 0; pokus < 5; pokus++) {
			final List<V> zamichane = new ArrayList<>(kese);
			Collections.shuffle(zamichane, new Random(pokus));
			final VysledekPolicek v = VypocetMatice.spocti(PravidlaTypu.proMatici().sMaxKandidatu(3), 50, 14, zamichane);
			Assert.assertEquals(zaklad.stitkyChybejicich(), v.stitkyChybejicich());
			Assert.assertEquals(idKandidatu(zaklad), idKandidatu(v));
		}
		Assert.assertTrue("na každé políčko nejvýš tři", zaklad.kandidati().size() <= 3 * 81);
	}

	@Test
	public void maticeOdmitneNullyANeplatnyBod() {
		final List<V> kese = Collections.singletonList(new V("A"));
		try {
			VypocetMatice.spocti(null, 50, 14, kese);
			Assert.fail();
		} catch (final NullPointerException e) {
			Assert.assertEquals("pravidla", e.getMessage());
		}
		try {
			VypocetMatice.spocti(PravidlaTypu.proMatici(), 50, 14, null);
			Assert.fail();
		} catch (final NullPointerException e) {
			Assert.assertEquals("kese", e.getMessage());
		}
		try {
			VypocetMatice.spocti(PravidlaTypu.proMatici(), 50, 14, Arrays.asList(new V("A"), null));
			Assert.fail();
		} catch (final NullPointerException e) {
			Assert.assertEquals("keš", e.getMessage());
		}
		for (final double[] zly : new double[][] { { Double.NaN, 14 }, { 50, Double.NaN }, { 91, 14 }, { 50, -181 }, { Double.POSITIVE_INFINITY, 14 } }) {
			try {
				VypocetMatice.spocti(PravidlaTypu.proMatici(), zly[0], zly[1], kese);
				Assert.fail(Arrays.toString(zly));
			} catch (final IllegalArgumentException e) {
				// očekávané
			}
		}
		try {
			PravidlaTypu.proMatici().sMaxKandidatu(0);
			Assert.fail();
		} catch (final IllegalArgumentException e) {
			// očekávané
		}
	}

	@Test
	public void prazdnyVyctesDaVsechnaPolickaBezKandidatu() {
		final VysledekPolicek m = matice(Collections.<V> emptyList());
		Assert.assertEquals(81, m.pocetChybejicich());
		Assert.assertEquals("D1/T1", m.stitkyChybejicich().get(0));
		Assert.assertEquals("D5/T5", m.stitkyChybejicich().get(80));
		Assert.assertTrue(m.kandidati().isEmpty());
	}

	@Test(timeout = 30_000)
	public void maticeSeSta_tisiciKesiSeSpocitaRychle() {
		final Random r = new Random(1);
		final List<V> kese = new ArrayList<>();
		for (int i = 0; i < 300_000; i++) {
			final V k = new V("GC" + i).poloha(r.nextDouble() * 160 - 80, r.nextDouble() * 340 - 170).dt(r.nextInt(9), r.nextInt(9));
			k.nalezena = i % 2 == 0;
			kese.add(k);
		}
		final VysledekPolicek v = matice(kese);
		Assert.assertEquals(0, v.pocetChybejicich());
	}

	// ---- Kalendář

	@Test
	public void kalendarSDnesnimDnemNaPrelomuRokuAVPrestupnemRoce() {
		for (final LocalDate dnes : new LocalDate[] { LocalDate.of(2026, 12, 31), LocalDate.of(2027, 1, 1), LocalDate.of(2028, 2, 29), LocalDate.of(2027, 2, 28), LocalDate.of(2096, 3, 1) }) {
			final VysledekPolicek v = kalendar(dnes, Collections.<V> emptyList());
			Assert.assertEquals(dnes.toString(), 365, v.pocetChybejicich());
			Assert.assertEquals(dnes.toString(), 365, v.celkem());
			if (dnes.getMonthValue() != 2 || dnes.getDayOfMonth() != 29) {
				Assert.assertEquals(dnes.getDayOfMonth() + ". " + dnes.getMonthValue() + ". (dnes)", v.stitkyChybejicich().get(0));
			} else {
				Assert.assertEquals("29. 2. se bez požadavku na přestupný rok nepočítá", "1. 3. (za 1 den)", v.stitkyChybejicich().get(0));
			}
			final VysledekPolicek prestupny = VypocetKalendare.spocti(PravidlaKalendare.vychozi().sPrestupnym(true), dnes, 50, 14, Collections.<V> emptyList());
			Assert.assertEquals(366, prestupny.pocetChybejicich());
			Assert.assertEquals(366, prestupny.celkem());
			final int dniDo = VypocetKalendare.dniDoDalsiho(java.time.MonthDay.of(2, 29), dnes);
			Assert.assertTrue(dnes + " " + dniDo, prestupny.stitkyChybejicich().contains(VypocetKalendare.stitek(java.time.MonthDay.of(2, 29), dniDo)));
			Assert.assertTrue("dny do 29. 2.: " + dniDo, dniDo >= 0 && dniDo <= 8 * 366);
		}
	}

	@Test
	public void vsechnyDnyNalezeneDajiPrazdnyKalendarAPrestupnyJenSVyzadanymLeapDnem() {
		final List<V> kese = new ArrayList<>();
		for (LocalDate d = LocalDate.of(2024, 1, 1); d.getYear() == 2024; d = d.plusDays(1)) {
			kese.add(new V("N" + d).nalez(d));
		}
		Assert.assertEquals(0, kalendar(LocalDate.of(2026, 11, 5), kese).pocetChybejicich());
		Assert.assertEquals(0, VypocetKalendare.spocti(PravidlaKalendare.vychozi().sPrestupnym(true), LocalDate.of(2026, 11, 5), 50, 14, kese).pocetChybejicich());
		kese.removeIf(k -> k.datumNalezu.getMonthValue() == 2 && k.datumNalezu.getDayOfMonth() == 29);
		Assert.assertEquals(0, kalendar(LocalDate.of(2026, 11, 5), kese).pocetChybejicich());
		Assert.assertEquals(1, VypocetKalendare.spocti(PravidlaKalendare.vychozi().sPrestupnym(true), LocalDate.of(2026, 11, 5), 50, 14, kese).pocetChybejicich());
	}

	@Test
	public void nalezBezDataALogNeNaGcComSeVKalendariNepocita() {
		final V bezData = new V("A").nalez(null);
		final V ne = new V("B").nalez(LocalDate.of(2025, 5, 5));
		ne.log = VstupKes.StavLogu.NE;
		Assert.assertEquals(365, kalendar(LocalDate.of(2026, 11, 5), Arrays.asList(bezData, ne)).pocetChybejicich());
	}

	@Test
	public void eventDnesJeKandidatSNulouDniEventVMinulostiNeAEventSVyssimRokemPocitaDni() {
		final LocalDate dnes = LocalDate.of(2026, 11, 5);
		final V dnesni = new V("DNES").eventDne(dnes);
		final V vcera = new V("VCERA").eventDne(dnes.minusDays(1));
		final V pristiRok = new V("ROK").eventDne(dnes.plusYears(1));
		final V dalekaBudoucnost = new V("DALEKO").eventDne(LocalDate.of(9999, 12, 31));
		final V bezData = new V("BEZ");
		bezData.event = true;
		final VysledekPolicek v = kalendar(dnes, Arrays.asList(dnesni, vcera, pristiRok, dalekaBudoucnost, bezData));
		final List<String> id = idKandidatu(v);
		Assert.assertTrue(id.toString(), id.contains("DNES") && id.contains("ROK") && id.contains("DALEKO"));
		Assert.assertFalse(id.toString(), id.contains("VCERA") || id.contains("BEZ"));
		for (final KandidatPolicka k : v.kandidati()) {
			Assert.assertNotNull(k.dniDo());
			Assert.assertTrue(k.id() + " " + k.dniDo(), k.dniDo() >= 0);
		}
		Assert.assertEquals(Integer.valueOf(0), v.kandidati().stream().filter(k -> k.id().equals("DNES")).findFirst().get().dniDo());
		Assert.assertEquals(Integer.valueOf(365), v.kandidati().stream().filter(k -> k.id().equals("ROK")).findFirst().get().dniDo());
	}

	@Test
	public void vyroceVKalendariJenNaVyzadaniAEventNaZnamyDenVNalezenemDniNeniKandidat() {
		final LocalDate dnes = LocalDate.of(2026, 11, 5);
		final V bezna = new V("BEZNA");
		bezna.datum = LocalDate.of(2010, 3, 14);
		final V eventNalezenyDen = new V("EV").eventDne(LocalDate.of(2026, 12, 24));
		final V nalez = new V("N").nalez(LocalDate.of(2020, 12, 24));
		final List<V> kese = Arrays.asList(bezna, eventNalezenyDen, nalez);
		Assert.assertTrue(idKandidatu(kalendar(dnes, kese)).isEmpty());
		final VysledekPolicek sVyroci = VypocetKalendare.spocti(PravidlaKalendare.vychozi().sKandidaty(PravidlaKalendare.Kandidati.EVENTY_A_VYROCI), dnes, 50, 14, kese);
		Assert.assertEquals(Collections.singletonList("BEZNA"), idKandidatu(sVyroci));
		Assert.assertEquals(Integer.valueOf(129), sVyroci.kandidati().get(0).dniDo());
	}

	@Test
	public void vysledekKalendareNezaleziNaPoradiVstupuAMaxKandidatu() {
		final LocalDate dnes = LocalDate.of(2026, 11, 5);
		final Random r = new Random(4);
		final List<V> kese = new ArrayList<>();
		for (int i = 0; i < 2000; i++) {
			final V k = new V("GC" + i).poloha(49 + r.nextDouble() * 2, 13 + r.nextDouble() * 2);
			if (r.nextInt(3) == 0) {
				k.nalez(LocalDate.of(2020, 1, 1).plusDays(r.nextInt(300)));
			} else {
				k.eventDne(dnes.plusDays(r.nextInt(400)));
			}
			kese.add(k);
		}
		final PravidlaKalendare pravidla = PravidlaKalendare.vychozi().sTypy(PravidlaTypu.proKalendar().sMaxKandidatu(2));
		final VysledekPolicek zaklad = VypocetKalendare.spocti(pravidla, dnes, 50, 14, kese);
		for (int pokus = 0; pokus < 5; pokus++) {
			final List<V> zamichane = new ArrayList<>(kese);
			Collections.shuffle(zamichane, new Random(pokus));
			final VysledekPolicek v = VypocetKalendare.spocti(pravidla, dnes, 50, 14, zamichane);
			Assert.assertEquals(zaklad.stitkyChybejicich(), v.stitkyChybejicich());
			Assert.assertEquals(idKandidatu(zaklad), idKandidatu(v));
		}
	}

	@Test
	public void kalendarOdmitneNullyAneplatnyBod() {
		final List<V> kese = Collections.singletonList(new V("A"));
		final LocalDate dnes = LocalDate.of(2026, 11, 5);
		for (final Runnable r : new Runnable[] { () -> VypocetKalendare.spocti(null, dnes, 50, 14, kese), () -> VypocetKalendare.spocti(PravidlaKalendare.vychozi(), null, 50, 14, kese),
				() -> VypocetKalendare.spocti(PravidlaKalendare.vychozi(), dnes, 50, 14, null), () -> VypocetKalendare.spocti(PravidlaKalendare.vychozi(), dnes, 50, 14, Arrays.asList(new V("A"), null)) }) {
			try {
				r.run();
				Assert.fail();
			} catch (final NullPointerException e) {
				// očekávané
			}
		}
		for (final double[] zly : new double[][] { { Double.NaN, 14 }, { 50, Double.NaN }, { -91, 14 }, { 50, 181 } }) {
			try {
				VypocetKalendare.spocti(PravidlaKalendare.vychozi(), dnes, zly[0], zly[1], kese);
				Assert.fail(Arrays.toString(zly));
			} catch (final IllegalArgumentException e) {
				// očekávané
			}
		}
	}

	// ---- Výběr dalšího políčka

	private static VysledekPolicek sKandidaty(final String... id) {
		final List<KandidatPolicka> k = new ArrayList<>();
		for (int i = 0; i < id.length; i++) {
			k.add(new KandidatPolicka(id[i], 50 + i * 0.01, 14, "S" + i, "S" + i, null));
		}
		return new VysledekPolicek(81, Collections.singletonList("S0"), k);
	}

	@Test
	public void vyberDalsihoPolickaPrezijeNovyVysledekResetNullAStredSNaN() {
		final DalsiPolicko d = new DalsiPolicko();
		Assert.assertNull(d.dalsi(null, 50, 14));
		Assert.assertNull(d.predchozi(null));
		final VysledekPolicek prazdny = new VysledekPolicek(81, Collections.<String> emptyList(), Collections.<KandidatPolicka> emptyList());
		Assert.assertNull(d.dalsi(prazdny, 50, 14));
		Assert.assertNull(d.predchozi(prazdny));
		final VysledekPolicek v = sKandidaty("A", "B", "C");
		final List<String> kolo = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			kolo.add(d.dalsi(v, Double.NaN, Double.NaN).id());
		}
		Assert.assertEquals("střed NaN: každý jednou", 3, new java.util.HashSet<>(kolo).size());
		Assert.assertEquals("po kole znovu", kolo.get(0), d.dalsi(v, Double.NaN, Double.NaN).id());
		Assert.assertEquals(kolo.get(0), d.dalsi(v, Double.POSITIVE_INFINITY, 14).id() == null ? null : kolo.get(0));
		d.reset();
		Assert.assertNull("po resetu není předchozí", d.predchozi(v));
		final VysledekPolicek jiny = sKandidaty("X");
		Assert.assertEquals("X", d.dalsi(jiny, 50, 14).id());
		Assert.assertEquals("X", d.dalsi(jiny, 50, 14).id());
		Assert.assertNull("předchozí z jiného výsledku", d.predchozi(v));
	}

	@Test
	public void predchoziJdeZpetPoPoradiVyberuAZastaviSeNaPrvnim() {
		final DalsiPolicko d = new DalsiPolicko();
		final VysledekPolicek v = sKandidaty("A", "B", "C", "D");
		final String prvni = d.dalsi(v, 50, 14).id();
		final String druhy = d.dalsi(v, 50, 14).id();
		final String treti = d.dalsi(v, 50, 14).id();
		Assert.assertEquals(druhy, d.predchozi(v).id());
		Assert.assertEquals(prvni, d.predchozi(v).id());
		Assert.assertNull(d.predchozi(v));
		Assert.assertEquals("po návratu se vybírá znovu od druhého", druhy, d.dalsi(v, 50, 14).id());
		Assert.assertEquals(treti, d.dalsi(v, 50, 14).id());
	}

	// ---- KesVstup

	private static Kes gc(final String id, final EKesVztah vztah, final double lat, final double lon) {
		final Kes k = new Kes();
		k.setIdentifier(id);
		k.setVztahx(vztah);
		final Wpt w = new Wpt();
		w.setWgs(new Wgs(lat, lon));
		w.setSym("Traditional Cache");
		k.addWpt(w);
		return k;
	}

	@Test
	public void kesoidyBezKoduSMalymiPismenyBezBoduNullAPrazdnySeznamSePreskoci() {
		Assert.assertNull(KesVstup.z(null));
		Assert.assertNull("kód malými písmeny", KesVstup.z(gc("gc1abc", EKesVztah.FOUND, 50, 14)));
		Assert.assertNull("prázdný kód", KesVstup.z(gc("", EKesVztah.FOUND, 50, 14)));
		Assert.assertNotNull("jen předpona GC", KesVstup.z(gc("GC", EKesVztah.FOUND, 50, 14)));
		final Kes bezBodu = new Kes();
		bezBodu.setIdentifier("GC1");
		Assert.assertNull(KesVstup.z(bezBodu));
		Assert.assertTrue(KesVstup.zKesoidu(Collections.<Kes> emptyList()).isEmpty());
		Assert.assertEquals(0, KesVstup.zKesoidu(Collections.<Kes> emptyList()).size());
		try {
			KesVstup.zKesoidu(null).size();
			Assert.fail();
		} catch (final NullPointerException e) {
			// očekávané
		}
	}

	@Test
	public void sbirkaPreskociNullyAVyhodiNoSuchElementPoKonci() {
		final List<Kes> v = Arrays.asList(null, gc("GC1", EKesVztah.NORMAL, 50, 14), null, gc("OC1", EKesVztah.NORMAL, 50, 14), gc("GC2", EKesVztah.NORMAL, 50.1, 14.1));
		final java.util.Collection<KesVstup> sbirka = KesVstup.zKesoidu(v);
		Assert.assertEquals(2, sbirka.size());
		final java.util.Iterator<KesVstup> it = sbirka.iterator();
		Assert.assertEquals("GC1", it.next().id());
		Assert.assertTrue(it.hasNext());
		Assert.assertTrue("opakované hasNext nic nepřeskočí", it.hasNext());
		Assert.assertEquals("GC2", it.next().id());
		Assert.assertFalse(it.hasNext());
		try {
			it.next();
			Assert.fail();
		} catch (final java.util.NoSuchElementException e) {
			// očekávané
		}
		int projito = 0;
		for (final KesVstup k : sbirka) {
			Assert.assertNotNull(k);
			projito++;
		}
		Assert.assertEquals("sbírku lze projít opakovaně", 2, projito);
	}

	@Test
	public void sbirkaSePocitaZeSeznamuPoOpakovanemPrubehuSeZmenaVidiJenVPruchodu() {
		final List<Kes> v = new ArrayList<>(Arrays.asList(gc("GC1", EKesVztah.NORMAL, 50, 14), gc("GC2", EKesVztah.NORMAL, 50.1, 14.1)));
		final java.util.Collection<KesVstup> sbirka = KesVstup.zKesoidu(v);
		Assert.assertEquals(2, sbirka.size());
		v.add(gc("GC3", EKesVztah.NORMAL, 50.2, 14.2));
		Assert.assertEquals("velikost se spočítá jednou", 2, sbirka.size());
		int projito = 0;
		for (final KesVstup k : sbirka) {
			Assert.assertNotNull(k);
			projito++;
		}
		Assert.assertEquals("průchod vidí aktuální seznam", 3, projito);
	}

	@Test
	public void nesmyslneDatumyVeZdrojiNevadi() {
		final Kes nalezena = gc("GC1", EKesVztah.FOUND, 50, 14);
		nalezena.setFoundTime("není datum");
		nalezena.setHidden("2026-02-30");
		final KesVstup v = KesVstup.z(nalezena, ZoneId.of("Europe/Prague"));
		Assert.assertNull(v.datumNalezu());
		Assert.assertNull(v.datum());
		Assert.assertFalse(v.event());
		Assert.assertEquals(-1, v.obtiznost());
		Assert.assertEquals(-1, v.teren());
		// Taková keš se do výpočtů vůbec nezapočítá, nic nespadne.
		Assert.assertEquals(365, VypocetKalendare.spocti(PravidlaKalendare.vychozi(), LocalDate.of(2026, 11, 5), 50, 14, Collections.singletonList(v)).pocetChybejicich());
		Assert.assertEquals(81, VypocetMatice.spocti(PravidlaTypu.proMatici(), 50, 14, Collections.singletonList(v)).pocetChybejicich());
	}
}
