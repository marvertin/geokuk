package cz.geokuk.core.render;

import cz.geokuk.core.napoveda.NapovedaModelChangedEvent;
import cz.geokuk.framework.DialogOpeningAction0;
import cz.geokuk.framework.JMyDialog0;


public class RenderAction extends DialogOpeningAction0 {

  private static final long serialVersionUID = -5465641756515262340L;

  public RenderAction() {
    super("Rendrovat...");
    putValue(SHORT_DESCRIPTION, "Zobrazí dialog s možností vyrendrovat mapový podklad libovolné velikosti i s kešemi a mřížkami s možností kalibrací pro GoogleEarth, OziExplorer a jiné programy.");
  }

  public void onEvent(NapovedaModelChangedEvent event) {
    //    setEnabled(event.getModel().isOnlineMode());
  }

  /* (non-Javadoc)
   * @see cz.geokuk.framework.DialogOpeningAction0#createDialog()
   */
  @Override
  public JMyDialog0 createDialog() {
    return new JRenderDialog();
  }
}
