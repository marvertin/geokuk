/**
 * 
 */
package cz.geokuk.plugins.vylety;

import cz.geokuk.plugins.kesoid.mvc.KeskyNactenyEvent;

/**
 * @author veverka
 *
 */
public class VyletNacitaniKesoiduWatchDog {

  private VyletModel vyletModel;

  public void onEvent(KeskyNactenyEvent aEvent) {
    vyletModel.startLogingIgnoreList(aEvent.getVsechny());
    vyletModel.znovuVsechnoPripni();
    vyletModel.vyresPripadneNahraniZastaralychVyletu();


  }

  public void inject(VyletModel vyletModel) {
    this.vyletModel = vyletModel;
  }

}
