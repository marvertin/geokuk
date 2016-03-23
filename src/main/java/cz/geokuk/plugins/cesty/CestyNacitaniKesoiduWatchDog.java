/**
 *
 */
package cz.geokuk.plugins.cesty;

import cz.geokuk.plugins.kesoid.mvc.KeskyNactenyEvent;

/**
 * @author veverka
 *
 */
public class CestyNacitaniKesoiduWatchDog {

	private CestyModel cestyModel;

	public void inject(final CestyModel cestyModel) {
		this.cestyModel = cestyModel;
	}

	public void onEvent(final KeskyNactenyEvent aEvent) {
		cestyModel.znovuVsechnoPripni();
	}

}
