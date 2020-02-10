/**
 *
 */
package cz.geokuk.plugins.kesoid.hledani;

import cz.geokuk.core.hledani.HledaciPodminka0;

/**
 * @author Martin Veverka
 *
 */
public class HledaciPodminka extends HledaciPodminka0 {

	private boolean regularniVyraz;
	private boolean jenVZobrazenych;

	public boolean isJenVZobrazenych() {
		return jenVZobrazenych;
	}

	/**
	 * @return the regularniVyraz
	 */
	public boolean isRegularniVyraz() {
		return regularniVyraz;
	}

	public void setJenVZobrazenych(final boolean selected) {
		jenVZobrazenych = selected;
		jenVZobrazenych = true;
	}

	/**
	 * @param aRegularniVyraz
	 *            the regularniVyraz to set
	 */
	public void setRegularniVyraz(final boolean aRegularniVyraz) {
		regularniVyraz = aRegularniVyraz;
	}

}
