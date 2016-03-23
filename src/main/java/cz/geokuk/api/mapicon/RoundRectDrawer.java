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
	protected void fill(Graphics2D g) {
		int arcWidth = getInt("arcWidth", 10);
		int arcHeight = getInt("arcHeight", 10);
		g.fillRoundRect(0, 0, getWidth(), getHeight(), arcWidth, arcHeight);
	}

	@Override
	protected void draw(Graphics2D g) {
		int arcWidth = getInt("arcWidth", 10);
		int arcHeight = getInt("arcHeight", 10);
		g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arcWidth, arcHeight);
	}

}
