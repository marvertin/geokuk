/**
 *
 */
package cz.geokuk.core.render;

/**
 * @author Martin Veverka
 *
 */
public enum EImageType {
	bmp(false), png(true), jpg(false),;

	private final boolean umoznujePruhlednost;

	/**
	 *
	 */
	private EImageType(final boolean umoznujePruhlednost) {
		this.umoznujePruhlednost = umoznujePruhlednost;
	}

	public String getType() {
		return name().toLowerCase();

	}

	/**
	 * @return the umoznujePruhlednost
	 */
	public boolean isUmoznujePruhlednost() {
		return umoznujePruhlednost;
	}
}
