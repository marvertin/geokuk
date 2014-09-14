/**
 * 
 */
package cz.geokuk.plugins.kesoid.importek;

import java.util.HashMap;
import java.util.Map;

/**
 * @author veverka
 *
 */
public class Gpxg {

  public int elevation;
  public String found;
  public int flag;
  public int hodnoceni;
  public int hodnoceniPocet;
  public int bestOf;
  public int favorites;
  public int znamka;
  public String czkraj;
  public String czokres;
  public Map<String, String> userTags;


  public void putUserTag(String genname, String alelaname) {
    if (userTags == null) {
      userTags = new HashMap<>();
    }
    userTags.put(genname, alelaname);
  }

}
