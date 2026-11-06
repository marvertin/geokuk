package cz.geokuk.plugins.challenge;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

/** Rychlé čtení dat dává stejné výsledky jako čtení regulárním výrazem na náhodných zápisech včetně nesmyslných. */
public class DatumyKeseShodaTest {

	private static final Pattern ISO = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2})(?:[T ]\\d{2}:\\d{2}(?::\\d{2})?(?:\\.\\d+)?(Z|[+-]\\d{2}:\\d{2}))?.*$");

	private static LocalDate referencni(final String hodnota, final ZoneId pasmo) {
		if (hodnota == null) {
			return null;
		}
		final String s = hodnota.trim();
		try {
			if (s.length() >= 8 && s.matches("\\d{8,14}")) {
				return LocalDate.of(Integer.parseInt(s.substring(0, 4)), Integer.parseInt(s.substring(4, 6)), Integer.parseInt(s.substring(6, 8)));
			}
			final Matcher m = ISO.matcher(s);
			if (!m.matches()) {
				return null;
			}
			if (m.group(2) != null && pasmo != null && !(m.group(2).equals("Z") && s.substring(11).matches("00:00(:00(\\.0+)?)?Z"))) {
				return OffsetDateTime.parse(s.replace(' ', 'T').substring(0, m.end(2))).atZoneSameInstant(pasmo).toLocalDate();
			}
			return LocalDate.parse(m.group(1));
		} catch (final DateTimeException e) {
			return null;
		}
	}

	private static final String[] ZACATKY = { "2026-03-14", "2024-02-29", "2026-02-29", "2026-13-01", "2026-00-10", "2026-3-14", "20260314", "202603142330", "2026031", "123456789012345",
			"2026-03-1", "26-03-14", "abcd-ef-gh", "", " ", "２０２６-03-14", "2026/03/14", "99999999", "2026-03-14 " };
	private static final String[] STREDY = { "", "T", " ", "T23:30", "T23:30:00", "T23:30:00.123", "T00:00", "T00:00:00", "T00:00:00.000", " 00:00:00", "T24:61:00", "T1:30:00", "T23:30:00.",
			"T23:30:00.1234567890", "x", "T\n", "\n", "\r", "\u0085", "\u2028", "T23:30:00 foo", "-", "--", "\nx", "\rx", "\u0085x", "\u2028x", "\u2029x", "T\nabc", "+x", "Zx", " \n 5", " \r 5", " \u0085", " \u2028", " \u2029" };
	private static final String[] KONCE = { "", "Z", "z", "+01:00", "-05:00", "+0100", "+01", "-00:00", "+18:00", "+19:00", "Z ", "Z\n", " Z", "+01:00[Europe/Prague]", "ZZ", "+1:00", ".5Z" };
	private static final String[] OKOLI = { "", " ", "\t", "  " };

	@Test
	public void nahodneZapisyDavajiStejnyVysledek() {
		final Random r = new Random(42);
		final ZoneId[] pasma = { ZoneId.of("America/New_York"), ZoneId.of("Europe/Prague"), ZoneId.of("Asia/Tokyo"), null };
		int platnych = 0;
		for (int i = 0; i < 300_000; i++) {
			final String o = OKOLI[r.nextInt(OKOLI.length)];
			final String s = o + ZACATKY[r.nextInt(ZACATKY.length)] + STREDY[r.nextInt(STREDY.length)] + KONCE[r.nextInt(KONCE.length)] + OKOLI[r.nextInt(OKOLI.length)];
			final ZoneId p = pasma[r.nextInt(pasma.length)];
			final LocalDate ocekavano = referencni(s, p);
			Assert.assertEquals("„" + s.replace("\n", "\\n") + "“ v " + p, ocekavano, DatumyKese.datum(s, p));
			if (ocekavano != null) {
				platnych++;
			}
		}
		Assert.assertTrue("náhodné zápisy mají obsahovat i čitelné: " + platnych, platnych > 10_000);
		Assert.assertNull(DatumyKese.datum(null, null));
	}
}
