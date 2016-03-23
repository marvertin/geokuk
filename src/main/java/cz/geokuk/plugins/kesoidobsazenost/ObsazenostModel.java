package cz.geokuk.plugins.kesoidobsazenost;

import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.*;

public class ObsazenostModel extends PodkladMapSpecificModel0<ObsazenostModel, ObsazenostSettings> {

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.kes.popisky.PodkladMapSpecificModel0#createDefaults()
	 */
	@Override
	protected ObsazenostSettings createDefaults() {
		return new ObsazenostSettings();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.kes.popisky.PodkladMapSpecificModel0#createEvent(java.lang.Object)
	 */
	@Override
	protected ObsazenostPreferencesChangeEvent createEvent(final ObsazenostSettings structure) {
		return new ObsazenostPreferencesChangeEvent(structure);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.kes.popisky.PodkladMapSpecificModel0#visiblexxx()
	 */
	@Override
	protected Onoff<ObsazenostModel> visiblexxx() {
		return visible;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.kes.popisky.PodkladMapSpecificModel0#getOnoffEventClass()
	 */
	@Override
	protected Class<? extends OnoffEvent0<ObsazenostModel>> getOnoffEventClass() {
		return ObsazenostOnoffEvent.class;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.kes.popisky.PodkladMapSpecificModel0#preferenceNodeName()
	 */
	@Override
	protected String preferenceNodeName() {
		return FPref.KESOID_OBSAZENOST_node;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.framework.PodkladMapSpecificModel0#putVisibleToPreferences(boolean)
	 */
	@Override
	protected void putVisibleToPreferences(final boolean onoff) {
		currPrefe().node(FPref.MRIZKA_node).putBoolean(FPref.ZOBRAZ_OBSAZENOST_value, onoff);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.framework.PodkladMapSpecificModel0#getVisibleFromPreferences(boolean)
	 */
	@Override
	protected boolean getVisibleFromPreferences(final boolean defaultOnoff) {
		return currPrefe().node(FPref.MRIZKA_node).getBoolean(FPref.ZOBRAZ_OBSAZENOST_value, defaultOnoff);
	}

}
