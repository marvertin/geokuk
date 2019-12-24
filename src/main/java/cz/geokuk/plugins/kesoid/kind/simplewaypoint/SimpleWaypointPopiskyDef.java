package cz.geokuk.plugins.kesoid.kind.simplewaypoint;

import cz.geokuk.plugins.kesoid.kind.PopiskyDefBuilder0;
import cz.geokuk.plugins.kesoidpopisky.PopiskyPatterns;


public class SimpleWaypointPopiskyDef extends PopiskyDefBuilder0 {

	@Override
	public void init() {
		label = "Jednoduchý waypoint";
		defaultPattern = new PopiskyPatterns().getSimplewaypointPattern(); // TODO nepoužije se, ale aŤ se ví, co refactorovat
	}


}
