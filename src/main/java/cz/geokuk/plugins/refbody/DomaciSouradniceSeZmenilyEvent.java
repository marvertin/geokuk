/**
 *
 */
package cz.geokuk.plugins.refbody;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.Event0;

/**
 * @author Martin Veverka
 *
 */
public class DomaciSouradniceSeZmenilyEvent extends Event0<RefbodyModel> {

	public final Wgs hc;

	/**
	 * @param aWgs
	 */
	public DomaciSouradniceSeZmenilyEvent(final Wgs hc) {
		this.hc = hc;
	}

}
