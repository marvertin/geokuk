package cz.geokuk.plugins.kesoidpopisky;


import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.Onoff;
import cz.geokuk.framework.OnoffEvent0;
import cz.geokuk.framework.PodkladMapSpecificModel0;

public class PopiskyModel extends PodkladMapSpecificModel0<PopiskyModel, PopiskySettings> {

	/* (non-Javadoc)
	 * @see cz.geokuk.kes.popisky.PodkladMapSpecificModel0#createDefaults()
	 */
	@Override
	protected PopiskySettings createDefaults() {
		return new PopiskySettings();
	}

	/* (non-Javadoc)
	 * @see cz.geokuk.kes.popisky.PodkladMapSpecificModel0#createEvent(java.lang.Object)
	 */
	@Override
	protected PopiskyPreferencesChangeEvent createEvent(PopiskySettings structure) {
		return new PopiskyPreferencesChangeEvent(structure);
	}

	/* (non-Javadoc)
	 * @see cz.geokuk.kes.popisky.PodkladMapSpecificModel0#visiblexxx()
	 */
	@Override
	protected Onoff<PopiskyModel> visiblexxx() {
		return visible;
	}

	/* (non-Javadoc)
	 * @see cz.geokuk.kes.popisky.PodkladMapSpecificModel0#getOnoffEventClass()
	 */
	@Override
	protected Class<? extends OnoffEvent0<PopiskyModel>> getOnoffEventClass() {
		return PopiskyOnoffEvent.class;
	}

	/* (non-Javadoc)
	 * @see cz.geokuk.kes.popisky.PodkladMapSpecificModel0#preferenceNodeName()
	 */
	@Override
	protected String preferenceNodeName() {
		return FPref.KESOID_POPISKY_node;
	}

	/* (non-Javadoc)
	 * @see cz.geokuk.framework.PodkladMapSpecificModel0#putVisibleToPreferences(boolean)
	 */
	@Override
	protected void putVisibleToPreferences(boolean onoff) {
		currPrefe().node(FPref.VSEOBECNE_node).putBoolean(FPref.ZOBRAZ_POPISY_KESI_value, onoff);
	}

	/* (non-Javadoc)
	 * @see cz.geokuk.framework.PodkladMapSpecificModel0#getVisibleFromPreferences(boolean)
	 */
	@Override
	protected boolean getVisibleFromPreferences(boolean defaultOnoff) {
		return currPrefe().node(FPref.VSEOBECNE_node).getBoolean(FPref.ZOBRAZ_POPISY_KESI_value, defaultOnoff);
	}


}
