package cz.geokuk.plugins.mapy;

import java.awt.event.KeyEvent;

public class AquaticTuristPodkladAction extends PodkladAction0 {

  private static final long serialVersionUID = 43564813465663197L;

  public AquaticTuristPodkladAction() {
    super("Letní");
    putValue(SHORT_DESCRIPTION, "Letní turistická mapa.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_L);
    //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('w'));
  }

  @Override
  public EMapPodklad getPodklad() {
    return EMapPodklad.TURIST_AQUATIC;
  }
}
