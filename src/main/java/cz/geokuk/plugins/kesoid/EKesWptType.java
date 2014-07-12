package cz.geokuk.plugins.kesoid;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Typy waypoint. N2které předdefinované jsou zde.
 * Není to ale výčet typů, protože mohou být nahrávány adhok typy.
 *
 * @author tatinek
 */
public enum EKesWptType {
    //CACHE,
    FINAL_LOCATION,
    STAGES_OF_A_MULTICACHE,
    QUESTION_TO_ANSWER,
    REFERENCE_POINT,
    PARKING_AREA,
    TRAILHEAD;

    private static final Logger log =
            LogManager.getLogger(EKesWptType.class.getSimpleName());

    public static EKesWptType decode(String aKesWptTpeStr) {
        try {
            return EKesWptType.valueOf(upravNaVyctovec(aKesWptTpeStr));
        } catch (IllegalArgumentException e) {
            log.warn("Unknown waypoint type : {}", aKesWptTpeStr);
            return null;
        }
    }

    private static String upravNaVyctovec(String pp) {
        if (pp == null) return null;
        return pp.replace(' ', '_').replace('-', '_').toUpperCase();
    }

}
