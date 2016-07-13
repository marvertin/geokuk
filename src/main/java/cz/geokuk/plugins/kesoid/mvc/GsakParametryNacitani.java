package cz.geokuk.plugins.kesoid.mvc;

import java.util.*;

public class GsakParametryNacitani {
	private Set<String> casNalezu = Collections.emptySet();
	private Set<String> casNenalezu = Collections.emptySet();
	private boolean nacistVsechnyDatabaze = false;

	public Set<String> getCasNalezu() {
		return casNalezu;
	}

	public void setCasNalezu(final Collection<String> aCasNalezu) {
		this.casNalezu = Collections.unmodifiableSet(new LinkedHashSet<>(aCasNalezu));
	}

	public Set<String> getCasNenalezu() {
		return casNenalezu;
	}

	public void setCasNenalezu(final Collection<String> aCasNenalezu) {
		this.casNenalezu = Collections.unmodifiableSet(new LinkedHashSet<>(aCasNenalezu));
	}

	public boolean isNacistVsechnyDatabaze() {
		return nacistVsechnyDatabaze;
	}

	public void setNacistVsechnyDatabaze(final boolean aNacistVsechnyDatabaze) {
		this.nacistVsechnyDatabaze = aNacistVsechnyDatabaze;
	}

	// ------------------------------------------------------------------------------------------------
	@Override
	public String toString() {
		return "GsakParametryNacitani [casNalezu=" + casNalezu + ", casNenalezu=" + casNenalezu + ", nacistVsechnyDatabaze=" + nacistVsechnyDatabaze + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (casNalezu == null ? 0 : casNalezu.hashCode());
		result = prime * result + (casNenalezu == null ? 0 : casNenalezu.hashCode());
		result = prime * result + (nacistVsechnyDatabaze ? 1231 : 1237);
		return result;
	}

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
		final GsakParametryNacitani other = (GsakParametryNacitani) obj;
		if (casNalezu == null) {
			if (other.casNalezu != null) {
				return false;
			}
		} else if (!casNalezu.equals(other.casNalezu)) {
			return false;
		}
		if (casNenalezu == null) {
			if (other.casNenalezu != null) {
				return false;
			}
		} else if (!casNenalezu.equals(other.casNenalezu)) {
			return false;
		}
		if (nacistVsechnyDatabaze != other.nacistVsechnyDatabaze) {
			return false;
		}
		return true;
	}
}
