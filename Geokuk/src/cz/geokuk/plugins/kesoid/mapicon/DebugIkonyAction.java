package cz.geokuk.plugins.kesoid.mapicon;

import cz.geokuk.framework.DialogOpeningAction0;
import cz.geokuk.framework.JMyDialog0;



public class DebugIkonyAction extends DialogOpeningAction0 {

  private static final long serialVersionUID = -2637836928166450446L;

  public DebugIkonyAction() {
    super("Ladění ikon...");
    putValue(SHORT_DESCRIPTION, "Zobrazí dialog pro nastavení parametrů zvýrazňovaích kruhů.");
    //putValue(MNEMONIC_KEY, KeyEvent.VK_K);
    //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl F8"));
  }

  /* (non-Javadoc)
   * @see cz.geokuk.framework.DilogOpeningAction0#createDialog()
   */
  @Override
  public JMyDialog0 createDialog() {
    return  new JDebugIkonyDialog();
  }


}
