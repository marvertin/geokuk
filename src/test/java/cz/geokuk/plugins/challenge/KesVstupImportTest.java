package cz.geokuk.plugins.challenge;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.importek.ImportKesiTestPristup;

/** Typ a datum nálezu u keší načtených stejnou cestou jako z GPX, GeoGetu a GSAKu. */
public class KesVstupImportTest {

	private static String nalezena(final String kod, final String typ, final String datum) {
		return ImportKesiTestPristup.kesNa(kod, "Geocache Found", typ, "Cizí", 1, true, false, 50.1, 14.4).replace("</wpt>",
				"<gpxg:GeogetExtension xmlns:gpxg=\"https://www.geoget.cz/GpxExtensions/v2\"><gpxg:Found>" + datum + "</gpxg:Found></gpxg:GeogetExtension></wpt>");
	}

	private static List<KesVstup> nacti(final String... wpt) throws Exception {
		final KesBag bag = ImportKesiTestPristup.importuj(wpt);
		final List<KesVstup> v = new ArrayList<>();
		for (final Kesoid k : bag.getKesoidy()) {
			final KesVstup x = KesVstup.z(k, java.time.ZoneId.of("UTC"));
			if (x != null) {
				v.add(x);
			}
		}
		return v;
	}

	@Test
	public void nalezenaKesMaTypIDatumNalezu() throws Exception {
		final List<KesVstup> v = nacti(nalezena("GC1", "Traditional Cache", "2024-03-14"));
		Assert.assertEquals(1, v.size());
		Assert.assertTrue(v.get(0).nalezena());
		Assert.assertEquals(LocalDate.of(2024, 3, 14), v.get(0).datumNalezu());
		Assert.assertFalse(v.get(0).lab());
		Assert.assertFalse(v.get(0).locationless());
	}

	@Test
	public void nalezenaLabALocationlessSeVKalendariNepocita() throws Exception {
		final List<KesVstup> v = nacti(nalezena("GC1", "Lab Cache", "2024-03-14"), nalezena("GC2", "Locationless (Reverse) Cache", "2024-03-15"),
				nalezena("GC3", "Traditional Cache", "2024-03-16"));
		for (final KesVstup k : v) {
			Assert.assertTrue(k.nalezena());
		}
		final VysledekPolicek kal = VypocetKalendare.spocti(PravidlaKalendare.vychozi(), LocalDate.of(2026, 11, 5), 50, 14, v);
		Assert.assertEquals("jen Traditional vyplní den", 364, kal.pocetChybejicich());
		Assert.assertTrue("Lab", kal.stitkyChybejicich().toString().contains("14. 3."));
		Assert.assertTrue("Locationless", kal.stitkyChybejicich().toString().contains("15. 3."));
		Assert.assertFalse("Traditional", kal.stitkyChybejicich().toString().contains("16. 3."));
	}
}
