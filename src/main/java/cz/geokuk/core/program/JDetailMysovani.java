package cz.geokuk.core.program;

import java.awt.Dimension;
import java.awt.event.*;

import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;

import cz.geokuk.core.coord.JSingleSlide0;
import cz.geokuk.core.coord.PoziceModel;

/**
 * Musí být úplně nahoře, je průhledná a chytá události myši a případně je distribuuje. Také zobrazuje myší kourzor.
 */
public final class JDetailMysovani extends JSingleSlide0 implements MouseInputListener, MouseWheelListener {

	private static final long serialVersionUID = 4979888007463850390L;

	private PoziceModel poziceModel;

	/**
	 * @param jKachlovnik
	 */
	JDetailMysovani() {
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
	}

	public void inject(final PoziceModel poziceModel) {
		this.poziceModel = poziceModel;
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		poziceModel.setPozice(getSoord().transform(e.getPoint()).toWgs());
	}

	@Override
	public void mouseDragged(final MouseEvent e) {}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
	 */

	@Override
	public void mouseEntered(final MouseEvent e) {}

	@Override
	public void mouseExited(final MouseEvent e) {}

	@Override
	public void mouseMoved(final MouseEvent e) {
		poziceModel.setMys(e.getPoint(), getSoord().transform(e.getPoint()), getUpravenaMys());
	}

	@Override
	public void mousePressed(final MouseEvent e) {}

	@Override
	public void mouseReleased(final MouseEvent e) {}

	@Override
	public void mouseWheelMoved(final MouseWheelEvent mwe) {
		final JComponent c = (JComponent) getParent();
		final int krok = mwe.getUnitsToScroll();

		final Dimension d = c.getPreferredSize();
		final Dimension ma = c.getMaximumSize();
		final Dimension mi = c.getMinimumSize();

		d.height += krok;
		d.width += krok;

		d.width = Math.max(Math.min(d.width, ma.width), mi.width);
		d.height = Math.max(Math.min(d.height, ma.height), mi.height);
		// System.out.println(krok + " " + d + " " + ma + " " + mi);
		c.setPreferredSize(d);
		c.revalidate();

	}
}