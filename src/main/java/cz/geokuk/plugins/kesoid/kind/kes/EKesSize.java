package cz.geokuk.plugins.kesoid.kind.kes;

public enum EKesSize {
	MICRO,
	SMALL,
	REGULAR,
	LARGE,
	HUGE,
	UNKNOWN,

	NOT_CHOSEN,
	OTHER,

	VIRTUAL,;

	public static EKesSize decode(final String aKesSizeStr) {
		try {
			return EKesSize.valueOf(upravNaVyctovec(aKesSizeStr));
		} catch (final IllegalArgumentException e) {
			return UNKNOWN;
		}
	}

	private static String upravNaVyctovec(final String pp) {
		return pp.replace(' ', '_').replace('-', '_').toUpperCase();
	}

	public char getOneLetterSize() {
		return name().charAt(0);
	}
}
