package cz.geokuk.plugins.cesty.akce.soubor;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.plugins.cesty.CestyChangedEvent;
import cz.geokuk.plugins.cesty.data.Doc;

public class UlozAction extends SouboeCestaAction0 {

  private static final long serialVersionUID = 1L;


  public UlozAction() {
    super("Uložit cesty (gpx)");
    putValue(SHORT_DESCRIPTION, "Uloží zadaný výlet do GPX");
    putValue(MNEMONIC_KEY, KeyEvent.VK_V);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl S"));
    //putValue(SMALL_ICON, ImageLoader.seekResIcon("x16/vylet/vyletAno.png"));
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    ulozit();
  }

  public void onEvent(CestyChangedEvent event) {
    Doc doc = event.getDoc();
    setEnabled(doc != null && doc.isChanged());
  }


}
