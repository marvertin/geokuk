package cz.geokuk.plugins.kesoid.kind.kes;

import cz.geokuk.plugins.kesoidpopisky.PopiskyPatterns;


public class KesPopiskyDef extends KesPopiskyDef0 {

	@Override
	public void init() {
		super.init();

		label = "Geokeš";
		defaultPattern = new PopiskyPatterns().getKesPattern(); // TODO nepoužije se, ale aŤ se ví, co refactorovat

	}


}
