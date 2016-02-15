/**
 * 
 */
package cz.geokuk.plugins.mrizky;

import cz.geokuk.core.coord.JSingleSlide0;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Utm;


/**
 * @author veverka
 *
 */
public class JMrizkaS42 extends JMrizka0 {

  protected static final double MINUTA = 1.0 / 60;

  private static final long serialVersionUID = 4558815639199835559L;

  /* (non-Javadoc)
   * @see mrizka.JMrizka0#convertToMou(double, double)
   */
  @Override
  public Mou convertToMou(double aX, double aY) {
    Utm utm = new Utm(aY, aX);
    Mou mou = utm.toMou();
    return mou;
  }

  /* (non-Javadoc)
   * @see mrizka.JMrizka0#convertToX(coordinates.Mou)
   */
  @Override
  public double convertToX(Mou aMou) {
    return aMou.toUtm().ux;
  }

  /* (non-Javadoc)
   * @see mrizka.JMrizka0#convertToY(coordinates.Mou)
   */
  @Override
  public double convertToY(Mou aMou) {
    return aMou.toUtm().uy;
  }


  @Override
  public String getTextY(double y) {
    return y + "";
  }

  @Override
  public String getTextX(double x) {
    return x + "";
  }

  /* (non-Javadoc)
   * @see mrizka.JMrizka0#initPainting(mrizka.JMrizka0.Vykreslovac)
   */
  @Override
  public void initPainting(Vykreslovac v) {
    for (int rad =1; rad < 100000000; rad = rad * 10) {
      v.rastr(rad, 1);
      v.rastr(rad * 2, 5);
      v.rastr(rad * 5, 2);
    }
  }
  @Override

  public JSingleSlide0 createRenderableSlide() {
    return new JMrizkaS42();
  }

}
