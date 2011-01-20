/**
 * 
 */
package cz.geokuk.core.coord;

import cz.geokuk.framework.Event0;

/**
 * @author veverka
 *
 */
public class PoziceChangedEvent extends Event0<PoziceModel> {

  public final Pozice pozice;

  /**
   * @param aPozice
   * @param aMeloBySeCentrovat
   */
  PoziceChangedEvent(Pozice aPozice) {
    pozice = aPozice;
  }
}
