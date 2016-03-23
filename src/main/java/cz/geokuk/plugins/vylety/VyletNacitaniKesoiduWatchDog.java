/**
 *
 */
package cz.geokuk.plugins.vylety;

import cz.geokuk.plugins.kesoid.mvc.KeskyNactenyEvent;

/**
 * @author Martin Veverka
 *
 */
public class VyletNacitaniKesoiduWatchDog {

	private VyletModel vyletModel;

	public void inject(final VyletModel vyletModel) {
		this.vyletModel = vyletModel;
	}

	public void onEvent(final KeskyNactenyEvent aEvent) {
		vyletModel.startLoadingVylet(aEvent.getVsechny());
	}

}
