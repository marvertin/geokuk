/**
 *
 */
package cz.geokuk.api.mapicon;

import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.util.Deque;

/**
 * @author Martin Veverka
 *
 */
public class StdArchivDrawer extends Drawer0 {

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
		final Imagant imagant1 = puvodni.cloneEmpty();
		final ColorConvertOp colorConvertOp = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
		imagant1.getImage().createGraphics().drawImage(puvodni.getImage(), colorConvertOp, 0, 0);
		final RescaleOp rescaleOp = new RescaleOp(new float[] { 1.3f, 1.1f, 0.7f, 0.6f }, new float[] { 0f, 0f, 0f, 0f }, null);
		final BufferedImage img2 = rescaleOp.filter(imagant1.getImage(), null);
		imaganti.addFirst(new Imagant(img2, 0, 0));
	}

}
