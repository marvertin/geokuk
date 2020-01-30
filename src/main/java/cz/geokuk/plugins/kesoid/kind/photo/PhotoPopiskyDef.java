package cz.geokuk.plugins.kesoid.kind.photo;

import cz.geokuk.plugins.kesoid.kind.PopiskyDefBuilder0;


public class PhotoPopiskyDef extends PopiskyDefBuilder0 {

	@Override
	public void init() {
		label = "Fotka";
		defaultPattern = "{wpt}";
	}


}
