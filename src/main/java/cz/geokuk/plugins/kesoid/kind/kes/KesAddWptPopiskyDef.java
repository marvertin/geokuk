package cz.geokuk.plugins.kesoid.kind.kes;

import cz.geokuk.plugins.kesoidpopisky.PopiskyPatterns;


public class KesAddWptPopiskyDef extends KesPopiskyDef0 {

	@Override
	public void init() {
		super.init();

		label = "Geokeš additional waypoint";
		defaultPattern = new PopiskyPatterns().getKesAdWptPattern(); // TODO nepoužije se, ale aŤ se ví, co refactorovat

	}


}
