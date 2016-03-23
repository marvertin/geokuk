/**
 *
 */
package cz.geokuk.api.mapicon;

import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.util.Deque;

/**
 * @author veverka
 *
 */
public class StdArchivDrawer extends Drawer0 {

	/*
	 * (non-Javadoc)
	 * 
	 * @see geokuk.mapicon.Vykreslovac#draw(geokuk.mapicon.VykreslovaciKontext, java.util.List)
	 */
	@Override
	public void draw(Deque<Imagant> imaganti) {
		if (imaganti.peekFirst() == null)
			return;
		Imagant puvodni = imaganti.removeFirst();
		if (puvodni == null)
			return;
		Imagant imagant1 = puvodni.cloneEmpty();
		ColorConvertOp colorConvertOp = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
		imagant1.getImage().createGraphics().drawImage(puvodni.getImage(), colorConvertOp, 0, 0);
		RescaleOp rescaleOp = new RescaleOp(new float[] { 1.3f, 1.1f, 0.7f, 0.6f }, new float[] { 0f, 0f, 0f, 0f }, null);
		BufferedImage img2 = rescaleOp.filter(imagant1.getImage(), null);
		imaganti.addFirst(new Imagant(img2));
	}

}
