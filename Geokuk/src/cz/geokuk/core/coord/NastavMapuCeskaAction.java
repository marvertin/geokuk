/**
 * 
 */
package cz.geokuk.core.coord;


import java.awt.event.ActionEvent;

import cz.geokuk.framework.Action0;


/**
 * @author veverka
 *
 */
public class NastavMapuCeskaAction extends Action0 {

  private static final long serialVersionUID = -8054017274338240706L;

  /**
   * 
   */
  public NastavMapuCeskaAction() {
    super("Na mapu Česka");
    putValue(SHORT_DESCRIPTION, "Změna pozice a měřítka mapy, aby ukazovala českou republiku.");
    //    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));

  }
  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */

  @Override
  public void actionPerformed(ActionEvent e) {
    vyrezModel.presunMapuNaMoustred(VyrezModel.DEFAULTNI_DOMACI_SOURADNICE.toMou());
    vyrezModel.setMoumer(6);
  }

}




