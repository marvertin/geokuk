/**
 * 
 */
package cz.geokuk.util.gui;

import java.awt.event.MouseWheelListener;

import javax.swing.event.MouseInputListener;

import cz.geokuk.core.coordinates.Mou;

/**
 * @author veverka
 *
 */
public interface MouseAllListener extends MouseInputListener, MouseWheelListener {

  
  /**
   * Vrátí upravené souřadnice myši, pokud něco takobého zná
   * @return
   */
  public Mou getUpravenaMys();

}
