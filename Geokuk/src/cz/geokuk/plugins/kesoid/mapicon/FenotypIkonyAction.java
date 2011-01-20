package cz.geokuk.plugins.kesoid.mapicon;


import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.framework.DialogOpeningAction0;
import cz.geokuk.framework.JMyDialog0;



public class FenotypIkonyAction extends DialogOpeningAction0 {

  private static final long serialVersionUID = -2637836928166450446L;

  public FenotypIkonyAction() {
    super("Výběr fenotypu...");
    putValue(SHORT_DESCRIPTION, "Nastavení jaké dekorace se objeví na ikonách keších i jiných waypointů.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_F);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F6"));
  }

  /* (non-Javadoc)
   * @see cz.geokuk.framework.DilogOpeningAction0#createDialog()
   */
  @Override
  public JMyDialog0 createDialog() {
    return new JFenotypIkonyDialog();
  }


}
