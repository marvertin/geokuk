package cz.geokuk.plugins.kesoidkruhy;

import cz.geokuk.framework.Event0;

public class KruhyPreferencesChangeEvent extends Event0<KruhyModel> {
	public final KruhySettings kruhy;

	KruhyPreferencesChangeEvent(final KruhySettings kruhy) {
		this.kruhy = kruhy;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return kruhy.toString();
	}

}
