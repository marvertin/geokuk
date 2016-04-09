package cz.geokuk.plugins.kesoid;

/**
 * @todo 1. Doplnit všechny známé typy keší. [ISSUE#48, 2016-04-09, Bohusz]
 * @todo 2. Zvážit, zda nedoplnit i typy další objektů (Munzee, Waymarků, ...). [ISSUE#48, 2016-04-09, Bohusz]
 */
public enum EKesType {

	TRADITIONAL, MULTI, UNKNOWN, LETTERBOX_HYBRID, EARTHCACHE, WHERIGO, CACHE_IN_TRASH_OUT_EVENT, EVENT, VIRTUAL, WEBCAM, LOCATIONLESS_REVERSE, MEGA_EVENT,;

	public static EKesType decode(final String aKesTypeStr) {
		try {
			return EKesType.valueOf(upravNaVyctovec(aKesTypeStr));
		} catch (final IllegalArgumentException e) {
			// TODO Zkontrolovat: Tady se původně vracelo null, ale kvůli tomu to pak zhučí v KesoidImportBuilder.decodePseudoKesType. [ISSUE#48, 2016-04-09, Bohusz]
			return LOCATIONLESS_REVERSE;
		}
	}

	private static String upravNaVyctovec(final String pp) {
		return pp.replace(' ', '_').replace('-', '_').toUpperCase().replaceAll("_CACHE", "").replace("(", "").replace(")", "");
	}

}
