package cz.geokuk.plugins.kesoid.kind.photo;

import cz.geokuk.plugins.kesoid.kind.PopiskyDefBuilder0;
import cz.geokuk.plugins.kesoidpopisky.PopiskyPatterns;


public class PhotoPopiskyDef extends PopiskyDefBuilder0 {

	@Override
	public void init() {
		label = "Fotka";
		defaultPattern = new PopiskyPatterns().getPhotoPattern(); // TODO nepoužije se, ale aŤ se ví, co refactorovat
	}


}
