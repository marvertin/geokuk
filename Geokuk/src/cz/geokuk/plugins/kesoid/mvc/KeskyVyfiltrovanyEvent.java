/**
 * 
 */
package cz.geokuk.plugins.kesoid.mvc;

import cz.geokuk.framework.Event0;
import cz.geokuk.plugins.kesoid.KesBag;

/**
 * @author veverka
 *
 */
public class KeskyVyfiltrovanyEvent extends Event0<KesoidModel> {
  private final KesBag filtrovane;
  //private final Set<Kes> filtrovaneKese;

  public KeskyVyfiltrovanyEvent(KesBag filtrovane) {
    this.filtrovane = filtrovane;
  }

  public KesBag getFiltrovane() {
    return filtrovane;
  }


}
