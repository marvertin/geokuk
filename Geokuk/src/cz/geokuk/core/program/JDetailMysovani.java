/**
 * 
 */
package cz.geokuk.core.program;


import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;

import cz.geokuk.core.coord.JSingleSlide0;
import cz.geokuk.core.coord.PoziceModel;
import cz.geokuk.framework.MouseGestureContext;

/**
 * Musí být úplně nahoře, je průhledná
 * a chytá události myši a případně je distribuuje.
 * Také zobrazuje myší kourzor.
 */
public final class JDetailMysovani extends JSingleSlide0  implements MouseInputListener, MouseWheelListener  {

  private static final long serialVersionUID = 4979888007463850390L;

  /**
   * @param jKachlovnik
   */
  JDetailMysovani() {
    addMouseListener(this);
    addMouseMotionListener(this);
    addMouseWheelListener(this);

  }

  private PoziceModel poziceModel;


  public void inject(PoziceModel poziceModel) {
    this.poziceModel = poziceModel;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    poziceModel.setPozice(getSoord().getMouCur(e.getPoint()).toWgs());
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent mwe) {
    JComponent c = (JComponent) getParent();
    int krok = mwe.getUnitsToScroll();

    Dimension d = c.getPreferredSize();
    Dimension ma = c.getMaximumSize();
    Dimension mi = c.getMinimumSize();

    d.height += krok;
    d.width  += krok;

    d.width = Math.max(Math.min(d.width, ma.width), mi.width);
    d.height = Math.max(Math.min(d.height, ma.height), mi.height);
    //System.out.println(krok + " " + d + " " +  ma + " " + mi);
    c.setPreferredSize(d);
    c.revalidate();

  }

  /* (non-Javadoc)
   * @see java.awt.event.MouseWheelListener#mouseWheelMoved(java.awt.event.MouseWheelEvent)
   */

  @Override
  public void mouseMoved(MouseEvent e, MouseGestureContext ctx) {
    poziceModel.setMys(e.getPoint(), getSoord().getMouCur(e.getPoint()));
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

  @Override
  public void mousePressed(MouseEvent e) {
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }

  @Override
  public void mouseDragged(MouseEvent e) {
  }

  @Override
  public void mouseMoved(MouseEvent e) {
  }



}