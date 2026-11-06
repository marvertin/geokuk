package cz.geokuk.plugins.challenge;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Čtení dat keše ze zdrojů dat: ISO s časem, „RRRR-MM-DD“, „RRRRMMDD“ (GeoGet). */
final class DatumyKese {

	private static final Pattern ISO = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2})(?:[T ]\\d{2}:\\d{2}(?::\\d{2})?(?:\\.\\d+)?(Z|[+-]\\d{2}:\\d{2}))?.*$");

	private DatumyKese() {}

	/** Čas přesně 00:00 (s nulovými sekundami a zlomky). */
	private static boolean jePulnoc(final String s) {
		return s.substring(11).matches("00:00(:00(\\.0+)?)?Z");
	}

	/**
	 * @param pasmo
	 *            pásmo uživatele; má-li hodnota časovou zónu, datum se převede do něj, jinak se vezme datum, jak je zapsáno; null = vždy datum, jak je zapsáno
	 * @return null, když hodnota chybí nebo nejde přečíst
	 *
	 *         Zóna je jen {@code Z} nebo {@code ±hh:mm}; posun {@code +0100} se bere jako zápis bez zóny. Zlomek sekundy delší než 9 číslic se zónou je nečitelný (null), bez zóny
	 *         se přečte datum. Čas přesně 00:00 se zónou {@code Z} je zápis samotného data a nepřevádí se.
	 */
	static LocalDate datum(final String hodnota, final ZoneId pasmo) {
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
			if (m.group(2) != null && pasmo != null && !(m.group(2).equals("Z") && jePulnoc(s))) {
				return OffsetDateTime.parse(s.replace(' ', 'T').substring(0, m.end(2))).atZoneSameInstant(pasmo).toLocalDate();
			}
			return LocalDate.parse(m.group(1));
		} catch (final DateTimeException e) {
			return null;
		}
	}
}
