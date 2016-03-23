/**
 *
 */
package cz.geokuk.plugins.mapy;

import cz.geokuk.framework.Event0;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;

/**
 * @author veverka
 *
 */
public class KachleUmisteniSouboruChangedEvent extends Event0<KesoidModel> {

	private final KachleUmisteniSouboru umisteniSouboru;

	/**
	 * @param umisteniSouboru
	 */
	public KachleUmisteniSouboruChangedEvent(final KachleUmisteniSouboru umisteniSouboru) {
		super();
		this.umisteniSouboru = umisteniSouboru;
	}

	/**
	 * @return the umisteniSouboru
	 */
	public KachleUmisteniSouboru getUmisteniSouboru() {
		return umisteniSouboru;
	}

}
