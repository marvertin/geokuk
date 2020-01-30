package cz.geokuk.plugins.kesoidpopisky;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.*;
import cz.geokuk.plugins.kesoid.kind.KesoidPluginManager;
import cz.geokuk.plugins.mapy.kachle.data.EKaType;
import lombok.SneakyThrows;

public class PopiskyModel extends PodkladMapSpecificModel0<PopiskyModel, PopiskySettings> {

	private KesoidPluginManager kpm;
	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.kes.popisky.PodkladMapSpecificModel0#createDefaults()
	 */
	@Override
	protected PopiskySettings createDefaults() {
//		final Map<Kepodr, String> result =;
		migrace(); // Provede migraci jmen starých popisků na nové názvy hodnot dle Kepodr. Migrace je jednorázová při spuštění této verze.
		return new PopiskySettings(kpm.getPopisekDefMap().entrySet().stream()
				.collect(Collectors.toMap(Entry::getKey, e -> e.getValue().getDefaultPattern())));
	}

	@SneakyThrows
	private void migrace() {
		for (final EKaType katyp : EKaType.values()) {
			final MyPreferences nodeKesoidPopisky = currPrefe().node(FPref.KESOID_POPISKY_node);
			if (nodeKesoidPopisky.nodeExists(jmenoPodkladu(katyp))) {
				final MyPreferences nodeKa = nodeKesoidPopisky.node(jmenoPodkladu(katyp));
				if (! nodeKa.nodeExists("patterns2")) {
					if (nodeKa.nodeExists("patterns")) {
						final Map<String, String> starePopiskyPatterns = nodeKa.getStructure("patterns", new MigracePopiskyPatterns()).asMap();
						System.out.println("Migrace popisky/patterns: " + starePopiskyPatterns);
						nodeKa.putMap("patterns2", starePopiskyPatterns);
					}
				}
			}
		}
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


	public void inject(final KesoidPluginManager kpm) {
		this.kpm = kpm;
	}

}
