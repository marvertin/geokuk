package cz.geokuk.plugins.cesty.akce;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.core.coordinates.Uchopenec;
import cz.geokuk.img.ImageLoader;
import cz.geokuk.plugins.kesoid.Wpt;

public class VyletNeAction extends VyletActionIndividual0 {

  private static final long serialVersionUID = -2637836928166450446L;

  public VyletNeAction(Uchopenec uchopenec) {
    super(uchopenec);
    putValue(NAME, "Přidej na ignorlist");
    putValue(SHORT_DESCRIPTION, "Zařadí kešoid mezi kešoidy ignorované. Pokud byl výlet v nějaké cestě, odstraní ho.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_I);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('-'));
    putValue(SMALL_ICON, ImageLoader.seekResIcon("x16/vylet/vyletNe.png"));

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    cestyModel.addToIgnoreList(effectiveMouable());
  }

  @Override
  protected void enablujPokudMaSmysl() {
    setEnabled(! cestyModel.isOnIgnoreList(effectiveMouable()) && (effectiveMouable() instanceof Wpt));
  }
}
