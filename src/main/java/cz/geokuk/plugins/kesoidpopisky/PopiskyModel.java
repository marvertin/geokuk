package cz.geokuk.plugins.kesoidpopisky;

import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.*;

public class PopiskyModel extends PodkladMapSpecificModel0<PopiskyModel, PopiskySettings> {

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.kes.popisky.PodkladMapSpecificModel0#createDefaults()
	 */
	@Override
	protected PopiskySettings createDefaults() {
		return new PopiskySettings();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.kes.popisky.PodkladMapSpecificModel0#createEvent(java.lang.Object)
	 */
	@Override
	protected PopiskyPreferencesChangeEvent createEvent(final PopiskySettings structure) {
		return new PopiskyPreferencesChangeEvent(structure);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.kes.popisky.PodkladMapSpecificModel0#getOnoffEventClass()
	 */
	@Override
	protected Class<? extends OnoffEvent0<PopiskyModel>> getOnoffEventClass() {
		return PopiskyOnoffEvent.class;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.framework.PodkladMapSpecificModel0#getVisibleFromPreferences(boolean)
	 */
	@Override
	protected boolean getVisibleFromPreferences(final boolean defaultOnoff) {
		return currPrefe().node(FPref.VSEOBECNE_node).getBoolean(FPref.ZOBRAZ_POPISY_KESI_value, defaultOnoff);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.kes.popisky.PodkladMapSpecificModel0#preferenceNodeName()
	 */
	@Override
	protected String preferenceNodeName() {
		return FPref.KESOID_POPISKY_node;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.framework.PodkladMapSpecificModel0#putVisibleToPreferences(boolean)
	 */
	@Override
	protected void putVisibleToPreferences(final boolean onoff) {
		currPrefe().node(FPref.VSEOBECNE_node).putBoolean(FPref.ZOBRAZ_POPISY_KESI_value, onoff);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.kes.popisky.PodkladMapSpecificModel0#visiblexxx()
	 */
	@Override
	protected Onoff<PopiskyModel> visiblexxx() {
		return visible;
	}

}
