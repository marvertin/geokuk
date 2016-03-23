/**
 *
 */
package cz.geokuk.plugins.kesoid.detailroh;

import java.awt.*;

import cz.geokuk.core.coord.JSingleSlide0;

/**
 * @author Martin Veverka
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

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(final Graphics aG) {
		final Graphics2D g = (Graphics2D) aG;
		g.setColor(Color.RED);
		final int xs = getWidth() / 2;
		final int ys = getHeight() / 2;
		final int r = 20;
		g.drawLine(0, ys, getWidth(), ys);
		g.drawLine(xs, 0, xs, getHeight());
		g.drawOval(xs - r, ys - r, 2 * r, 2 * r);
	}
}
