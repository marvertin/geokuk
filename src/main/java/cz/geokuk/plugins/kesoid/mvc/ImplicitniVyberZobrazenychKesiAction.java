/**
 * 
 */
package cz.geokuk.plugins.kesoid.mvc;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.framework.Action0;



/**
 * @author veverka
 *
 */
public class ImplicitniVyberZobrazenychKesiAction extends Action0 {

  private static final long serialVersionUID = -2882817111560336824L;
  private KesoidModel kesoidModel;

  /**
   * @param aBoard
   */
  public ImplicitniVyberZobrazenychKesiAction() {
    super("Implicitní výběr");
    putValue(SHORT_DESCRIPTION, "Nastaví zobrazování typů keší na implicitní hodnoty.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_I);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("alt F2"));
  }
  
  /* (non-Javadoc)
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  @Override
  public void actionPerformed(ActionEvent aE) {
    kesoidModel.getFilter().setDefaults();
  }
  
  public void inject(KesoidModel kesoidModel) {
    this.kesoidModel = kesoidModel;
  }

}
