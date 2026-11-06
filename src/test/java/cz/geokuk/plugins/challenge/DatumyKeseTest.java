package cz.geokuk.plugins.challenge;

import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.Assert;
import org.junit.Test;

public class DatumyKeseTest {

	private static final ZoneId PRAHA = ZoneId.of("Europe/Prague");

	@Test
	public void zapsaneDatumyBezZony() {
		Assert.assertEquals(LocalDate.of(2026, 3, 14), DatumyKese.datum("2026-03-14", PRAHA));
		Assert.assertEquals(LocalDate.of(2026, 3, 14), DatumyKese.datum("2026-03-14T23:30:00", PRAHA));
		Assert.assertEquals(LocalDate.of(2026, 3, 14), DatumyKese.datum("2026-03-14T23:30:00.000", PRAHA));
		Assert.assertEquals(LocalDate.of(2026, 3, 14), DatumyKese.datum("2026-03-14 23:30:00", PRAHA));
		Assert.assertEquals(LocalDate.of(2026, 3, 14), DatumyKese.datum("  2026-03-14T23:30  ", PRAHA));
		Assert.assertEquals(LocalDate.of(2026, 3, 14), DatumyKese.datum("20260314", PRAHA));
		Assert.assertEquals(LocalDate.of(2026, 3, 14), DatumyKese.datum("202603142330", PRAHA));
		Assert.assertEquals("nejdelší čtená číselná podoba", LocalDate.of(2026, 3, 14), DatumyKese.datum("20260314233000", PRAHA));
		Assert.assertNull("příliš dlouhé", DatumyKese.datum("202603142330000", PRAHA));
	}

	@Test
	public void zonaSePrevedeDoPasma() {
		Assert.assertEquals(LocalDate.of(2026, 3, 15), DatumyKese.datum("2026-03-14T23:30:00Z", PRAHA));
		Assert.assertEquals(LocalDate.of(2026, 3, 14), DatumyKese.datum("2026-03-14T23:30:00Z", ZoneId.of("UTC")));
		Assert.assertEquals(LocalDate.of(2026, 3, 15), DatumyKese.datum("2026-03-14T23:30:00-05:00", ZoneId.of("UTC")));
		Assert.assertEquals(LocalDate.of(2026, 3, 14), DatumyKese.datum("2026-03-15T00:30:00+02:00", ZoneId.of("UTC")));
		Assert.assertEquals("přelom roku v létě i v zimě", LocalDate.of(2027, 1, 1), DatumyKese.datum("2026-12-31T23:30:00Z", PRAHA));
		Assert.assertEquals("letní čas", LocalDate.of(2026, 7, 2), DatumyKese.datum("2026-07-01T22:30:00Z", PRAHA));
		Assert.assertEquals("bez sekund", LocalDate.of(2026, 3, 15), DatumyKese.datum("2026-03-14T23:30Z", PRAHA));
		Assert.assertEquals("zlomky sekund", LocalDate.of(2026, 3, 15), DatumyKese.datum("2026-03-14T23:30:00.123Z", PRAHA));
	}

	@Test
	public void bezPasmaSeZonaNeprevadi() {
		Assert.assertEquals(LocalDate.of(2026, 3, 14), DatumyKese.datum("2026-03-14T23:30:00Z", null));
	}

	@Test
	public void prestupnyDen() {
		Assert.assertEquals(LocalDate.of(2024, 2, 29), DatumyKese.datum("2024-02-29", PRAHA));
		Assert.assertEquals(LocalDate.of(2024, 2, 29), DatumyKese.datum("20240229", PRAHA));
		Assert.assertNull(DatumyKese.datum("2026-02-29", PRAHA));
	}

	@Test
	public void neplatneHodnotyDajiNull() {
		for (final String s : new String[] { null, "", "  ", "abc", "2026-13-01", "2026-00-10", "20261301", "2026-3-14", "12345678901", "1234567" }) {
			Assert.assertNull(String.valueOf(s), DatumyKese.datum(s, PRAHA));
		}
	}

	@Test
	public void mezeraMistoTAZonaSePrevede() {
		Assert.assertEquals(LocalDate.of(2024, 3, 6), DatumyKese.datum("2024-03-05 23:30:00+01:00", ZoneId.of("Asia/Tokyo")));
		Assert.assertEquals(LocalDate.of(2024, 3, 5), DatumyKese.datum("2024-03-05 23:30:00+01:00", ZoneId.of("UTC")));
		Assert.assertEquals(LocalDate.of(2024, 3, 6), DatumyKese.datum("2024-03-05 23:30:00Z", PRAHA));
	}

	@Test
	public void zlomekDelsiNezDevetCisliSeZonouJeNecitelny() {
		Assert.assertNull(DatumyKese.datum("2026-03-14T23:30:00.1234567890Z", PRAHA));
		Assert.assertEquals(LocalDate.of(2026, 3, 14), DatumyKese.datum("2026-03-14T23:30:00.1234567890", PRAHA));
	}

	@Test
	public void posunBezDvojteckySeBereJakoBezZony() {
		Assert.assertEquals(LocalDate.of(2026, 3, 14), DatumyKese.datum("2026-03-14T23:30:00+0100", ZoneId.of("Asia/Tokyo")));
	}

	@Test
	public void pulnocSZonouZUJeDatumBezPrevodu() {
		for (final String pasmo : new String[] { "America/New_York", "Pacific/Honolulu", "UTC", "Europe/Prague", "Asia/Tokyo" }) {
			Assert.assertEquals(pasmo, LocalDate.of(2026, 3, 14), DatumyKese.datum("2026-03-14T00:00:00Z", ZoneId.of(pasmo)));
		}
		Assert.assertEquals(LocalDate.of(2026, 3, 14), DatumyKese.datum("2026-03-14T00:00Z", ZoneId.of("America/New_York")));
		Assert.assertEquals(LocalDate.of(2026, 3, 14), DatumyKese.datum("2026-03-14T00:00:00.000Z", ZoneId.of("America/New_York")));
		Assert.assertEquals(LocalDate.of(2026, 3, 14), DatumyKese.datum("2026-03-14 00:00:00Z", ZoneId.of("America/New_York")));
	}

	@Test
	public void jinyCasSZonouZUSePrevadi() {
		Assert.assertEquals(LocalDate.of(2026, 3, 13), DatumyKese.datum("2026-03-14T00:00:01Z", ZoneId.of("America/New_York")));
		Assert.assertEquals(LocalDate.of(2026, 3, 13), DatumyKese.datum("2026-03-14T00:01:00Z", ZoneId.of("America/New_York")));
		Assert.assertEquals(LocalDate.of(2026, 3, 13), DatumyKese.datum("2026-03-14T00:00:00.001Z", ZoneId.of("America/New_York")));
		Assert.assertEquals("půlnoc s posunem +01:00 je 4. 3. v New Yorku", LocalDate.of(2024, 3, 4), DatumyKese.datum("2024-03-05T00:00:00+01:00", ZoneId.of("America/New_York")));
		Assert.assertEquals("posun jiný než Z je skutečný okamžik", LocalDate.of(2026, 3, 13), DatumyKese.datum("2026-03-14T00:00:00+02:00", ZoneId.of("UTC")));
	}
}
