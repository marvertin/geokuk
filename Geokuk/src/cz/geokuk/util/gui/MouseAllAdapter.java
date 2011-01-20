/**
 * 
 */
package cz.geokuk.util.gui;

import java.awt.event.MouseAdapter;

import cz.geokuk.core.coordinates.Mou;

/**
 * @author veverka
 *
 */
public class MouseAllAdapter extends MouseAdapter implements MouseAllListener {

  /* (non-Javadoc)
   * @see cz.geokuk.util.MouseAllListener#getUpravenaMys()
   */
  @Override
  public Mou getUpravenaMys() {
    return null;
  }

}
