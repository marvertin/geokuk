/**
 *
 */
package cz.geokuk.util.gui;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JPopupMenu;

import cz.geokuk.core.coordinates.Mouable;
import cz.geokuk.framework.MouseGestureContext;
import cz.geokuk.framework.MySlideListener;
import cz.geokuk.plugins.kesoid.Wpt;

/**
 * @author veverka
 *
 */
public final class MouseKonecListener implements MySlideListener {

	@Override
	public void addPopouItems(JPopupMenu popupMenu, MouseGestureContext ctx) {
	}

	@Override
	public Mouable getUpravenaMys() {
		return null;
	}

	@Override
	public Wpt getWptPodMysi() {
		return null;
	}

	@Override
	public void mouseClicked(MouseEvent e, MouseGestureContext ctx) {
	}

	@Override
	public void mouseDragged(MouseEvent e, MouseGestureContext ctx) {
	}

	@Override
	public void mouseEntered(MouseEvent e, MouseGestureContext ctx) {
	}

	@Override
	public void mouseExited(MouseEvent e, MouseGestureContext ctx) {
	}

	@Override
	public void mouseMoved(MouseEvent e, MouseGestureContext ctx) {
	}

	@Override
	public void mousePressed(MouseEvent e, MouseGestureContext ctx) {
	}

	@Override
	public void mouseReleased(MouseEvent e, MouseGestureContext ctx) {
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e, MouseGestureContext ctx) {
	}

	@Override
	public Cursor getMouseCursor(boolean pressed) {
		return null;
	}

	@Override
	public void ctrlKeyPressed(MouseGestureContext ctx) {
	}

	@Override
	public void ctrlKeyReleased(MouseGestureContext ctx) {
	}

	@Override
	public void zjistiBlizkost(MouseGestureContext ctx) {
	}

}
