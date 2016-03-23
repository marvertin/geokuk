/**
 *
 */
package cz.geokuk.api.mapicon;

import java.awt.Graphics2D;

/**
 * @author veverka
 *
 */
public class RoundRectDrawer extends ShapeDrawer {

	@Override
	protected void draw(final Graphics2D g) {
		final int arcWidth = getInt("arcWidth", 10);
		final int arcHeight = getInt("arcHeight", 10);
		g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcWidth, arcHeight);
	}

	@Override
	protected void fill(final Graphics2D g) {
		final int arcWidth = getInt("arcWidth", 10);
		final int arcHeight = getInt("arcHeight", 10);
		g.fillRoundRect(0, 0, getWidth(), getHeight(), arcWidth, arcHeight);
	}

}
