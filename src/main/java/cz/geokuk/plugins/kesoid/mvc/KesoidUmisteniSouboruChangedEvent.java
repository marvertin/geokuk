/**
 *
 */
package cz.geokuk.plugins.kesoid.mvc;

import cz.geokuk.framework.Event0;

/**
 * @author Martin Veverka
 *
 */
public class KesoidUmisteniSouboruChangedEvent extends Event0<KesoidModel> {

	private final KesoidUmisteniSouboru umisteniSouboru;

	/**
	 * @param umisteniSouboru
	 */
	public KesoidUmisteniSouboruChangedEvent(final KesoidUmisteniSouboru umisteniSouboru) {
		super();
		this.umisteniSouboru = umisteniSouboru;
	}

	/**
	 * @return the umisteniSouboru
	 */
	public KesoidUmisteniSouboru getUmisteniSouboru() {
		return umisteniSouboru;
	}

}
