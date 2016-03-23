package cz.geokuk.core.coordinates;

public enum ESmer {

	SEVER,
	VYCHOD,
	JIH,
	ZAPAD

	;

	public double getAzimut() {
		return Math.PI  / 2 * ordinal();
	}

}
