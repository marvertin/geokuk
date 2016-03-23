/**
 *
 */
package cz.geokuk.api.mapicon;

import java.awt.*;

/**
 * @author Martin Veverka
 *
 */
public abstract class ShapeDrawer extends PaintingDrawer0 {

	protected abstract void draw(Graphics2D g);

	/*
	 * (non-Javadoc)
	 *
	 * @see geokuk.mapicon.Vykreslovac#draw(geokuk.mapicon.VykreslovaciKontext, java.util.List)
	 */
	@Override
	protected void drawImage(final Graphics2D g) {

		g.setColor(getFillColor());
		fill(g);
		if (getLineWidth() > 0) {
			g.setColor(getDrawColor());
			g.setStroke(new BasicStroke(getLineWidth()));
			draw(g);
		}
	}

	protected abstract void fill(Graphics2D g);

	protected final Color getFillColor() {
		return getColor("fill", Color.GRAY);
	}

	protected final int getLineWidth() {
		return getInt("lineWidth", 1);
	}

	private Color getDrawColor() {
		return getColor("draw", Color.BLACK);
	}

}
