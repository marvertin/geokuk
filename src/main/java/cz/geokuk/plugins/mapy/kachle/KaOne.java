package cz.geokuk.plugins.mapy.kachle;

class KaOne extends Ka0 {

	// private int DOPLNKOVAC = 1<<28;

	private final EKaType type;

	public KaOne(final KaLoc loc, final EKaType type) {
		super(loc);
		this.type = type;
	}

	@Override
	public String typToString() {
		return type.name();
	}

	public EKaType getType() {
		return type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (type == null ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final KaOne other = (KaOne) obj;
		if (type == null) {
			if (other.type != null) {
				return false;
			}
		} else if (!type.equals(other.type)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return super.getLoc().toString() + "*" + type;
	}

}
