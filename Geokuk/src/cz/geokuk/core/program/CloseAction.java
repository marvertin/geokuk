/**
 * 
 */
package cz.geokuk.core.program;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import cz.geokuk.framework.Action0;

/**
 * @author veverka
 *
 */
public class CloseAction extends Action0 {

  private static final long serialVersionUID = -8054017274338240706L;

  /**
   * 
   */
  public CloseAction() {
    super("Konec");
    putValue(SHORT_DESCRIPTION, "Zavřít okno a ukončit process");
    putValue(MNEMONIC_KEY, KeyEvent.VK_K);
  }
  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */

  @Override
  public void actionPerformed(ActionEvent e) {
    getMainFrame().dispose();
    System.exit(0);
  }

}
