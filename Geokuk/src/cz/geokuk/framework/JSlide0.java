/**
 * 
 */
package cz.geokuk.framework;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import cz.geokuk.core.coordinates.Mouable;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.util.gui.MouseKonecListener;

/**
 * @author veverka
 *
 */
public class JSlide0 extends JComponent implements MySlideListener {

  private static final MySlideListener KONEC = new MouseKonecListener();
  private static final long serialVersionUID = 1L;

  private JSlide0 nextChained;

  void addChained(JSlide0 slide) {
    JSlide0 ch;
    for (ch = this; ch.nextChained != null; ch = ch.nextChained) {
    }
    ch.nextChained = slide;
  }

  protected MySlideListener chain() {
    for (JSlide0 ch = nextChained; ch != null; ch = ch.nextChained) {
      if (ch.isVisible())
        return ch; // na tomto voláme
    }
    return KONEC; // už není nic v řetězu
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseClicked(MouseEvent e, MouseGestureContext ctx) {
    chain().mouseClicked(e, ctx);
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseEntered(MouseEvent e, MouseGestureContext ctx) {
    chain().mouseEntered(e, ctx);
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseExited(MouseEvent e, MouseGestureContext ctx) {
    chain().mouseExited(e, ctx);
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  @Override
  public void mousePressed(MouseEvent e, MouseGestureContext ctx) {
    chain().mousePressed(e, ctx);
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseReleased(MouseEvent e, MouseGestureContext ctx) {
    chain().mouseReleased(e, ctx);
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseDragged(MouseEvent e, MouseGestureContext ctx) {
    chain().mouseDragged(e, ctx);
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseMoved(MouseEvent e, MouseGestureContext ctx) {
    chain().mouseMoved(e, ctx);
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent e, MouseGestureContext ctx) {
    chain().mouseWheelMoved(e, ctx);
  }

  @Override
  public void ctrlKeyPressed(MouseGestureContext ctx) {
    chain().ctrlKeyPressed(ctx);
  }

  @Override
  public void ctrlKeyReleased(MouseGestureContext ctx) {
    chain().ctrlKeyReleased(ctx);
  }


  @Override
  public Mouable getUpravenaMys() {
    Mouable upravenaMys = chain().getUpravenaMys();
    //    if (upravenaMys == null) {
    //      new Throwable().printStackTrace();
    //    }
    return upravenaMys;
  }

  @Override
  public void addPopouItems(JPopupMenu popupMenu, MouseGestureContext ctx) {
    chain().addPopouItems(popupMenu, ctx);
  }

  @Override
  public Wpt getWptPodMysi() {
    return chain().getWptPodMysi();
  }

  @Override
  public Cursor getMouseCursor(boolean pressed) {
    return chain().getMouseCursor(pressed);
  }


  @Override
  public void zjistiBlizkost(MouseGestureContext ctx) {
    chain().zjistiBlizkost(ctx);
  }

}
