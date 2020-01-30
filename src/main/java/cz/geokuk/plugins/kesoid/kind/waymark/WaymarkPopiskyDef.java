package cz.geokuk.plugins.kesoid.kind.waymark;

import cz.geokuk.plugins.kesoid.kind.PopiskyDefBuilder0;


public class WaymarkPopiskyDef extends PopiskyDefBuilder0 {

	@Override
	public void init() {
		label = "Waymark";
		defaultPattern = "{nazev} ({wpt})";
	}


}
