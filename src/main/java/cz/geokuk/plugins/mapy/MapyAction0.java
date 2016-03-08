package cz.geokuk.plugins.mapy;

import cz.geokuk.framework.ToggleAction0;
import cz.geokuk.plugins.mapy.kachle.EKaType;

public abstract class MapyAction0 extends ToggleAction0 {

  private static final long serialVersionUID = 8106696486908484270L;
  private MapyModel mapyModel;
  private EKaType katype;

  public MapyAction0(EKaType katype) {
    super(katype.getNazev());
    this.katype = katype;
    putValue(SHORT_DESCRIPTION, katype.getPopis());
    if (katype.getKlavesa() != 0) {
      putValue(MNEMONIC_KEY, katype.getKlavesa());
    }
    if (katype.getKeyStroke() != null) {
      putValue(ACCELERATOR_KEY, katype.getKeyStroke());
    }
  }

  public void inject(MapyModel mapyModel) {
    this.mapyModel = mapyModel;
  }
  
  protected EKaType getKaType() {
    return katype;
  }

  public MapyModel getMapyModel() {
    return mapyModel;
  }
  
}
