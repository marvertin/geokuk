package cz.geokuk.plugins.mapy;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;


public class BaseNPodkladAction extends PodkladAction0 {

  private static final long serialVersionUID = -262970268937158619L;

  public BaseNPodkladAction() {
    super("Základní");
    putValue(SHORT_DESCRIPTION, "Základní mapa se silnicemi.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_Z);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('z'));
  }

  @Override
  public EMapPodklad getPodklad() {
    return EMapPodklad.BASE_N;
  }


}
