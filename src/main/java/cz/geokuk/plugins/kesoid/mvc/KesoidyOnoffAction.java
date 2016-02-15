package cz.geokuk.plugins.kesoid.mvc;


import cz.geokuk.framework.ToggleAction0;


public class KesoidyOnoffAction extends ToggleAction0  {

  private static final long serialVersionUID = -7547868179813232769L;
  private KesoidModel kesoidModel;



  public KesoidyOnoffAction() {
    super("Zobrazovat kešoidy");
    //putValue(MNEMONIC_KEY, KeyEvent.VK_Y);
    putValue(SHORT_DESCRIPTION, "Řídí, zda se vůbec budou zorbazovat kešoidy.");
    //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
  }


  public void inject (KesoidModel kesoidModel) {
    this.kesoidModel = kesoidModel;
  }

  public void onEvent(KesoidOnoffEvent event) {
    setSelected(event.isOnoff());
  }

  @Override
  protected void onSlectedChange(boolean nastaveno) {
    kesoidModel.setOnoff(nastaveno);
  }



}
