package cz.geokuk.plugins.mapy;

import java.awt.event.KeyEvent;


public class Ophot0203PodkladAction extends PodkladAction0 {

  private static final long serialVersionUID = -262970268937158619L;

  public Ophot0203PodkladAction() {
    super("Fotomapa 2002-2003");
    putValue(SHORT_DESCRIPTION, "Starší fotomapa");
    putValue(MNEMONIC_KEY, KeyEvent.VK_2);
  }

  @Override
  public EMapPodklad getPodklad() {
    return EMapPodklad.OPHOTO0203;
  }


}
