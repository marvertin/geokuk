package cz.geokuk.plugins.kesoid.kind.waymark;

import cz.geokuk.plugins.kesoid.EWptStatus;
import cz.geokuk.plugins.kesoid.EWptVztah;
import cz.geokuk.plugins.kesoid.importek.GpxWpt;
import cz.geokuk.plugins.kesoid.kind.Wpt0;
import cz.geokuk.plugins.kesoid.mvc.GccomNick;

public class Waym extends Wpt0 {
	private static final String GEOCACHE_FOUND = "Geocache Found";

	GpxWpt gpxwpt;
	GccomNick gccomnick;

	void fill(final Wpt0 wpt) {
		//wpt.setKepodr(kepodr);
		wpt.setWgs(gpxwpt.wgs);
		wpt.setElevation(urciElevation());
		wpt.setIdentifier(gpxwpt.name);
		wpt.setNazev(vytvorNazev());
		wpt.setStatus(urciStatus());
		wpt.setVztah(urciVztah());

		//wpt.setStatus(urciStatus(gpxwpt.));
	}

	private EWptVztah urciVztah() {
		if (gpxwpt.groundspeak != null) {

			if (gpxwpt.explicitneUrcenoVlastnictvi) {
				return EWptVztah.OWN;
			}
			if (gccomnick.name.equals(gpxwpt.groundspeak.owner)) {
				return EWptVztah.OWN;
			}
			if (gccomnick.id == gpxwpt.groundspeak.ownerid) {
				return EWptVztah.OWN;
			}
			if (gccomnick.name.equals(gpxwpt.groundspeak.placedBy)) {
				return EWptVztah.OWN;
			} else if (GEOCACHE_FOUND.equals(gpxwpt.sym)) {
				return EWptVztah.FOUND;
			} else {
				return EWptVztah.NORMAL;
			}
		} else {
			return EWptVztah.NORMAL;
		}

	}


	private EWptStatus urciStatus() {
		if (gpxwpt.groundspeak != null) {
			if (gpxwpt.groundspeak.archived) {
				return EWptStatus.ARCHIVED;
			} else if (!gpxwpt.groundspeak.archived) {
				return EWptStatus.DISABLED;
			} else {
				return EWptStatus.ACTIVE;
			}
		} else {
			return EWptStatus.ACTIVE;
		}
	}

	private int urciElevation() {
		if (gpxwpt.ele != 0) {
			return (int) gpxwpt.ele;
		} else {
			if (gpxwpt.gpxg != null) {
				return gpxwpt.gpxg.elevation;
			} else {
				return 0;
			}
		}
	}

	private String vytvorNazev() {
		String s;
		if (gpxwpt.desc == null) {
			if (gpxwpt.cmt == null) {
				s = "?";
			} else {
				s = gpxwpt.cmt;
			}
		} else {
			if (gpxwpt.cmt == null) {
				s = gpxwpt.desc;
			} else {
				if (gpxwpt.cmt.toLowerCase().contains(gpxwpt.desc.toLowerCase())) {
					s = gpxwpt.desc;
				} else {
					s = gpxwpt.desc + ", " + gpxwpt.cmt;
				}
			}
		}
		return s;
	}
}
