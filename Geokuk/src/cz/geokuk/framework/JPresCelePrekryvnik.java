/**
 * 
 */
package cz.geokuk.framework;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import cz.geokuk.core.coord.JCoordPrekryvnik0;
import cz.geokuk.core.coord.VyrezChangedEvent;
import cz.geokuk.core.coord.VyrezModel;

/**
 * @author veverka
 *
 */
public class JPresCelePrekryvnik extends JCoordPrekryvnik0 implements AfterEventReceiverRegistrationInit {
  private static final long serialVersionUID = -5996655830197513951L;
  private VyrezModel vyrezModel;

  public void onEvent(VyrezChangedEvent event) {
    // V hlavním překryvníku sledujeme pohyb výřezu a podle toho posouváme pohled
    setSoord(event.getMoord());
  }

  @Override
  public void initAfterEventReceiverRegistration() {
    // Listener zajístí, že se změna šířky a výšky pošle všem zájemcům
    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        vyrezModel.setVelikost(getWidth(), getHeight());
      }
    });
  }

  public void inject(VyrezModel vyrezModel) {
    this.vyrezModel = vyrezModel;
  }

}
