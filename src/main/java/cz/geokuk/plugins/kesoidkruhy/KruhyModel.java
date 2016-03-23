package cz.geokuk.plugins.kesoidkruhy;


import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.Onoff;
import cz.geokuk.framework.PodkladMapSpecificModel0;

public class KruhyModel extends PodkladMapSpecificModel0<KruhyModel, KruhySettings> {


	/* (non-Javadoc)
	 * @see cz.geokuk.kes.popisky.PodkladMapSpecificModel0#createDefaults()
	 */
	@Override
	protected KruhySettings createDefaults() {
		return new KruhySettings();
	}

	/* (non-Javadoc)
	 * @see cz.geokuk.kes.popisky.PodkladMapSpecificModel0#createEvent(java.lang.Object)
	 */
	@Override
	protected KruhyPreferencesChangeEvent createEvent(KruhySettings structure) {
		return new KruhyPreferencesChangeEvent(structure);
	}

	/* (non-Javadoc)
	 * @see cz.geokuk.kes.popisky.PodkladMapSpecificModel0#visiblexxx()
	 */
	@Override
	protected Onoff<KruhyModel> visiblexxx() {
		return visible;
	}

	/* (non-Javadoc)
	 * @see cz.geokuk.kes.popisky.PodkladMapSpecificModel0#preferenceNodeName()
	 */
	@Override
	protected String preferenceNodeName() {
		return FPref.ZVYRAZNOVACI_KRUHY_node;
	}


	/* (non-Javadoc)
	 * @see cz.geokuk.framework.PodkladMapSpecificModel0#putVisibleToPreferences(boolean)
	 */
	@Override
	protected void putVisibleToPreferences(boolean onoff) {
	}

	/* (non-Javadoc)
	 * @see cz.geokuk.framework.PodkladMapSpecificModel0#getVisibleFromPreferences(boolean)
	 */
	@Override
	protected boolean getVisibleFromPreferences(boolean defaultOnoff) {
		return false;
	}



	/* (non-Javadoc)
	 * @see cz.geokuk.framework.PodkladMapSpecificModel0#reloadPreferences()
	 */
	@Override
	protected void reloadPreferences() {
		super.reloadPreferences();
	}
}
