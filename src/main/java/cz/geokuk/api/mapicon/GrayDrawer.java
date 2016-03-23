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

	/*
	 * (non-Javadoc)
	 *
	 * @see geokuk.mapicon.Vykreslovac#draw(geokuk.mapicon.VykreslovaciKontext, java.util.List)
	 */
	@Override
	public void draw(final Deque<Imagant> imaganti) {
		if (imaganti.peekFirst() == null) {
			return;
		}
		final Imagant puvodni = imaganti.removeFirst();
		if (puvodni == null) {
			return;
		}
		final Imagant imagant = puvodni.cloneEmpty();
		final ColorConvertOp colorConvertOp = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
		imagant.getImage().createGraphics().drawImage(puvodni.getImage(), colorConvertOp, 0, 0);
		imaganti.addFirst(imagant);
	}

}
