/**
 *
 */
package cz.geokuk.api.mapicon;

import java.awt.Insets;
import java.awt.image.BufferedImage;

import cz.geokuk.plugins.kesoid.Repaintanger;


/**
 * Obrázek i s offsetem.
 * @author veverka
 *
 */
public class Imagant {

	private final BufferedImage image;

	/** Pozice, o kolik je řečeno, že se má obrázek posunout od středu */
	private int xoffset;
	private int yoffset;

	/** Pozice středu obrázku bez aplikace offsetu */
	private int x0;
	private int y0;

	/**
	 * @param aBi
	 */
	public Imagant(BufferedImage aBi) {
		image = aBi;
		x0 = - image.getWidth() / 2;
		y0 = - image.getHeight() / 2;
	}

	public Imagant cloneEmpty() {
		BufferedImage bi = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Imagant imagant = new Imagant(bi);
		imagant.setXoffset(xoffset);
		imagant.setYoffset(yoffset);
		return imagant;
	}

	/**
	 * @return the xpos
	 */
	public int getXpos() {
		return x0 + xoffset;
	}



	/**
	 * @return the ypos
	 */
	public int getYpos() {
		return y0 + yoffset;
	}


	/**
	 * @return the image
	 */
	public BufferedImage getImage() {
		return image;
	}

	/**
	 * @param aXoffset the xoffset to set
	 */
	public void setXoffset(int aXoffset) {
		xoffset = aXoffset;
	}

	/**
	 * @param aYoffset the yoffset to set
	 */
	public void setYoffset(int aYoffset) {
		yoffset = aYoffset;
	}

	public void computeInsets(Repaintanger repaintanger) {
		Insets insets = new Insets(-getYpos(), -getXpos(),
				getYpos() + image.getHeight() , getXpos() + image.getWidth());
		repaintanger.include(insets);
	}

}
