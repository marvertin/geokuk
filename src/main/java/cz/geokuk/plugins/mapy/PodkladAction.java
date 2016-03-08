package cz.geokuk.plugins.mapy;

import cz.geokuk.plugins.mapy.kachle.EKaType;

public class PodkladAction extends MapyAction0 {

  private static final long serialVersionUID = 8106696486908484270L;

  public PodkladAction(EKaType katype) {
    super(katype);
  }

  public void onEvent(ZmenaMapNastalaEvent event) {
    setSelected(getMapyModel().getPodklad() == getPodklad());
  }
  
  public EKaType getPodklad() {
    return super.getKaType();
  }

  @Override
  protected void onSlectedChange(boolean nastaveno) {
    if (nastaveno) {
      getMapyModel().setPodklad(getPodklad());
    }
  }
  
}
