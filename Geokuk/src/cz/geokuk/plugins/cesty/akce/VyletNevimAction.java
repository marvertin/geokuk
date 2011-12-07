package cz.geokuk.plugins.cesty.akce;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.core.coordinates.Uchopenec;
import cz.geokuk.img.ImageLoader;

public class VyletNevimAction extends VyletActionIndividual0 {

  private static final long serialVersionUID = 1L;

  public VyletNevimAction(Uchopenec uchopenec) {
    super(uchopenec);
    putValue(NAME, "Smaž z cesty");
    putValue(SHORT_DESCRIPTION, "Odstraní bod z cesty. Pokud je to kešoid a není na cestě, ale je na ignorelistu, odstraní ho odstud.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_N);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("DELETE"));
    putValue(SMALL_ICON, ImageLoader.seekResIcon("x16/vylet/vyletNevim.png"));
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    vyletModel.removeFromBoth(effectiveMouable());
  }

  @Override
  protected void enablujPokudMaSmysl() {
    setEnabled(vyletModel.isOnVylet(effectiveMouable()) || vyletModel.isOnIgnoreList(effectiveMouable()));
  }
}
