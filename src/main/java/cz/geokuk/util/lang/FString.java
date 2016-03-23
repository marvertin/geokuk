package cz.geokuk.util.lang;

import java.lang.reflect.Array;

/**
 * Třída opbsahuje užitečnou práci s řetězci.
 *
 * @author Michal Polák
 * @version $Revision: 35 $
 * @see "TW0139Util.vjp"
 * @see "$Header: /Zakazky/TWare/Distribuce/TW0139/Util/cz/tconsult/tw/util/TwString.java 35    22.04.01 14:54 Roztocil $"
 */
public final class FString {
	/** {@link TwString} je jen knihovna funkcí, nikoliv instanciovatelný objekt. */
	private FString() {
		/* INTENDED: zabránění externí instanciace. */ }

	/** Pokud to lze, zařízne String na danou délku. */
	public static String truncateRight(final String s, final int maxlen) {
		if (s.length() > maxlen) {
			return s.substring(0, maxlen);
		}
		return s;
	}

	/** Pokud to lze, zařízne String na danou délku. */
	public static String truncateLeft(final String s, final int maxlen) {
		if (s.length() > maxlen) {
			return s.substring(s.length() - maxlen, s.length());
		}
		return s;
	}

	/** Vrací true, pokud je řětězec null nebo délku 0. */
	public static boolean isVoid(final String s) {
		return (s == null || s.length() == 0);
	}

	/**
	 * Vrací true, pokud je řetězec prázdný, to znamená null, nebo prázdný nebo mezery.
	 */
	public static boolean isEmpty(final String s) {
		return StringUtils.isBlank(s);
	}

	/**
	 * Pomocí oddělovače spojí prvky pole do Stringu.
	 *
	 * @param aDelimiter
	 *            Oddělovač.
	 * @param aPole
	 *            Pole nějakých objektů, jejichž hodnoty .toString() budou spojeny do výsledku.
	 */
	public static String mergeArray(final String aDelimiter, final Object aPole) {
		if (aPole == null) {
			return null;
		}

		final Class<?> c = aPole.getClass();
		if (!c.isArray()) {
			throw new IllegalArgumentException("Argument aPole typu " + c + " není pole.");
		}

		final StringBuilder result = new StringBuilder(300);
		final int len = Array.getLength(aPole);
		for (int i = 0; i < len; i++) {
			if (i > 0) {
				result.append(aDelimiter);
			}
			result.append(Array.get(aPole, i));
		}

		return result.toString();
	}

	/**
	 * Zarovná řetězec na požadovanou délku.
	 *
	 * Pokud je řetězec příliš krátký,
	 *
	 * @param s
	 *            Zarovnávaný řetězec.
	 * @param len
	 *            Požadovaná délka řetězce po zarovnání.
	 * @param fill
	 *            Řetězec, kterým má být zarovnávaný řetězec doplněn na požadovanou délku. * Je-li prázdný nebo null, použije se jedna mezera.
	 * @return Zadaný řetězec doplněný zprava na požadovanou délku určený vyplňovacím řetězcem.
	 */
	public static String alignRight(final String s, final int len, String fill) {
		if (s.length() == len) {
			return s;
		}
		if (s.length() < len) {
			if (isVoid(fill)) {
				fill = " ";
			}
			final StringBuilder sb = new StringBuilder(len);
			sb.append(s);
			while (sb.length() < len) {
				sb.append(fill);
			}
			if (sb.length() == len) {
				return sb.toString();
			}
			return truncateRight(sb.toString(), len);
		}
		return truncateRight(s, len);
	}

	/**
	 * Zarovná řetězec na požadovanou délku.
	 *
	 * Pokud je řetězec příliš krátký,
	 *
	 * @param s
	 *            Zarovnávaný řetězec.
	 * @param len
	 *            Požadovaná délka řetězce po zarovnání.
	 * @param fill
	 *            Znak, kterým má být zarovnávaný řetězec doplněn na požadovanou délku.
	 * @return Zadaný řetězec doplněný zprava na požadovanou délku určený vyplňovacím znakem.
	 */
	public static String alignRight(final String s, final int len, final char fill) {
		return alignRight(s, len, String.valueOf(fill));
	}

	/**
	 * Porovná dva řetězce, přičemž i shoda v <b>null</b> se považuje za rovnost.
	 *
	 * @param s1
	 * @param s2
	 * @return
	 */
	public static boolean equals(final String s1, final String s2) {
		return s1 == null && s2 == null || !(s1 == null || s2 == null) && s1.equals(s2);
	}

	public static String intern(final String aString) {
		return aString == null ? null : aString.intern();
	}

}
