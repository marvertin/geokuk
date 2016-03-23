/**
 *
 */
package cz.geokuk.plugins.kesoidpopisky;

import java.util.EnumMap;

import cz.geokuk.framework.Copyable;
import cz.geokuk.framework.Preferenceble;
import cz.geokuk.plugins.kesoid.data.EKesoidKind;

/**
 * @author veverka
 *
 */
@Preferenceble
public class PopiskyPatterns implements Copyable<PopiskyPatterns> {

	private String kesPattern = "{info} - {nazev} ({wpt})";
	private String waymarkPattern = "{nazev} ({wpt})";
	private String cgpPattern = "{wpt}";
	private String simplewaypointPattern = "{nazev} ({wpt})";
	private final String munzeePattern = "{nazev} ({wpt})";
	private final String photoPattern = "{wpt}";

	public EnumMap<EKesoidKind, String> asMap() {
		final EnumMap<EKesoidKind, String> map = new EnumMap<>(EKesoidKind.class);
		map.put(EKesoidKind.KES, kesPattern);
		map.put(EKesoidKind.WAYMARK, waymarkPattern);
		map.put(EKesoidKind.CGP, cgpPattern);
		map.put(EKesoidKind.SIMPLEWAYPOINT, simplewaypointPattern);
		map.put(EKesoidKind.MUNZEE, munzeePattern);
		map.put(EKesoidKind.PHOTO, photoPattern);
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

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final PopiskyPatterns other = (PopiskyPatterns) obj;
		if (cgpPattern == null) {
			if (other.cgpPattern != null) {
				return false;
			}
		} else if (!cgpPattern.equals(other.cgpPattern)) {
			return false;
		}
		if (kesPattern == null) {
			if (other.kesPattern != null) {
				return false;
			}
		} else if (!kesPattern.equals(other.kesPattern)) {
			return false;
		}
		if (simplewaypointPattern == null) {
			if (other.simplewaypointPattern != null) {
				return false;
			}
		} else if (!simplewaypointPattern.equals(other.simplewaypointPattern)) {
			return false;
		}
		if (waymarkPattern == null) {
			if (other.waymarkPattern != null) {
				return false;
			}
		} else if (!waymarkPattern.equals(other.waymarkPattern)) {
			return false;
		}
		return true;
	}

	/**
	 * @return the cgpPattern
	 */
	public String getCgpPattern() {
		return cgpPattern;
	}

	/**
	 * @return the kesPattern
	 */
	public String getKesPattern() {
		return kesPattern;
	}

	/**
	 * @return the simplewaypointPattern
	 */
	public String getSimplewaypointPattern() {
		return simplewaypointPattern;
	}

	/**
	 * @return the waymarkPattern
	 */
	public String getWaymarkPattern() {
		return waymarkPattern;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (cgpPattern == null ? 0 : cgpPattern.hashCode());
		result = prime * result + (kesPattern == null ? 0 : kesPattern.hashCode());
		result = prime * result + (simplewaypointPattern == null ? 0 : simplewaypointPattern.hashCode());
		result = prime * result + (waymarkPattern == null ? 0 : waymarkPattern.hashCode());
		return result;
	}

	/**
	 * @param cgpPattern
	 *            the cgpPattern to set
	 */
	public void setCgpPattern(final String cgpPattern) {
		this.cgpPattern = cgpPattern;
	}

	/**
	 * @param kesPattern
	 *            the kesPattern to set
	 */
	public void setKesPattern(final String kesPattern) {
		this.kesPattern = kesPattern;
	}

	/**
	 * @param simplewaypointPattern
	 *            the simplewaypointPattern to set
	 */
	public void setSimplewaypointPattern(final String simplewaypointPattern) {
		this.simplewaypointPattern = simplewaypointPattern;
	}

	/**
	 * @param waymarkPattern
	 *            the waymarkPattern to set
	 */
	public void setWaymarkPattern(final String waymarkPattern) {
		this.waymarkPattern = waymarkPattern;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "PopiskyPatterns [kesPattern=" + kesPattern + ", waymarkPattern=" + waymarkPattern + ", cgpPattern=" + cgpPattern + ", simplewaypointPattern=" + simplewaypointPattern + "]";
	}

}
