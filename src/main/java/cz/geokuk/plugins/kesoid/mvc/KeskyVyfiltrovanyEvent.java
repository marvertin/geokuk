/**
 *
 */
package cz.geokuk.plugins.kesoid.mvc;

import cz.geokuk.framework.Event0;
import cz.geokuk.plugins.kesoid.KesBag;

/**
 * @author veverka
 *
 */
public class KeskyVyfiltrovanyEvent extends Event0<KesoidModel> {
	private final KesBag filtrovane;
	private final KesBag vsechny;
	// private final Set<Kes> filtrovaneKese;

	public KeskyVyfiltrovanyEvent(final KesBag filtrovane, final KesBag vsechny) {
		this.filtrovane = filtrovane;
		this.vsechny = vsechny;
	}

	public KesBag getFiltrovane() {
		return filtrovane;
	}

	public KesBag getVsechny() {
		return vsechny;
	}

}
