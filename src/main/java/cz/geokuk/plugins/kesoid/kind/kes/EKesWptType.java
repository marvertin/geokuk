package cz.geokuk.plugins.kesoid.kind.kes;

import lombok.extern.slf4j.Slf4j;

/**
 * Typy waypoint. N2které předdefinované jsou zde. Není to ale výčet typů, protože mohou být nahrávány adhok typy.
 *
 * @author Martin Veverka
 */
@Slf4j
public enum EKesWptType {
	// CACHE,
	FINAL_LOCATION, STAGES_OF_A_MULTICACHE, QUESTION_TO_ANSWER, REFERENCE_POINT, PARKING_AREA, TRAILHEAD;

	@SuppressWarnings("unused")


	public static EKesWptType decode(final String aKesWptTpeStr) {
		try {
			return EKesWptType.valueOf(upravNaVyctovec(aKesWptTpeStr));
		} catch (final IllegalArgumentException e) {
			// log.warn("Unknown waypoint type : {}", aKesWptTpeStr);
			return null;
		}
	}

	private static String upravNaVyctovec(final String pp) {
		if (pp == null) {
			return null;
		}
		return pp.replace(' ', '_').replace('-', '_').toUpperCase();
	}

}
