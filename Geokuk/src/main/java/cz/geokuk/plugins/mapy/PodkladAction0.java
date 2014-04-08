package cz.geokuk.plugins.mapy;

import cz.geokuk.framework.ToggleAction0;

public abstract class PodkladAction0 extends ToggleAction0 {

  private static final long serialVersionUID = 8106696486908484270L;
  private MapyModel mapyModel;

  public PodkladAction0(String name) {
    super(name);
  }

  public void inject(MapyModel mapyModel) {
    this.mapyModel = mapyModel;
  }
  
  public void onEvent(ZmenaMapNastalaEvent event) {
    setSelected(mapyModel.getPodklad() == getPodklad());
  }
  
  public abstract EMapPodklad getPodklad();

  @Override
  protected void onSlectedChange(boolean nastaveno) {
    if (nastaveno) {
      mapyModel.setPodklad(getPodklad());
    }
  }
  
}
