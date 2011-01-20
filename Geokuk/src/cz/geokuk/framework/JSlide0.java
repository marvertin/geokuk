/**
 * 
 */
package cz.geokuk.framework;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.JComponent;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.util.gui.MouseAllAdapter;
import cz.geokuk.util.gui.MouseAllListener;

/**
 * @author veverka
 *
 */
public class JSlide0 extends JComponent implements MouseAllListener {

  private static final MouseAllAdapter KONEC = new MouseAllAdapter();
  private static final long serialVersionUID = 1L;

  private JSlide0 nextChained;
  
  void addChained(JSlide0 slide) {
    JSlide0 ch;
    for (ch = this; ch.nextChained != null; ch = ch.nextChained) {
    }
    ch.nextChained = slide;
  }

  protected MouseAllListener chain() {
    for (JSlide0 ch = nextChained; ch != null; ch = ch.nextChained) {
      if (ch.isVisible()) {
        return ch; // na tomto voláme
      }
    }
    return KONEC; // už není nic v řetězu
  }
  
  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseClicked(MouseEvent e) {
    chain().mouseClicked(e);
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseEntered(MouseEvent e) {
    chain().mouseEntered(e);
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseExited(MouseEvent e) {
    chain().mouseExited(e);
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  @Override
  public void mousePressed(MouseEvent e) {
    chain().mousePressed(e);
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseReleased(MouseEvent e) {
    chain().mouseReleased(e);
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseDragged(MouseEvent e) {
    chain().mouseDragged(e);
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseMoved(MouseEvent e) {
    chain().mouseMoved(e);
  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
   */
  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    chain().mouseWheelMoved(e);
  }

  /* (non-Javadoc)
   * @see cz.geokuk.util.MouseAllListener#getUpravenaMys()
   */
  @Override
  public Mou getUpravenaMys() {
    return chain().getUpravenaMys();
  }
  
  
}
