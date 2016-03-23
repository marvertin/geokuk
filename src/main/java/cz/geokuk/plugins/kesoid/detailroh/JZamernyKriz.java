/**
 *
 */
package cz.geokuk.plugins.kesoid.detailroh;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import cz.geokuk.core.coord.JSingleSlide0;

/**
 * @author veverka
 *
 */
public class JZamernyKriz extends JSingleSlide0 {

	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	public JZamernyKriz() {
		setOpaque(false);
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(Graphics aG) {
		Graphics2D g = (Graphics2D) aG;
		g.setColor(Color.RED);
		int xs = getWidth() / 2;
		int ys = getHeight() / 2;
		int r = 20;
		g.drawLine(0, ys, getWidth(), ys);
		g.drawLine(xs, 0, xs, getHeight());
		g.drawOval(xs - r, ys - r, 2 * r, 2 * r);
	}
}
