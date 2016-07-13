package cz.geokuk.plugins.kesoid.mvc;

import cz.geokuk.framework.Event0;

/**
 * @author Bohuslav Roztočil
 *
 */
public class GsakParametryNacitaniChangedEvent extends Event0<KesoidModel> {

	private final GsakParametryNacitani iGsakParametryNacitani;

	public GsakParametryNacitaniChangedEvent(final GsakParametryNacitani aGsakParametryNacitani) {
		super();
		iGsakParametryNacitani = aGsakParametryNacitani;
	}

	public GsakParametryNacitani getGsakParametryNacitani() {
		return iGsakParametryNacitani;
	}

}
