package cz.geokuk.plugins.challenge;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.plugins.kesoid.EKesVztah;
import cz.geokuk.plugins.kesoid.Wpt;
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
		Assert.assertFalse(KesVstup.z(kes("GC9", EKesVztah.FOUND, null, 50.1, 14.2)).locationless());
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
}
