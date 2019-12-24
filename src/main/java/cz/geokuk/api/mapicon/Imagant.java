/**
 *
 */
package cz.geokuk.api.mapicon;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.util.List;

import cz.geokuk.plugins.kesoid.Repaintanger;
import cz.geokuk.util.index2d.BoundingRect;

/**
 * Obrázek i s offsetem.
 *
 * @author Martin Veverka
 *
 */
public class Imagant {

	private final BufferedImage image;

	/** Pozice, o kolik je řečeno, že se má obrázek posunout od středu */
	private final int xoffset;
	private final int yoffset;

	/**
	 * Pozice počátku obrázku, když střed obrázku bude umístěn v počátku souřadnic. A to bez aplikace offsetu. Je logické, že se jedná o záporné hodnoty.
	 */
	private final int x0;
	private final int y0;

	/**
	 * @param aBi
	 */
	public Imagant(final BufferedImage aBi, final int xoffset, final int yoffset) {
		image = aBi;
		this.xoffset = xoffset;
		this.yoffset = yoffset;
		x0 = -image.getWidth() / 2;
		y0 = -image.getHeight() / 2;
	}

	public Imagant cloneEmpty() {
		final BufferedImage bi = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		final Imagant imagant = new Imagant(bi, xoffset, yoffset);
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
	 * @return X pozice, kde musím začít vykreslovat obrázek, aby byl v počátku souřadnic bod, který tam má být. Nebo také offset, který musím připoítat k X souřadnici, na které chci mít obrázek.
	 */
	public int getXpos() {
		return x0 + xoffset;
	}

	/**
	 * @return Y pozice, kde musím začít vykreslovat obrázek, aby byl v počátku souřadnic bod, který tam má být. Nebo také offset, který musím připoítat k Y souřadnici, na které chci mít obrázek.
	 */
	public int getYpos() {
		return y0 + yoffset;
	}

	private static BoundingRect sjednoceni(final List<Imagant> imaganti) {
		// Spočítat hranice
		int xx1 = 0, xx2 = 0, yy1 = 0, yy2 = 0;
		for (final Imagant imagant : imaganti) {
			if (imagant == null) {
				continue;
			}
			final int x1 = imagant.getXpos();
			final int y1 = imagant.getYpos();
			final int x2 = x1 + imagant.getImage().getWidth();
			final int y2 = y1 + imagant.getImage().getHeight();
			xx1 = Math.min(xx1, x1);
			xx2 = Math.max(xx2, x2);
			yy1 = Math.min(yy1, y1);
			yy2 = Math.max(yy2, y2);
		}
		if (xx2 - xx1 == 0 || yy2 - yy1 == 0) {
			return null;
		}
		final BoundingRect br = new BoundingRect(xx1, yy1, xx2, yy2);
		return br;
	}

	/**
	 * Překreslí list imagantům přes sebe do nového obrázku. Výsledný obrázek má takovou velikost, aby zahrnul všechny obrázky, které do něj přizpívají. Kreslí se přes definované středy.
	 * 
	 * @param imaganti
	 * @return
	 */
	public static Imagant prekresliNaSebe(final List<Imagant> imaganti) {
		if (imaganti.isEmpty()) {
			return null;
		}
		final BoundingRect br = Imagant.sjednoceni(imaganti);

		if (br == null) {
			// TODO log.warn()
			return null;
		}

		// vytvořit cílový brázek ve správné velikosti
		final int width = br.xx2 - br.xx1;
		final int height = br.yy2 - br.yy1;
		Imagant imagant;
		final BufferedImage resultbi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		final Graphics g = resultbi.getGraphics();
		try {
			g.translate(-br.xx1, -br.yy1); // nastavit správný střed

			// vykreslení obrázků
			for (final Imagant ima : imaganti) {
				if (ima != null) {
					g.drawImage(ima.getImage(), ima.getXpos(), ima.getYpos(), null);
				}
			}

			// a vyvorit výsledek
			imagant = new Imagant(resultbi, br.xx1 - -width / 2, br.yy1 - -height / 2);
		} finally {
			g.dispose();
		}
		return imagant;
	}

}
