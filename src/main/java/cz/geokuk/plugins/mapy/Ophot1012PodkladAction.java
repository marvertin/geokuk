package cz.geokuk.plugins.mapy;

public class Ophot1012PodkladAction extends PodkladAction0 {

  private static final long serialVersionUID = -262970268937158619L;

  public Ophot1012PodkladAction() {
    super("Letecká 2012");
    putValue(SHORT_DESCRIPTION, "Starší fotomapa");
  }

  @Override
  public EMapPodklad getPodklad() {
    return EMapPodklad.OPHOTO1012;
  }


}
