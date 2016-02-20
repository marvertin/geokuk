package cz.geokuk.plugins.mapy;

import java.awt.event.KeyEvent;

public class Ophot0406PodkladAction extends PodkladAction0 {

  private static final long serialVersionUID = -262970268937158619L;

  public Ophot0406PodkladAction() {
    super("Letecká 2006");
    putValue(SHORT_DESCRIPTION, "Starší fotomapa");
    putValue(MNEMONIC_KEY, KeyEvent.VK_6);
  }

  @Override
  public EMapPodklad getPodklad() {
    return EMapPodklad.OPHOTO0406;
  }


}
