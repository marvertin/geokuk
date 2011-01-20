/**
 * 
 */
package cz.geokuk.core.coord;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.framework.Action0;

/**
 * 
 * @author veverka
 *
 */
public class BezNaStredAction extends Action0 {

  private static final long serialVersionUID = -2882817111560336824L;
  private Mou moustred;
  /**
   * @param aBoard
   */
  public BezNaStredAction() {
    super("Na střed");
    putValue(SHORT_DESCRIPTION, "Přesune záměrný kříž na střed mapy.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_T);
    //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('D', InputEvent.CTRL_DOWN_MASK));
  }
  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent aE) {
    poziceModel.setPozice(moustred.toWgs());
  }

  public void onEvent(VyrezChangedEvent event) {
    moustred = event.getMoord().getMoustred();
  }

}
