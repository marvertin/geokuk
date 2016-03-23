package cz.geokuk.core.coord;

import cz.geokuk.core.coordinates.*;
import cz.geokuk.plugins.cesty.data.Bod;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.Wpt;

public class Poziceq {

	/** Sem pozice vede */
	private final Mouable	mouable;

	/** Tak sem to bylo dáno bez ohledu na to, jak se MOu mění v mouabblu */
	private final Mou		originalMou;

	public Poziceq() {
		mouable = null;
		originalMou = null;
	}

	/**
	 */
	public Poziceq(final Mouable mouable) {
		this.mouable = mouable;
		originalMou = mouable.getMou();
		assert this.mouable != null;
		assert originalMou != null;
	}

	public boolean isNoPosition() {
		return mouable == null;
	}

	public Mouable getPoziceMouable() {
		if (isNoPosition()) {
			return null;
		}
		return mouable.getMou().equals(originalMou) ? mouable : originalMou;
	}

	public Mou getPoziceMou() {
		final Mouable poziceMouable = getPoziceMouable();
		return poziceMouable == null ? null : poziceMouable.getMou();
	}

	public Wpt getWpt() {
		final Mouable mouable = getPoziceMouable();
		if (mouable instanceof Wpt) {
			final Wpt wpt = (Wpt) mouable;
			return wpt;
		}
		return null;
	}

	public Bod getBod() {
		final Mouable mouable = getPoziceMouable();
		if (mouable instanceof Bod) {
			final Bod bod = (Bod) mouable;
			return bod;
		}
		return null;
	}

	public Kesoid getKesoid() {
		final Wpt wpt = getWpt();
		if (wpt == null) {
			return null;
		}
		return wpt.getKesoid();
	}

	public Wgs getWgs() {
		// Here, if the underlying mouable has latitude and longitude, simply return its Wgs, because it's more accurate
		// than converting the coordinates many times. At the moment, only waypoints satisfy this condition, but if other
		// mouables are introduced, this should be adjusted and an interface should be introduced
		final Wpt wpt = getWpt();
		if (wpt != null) {
			return wpt.getWgs();
		}

		// if not, just do it the old way
		final Mou mou = getPoziceMou();
		return mou == null ? null : mou.toWgs();
	}

	// public Utm getUtm() {
	// Mou mou = getPoziceMou();
	// return mou == null ? null : mou.toUtm();
	// }

}
