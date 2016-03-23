package cz.geokuk.framework;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JPopupMenu;

import cz.geokuk.core.coordinates.Mouable;
import cz.geokuk.plugins.kesoid.Wpt;

public interface MySlideListener {

	/**
	 * Vrátí upravené souřadnice myši, pokud něco takobého zná
	 * @return
	 */
	public Mouable getUpravenaMys();

	/**
	 * Vrátí waypoint, na kterém je myš, pokud něco takového zná.
	 * @return
	 */
	public Wpt getWptPodMysi();

	public void addPopouItems(JPopupMenu popupMenu, MouseGestureContext ctx);

	/** Vrací kurzor, který má být, slider přepíše tuto metodu aby vracel kurozr a chainuje dál. */
	public Cursor getMouseCursor(boolean pressed);

	///////////////////
	/**
	 * Invoked when the mouse button has been clicked (pressed
	 * and released) on a component.
	 */
	public void mouseClicked(MouseEvent e, MouseGestureContext ctx);

	/**
	 * Invoked when a mouse button has been pressed on a component.
	 */
	public void mousePressed(MouseEvent e, MouseGestureContext ctx);

	/**
	 * Invoked when a mouse button has been released on a component.
	 */
	public void mouseReleased(MouseEvent e, MouseGestureContext ctx);

	/**
	 * Invoked when the mouse enters a component.
	 */
	public void mouseEntered(MouseEvent e, MouseGestureContext ctx);

	/**
	 * Invoked when the mouse exits a component.
	 */
	public void mouseExited(MouseEvent e, MouseGestureContext ctx);

	/**
	 * Invoked when a mouse button is pressed on a component and then
	 * dragged.  <code>MOUSE_DRAGGED</code> events will continue to be
	 * delivered to the component where the drag originated until the
	 * mouse button is released (regardless of whether the mouse position
	 * is within the bounds of the component).
	 * <p>
	 * Due to platform-dependent Drag&Drop implementations,
	 * <code>MOUSE_DRAGGED</code> events may not be delivered during a native
	 * Drag&Drop operation.
	 */
	public void mouseDragged(MouseEvent e, MouseGestureContext ctx);

	/**
	 * Invoked when the mouse cursor has been moved onto a component
	 * but no buttons have been pushed.
	 */
	public void mouseMoved(MouseEvent e, MouseGestureContext ctx);

	/**
	 * Invoked when the mouse wheel is rotated.
	 * @see MouseWheelEvent
	 */
	public void mouseWheelMoved(MouseWheelEvent e, MouseGestureContext ctx);

	public void ctrlKeyPressed( MouseGestureContext ctx);

	public void ctrlKeyReleased( MouseGestureContext ctx);

	public void zjistiBlizkost(MouseGestureContext ctx);


}
