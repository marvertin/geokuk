package cz.geokuk.plugins.mapy.kachle;

import java.awt.event.KeyEvent;

import cz.geokuk.framework.ToggleAction0;


public class UkladatMapyNaDiskAction extends ToggleAction0 {
  private static final long serialVersionUID = -3631232428454275961L;

  public KachleModel kachleModel;
  
  
  public UkladatMapyNaDiskAction() {
    super("Ukládat mapy");
    putValue(SHORT_DESCRIPTION, "Mapy se ukládají do specifické složky na disk, aby se nemusely pokaždé stahovat.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_U);
    
  }

  public void onEvent(KachleModelChangeEvent event) { 
    setSelected(kachleModel.isUkladatMapyNaDisk());
  }

  @Override
  protected void onSlectedChange(boolean nastaveno) {
    kachleModel.setUkladatMapyNaDisk(nastaveno);
  }
  
  public void inject(KachleModel kachleModel) {
    this.kachleModel = kachleModel;
  }

  


}
