package cz.geokuk.plugins.mapy;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;


public class OphototPodkladAction extends PodkladAction0 {

  private static final long serialVersionUID = -262970268937158619L;

  public OphototPodkladAction() {
    super("Fotomapa");
    putValue(SHORT_DESCRIPTION, "Ortho foto mapa");
    putValue(MNEMONIC_KEY, KeyEvent.VK_F);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('f'));
  }

  @Override
  public EMapPodklad getPodklad() {
    return EMapPodklad.OPHOTO;
  }


}
