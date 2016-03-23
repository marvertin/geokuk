package cz.geokuk.core.coordinates;

public class Moud {

	public int	dxx;
	public int	dyy;

	public Moud() {
	}

	public Moud(final int dxx, final int dyy) {
		this.dxx = dxx;
		this.dyy = dyy;
	}

	public Moud(final Moud mou) {
		dxx = mou.dxx;
		dyy = mou.dyy;
	}

	public Moud add(final int dxx, final int dyy) {
		return new Moud(this.dxx + dxx, this.dyy + dyy);
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Moud)) {
			return false;
		}
		final Moud m = (Moud) obj;
		return dxx == m.dxx && dyy == m.dyy;
	}

	public long getKvadratVzdalenosti() {
		final long lxx = dxx;
		final long lyy = dyy;
		return lxx * lxx + lyy * lyy;
	}

	@Override
	public int hashCode() {
		return dxx ^ dyy;
	}

	public boolean isAnyRozmerEmpty() {
		return dxx <= 0 || dyy <= 0;
	}

	public Moud sub(final int dxx, final int dyy) {
		return new Moud(this.dxx - dxx, this.dyy - dyy);
	}

	@Override
	public String toString() {
		return "[" + Integer.toHexString(dxx) + "," + Integer.toHexString(dyy) + "]";
	}
}
