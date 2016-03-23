/**
 *
 */
package cz.geokuk.api.mapicon;

import java.awt.Insets;
import java.awt.image.BufferedImage;

import cz.geokuk.plugins.kesoid.Repaintanger;

/**
 * Obrázek i s offsetem.
 *
 * @author veverka
 *
 */
public class Imagant {

	private final BufferedImage	image;

	/** Pozice, o kolik je řečeno, že se má obrázek posunout od středu */
	private int					xoffset;
	private int					yoffset;

	/** Pozice středu obrázku bez aplikace offsetu */
	private final int			x0;
	private final int			y0;

	/**
	 * @param aBi
	 */
	public Imagant(final BufferedImage aBi) {
		image = aBi;
		x0 = -image.getWidth() / 2;
		y0 = -image.getHeight() / 2;
	}

	public Imagant cloneEmpty() {
		final BufferedImage bi = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		final Imagant imagant = new Imagant(bi);
		imagant.setXoffset(xoffset);
		imagant.setYoffset(yoffset);
		return imagant;
	}

	public void computeInsets(final Repaintanger repaintanger) {
		final Insets insets = new Insets(-getYpos(), -getXpos(), getYpos() + image.getHeight(), getXpos() + image.getWidth());
		repaintanger.include(insets);
	}

	/**
	 * @return the image
	 */
	public BufferedImage getImage() {
		return image;
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
	 * @param aXoffset
	 *            the xoffset to set
	 */
	public void setXoffset(final int aXoffset) {
		xoffset = aXoffset;
	}

	/**
	 * @param aYoffset
	 *            the yoffset to set
	 */
	public void setYoffset(final int aYoffset) {
		yoffset = aYoffset;
	}

}
