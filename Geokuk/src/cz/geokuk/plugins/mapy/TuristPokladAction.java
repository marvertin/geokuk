package cz.geokuk.plugins.mapy;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;


public class TuristPokladAction extends PodkladAction0 {

  private static final long serialVersionUID = -262970268937158619L;

  public TuristPokladAction() {
    super("Turistická");
    putValue(SHORT_DESCRIPTION, "Turistická mapa.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_T);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('t'));
  }

  @Override
  public EMapPodklad getPodklad() {
    return EMapPodklad.TURIST;
  }


}
