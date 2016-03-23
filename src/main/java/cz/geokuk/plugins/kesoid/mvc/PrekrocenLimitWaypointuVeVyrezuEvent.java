/**
 *
 */
package cz.geokuk.plugins.kesoid.mvc;

import cz.geokuk.framework.Event0;

/**
 * @author veverka
 *
 */
public class PrekrocenLimitWaypointuVeVyrezuEvent extends Event0<KesoidModel> {
	private final boolean prekrocen;

	PrekrocenLimitWaypointuVeVyrezuEvent(boolean prekrocen) {
		this.prekrocen = prekrocen;
	}

	/**
	 * @return the prekrocen
	 */
	public boolean isPrekrocen() {
		return prekrocen;
	}

}
