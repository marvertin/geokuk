/**
 *
 */
package cz.geokuk.plugins.kesoidpopisky;

import java.util.HashMap;
import java.util.Map;

import cz.geokuk.framework.Preferenceble;
import lombok.Data;

/**
 * Jen kvůli migraci, jinak nepoužívat.
 * @author Martin Veverka
 *
 */
@Preferenceble
@Data
public class MigracePopiskyPatterns {
	private String kesPattern = "{info} - {nazev} ({wpt})";
	private  String kesAdWptPattern = "{info} - {nazev} ({wpt})";
	private  String waymarkPattern = "{nazev} ({wpt})";
	private  String cgpPattern = "{wpt}";
	private  String simplewaypointPattern = "{nazev} ({wpt})";
	private  String munzeePattern = "{nazev} ({wpt})";
	private  String photoPattern = "{wpt}";

	public Map<String, String> asMap() {
		final Map<String, String> map = new HashMap<>();
		map.put("kes", kesPattern);
		map.put("kesadwpt", kesAdWptPattern);
		map.put("waymark", waymarkPattern);
		map.put("cgp", cgpPattern);
		map.put("simplewaypoint", simplewaypointPattern);
		map.put("munzee", munzeePattern);
		map.put("photo", photoPattern);
		return map;
	}

}
