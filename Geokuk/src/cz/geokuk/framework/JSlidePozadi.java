/**
 * 
 */
package cz.geokuk.framework;

import java.awt.Color;
import java.awt.Graphics;

import cz.geokuk.core.coord.ZmenaSouradnicMysiEvent;
import cz.geokuk.core.coordinates.Mou;

/**
 * @author veverka
 *
 */
public class JSlidePozadi extends JSlide0 {

  /**
   * 
   */
  private static final long serialVersionUID = -595069134117796569L;
  private Mou moumys;

  /**
   * 
   */
  public JSlidePozadi() {
    setBackground(Color.BLUE);
    setOpaque(true);
  }

  public void onEvent(ZmenaSouradnicMysiEvent event) {
    moumys = event.moucur;
  }

  /* (non-Javadoc)
   * @see cz.geokuk.framework.JSlide0#getUpravenaMys()
   */
  @Override
  public Mou getUpravenaMys() {
    return moumys;
  }


  /* (non-Javadoc)
   * @see java.awt.Container#paintComponents(java.awt.Graphics)
   */
  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.setColor(Color.DARK_GRAY);
    g.fillRect(0, 0, getWidth(), getHeight());
  }


}
