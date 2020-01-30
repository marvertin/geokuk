package cz.geokuk.plugins.kesoid.kind.munzee;

import cz.geokuk.plugins.kesoid.kind.PopiskyDefBuilder0;


public class MunzeePopiskyDef extends PopiskyDefBuilder0 {

	@Override
	public void init() {
		label = "Munzee";
		defaultPattern = "{nazev} ({wpt})";
	}


}
