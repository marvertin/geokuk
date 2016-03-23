/**
 *
 */
package cz.geokuk.plugins.kesoid.mvc;

import cz.geokuk.framework.OnoffEvent0;

/**
 * @author Martin Veverka
 *
 */
public class KesoidOnoffEvent extends OnoffEvent0<KesoidModel> {

	KesoidOnoffEvent(final boolean onoff) {
		this.onoff = onoff;
	}
}
