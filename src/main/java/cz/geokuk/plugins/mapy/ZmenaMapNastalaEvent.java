/**
 *
 */
package cz.geokuk.plugins.mapy;

import cz.geokuk.framework.Event0;
import cz.geokuk.plugins.mapy.kachle.KaSet;

/**
 * @author veverka
 *
 */
public class ZmenaMapNastalaEvent extends Event0<MapyModel> {

	private final KaSet kaSet;

	/**
	 * @param aKaSet
	 */
	public ZmenaMapNastalaEvent(final KaSet aKaSet) {
		kaSet = aKaSet;
	}

	/**
	 * @return the kaSet
	 */
	public KaSet getKaSet() {
		return kaSet;
	}
}
