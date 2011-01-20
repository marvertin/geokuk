package cz.geokuk.plugins.mapy.kachle;

import java.awt.event.KeyEvent;

import cz.geokuk.core.napoveda.NapovedaModel;
import cz.geokuk.core.napoveda.NapovedaModelChangedEvent;
import cz.geokuk.framework.ToggleAction0;


public class OnlineModeAction extends ToggleAction0 {
  private static final long serialVersionUID = -3631232428454275961L;

  private NapovedaModel napovedaModel;
  
  
  public OnlineModeAction() {
    super("Online");
    putValue(SHORT_DESCRIPTION, "Režim online, kdy se stahují mapy z webu, v offlinu se berou mapy pouze z diskové keše.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_O);
    
  }

  public void onEvent(NapovedaModelChangedEvent event) { 
    setSelected(napovedaModel.isOnlineMode());
  }

  @Override
  protected void onSlectedChange(boolean nastaveno) {
    napovedaModel.setOnlineMode(nastaveno);
  }
  
  public void inject(NapovedaModel napovedaModel) {
    this.napovedaModel = napovedaModel;
  }

  


}
