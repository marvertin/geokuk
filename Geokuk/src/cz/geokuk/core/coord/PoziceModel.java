/**
 * 
 */
package cz.geokuk.core.coord;

import java.awt.Point;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.AfterInjectInit;
import cz.geokuk.framework.Model0;
import cz.geokuk.plugins.kesoid.Wpt;

/**
 * @author veverka
 *
 */
public class PoziceModel extends Model0 implements AfterInjectInit {


  private Pozice pozice;


  public void setPozice(Wgs wgs) {
    setPozice(new Pozice(wgs));
  }

  public void setPozice(Wpt wpt) {
    setPozice(new Pozice(wpt));
  }

  /**
   * @param pozice the pozice to set
   */
  public void setPozice(Pozice pozice) {
    if (pozice.equals(this.pozice)) return;
    this.pozice = pozice;
    fire(new PoziceChangedEvent(pozice));
  }

  public void clearPozice() {
    setPozice(new Pozice());
  }

  /**
   * @return the pozice
   */
  public Pozice getPozice() {
    if (pozice == null) throw new RuntimeException("Pozice nebyla nastavena!");
    return pozice;
  }

  public Wgs getWgs() {
    return pozice.getWgs();
  }

  public void onEvent(PoziceChangedEvent event) {
    setPozice(event.pozice);
  }


  /* (non-Javadoc)
   * @see cz.geokuk.program.AfterInjectInit#initAfterInject()
   */
  @Override
  public void initAndFire() {
    clearPozice();
  }

  public void setMys(Point cur, Mou mouCur) {
    fire(new ZmenaSouradnicMysiEvent(cur, mouCur));
  }

}
