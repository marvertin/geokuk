package cz.geokuk.plugins.mapy;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;


public class Army2PodkladAction extends PodkladAction0 {

  private static final long serialVersionUID = -262970268937158619L;

  public Army2PodkladAction() {
    super("Historická");
    putValue(SHORT_DESCRIPTION, "Historická mapa z let 1836-52");
    putValue(MNEMONIC_KEY, KeyEvent.VK_H);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('h'));
  }


  @Override
  public EMapPodklad getPodklad() {
    return EMapPodklad.ARMY2;
  }

  

}
