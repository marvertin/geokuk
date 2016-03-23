package cz.geokuk.plugins.kesoid;

public enum EKesSize {
	MICRO, SMALL, REGULAR, LARGE, HUGE, UNKNOWN,

	NOT_CHOSEN, OTHER,

	VIRTUAL,;

	public static EKesSize decode(String aKesSizeStr) {
		try {
			return EKesSize.valueOf(upravNaVyctovec(aKesSizeStr));
		} catch (IllegalArgumentException e) {
			return UNKNOWN;
		}
	}

	private static String upravNaVyctovec(String pp) {
		return pp.replace(' ', '_').replace('-', '_').toUpperCase();
	}

	public char getOneLetterSize() {
		return name().charAt(0);
	}
}
