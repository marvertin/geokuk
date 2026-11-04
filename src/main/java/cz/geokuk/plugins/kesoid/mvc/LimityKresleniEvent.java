package cz.geokuk.plugins.kesoid.mvc;

import cz.geokuk.framework.Event0;
import cz.geokuk.plugins.kesoid.LimityKresleni;

public class LimityKresleniEvent extends Event0<KesoidModel> {
	private final LimityKresleni limity;

	public LimityKresleniEvent(final LimityKresleni limity) {
		this.limity = limity;
	}

	public LimityKresleni getLimity() {
		return limity;
	}
}
