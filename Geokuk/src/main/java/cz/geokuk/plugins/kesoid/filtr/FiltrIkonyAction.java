package cz.geokuk.plugins.kesoid.filtr;


import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.framework.DialogOpeningAction0;
import cz.geokuk.framework.JMyDialog0;
import cz.geokuk.img.ImageLoader;


public class FiltrIkonyAction extends DialogOpeningAction0 {

  private static final long serialVersionUID = -2637836928166450446L;

  public FiltrIkonyAction() {
    super("Fitr...");
    putValue(SHORT_DESCRIPTION, "Nastavení Filtrování zobrazených waypointů.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_F);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F2"));
    putValue(SMALL_ICON, ImageLoader.seekResIcon("x16/filtr.png"));
  }

  /* (non-Javadoc)
   * @see cz.geokuk.framework.DilogOpeningAction0#createDialog()
   */
  @Override
  public JMyDialog0 createDialog() {
    return  new JFiltrIkonyDialog();
  }


}
