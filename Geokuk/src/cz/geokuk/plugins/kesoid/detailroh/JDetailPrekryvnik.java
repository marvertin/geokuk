/**
 * 
 */
package cz.geokuk.plugins.kesoid.detailroh;

import cz.geokuk.core.coord.JCoordPrekryvnik0;
import cz.geokuk.core.coord.PoziceChangedEvent;

/**
 * @author veverka
 *
 */
public class JDetailPrekryvnik extends JCoordPrekryvnik0 {
  private static final int DETAIL_MOUMER = 17;
  private static final long serialVersionUID = -5996655830197513951L;

  /**
   * 
   */
  public JDetailPrekryvnik() {
    // TODO To by mělo jít jistojistě odstranit
    //    getCoord().setMoustred(new Wgs(50.284, 14.3563).toMou());
    //    getCoord().setMoumer(17);

  }

  public void onEvent(PoziceChangedEvent aEvent) {
    if (aEvent.pozice.isNoPosition()) {
      setVisible(false);
    } else {
      setSoord(getSoord().derive(DETAIL_MOUMER, aEvent.pozice.getWgs().toMou()));
      repaint(); // musíme překreslit při změně středu
      setVisible(true);
    }
  }



}
