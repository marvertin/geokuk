/**
 * 
 */
package cz.geokuk.plugins.kesoid.mapicon;


import javax.swing.Action;
import javax.swing.Icon;

import cz.geokuk.framework.ToggleAction0;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;


/**
 * @author veverka
 *
 */
public class SwitchSadaAction extends ToggleAction0  {

  private static final long serialVersionUID = -8054017274338240706L;
  private KesoidModel kesoidModel;
  private final ASada sada;

  /**
   * 
   */
  public SwitchSadaAction(ASada sada, Icon ikonaSady) {
    super(sada.name());
    this.sada = sada;
    super.putValue(SMALL_ICON,  ikonaSady);
    putValue(Action.SHORT_DESCRIPTION, "Výběr sady ikon: " + sada);


  }


  //  /* (non-Javadoc)
  //   * @see cz.geokuk.program.AfterEventReceiverRegistrationInit#initAfterEventReceiverRegistration()
  //   */
  //  @Override
  //  public void initAfterEventReceiverRegistration() {
  //    super.putValue(NAME, sestavJmeno());
  //    super.putValue(SMALL_ICON,  ikonBag.seekIkon(ikonBag.getGenom().getGenotypProAlelu(alela)));
  //    super.putValue(SHORT_DESCRIPTION, sestavJmeno());
  //  }

  public void inject(KesoidModel kesoidModel) {
    this.kesoidModel = kesoidModel;
  }


  @Override
  protected void onSlectedChange(boolean nastaveno) {
    if (nastaveno) {
      kesoidModel.setJmenoAktualniSadyIkon(sada);
    }
  }



}



