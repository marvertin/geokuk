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
	public void addPopouItems(final JPopupMenu popupMenu, final MouseGestureContext ctx) {
	}

	@Override
	public void ctrlKeyPressed(final MouseGestureContext ctx) {
	}

	@Override
	public void ctrlKeyReleased(final MouseGestureContext ctx) {
	}

	@Override
	public Cursor getMouseCursor(final boolean pressed) {
		return null;
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
	public void mouseClicked(final MouseEvent e, final MouseGestureContext ctx) {
	}

	@Override
	public void mouseDragged(final MouseEvent e, final MouseGestureContext ctx) {
	}

	@Override
	public void mouseEntered(final MouseEvent e, final MouseGestureContext ctx) {
	}

	@Override
	public void mouseExited(final MouseEvent e, final MouseGestureContext ctx) {
	}

	@Override
	public void mouseMoved(final MouseEvent e, final MouseGestureContext ctx) {
	}

	@Override
	public void mousePressed(final MouseEvent e, final MouseGestureContext ctx) {
	}

	@Override
	public void mouseReleased(final MouseEvent e, final MouseGestureContext ctx) {
	}

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e, final MouseGestureContext ctx) {
	}

	@Override
	public void zjistiBlizkost(final MouseGestureContext ctx) {
	}

}
