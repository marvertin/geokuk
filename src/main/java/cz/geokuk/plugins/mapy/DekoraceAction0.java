package cz.geokuk.plugins.mapy;

import java.util.EnumSet;

import cz.geokuk.framework.ToggleAction0;

public abstract class DekoraceAction0 extends ToggleAction0 {

  private static final long serialVersionUID = 8106696486908484270L;
  protected MapyModel mapyModel;

  public DekoraceAction0(String name) {
    super(name);
  }

  public void inject(MapyModel mapyModel) {
    this.mapyModel = mapyModel;
  }
  
  protected abstract EMapDekorace getDekorace();
  
  protected void nastavDekoraci(boolean onoff) {
    EnumSet<EMapDekorace> dekoraces = mapyModel.getDekorace();
    if (onoff) dekoraces.add(getDekorace()); 
        else dekoraces.remove(getDekorace());
    mapyModel.setDekorace(dekoraces);
  }
  
  @Override
  protected void onSlectedChange(boolean nastaveno) {
    nastavDekoraci(nastaveno);
  }
  
  public void onEvent(ZmenaMapNastalaEvent event) {
    EnumSet<EMapDekorace> dekoraces = mapyModel.getDekorace();
    boolean nastaveno = dekoraces.contains(getDekorace());
    setSelected(nastaveno);
  }
  
}
