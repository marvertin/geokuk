package cz.geokuk.plugins.kesoid.kind.cgp;

import cz.geokuk.plugins.kesoid.kind.PopiskyDefBuilder0;


public class CgpPopiskyDef extends PopiskyDefBuilder0 {

	@Override
	public void init() {
		label = "Český geodetický bod";
		defaultPattern = "{wpt}";

		def("{puvodnipotvora}", (sb, wpt) -> sb.append(wpt.getKesoid().getIdentifier()));

	}


}
