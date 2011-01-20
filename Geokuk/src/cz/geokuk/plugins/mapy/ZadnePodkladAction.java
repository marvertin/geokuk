package cz.geokuk.plugins.mapy;



public class ZadnePodkladAction extends PodkladAction0 {

  private static final long serialVersionUID = -262970268937158619L;

  public ZadnePodkladAction() {
    super("žádná");
    putValue(SHORT_DESCRIPTION, "Mapy bez podkladu.");
    //putValue(MNEMONIC_KEY, KeyEvent.VK_Z);
    //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('z'));
  }

  @Override
  public EMapPodklad getPodklad() {
    return EMapPodklad.ZADNE;
  }


}
