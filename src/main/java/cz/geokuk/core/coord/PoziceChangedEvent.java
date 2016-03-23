/**
 *
 */
package cz.geokuk.core.coord;

import cz.geokuk.framework.Event0;

/**
 * @author veverka
 *
 */
public class PoziceChangedEvent extends Event0<PoziceModel> {

	public final Poziceq poziceq;

	/**
	 * @param aPozice
	 * @param aMeloBySeCentrovat
	 */
	PoziceChangedEvent(Poziceq aPoziceq) {
		assert aPoziceq != null;
		poziceq = aPoziceq;
	}
}
