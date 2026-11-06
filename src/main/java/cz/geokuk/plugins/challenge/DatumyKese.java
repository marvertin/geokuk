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

	private static boolean jenCislice(final String s, final int n) {
		for (int i = 0; i < n; i++) {
			if (!jeCislice(s.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	private static boolean jeCislice(final char c) {
		return c >= '0' && c <= '9';
	}

	private static int cislo(final String s, final int od, final int konec) {
		int v = 0;
		for (int i = od; i < konec; i++) {
			v = v * 10 + (s.charAt(i) - '0');
		}
		return v;
	}

	/** „RRRR-MM-DD“ na začátku. */
	private static boolean zacinaDatem(final String s) {
		for (int i = 0; i < 10; i++) {
			final char c = s.charAt(i);
			if (i == 4 || i == 7 ? c != '-' : !jeCislice(c)) {
				return false;
			}
		}
		return true;
	}

	/** Za datem je zóna nebo znak, který by pravidlo bez ní nepřijalo; jen tehdy se použije pomalejší čtení výrazem. */
	private static boolean potrebujeVyraz(final String s, final int n) {
		for (int i = 10; i < n; i++) {
			final char c = s.charAt(i);
			if (c == 'Z' || c == '+' || c == '-' || c == '\n' || c == '\r' || c == '\u0085' || c == '\u2028' || c == '\u2029') {
				return true;
			}
		}
		return false;
	}

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
			final int n = s.length();
			if (n >= 8 && n <= 14 && jenCislice(s, n)) {
				return LocalDate.of(cislo(s, 0, 4), cislo(s, 4, 6), cislo(s, 6, 8));
			}
			if (n < 10 || !zacinaDatem(s)) {
				return null;
			}
			if (!potrebujeVyraz(s, n)) {
				return LocalDate.of(cislo(s, 0, 4), cislo(s, 5, 7), cislo(s, 8, 10));
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
