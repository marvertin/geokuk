/**
 *
 */
package cz.geokuk.core.render;

/**
 * @author veverka
 *
 */
public class YNeniPamet extends Exception {

	private static final long serialVersionUID = 1682619442288080458L;

	/**
	 * @param e
	 */
	public YNeniPamet(final OutOfMemoryError e) {
		super(e);
	}

}
