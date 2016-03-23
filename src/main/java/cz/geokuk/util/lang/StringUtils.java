/**
 *
 */
package cz.geokuk.util.lang;

/**
 * @author Martin Veverka
 *
 */
public class StringUtils {

	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	public static boolean isBlank(final String s) {
		return s == null || s.length() == 0 || s.trim().length() == 0;
	}
}
