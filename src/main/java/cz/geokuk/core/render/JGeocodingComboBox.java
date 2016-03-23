/**
 *
 */
package cz.geokuk.core.render;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.util.*;

import javax.swing.JComboBox;

/**
 * @author veverka
 *
 */
public class JGeocodingComboBox extends JComboBox<String> {

	public static interface Listener {
		public void patternChanged(RenderSettings.Patterned patterned);
	}

	private static final long serialVersionUID = 8614892946049285548L;

	public static final SortedMap<String, String> PRAZDNE_GEOTAGGINGG_PATTERNS = new TreeMap<>();

	private RenderSettings.Patterned patterned = new RenderSettings.Patterned();
	private final SortedMap<String, String> allPatterns = new TreeMap<>();
	private final SortedMap<String, String> souradnicovePatterns = new TreeMap<>();
	private final SortedMap<String, String> geotaggingPatterns = new TreeMap<>();

	private final List<String> keys = new ArrayList<>();

	private final List<Listener> listeners = new ArrayList<>();

	private boolean blokujEventy;

	private String implicitnGeotaggingKey;

	/**
	 *
	 */
	public JGeocodingComboBox() {
		setEditable(true);
		registerEvents();
	}

	public void addListener(final Listener listener) {
		listeners.add(listener);
	}

	/**
	 * Přidá geotaging vzory, ty jsou asynchronně a později
	 */
	public void addPatterns(final SortedMap<String, String> pattsSouradnice, final SortedMap<String, String> pattsGeocoding) {
		allPatterns.clear();
		if (pattsSouradnice != null) {
			souradnicovePatterns.clear();
			souradnicovePatterns.putAll(pattsSouradnice);
		}
		allPatterns.putAll(souradnicovePatterns);

		if (pattsGeocoding != null) {
			geotaggingPatterns.clear();
			geotaggingPatterns.putAll(pattsGeocoding);
		}
		allPatterns.putAll(geotaggingPatterns);

		keys.clear();
		keys.addAll(allPatterns.keySet());
		vyskladejCheckBox();
	}

	@Override
	public Dimension getMaximumSize() {
		return new Dimension(super.getMaximumSize().width, getPreferredSize().height);
	}

	/**
	 * @return the patterned
	 */
	public RenderSettings.Patterned getPatterned() {
		return patterned.copy();
	}

	/**
	 * @param patterned
	 *            the patterned to set
	 */
	public void setPatterned(final RenderSettings.Patterned patterned) {
		if (patterned.equals(this.patterned)) {
			return;
		}
		this.patterned = patterned.copy();
		if (getSelectedIndex() >= 0) { // něco bylo vybráno, ne text, tak to novu vybrat, ale ponovu
			final int index = urciCoMabytVybrano();
			setSelectedIndex(index);
		}
		for (final Listener listener : listeners) {
			listener.patternChanged(patterned);
		}
	}

	private void nastavPatterned() {
		if (blokujEventy) {
			return;
		}
		final RenderSettings.Patterned p = patterned.copy();
		final int index = getSelectedIndex();
		// aby tam vůbec vešel
		if (index >= 0) {
			final String key = keys.get(index);
			if (!geotaggingPatterns.isEmpty()) { // mame data geotaggingu
				p.setPatternNumberCilovy(key);
				if (souradnicovePatterns.containsKey(key)) {
					p.setPatternNumberPredbezny(key);
				}
			} else { // mame jen zakladni data
				p.setPatternNumberPredbezny(key);
				if (p.getPatternNumberCilovy() != null && souradnicovePatterns.containsKey(p.getPatternNumberCilovy())) {
					p.setPatternNumberCilovy(key);
				}
			}
		}
		p.setText((String) getSelectedItem());
		setPatterned(p);
	}

	/**
	 *
	 */
	private void registerEvents() {
		addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				nastavPatterned();
			}
		});

	}

	private int urciCoMabytVybrano() {
		final String klic = urciVybranyKlic();
		return keys.indexOf(klic);
	}

	private String urciVybranyKlic() {
		if (allPatterns.size() == 0) {
			return null;
		}
		if (patterned.getPatternNumberCilovy() != null && allPatterns.containsKey(patterned.getPatternNumberCilovy())) {
			return patterned.getPatternNumberCilovy();
		}
		if (patterned.getPatternNumberCilovy() == null && implicitnGeotaggingKey != null) {
			return implicitnGeotaggingKey;
		}
		if (patterned.getPatternNumberPredbezny() != null && allPatterns.containsKey(patterned.getPatternNumberPredbezny())) {
			return patterned.getPatternNumberPredbezny();
		}
		return allPatterns.firstKey();
	}

	/**
	 *
	 */
	private void vyskladejCheckBox() {
		final int selectedIndex = getSelectedIndex();
		blokujEventy = true;
		final int puvodniPocetItemu = getItemCount();
		for (final String s : allPatterns.values()) {
			addItem(s.trim());
		}
		for (int i = 0; i < puvodniPocetItemu; i++) {
			removeItemAt(0);
		}
		blokujEventy = false;
		if (selectedIndex >= 0 || puvodniPocetItemu == 0) { // něco bylo vybráno, ne text, tak to novu vybrat, ale ponovu
			final int index = urciCoMabytVybrano();
			setSelectedIndex(-1);
			setSelectedIndex(index);
		}
	}

}
