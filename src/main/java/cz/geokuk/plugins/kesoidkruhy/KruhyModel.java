package cz.geokuk.plugins.kesoidkruhy;

import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.Onoff;
import cz.geokuk.framework.PodkladMapSpecificModel0;
import cz.geokuk.plugins.mapy.kachle.data.EKaType;

/** Zapnutí kruhů a jednotková velikost platí pro všechny podklady map, barva a velikost se pamatují podle podkladu. */
public class KruhyModel extends PodkladMapSpecificModel0<KruhyModel, KruhySettings> {

	static final String ZAPNUTO_value = "zapnuto";
	static final String JEDNOTKOVE_value = "jednotkove";

	/** Data se právě načítají z nastavení podkladu, zapnutí se převezme ze společné volby. */
	private boolean nacitani;
	private EKaType podklad;

	@Override
	public void setPodkladMap(final EKaType podklad) {
		this.podklad = podklad;
		nacitani = true;
		try {
			super.setPodkladMap(podklad);
		} finally {
			nacitani = false;
		}
	}

	@Override
	public void setData(final KruhySettings data) {
		final KruhySettings d = data.copy();
		if (!nacitani) {
			ulozSpolecne(d);
		} else if (prefNode.get(ZAPNUTO_value, null) != null) {
			d.setOnoff(prefNode.getBoolean(ZAPNUTO_value, false));
			d.setJednotkovaVelikost(prefNode.getBoolean(JEDNOTKOVE_value, false));
		} else if (podklad != null) {
			// Společná volba se poprvé převezme z podkladu, na kterém uživatel naposledy byl.
			ulozSpolecne(d);
		}
		super.setData(d);
	}

	private void ulozSpolecne(final KruhySettings d) {
		prefNode.putBoolean(ZAPNUTO_value, d.isOnoff());
		prefNode.putBoolean(JEDNOTKOVE_value, d.isJednotkovaVelikost());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.kes.popisky.PodkladMapSpecificModel0#createDefaults()
	 */
	@Override
	protected KruhySettings createDefaults() {
		return new KruhySettings();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.kes.popisky.PodkladMapSpecificModel0#createEvent(java.lang.Object)
	 */
	@Override
	protected KruhyPreferencesChangeEvent createEvent(final KruhySettings structure) {
		return new KruhyPreferencesChangeEvent(structure);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.framework.PodkladMapSpecificModel0#getVisibleFromPreferences(boolean)
	 */
	@Override
	protected boolean getVisibleFromPreferences(final boolean defaultOnoff) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.kes.popisky.PodkladMapSpecificModel0#preferenceNodeName()
	 */
	@Override
	protected String preferenceNodeName() {
		return FPref.ZVYRAZNOVACI_KRUHY_node;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.framework.PodkladMapSpecificModel0#putVisibleToPreferences(boolean)
	 */
	@Override
	protected void putVisibleToPreferences(final boolean onoff) {}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.framework.PodkladMapSpecificModel0#reloadPreferences()
	 */
	@Override
	protected void reloadPreferences() {
		nacitani = true;
		try {
			super.reloadPreferences();
		} finally {
			nacitani = false;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.kes.popisky.PodkladMapSpecificModel0#visiblexxx()
	 */
	@Override
	protected Onoff<KruhyModel> visiblexxx() {
		return visible;
	}
}
