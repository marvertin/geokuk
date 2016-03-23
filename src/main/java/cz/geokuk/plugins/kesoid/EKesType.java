package cz.geokuk.plugins.kesoid;

public enum EKesType {

	TRADITIONAL, MULTI, UNKNOWN, LETTERBOX_HYBRID, EARTHCACHE, WHERIGO, CACHE_IN_TRASH_OUT_EVENT, EVENT, VIRTUAL, WEBCAM, LOCATIONLESS_REVERSE, MEGA_EVENT,;

	public static EKesType decode(String aKesTypeStr) {
		try {
			return EKesType.valueOf(upravNaVyctovec(aKesTypeStr));
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private static String upravNaVyctovec(String pp) {
		return pp.replace(' ', '_').replace('-', '_').toUpperCase().replaceAll("_CACHE", "").replace("(", "").replace(")", "");
	}

}
