/**
 *
 */
package cz.geokuk.core.render;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JComboBox;

/**
 * @author veverka
 *
 */
public class JGeocodingComboBox extends JComboBox<String> {

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
	/**
	 * @param patterned the patterned to set
	 */
	public void setPatterned(RenderSettings.Patterned patterned) {
		if (patterned.equals(this.patterned)) return;
		this.patterned = patterned.copy();
		if (getSelectedIndex() >= 0) { // něco bylo vybráno, ne text, tak to novu vybrat, ale ponovu
			int index = urciCoMabytVybrano();
			setSelectedIndex(index);
		}
		for (Listener listener : listeners) {
			listener.patternChanged(patterned);
		}
	}

	/**
	 * @return the patterned
	 */
	public RenderSettings.Patterned getPatterned() {
		return patterned.copy();
	}

	/**
	 * Přidá geotaging vzory, ty jsou asynchronně a později
	 */
	public void addPatterns(SortedMap<String, String> pattsSouradnice, SortedMap<String, String> pattsGeocoding) {
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


	/**
	 *
	 */
	private void vyskladejCheckBox() {
		int selectedIndex = getSelectedIndex();
		blokujEventy = true;
		int puvodniPocetItemu = getItemCount();
		for (String s : allPatterns.values()) {
			addItem(s.trim());
		}
		for (int i = 0; i < puvodniPocetItemu; i++) {
			removeItemAt(0);
		}
		blokujEventy = false;
		if (selectedIndex >= 0 || puvodniPocetItemu == 0) { // něco bylo vybráno, ne text, tak to novu vybrat, ale ponovu
			int index = urciCoMabytVybrano();
			setSelectedIndex(-1);
			setSelectedIndex(index);
		}
	}

	private int urciCoMabytVybrano() {
		String klic = urciVybranyKlic();
		return keys.indexOf(klic);
	}

	private String urciVybranyKlic() {
		if (allPatterns.size() == 0) return null;
		if (patterned.getPatternNumberCilovy() != null && allPatterns.containsKey(patterned.getPatternNumberCilovy())) return patterned.getPatternNumberCilovy();
		if (patterned.getPatternNumberCilovy() == null && implicitnGeotaggingKey != null) return implicitnGeotaggingKey;
		if (patterned.getPatternNumberPredbezny() != null && allPatterns.containsKey(patterned.getPatternNumberPredbezny())) return patterned.getPatternNumberPredbezny();
		return allPatterns.firstKey();
	}

	private void nastavPatterned() {
		if (blokujEventy) return;
		RenderSettings.Patterned p = patterned.copy();
		int index = getSelectedIndex();
		// aby tam vůbec vešel
		if (index >= 0) {
			String key = keys.get(index);
			if (! geotaggingPatterns.isEmpty()) {  // mame data geotaggingu
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
		addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					nastavPatterned();
				}
			}
		});

	}

	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	public static interface Listener {
		public void patternChanged(RenderSettings.Patterned patterned);
	}

	@Override
	public Dimension getMaximumSize() {
		return new Dimension(super.getMaximumSize().width, getPreferredSize().height);
	}

}
