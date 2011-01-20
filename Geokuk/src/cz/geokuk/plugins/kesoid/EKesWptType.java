package cz.geokuk.plugins.kesoid;


/**
 * Typy waypoint. N2které předdefinované jsou zde.
 * Není to ale výčet typů, protože mohou být nahrávány adhok typy.
 * @author tatinek
 *
 */
public enum EKesWptType  {
  //CACHE,
  FINAL_LOCATION,
  STAGES_OF_A_MULTICACHE,
  QUESTION_TO_ANSWER,
  REFERENCE_POINT,
  PARKING_AREA,
  TRAILHEAD,
  
  //UNKNOWN, // neznámý typ kešového waypointu 
  ;


  public static EKesWptType decode(String aKesWptTpeStr) {
    try {
      return EKesWptType.valueOf(upravNaVyctovec(aKesWptTpeStr));
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  private static String upravNaVyctovec(String pp) {
  	if (pp == null) return null;
    return pp.replace(' ', '_').replace('-', '_').toUpperCase();
  }
  
}
