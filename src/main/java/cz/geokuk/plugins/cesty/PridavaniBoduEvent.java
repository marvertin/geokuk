package cz.geokuk.plugins.cesty;

import cz.geokuk.framework.Event0;

public class PridavaniBoduEvent extends Event0<CestyModel> {

	public final boolean probihaPridavani;


	public PridavaniBoduEvent(boolean probihaPridavani) {
		this.probihaPridavani = probihaPridavani;
	}


}
