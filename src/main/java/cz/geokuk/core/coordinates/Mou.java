package cz.geokuk.core.coordinates;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Mapy.cz coordinates.
 */

public class Mou extends Misto0 {

	@SuppressWarnings("unused")
	private static final Logger	log	= LogManager.getLogger(Mou.class.getSimpleName());

	public final int			xx;
	public final int			yy;

	public Mou(final int xx, final int yy) {
		this.xx = xx;
		this.yy = yy;
	}

	public Mou(final Mou mou) {
		xx = mou.xx;
		yy = mou.yy;
	}

	public Mou add(final int dxx, final int dyy) {
		return new Mou(xx + dxx, yy + dyy);
	}

	public Mou add(final Moud moud) {
		return new Mou(xx + moud.dxx, yy + moud.dyy);
	}

	public Mou sub(final Moud moud) {
		return new Mou(xx - moud.dxx, yy - moud.dyy);
	}

	public Moud sub(final Mou mou) {
		return new Moud(xx - mou.xx, yy - mou.yy);
	}

	@Override
	public int hashCode() {
		return xx ^ yy;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Mou)) {
			return false;
		}
		final Mou m = (Mou) obj;
		return xx == m.xx && yy == m.yy;
	}

	@Override
	public String toString() {
		return "[" + Integer.toHexString(xx) + "," + Integer.toHexString(yy) + "]";
	}

	public long getKvadratVzdalenosti(final Mou mou) {
		if (mou == null) {
			return Long.MAX_VALUE; // nevlastní bod je nekonečně daleko
		}
		return sub(mou).getKvadratVzdalenosti();
	}

	public Wgs toWgs() {
		return FGeoKonvertor.toWgs(this);
	}

	@Override
	public Mercator toMercator() {
		return FGeoKonvertor.toMercator(this);
	}

	@Override
	public Mou toMou() {
		return this;
	}

	@Override
	public Utm toUtm() {
		return FGeoKonvertor.toUtm(this);
	}

}
