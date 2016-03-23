/**
 *
 */
package cz.geokuk.api.mapicon;

import java.awt.Graphics2D;

/**
 * Vykreslí elipsu.
 *
 * @author veverka
 *
 */
public class OvalDrawer extends ShapeDrawer {

	@Override
	protected void draw(final Graphics2D g) {
		g.drawOval((getLineWidth() - 1) / 2, (getLineWidth() - 1) / 2, getWidth() - getLineWidth(), getHeight() - getLineWidth());
	}

	@Override
	protected void fill(final Graphics2D g) {
		g.fillOval(0, 0, getWidth(), getHeight());
	}

}
