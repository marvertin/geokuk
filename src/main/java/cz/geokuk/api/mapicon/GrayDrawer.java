/**
 * 
 */
package cz.geokuk.api.mapicon;

import java.awt.color.ColorSpace;
import java.awt.image.ColorConvertOp;
import java.util.Deque;



/**
 * @author veverka
 *
 */
public class GrayDrawer extends Drawer0 {

  /* (non-Javadoc)
   * @see geokuk.mapicon.Vykreslovac#draw(geokuk.mapicon.VykreslovaciKontext, java.util.List)
   */
  @Override
  public void draw(Deque<Imagant> imaganti) {
    if (imaganti.peekFirst() == null) return;
    Imagant puvodni = imaganti.removeFirst();
    if (puvodni == null) return;
    Imagant imagant = puvodni.cloneEmpty();
    ColorConvertOp colorConvertOp = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
    imagant.getImage().createGraphics().drawImage(puvodni.getImage(), colorConvertOp,0,0);
    imaganti.addFirst(imagant);
  }

}
