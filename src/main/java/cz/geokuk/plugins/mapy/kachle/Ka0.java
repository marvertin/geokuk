package cz.geokuk.plugins.mapy.kachle;

abstract class Ka0 {

	private final KaLoc loc;

	public Ka0(final KaLoc loc) {
		this.loc = loc;
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
		final Ka0 other = (Ka0) obj;
		if (loc == null) {
			if (other.loc != null) {
				return false;
			}
		} else if (!loc.equals(other.loc)) {
			return false;
		}
		return true;
	}

	public KaLoc getLoc() {
		return loc;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (loc == null ? 0 : loc.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return loc.toString();
	}

	public abstract String typToString();
}
