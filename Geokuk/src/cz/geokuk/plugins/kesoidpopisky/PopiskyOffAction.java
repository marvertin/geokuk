package cz.geokuk.plugins.kesoidpopisky;


import java.awt.event.ActionEvent;

import cz.geokuk.framework.Action0;


public class PopiskyOffAction extends Action0 {

  private static final long serialVersionUID = -7547868179813232769L;
  private PopiskyModel popiskyModel;
  
  

  public PopiskyOffAction() {
    super("Vypnout popisky");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    popiskyModel.visible.setOnoff(false);
  }


  public void inject (PopiskyModel popiskyModel) {
    this.popiskyModel = popiskyModel;
  }

  public void onEvent(PopiskyOnoffEvent event) {
    setEnabled(event.isOnoff());
  }
  
}
