package cz.geokuk.plugins.challenge;

import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.plugins.kesoid.EKesVztah;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.kind.kes.EKesDiffTerRating;
import cz.geokuk.plugins.kesoid.kind.kes.Kes;

public class KesVstupTest {

	private static Kes kes(final String id, final EKesVztah vztah, final String sym, final double lat, final double lon) {
		final Kes k = new Kes();
		k.setIdentifier(id);
		k.setVztahx(vztah);
		final Wpt w = new Wpt();
		w.setWgs(new Wgs(lat, lon));
		w.setSym(sym);
		k.addWpt(w);
		return k;
	}

	@Test
	public void prebereIdSouradniceANalezenost() {
		final KesVstup v = KesVstup.z(kes("GC1ABC", EKesVztah.FOUND, "Geocache Found", 50.1, 14.2));
		Assert.assertEquals("GC1ABC", v.id());
		Assert.assertEquals(50.1, v.lat(), 1e-9);
		Assert.assertEquals(14.2, v.lon(), 1e-9);
		Assert.assertTrue(v.nalezena());
		Assert.assertFalse(v.lab());
		Assert.assertFalse(v.locationless());
		Assert.assertEquals("log na gc.com model neumí", VstupKes.StavLogu.NEZNAMY, v.logNaGc());
	}

	@Test
	public void nenalezenaAVlastniNejsouNalezene() {
		Assert.assertFalse(KesVstup.z(kes("GC1", EKesVztah.NORMAL, "Geocache", 50.1, 14.2)).nalezena());
		Assert.assertFalse(KesVstup.z(kes("GC2", EKesVztah.OWN, "Geocache", 50.1, 14.2)).nalezena());
	}

	@Test
	public void labALocationlessPoznaPodleSymbolu() {
		Assert.assertTrue(KesVstup.z(kes("GCLAB1", EKesVztah.FOUND, "Lab Cache", 50.1, 14.2)).lab());
		Assert.assertTrue(KesVstup.z(kes("GC9", EKesVztah.FOUND, "Locationless Cache", 50.1, 14.2)).locationless());
		Assert.assertTrue(KesVstup.z(kes("GC9", EKesVztah.FOUND, "Locationless (Reverse) Cache", 50.1, 14.2)).locationless());
		Assert.assertFalse(KesVstup.z(kes("GC9", EKesVztah.FOUND, null, 50.1, 14.2)).locationless());
	}

	@Test
	public void labPoznaPodleTypuNeDleKodu() {
		Assert.assertTrue(KesVstup.z(kes("GC12345", EKesVztah.FOUND, "Lab Cache", 50.1, 14.2)).lab());
		Assert.assertTrue(KesVstup.z(kes("GCABC-1", EKesVztah.FOUND, " lab cache ", 50.1, 14.2)).lab());
		Assert.assertFalse("kód s pomlčkou nestačí", KesVstup.z(kes("GCABC-1", EKesVztah.FOUND, "Traditional Cache", 50.1, 14.2)).lab());
		Assert.assertFalse("slovo lab v jiném typu", KesVstup.z(kes("GC1", EKesVztah.FOUND, "Collab Cache", 50.1, 14.2)).lab());
	}

	@Test
	public void typyMimoKandidatyAleSeNalezyPocitaji() {
		for (final String typ : new String[] { "GPS Adventures Exhibit", "Community Celebration Event", "Lost and Found Event Caches", "Giga-Event Cache" }) {
			Assert.assertTrue(typ, KesVstup.z(kes("GC7", EKesVztah.NORMAL, typ, 50.1, 14.2)).mimoKandidaty());
		}
		Assert.assertFalse(KesVstup.z(kes("GC7", EKesVztah.NORMAL, "Traditional Cache", 50.1, 14.2)).mimoKandidaty());
		Assert.assertFalse(KesVstup.z(kes("GC7", EKesVztah.NORMAL, "Mega-Event Cache", 50.1, 14.2)).mimoKandidaty());
		Assert.assertFalse(KesVstup.z(kes("GC7", EKesVztah.NORMAL, null, 50.1, 14.2)).mimoKandidaty());
	}

	@Test
	public void kesBezSouradnicNeniVstup() {
		Assert.assertNull(KesVstup.z(kes("GC3", EKesVztah.FOUND, "Geocache", 0, 0)));
	}

	private static <K extends cz.geokuk.plugins.kesoid.Kesoid> K naplnit(final K k, final String id, final EKesVztah vztah) {
		k.setIdentifier(id);
		k.setVztahx(vztah);
		final Wpt w = new Wpt();
		w.setWgs(new Wgs(50.1, 14.2));
		w.setSym("Geocache");
		k.addWpt(w);
		return k;
	}

	@Test
	public void jenKesZGcComSeBereDoChallenge() {
		Assert.assertNotNull(KesVstup.z(kes("GC1ABC", EKesVztah.FOUND, "Geocache", 50.1, 14.2)));
		Assert.assertNull("Opencaching", KesVstup.z(kes("OC1234", EKesVztah.FOUND, "Geocache", 50.1, 14.2)));
		Assert.assertNull("nenalezená Opencaching není kandidát", KesVstup.z(kes("OP1234", EKesVztah.NORMAL, "Geocache", 50.1, 14.2)));
		Assert.assertNull("waymark", KesVstup.z(naplnit(new cz.geokuk.plugins.kesoid.kind.waymark.Waymark(), "GC9WM", EKesVztah.FOUND)));
		Assert.assertNull("munzee", KesVstup.z(naplnit(new cz.geokuk.plugins.kesoid.kind.munzee.Munzee(), "GC9MZ", EKesVztah.FOUND)));
		Assert.assertNull("bez kódu", KesVstup.z(kes(null, EKesVztah.FOUND, "Geocache", 50.1, 14.2)));
	}

	@Test
	public void vlastniAZakazaneKese() {
		Assert.assertTrue(KesVstup.z(kes("GC2", EKesVztah.OWN, "Geocache", 50.1, 14.2)).vlastni());
		Assert.assertFalse(KesVstup.z(kes("GC3", EKesVztah.NORMAL, "Geocache", 50.1, 14.2)).vlastni());
		final Kes zakazana = kes("GC4", EKesVztah.NORMAL, "Geocache", 50.1, 14.2);
		zakazana.setStatus(cz.geokuk.plugins.kesoid.EKesStatus.DISABLED);
		Assert.assertTrue(KesVstup.z(zakazana).neaktivni());
		final Kes archivovana = kes("GC5", EKesVztah.NORMAL, "Geocache", 50.1, 14.2);
		archivovana.setStatus(cz.geokuk.plugins.kesoid.EKesStatus.ARCHIVED);
		Assert.assertTrue(KesVstup.z(archivovana).neaktivni());
		Assert.assertFalse(KesVstup.z(kes("GC6", EKesVztah.NORMAL, "Geocache", 50.1, 14.2)).neaktivni());
	}

	@Test
	public void sbirkaMaPresnyPocetKesiKterePrejde() {
		final java.util.List<cz.geokuk.plugins.kesoid.Kesoid> v = new java.util.ArrayList<>();
		v.add(kes("GC1", EKesVztah.FOUND, "Geocache", 50.1, 14.2));
		v.add(kes("OC1", EKesVztah.FOUND, "Geocache", 50.1, 14.2));
		v.add(kes("GC2", EKesVztah.NORMAL, "Geocache", 0, 0));
		v.add(kes("GC3", EKesVztah.NORMAL, "Geocache", 50.2, 14.3));
		final java.util.Collection<KesVstup> sbirka = KesVstup.zKesoidu(v);
		int projito = 0;
		for (final KesVstup k : sbirka) {
			Assert.assertNotNull(k.id());
			projito++;
		}
		Assert.assertEquals(2, projito);
		Assert.assertEquals(2, sbirka.size());
	}

	private static Kes kesDT(final String id, final EKesVztah vztah, final String sym, final EKesDiffTerRating d, final EKesDiffTerRating t) {
		final Kes k = kes(id, vztah, sym, 50.1, 14.2);
		k.setDifficulty(d);
		k.setTerrain(t);
		return k;
	}

	@Test
	public void obtiznostATerenPoPulkach() {
		final KesVstup v = KesVstup.z(kesDT("GC1", EKesVztah.NORMAL, "Traditional Cache", EKesDiffTerRating.FOUR, EKesDiffTerRating.ONE));
		Assert.assertEquals(6, v.obtiznost());
		Assert.assertEquals(0, v.teren());
		final KesVstup kraj = KesVstup.z(kesDT("GC1", EKesVztah.NORMAL, "Traditional Cache", EKesDiffTerRating.FIVE, EKesDiffTerRating.ONE_HALF));
		Assert.assertEquals(8, kraj.obtiznost());
		Assert.assertEquals(1, kraj.teren());
		Assert.assertEquals("D4/T1", VypocetMatice.stitek(v.obtiznost(), v.teren()));
	}

	@Test
	public void neznamaNeboChybejiciObtiznostJeMimoRozsah() {
		final KesVstup nezname = KesVstup.z(kesDT("GC1", EKesVztah.NORMAL, "Traditional Cache", EKesDiffTerRating.UNKNOWN, EKesDiffTerRating.UNKNOWN));
		Assert.assertTrue(nezname.obtiznost() < 0 || nezname.obtiznost() > 8);
		Assert.assertTrue(nezname.teren() < 0 || nezname.teren() > 8);
		final KesVstup chybi = KesVstup.z(kes("GC2", EKesVztah.NORMAL, "Traditional Cache", 50.1, 14.2));
		Assert.assertEquals(-1, chybi.obtiznost());
		Assert.assertEquals(-1, chybi.teren());
	}

	@Test
	public void datumNalezuJenUNalezeneKesePodlePasma() {
		final Kes nalezena = kes("GC1", EKesVztah.FOUND, "Geocache Found", 50.1, 14.2);
		nalezena.setFoundTime("2026-03-14T23:30:00Z");
		Assert.assertEquals(LocalDate.of(2026, 3, 15), KesVstup.z(nalezena, ZoneId.of("Europe/Prague")).datumNalezu());
		Assert.assertEquals(LocalDate.of(2026, 3, 14), KesVstup.z(nalezena, ZoneId.of("UTC")).datumNalezu());
		final Kes nenalezena = kes("GC2", EKesVztah.NORMAL, "Traditional Cache", 50.1, 14.2);
		nenalezena.setFoundTime("2026-03-14T23:30:00Z");
		Assert.assertNull(KesVstup.z(nenalezena).datumNalezu());
		Assert.assertNull("bez data", KesVstup.z(kes("GC3", EKesVztah.FOUND, "Geocache Found", 50.1, 14.2)).datumNalezu());
	}

	@Test
	public void datumNalezuNenalezeneKeseJeNullIKdyzJeVyplneno() {
		for (final EKesVztah vztah : new EKesVztah[] { EKesVztah.NORMAL, EKesVztah.OWN }) {
			final Kes k = kes("GC1", vztah, "Traditional Cache", 50.1, 14.2);
			k.setFoundTime("2026-03-14");
			Assert.assertNull(vztah.name(), KesVstup.z(k, ZoneId.of("UTC")).datumNalezu());
		}
		final Kes nalezena = kes("GC2", EKesVztah.FOUND, "Geocache Found", 50.1, 14.2);
		nalezena.setFoundTime("2026-03-14");
		Assert.assertEquals(LocalDate.of(2026, 3, 14), KesVstup.z(nalezena, ZoneId.of("UTC")).datumNalezu());
	}

	@Test
	public void datumZverejneniABezPasma() {
		final Kes k = kes("GC1", EKesVztah.NORMAL, "Event Cache", 50.1, 14.2);
		k.setHidden("2026-12-24T23:30:00Z");
		Assert.assertEquals("datum zveřejnění se nepřevádí", LocalDate.of(2026, 12, 24), KesVstup.z(k, ZoneId.of("Asia/Tokyo")).datum());
		Assert.assertNull(KesVstup.z(kes("GC2", EKesVztah.NORMAL, "Event Cache", 50.1, 14.2)).datum());
	}

	@Test
	public void eventPoznaPodleTypu() {
		for (final String typ : new String[] { "Event Cache", "Cache In Trash Out Event", "Mega-Event Cache", "Giga-Event Cache", "Community Celebration Event", "Geocaching HQ Celebration",
				"Lost and Found Event Caches" }) {
			Assert.assertTrue(typ, KesVstup.z(kes("GC1", EKesVztah.NORMAL, typ, 50.1, 14.2)).event());
		}
		for (final String typ : new String[] { "Traditional Cache", "Multi-cache", "Lab Cache", "Locationless Cache", "Webcam Cache" }) {
			Assert.assertFalse(typ, KesVstup.z(kes("GC1", EKesVztah.NORMAL, typ, 50.1, 14.2)).event());
		}
		Assert.assertFalse(KesVstup.z(kes("GC1", EKesVztah.NORMAL, null, 50.1, 14.2)).event());
	}

	@Test(expected = NullPointerException.class)
	public void nullSeznamKesoiduSelzeHned() {
		KesVstup.zKesoidu(null);
	}

	@Test(expected = NullPointerException.class)
	public void nullSeznamKesoiduSPasmemSelzeHned() {
		KesVstup.zKesoidu(null, ZoneId.of("UTC"));
	}

	@Test(expected = NullPointerException.class)
	public void nullPasmo() {
		KesVstup.z(kes("GC1", EKesVztah.NORMAL, "Traditional Cache", 50.1, 14.2), null);
	}

	@Test
	public void celyRetezKesVypocetKalendareAMatice() {
		final Kes nalezena = kesDT("GC1", EKesVztah.FOUND, "Geocache Found", EKesDiffTerRating.TWO, EKesDiffTerRating.TWO);
		nalezena.setFoundTime("2026-03-14");
		final Kes event = kesDT("GC2", EKesVztah.NORMAL, "Event Cache", EKesDiffTerRating.ONE, EKesDiffTerRating.ONE);
		event.setHidden("2026-12-24T07:00:00Z");
		final Kes mimo = kesDT("GC3", EKesVztah.NORMAL, "Giga-Event Cache", EKesDiffTerRating.ONE, EKesDiffTerRating.ONE);
		mimo.setHidden("2026-12-25T07:00:00Z");
		final java.util.List<cz.geokuk.plugins.kesoid.Kesoid> kese = java.util.Arrays.<cz.geokuk.plugins.kesoid.Kesoid> asList(nalezena, event, mimo);
		final VysledekPolicek kal = VypocetKalendare.spocti(PravidlaKalendare.vychozi(), LocalDate.of(2026, 11, 5), 50, 14, KesVstup.zKesoidu(kese));
		Assert.assertEquals(364, kal.pocetChybejicich());
		Assert.assertEquals(1, kal.kandidati().size());
		Assert.assertEquals("GC2", kal.kandidati().get(0).id());
		final VysledekPolicek mat = VypocetMatice.spocti(PravidlaTypu.proMatici(), 50, 14, KesVstup.zKesoidu(kese));
		Assert.assertEquals(80, mat.pocetChybejicich());
		Assert.assertEquals("typ mimo kandidáty se nenabízí", 1, mat.kandidati().size());
		Assert.assertEquals("D1/T1", mat.kandidati().get(0).stitek());
	}
}
