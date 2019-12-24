package cz.geokuk.plugins.kesoid.kind.munzee;

import cz.geokuk.plugins.kesoid.kind.PopiskyDefBuilder0;
import cz.geokuk.plugins.kesoidpopisky.PopiskyPatterns;


public class MunzeePopiskyDef extends PopiskyDefBuilder0 {

	@Override
	public void init() {
		label = "Munzee";
		defaultPattern = new PopiskyPatterns().getMunzeePattern(); // TODO nepoužije se, ale aŤ se ví, co refactorovat
	}


}
