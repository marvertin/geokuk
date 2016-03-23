package cz.geokuk.util.lang;

import java.awt.Color;
import java.util.Collection;

public final class FUtil {

	public static <T> boolean addAll(final Collection<? super T> col, final Iterable<? extends T> iterable) {
		boolean celkovaZmena = false;
		for (final T tt : iterable) {
			final boolean zmena = col.add(tt);
			celkovaZmena = celkovaZmena || zmena;
		}
		return celkovaZmena;
	}

	public static char cestinuPryc(final char c) {
		switch (c) {
		case 'á':
			return 'a';
		case 'é':
			return 'e';
		case 'ě':
			return 'e';
		case 'í':
			return 'i';
		case 'ó':
			return 'o';
		case 'ú':
			return 'u';
		case 'ů':
			return 'u';
		case 'ý':
			return 'y';
		case 'Á':
			return 'A';
		case 'É':
			return 'E';
		case 'Ě':
			return 'E';
		case 'Í':
			return 'I';
		case 'Ó':
			return 'O';
		case 'Ú':
			return 'U';
		case 'Ů':
			return 'U';
		case 'ž':
			return 'z';
		case 'š':
			return 's';
		case 'č':
			return 'c';
		case 'ř':
			return 'r';
		case 'ŕ':
			return 'r';
		case 'ď':
			return 'd';
		case 'ť':
			return 't';
		case 'ň':
			return 'n';
		case 'ĺ':
			return 'l';
		case 'ľ':
			return 'l';
		case 'Ž':
			return 'Z';
		case 'Š':
			return 'S';
		case 'Č':
			return 'C';
		case 'Ř':
			return 'R';
		case 'Ŕ':
			return 'R';
		case 'Ď':
			return 'D';
		case 'Ť':
			return 'T';
		case 'Ň':
			return 'N';
		case 'Ĺ':
			return 'L';
		case 'Ľ':
			return 'L';
		default:
			return c;
		}

	}

	public static String cestinuPryc(final String s) {
		int i;
		final int n = s.length();
		for (i = 0; i < n; i++) {
			final char c = s.charAt(i);
			if (c < 128) {
				continue;
			}
			final char cc = FUtil.cestinuPryc(c);
			if (cc != c) {
				break;
			}
		}
		if (i == n) {
			return s; // není čeština
		}
		final StringBuilder sb = new StringBuilder(n);
		sb.append(s.substring(0, i));
		for (; i < n; i++) {
			char c = s.charAt(i);
			if (c >= 128) {
				c = FUtil.cestinuPryc(c);
			}
			sb.append(c);
		}
		return sb.toString();
	}

	public static <T> boolean equalsa(final T o1, final T o2) {
		return o1 == null && o2 == null || o1 != null && o1.equals(o2);
	}

	public static String getHtmlColor(final Color color) {
		return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
	}

	public static long soucetKvadratu(final int dx, final int dy) {
		final long ldx = dx;
		final long ldy = dy;
		final long result = ldx * ldx + ldy * ldy;
		return result;
	}

	public static String vycistiJmenoSouboru(final String s) {
		if (s == null) {
			return null;
		}
		StringBuilder sb = null;
		for (int i = 0; i < s.length(); i++) {
			final char c = s.charAt(i);
			char x;
			switch (c) {
			case '"':
				x = '\'';
				break;
			case '*':
				x = '+';
				break;
			case ':':
				x = ';';
				break;
			case '<':
				x = '[';
				break;
			case '>':
				x = ']';
				break;
			case '?':
				x = '!';
				break;
			case '|':
				x = '!';
				break;
			case '/':
				x = '!';
				break;
			case '\\':
				x = '!';
				break;
			default:
				x = c;
			}
			if (x != c && sb == null) {
				sb = new StringBuilder(s.length());
				sb.append(s.substring(0, i));
			}
			if (sb != null) {
				sb.append(x);
			}
		}
		return sb == null ? s : sb.toString();

	}

	private FUtil() {}

}
