package cz.geokuk.plugins.lovim;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.img.ImageLoader;
import cz.geokuk.plugins.kesoid.Kesoid;

public class VyletNeAction extends VyletActionIndividual0 {

  private static final long serialVersionUID = -2637836928166450446L;

  public VyletNeAction(Kesoid kes) {
    super("Ignoruj", kes);
    putValue(SHORT_DESCRIPTION, "Zařadí keš mezi keše ignorované.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_I);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('-'));
    putValue(SMALL_ICON, ImageLoader.seekResIcon("x16/vylet/vyletNe.png"));

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    vyletModel.add(EVylet.NE, kesoid());
  }

  @Override
  protected void enablujPokudMaSmysl() {
    setEnabled(vyletModel.get(kesoid()) != EVylet.NE);
  }
}
