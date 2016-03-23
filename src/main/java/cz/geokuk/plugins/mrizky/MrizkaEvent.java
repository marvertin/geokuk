package cz.geokuk.plugins.mrizky;

import cz.geokuk.framework.Event0;

public class MrizkaEvent extends Event0<MrizkaModel> {
	public final boolean onoff;

	MrizkaEvent(final String kteraMrizka, final boolean onoff) {
		subType = kteraMrizka;
		this.onoff = onoff;
	}

	@Override
	public String toString() {
		return "MrizkaEvent [kteraMrizka=" + subType + ", onoff=" + onoff + "]";
	}

}
