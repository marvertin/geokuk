/**
 * 
 */
package cz.geokuk.plugins.cesty;

import cz.geokuk.plugins.kesoid.mvc.KeskyNactenyEvent;

/**
 * @author veverka
 *
 */
public class CestyNacitaniKesoiduWatchDog {

  private CestyModel cestyModel;

  public void onEvent(KeskyNactenyEvent aEvent) {
    cestyModel.znovuVsechnoPripni();
    cestyModel.vyresPripadneNahraniZastaralychVyletu();


  }

  public void inject(CestyModel cestyModel) {
    this.cestyModel = cestyModel;
  }

}
