/**
 *
 */
package cz.geokuk.api.mapicon;

import java.awt.Graphics2D;

/**
 * @author veverka
 *
 */
public class RectDrawer extends ShapeDrawer {

	@Override
	protected void draw(final Graphics2D g) {
		g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
	}

	@Override
	protected void fill(final Graphics2D g) {
		g.fillRect(0, 0, getWidth(), getHeight());
	}

}
