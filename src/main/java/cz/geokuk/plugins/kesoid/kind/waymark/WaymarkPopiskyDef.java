package cz.geokuk.plugins.kesoid.kind.waymark;

import cz.geokuk.plugins.kesoid.kind.PopiskyDefBuilder0;
import cz.geokuk.plugins.kesoidpopisky.PopiskyPatterns;


public class WaymarkPopiskyDef extends PopiskyDefBuilder0 {

	@Override
	public void init() {
		label = "Waymark";
		defaultPattern = new PopiskyPatterns().getWaymarkPattern(); // TODO nepoužije se, ale aŤ se ví, co refactorovat
	}


}
