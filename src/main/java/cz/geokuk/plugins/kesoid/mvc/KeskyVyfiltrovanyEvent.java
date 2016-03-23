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
	private KesBag vsechny;
	//private final Set<Kes> filtrovaneKese;

	public KesBag getVsechny() {
		return vsechny;
	}

	public KeskyVyfiltrovanyEvent(KesBag filtrovane, KesBag vsechny) {
		this.filtrovane = filtrovane;
		this.vsechny = vsechny;
	}

	public KesBag getFiltrovane() {
		return filtrovane;
	}


}
