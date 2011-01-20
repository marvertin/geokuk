/**
 * 
 */
package cz.geokuk.plugins.mapy.kachle;


import java.awt.Image;
import java.util.EnumSet;

/**
 * @author veverka
 *
 */
public interface ImageReceiver {

  /**
   * @param aTyp
   * @param aImg
   */
  public void setImage(EnumSet<EKaType> types, Image img2, boolean aJeToUzCelyObrazek);


}
