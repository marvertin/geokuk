/**
 *
 */
package cz.geokuk.api.mapicon;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Deque;

/**
 * Předek vykreslujívcí obrázky.
 *
 * @author Martin Veverka
 *
 */
public abstract class PaintingDrawer0 extends Drawer0 {

	/*
	 * (non-Javadoc)
	 *
	 * @see geokuk.mapicon.Vykreslovac#draw(geokuk.mapicon.VykreslovaciKontext, java.util.List)
	 */
	@Override
	public final void draw(final Deque<Imagant> aImaganti) {

		final BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = bi.createGraphics();
		try {
			drawImage(g);
			aImaganti.add(new Imagant(bi, 0, 0));
		} finally {
			g.dispose();
		}
	}

	protected abstract void drawImage(Graphics2D g);

	protected final int getHeight() {
		return getInt("height", 32);
	}

	protected final int getWidth() {
		return getInt("width", 32);
	}

}
