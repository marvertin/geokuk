package cz.geokuk.plugins.cesty;

import cz.geokuk.framework.Event0;
import cz.geokuk.plugins.kesoid.Wpt;

public class IgnoreListChangedEvent extends Event0<VyletModel> {

  private final Wpt wpt;


  IgnoreListChangedEvent(Wpt wpt) {
    super();
    this.wpt = wpt;
  }

  /**
   * Zda se změnil celý ignorelist, pokud ano, tak musíme předpokládat, že se mohlo změnit cololi
   * Většinout to bude znamenat načtení ignorelistu.
   * @return
   */
  public boolean isVelkaZmena() {
    return wpt == null;
  }

  public Wpt getWpt() {
    return wpt;
  }

}
