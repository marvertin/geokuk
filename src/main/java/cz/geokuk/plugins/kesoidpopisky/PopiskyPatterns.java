/**
 *
 */
package cz.geokuk.plugins.kesoidpopisky;

import java.util.HashMap;
import java.util.Map;

import cz.geokuk.framework.Copyable;
import cz.geokuk.framework.Preferenceble;
import cz.geokuk.plugins.kesoid.Kepodr;
import cz.geokuk.plugins.kesoid.kind.cgp.CgpPlugin;
import cz.geokuk.plugins.kesoid.kind.kes.KesPlugin;
import cz.geokuk.plugins.kesoid.kind.munzee.MunzeePlugin;
import cz.geokuk.plugins.kesoid.kind.photo.PhotoPlugin;
import cz.geokuk.plugins.kesoid.kind.simplewaypoint.SimpleWaypointPlugin;
import cz.geokuk.plugins.kesoid.kind.waymark.WaymarkPlugin;
import lombok.Data;

/**
 * @author Martin Veverka
 *
 */
@Preferenceble
@Data
public class PopiskyPatterns implements Copyable<PopiskyPatterns> {
	// TODO předělat na mapu, nejdříve je ale nutné vyřšit načítání mapy z preferencable
	// a nebo ještě lépe vyřešit preferencable pro pluginy.
	private String kesPattern = "{info} - {nazev} ({wpt})";
	private  String kesAdWptPattern = "{info} - {nazev} ({wpt})";
	private  String waymarkPattern = "{nazev} ({wpt})";
	private  String cgpPattern = "{wpt}";
	private  String simplewaypointPattern = "{nazev} ({wpt})";
	private  String munzeePattern = "{nazev} ({wpt})";
	private  String photoPattern = "{wpt}";

	public Map<Kepodr, String> asMap() {
		final Map<Kepodr, String> map = new HashMap<>();
		map.put(KesPlugin.KES, kesPattern);
		map.put(KesPlugin.KESADWPT, kesAdWptPattern);
		map.put(WaymarkPlugin.WAYMARK, waymarkPattern);
		map.put(CgpPlugin.CGP, cgpPattern);
		map.put(SimpleWaypointPlugin.SIMPLEWAYPOINT, simplewaypointPattern);
		map.put(MunzeePlugin.MUNZEE, munzeePattern);
		map.put(PhotoPlugin.PHOTO, photoPattern);
		return map;
	}

	@Override
	public PopiskyPatterns copy() {
		try {
			return (PopiskyPatterns) super.clone();
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public void put(final Kepodr kepodr, final String text) {
		if (kepodr == KesPlugin.KES) {
			kesPattern = text;
		}
		if (kepodr == KesPlugin.KESADWPT) {
			kesAdWptPattern = text;
		}
		if (kepodr == WaymarkPlugin.WAYMARK) {
			waymarkPattern = text;
		}
		if (kepodr == CgpPlugin.CGP) {
			cgpPattern = text;
		}
		if (kepodr == SimpleWaypointPlugin.SIMPLEWAYPOINT) {
			simplewaypointPattern = text;
		}
		if (kepodr == MunzeePlugin.MUNZEE) {
			munzeePattern = text;
		}
		if (kepodr == PhotoPlugin.PHOTO) {
			photoPattern = text;
		}
	}



}
