package cz.geokuk.plugins.kesoid.kind.simplewaypoint;

import cz.geokuk.plugins.kesoid.kind.PopiskyDefBuilder0;


public class SimpleWaypointPopiskyDef extends PopiskyDefBuilder0 {

	@Override
	public void init() {
		label = "Jednoduchý waypoint";
		defaultPattern = "{nazev} ({wpt})";
	}


}
