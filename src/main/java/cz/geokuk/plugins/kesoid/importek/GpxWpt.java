/**
 *
 */
package cz.geokuk.plugins.kesoid.importek;

import cz.geokuk.core.coordinates.Wgs;

public class GpxWpt {
	public String desc;
	public String cmt;
	public String sym;
	public String time;
	public String name;
	public Wgs wgs;

	public Groundspeak groundspeak = null;
	public String type;

	public final GpxLink link = new GpxLink();
	public final Gpxg gpxg = new Gpxg();
	public double ele;
	public boolean explicitneUrcenoVlastnictvi;

	// Je to potřeba kvůli zjištění počtů z každého zdroje a přebíjení
	public InformaceOZdroji iInformaceOZdroji;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GpxWpt [name=" + name + ", sym=" + sym + ", type=" + type + ", wgs=" + wgs + ", cmt=" + cmt + ", desc=" + desc + ", time=" + time + ", link=" + link + ", groundspeak=" + groundspeak
				+ "]";
	}

	public String getEffectiveNazev() {
		String s;
		if (desc == null) {
			if (cmt == null) {
				s = "?";
			} else {
				s = cmt;
			}
		} else {
			if (cmt == null) {
				s = desc;
			} else {
				if (cmt.toLowerCase().contains(desc.toLowerCase())) {
					s = desc;
				} else {
					s = desc + ", " + cmt;
				}
			}
		}
		return s;
	}

	public int getEffectiveElevation() {
		if (ele != 0) {
			return (int) ele;
		} else {
			if (gpxg != null) {
				return gpxg.elevation;
			} else {
				return 0;
			}
		}
	}
}