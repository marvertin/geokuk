package cz.geokuk.core.hledani;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import cz.geokuk.core.coordinates.FGeoKonvertor;
import cz.geokuk.core.coordinates.Wgs;

public abstract class Hledac0<T extends Nalezenec0> {

	private Future<?> future;

	public Future<?> getFuture() {
		return future;
	}

	public void setFuture(final Future<?> future) {
		this.future = future;
	}

	public abstract List<T> hledej(HledaciPodminka0 podm);

	public List<T> najdiASerad(final HledaciPodminka0 podm) {
		final List<T> list = hledej(podm);
		// dopočítat vzdálenost a azimut
		final Wgs stredHledani = podm.getStredHledani();
		for (final T nal : list) {
			dopicitejVzdalenostAAzimut(nal, stredHledani);
		}

		// Seřadit
		try {
			Collections.sort(list, new PorovnavacOdStredu(getFuture()));
		} catch (final XZaknclovanoRazeni e) {
			return null; // hned zkoncit, protože řazení nedoběhlo
		}
		return list;

	}

	/**
	 * @param aNal
	 * @param aStredHledani
	 */
	protected void dopicitejVzdalenostAAzimut(final Nalezenec0 aNal, final Wgs aStredHledani) {
		final double dalka = FGeoKonvertor.dalka(aNal.getWgs(), aStredHledani);
		if (aStredHledani == null)
			return;
		double uhel = Wgs.azimut(aStredHledani, aNal.getWgs());
		uhel = uhel * 180 / Math.PI;
		if (uhel < 0)
			uhel += 360;
		aNal.setVzdalenost(dalka);
		aNal.setAzimut(uhel);
	}
}
