package cz.geokuk.core.hledani;

import cz.geokuk.core.coordinates.Wgs;

public abstract class HledaciPodminka0 {
	private Wgs stredHledani = new Wgs(49.284, 16.3563);
	private String vzorek;

	public Wgs getStredHledani() {
		return stredHledani;
	}

	/**
	 * @return the vzorek
	 */
	public String getVzorek() {
		return vzorek;
	}

	public void setStredHledani(final Wgs stredHledani) {
		this.stredHledani = stredHledani;
	}

	/**
	 * @param aVzorek
	 *            the vzorek to set
	 */
	public void setVzorek(final String aVzorek) {
		vzorek = aVzorek;
	}

}
