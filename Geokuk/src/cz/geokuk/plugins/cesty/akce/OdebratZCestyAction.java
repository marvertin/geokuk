package cz.geokuk.plugins.cesty.akce;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.core.coordinates.Uchopenec;
import cz.geokuk.img.ImageLoader;

public class OdebratZCestyAction extends CestyActionIndividual0 {

  private static final long serialVersionUID = 1L;

  public OdebratZCestyAction(Uchopenec uchopenec) {
    super(uchopenec);
    putValue(NAME, "Odebrat z cesty");
    putValue(SHORT_DESCRIPTION, "Odstraní bod z cesty.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_N);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("DELETE"));
    putValue(SMALL_ICON, ImageLoader.seekResIcon("x16/vylet/vyletNevim.png"));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    cestyModel.odeberBod(effectiveMouable());
  }

  @Override
  protected void enablujPokudMaSmysl() {
    setEnabled(cestyModel.isOnVylet(effectiveMouable()));
  }
}
