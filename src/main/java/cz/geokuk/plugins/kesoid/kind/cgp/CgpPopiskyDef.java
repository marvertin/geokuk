package cz.geokuk.plugins.kesoid.kind.cgp;

import cz.geokuk.plugins.kesoid.kind.PopiskyDefBuilder0;
import cz.geokuk.plugins.kesoidpopisky.PopiskyPatterns;


public class CgpPopiskyDef extends PopiskyDefBuilder0 {

	@Override
	public void init() {
		label = "Český geodetický bod";
		defaultPattern = new PopiskyPatterns().getCgpPattern(); // TODO nepoužije se, ale aŤ se ví, co refactorovat

		def("{puvodnipotvora}", (sb, wpt) -> sb.append(wpt.getKesoid().getIdentifier()));

	}


}
