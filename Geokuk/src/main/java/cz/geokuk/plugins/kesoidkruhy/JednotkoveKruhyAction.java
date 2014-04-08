package cz.geokuk.plugins.kesoidkruhy;


import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.framework.ToggleAction0;


public class JednotkoveKruhyAction extends ToggleAction0  {

  private static final long serialVersionUID = -7547868179813232769L;
  private KruhyModel model;
  
  

  public JednotkoveKruhyAction() {
    super("Jednotokové kruhy");
    putValue(SHORT_DESCRIPTION, "Kruhy mají velikost 10m, 100m, 10km atd.");
    putValue(MNEMONIC_KEY, KeyEvent.VK_J);
    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("alt F8"));
  }


  public void inject (KruhyModel model) {
    this.model = model;
  }

 
  public void onEvent(KruhyPreferencesChangeEvent event) {
    setSelected(event.kruhy.isJednotkovaVelikost());
  }


  @Override
  protected void onSlectedChange(boolean onoff) {
    KruhySettings data = model.getData();
    data.setJednotkovaVelikost(onoff);
    model.setData(data);
  }

  
  
}
