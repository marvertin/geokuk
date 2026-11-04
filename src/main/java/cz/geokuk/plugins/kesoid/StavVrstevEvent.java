package cz.geokuk.plugins.kesoid;

import cz.geokuk.framework.Event0;

/** Změnilo se, které zapnuté vrstvy kruhů se nekreslí. */
public class StavVrstevEvent extends Event0<StavVrstevModel> {

	private final StavVrstev stav;

	public StavVrstevEvent(final StavVrstev stav) {
		this.stav = stav;
	}

	public StavVrstev getStav() {
		return stav;
	}

	@Override
	public String toString() {
		return stav.toString();
	}
}
