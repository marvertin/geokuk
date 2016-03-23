package cz.geokuk.core.hledani;

import cz.geokuk.core.coordinates.Wgs;

public abstract class Nalezenec0 {

	protected double	vzdalenost;
	protected double	azimut;

	/**
	 * @return the vzdalenost
	 */
	public double getVzdalenost() {
		return vzdalenost;
	}

	/**
	 * @param aVzdalenost
	 *            the vzdalenost to set
	 */
	public void setVzdalenost(double aVzdalenost) {
		vzdalenost = aVzdalenost;
	}

	/**
	 * @return the azimut
	 */
	public double getAzimut() {
		return azimut;
	}

	/**
	 * @param aAzimut
	 *            the azimut to set
	 */
	public void setAzimut(double aAzimut) {
		azimut = aAzimut;
	}

	public abstract Wgs getWgs();

}
