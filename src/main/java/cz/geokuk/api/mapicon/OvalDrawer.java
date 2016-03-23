/**
 *
 */
package cz.geokuk.api.mapicon;

import java.awt.Graphics2D;


/**
 * Vykreslí elipsu.
 * @author veverka
 *
 */
public class OvalDrawer extends ShapeDrawer {


	@Override
	protected void fill(Graphics2D g) {
		g.fillOval(0, 0, getWidth(), getHeight());
	}


	@Override
	protected void draw(Graphics2D g) {
		g.drawOval((getLineWidth()-1) / 2, (getLineWidth()-1) / 2,
				getWidth() - getLineWidth(), getHeight() - getLineWidth());
	}


}
