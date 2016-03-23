/**
 *
 */
package cz.geokuk.framework;

import java.awt.Color;
import java.awt.Graphics;

import cz.geokuk.core.coord.ZmenaSouradnicMysiEvent;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Mouable;

/**
 * @author veverka
 *
 */
public class JSlidePozadi extends JSlide0 {

	/**
	 *
	 */
	private static final long	serialVersionUID	= -595069134117796569L;
	private Mou					moumys;

	/**
	 *
	 */
	public JSlidePozadi() {
		setBackground(Color.BLUE);
		setOpaque(true);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.framework.JSlide0#getUpravenaMys()
	 */
	@Override
	public Mouable getUpravenaMys() {
		// Bude zde null, vždy, když je myš mimo obrazovku
		return moumys;
	}

	public void onEvent(final ZmenaSouradnicMysiEvent event) {
		moumys = event.moucur;
		// System.out.println("Nastaveni moumys: " + moumys);
		// assert moumys != null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.Container#paintComponents(java.awt.Graphics)
	 */
	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, getWidth(), getHeight());
	}

}
