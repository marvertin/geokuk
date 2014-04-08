/**
 * 
 */
package cz.geokuk.plugins.kesoid.mvc;

import cz.geokuk.framework.DialogOpeningAction0;
import cz.geokuk.framework.JMyDialog0;

/**
 * @author veverka
 *
 */
public class InformaceoZdrojichAction extends DialogOpeningAction0 {

  private static final long serialVersionUID = -6267501966077611071L;

  /**
   * 
   */
  public InformaceoZdrojichAction() {
    super("Přehled zdrojů...");
    super.putValue(SHORT_DESCRIPTION, "Zobrazí dialog s informací o zdrojích ,z nichž byly načteny kešoidy a umožní vybírat, ze kterých zdrojů se bude číst.");
    setEnabled(false);
  }

  /* (non-Javadoc)
   * @see cz.geokuk.framework.DialogOpeningAction0#createDialog()
   */
  @Override
  public JMyDialog0 createDialog() {
    return new JInformaceOZdrojichDialog();
  }

  public void onEvent(KeskyNactenyEvent event) {
    setEnabled(true);
  }
}
