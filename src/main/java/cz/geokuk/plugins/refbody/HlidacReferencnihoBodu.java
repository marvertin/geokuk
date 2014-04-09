/**
 * 
 */
package cz.geokuk.plugins.refbody;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coord.PoziceChangedEvent;
import cz.geokuk.core.coord.Poziceq;
import cz.geokuk.core.coord.VyrezChangedEvent;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.EventFirer;

/**
 * Mění událost změny sředu mapy a změnu pozice na událost jedinou,
 * podle které se rohodují referenceři.
 * @author veverka
 *
 */
public class HlidacReferencnihoBodu {

  private Poziceq poziceq = new Poziceq();
  private Wgs stredMapy;
  private Object minulyStredHledani;
  private Coord moord;

  public void onEvent(VyrezChangedEvent aEvent) {
    moord = aEvent.getMoord();
    setStredMapy(aEvent.getMoord().getMoustred().toWgs());
    fairujSpolecnou(aEvent.getEventFirer());
  }

  public void onEvent(PoziceChangedEvent aEvent) {
    poziceq = aEvent.poziceq;
    fairujSpolecnou(aEvent.getEventFirer());
  }


  private void setStredMapy(Wgs wgs) {
    stredMapy = wgs;
  }

  private void fairujSpolecnou(EventFirer eventFirer) {
    Wgs referencniBod = getReferencniBod();
    if (minulyStredHledani != null && minulyStredHledani.equals(referencniBod)) return;
    eventFirer.fire(new ReferencniBodSeZmenilEvent(referencniBod, moord));
    minulyStredHledani = referencniBod;
  }


  private Wgs getReferencniBod() {
    if (poziceq.isNoPosition()) {
        return stredMapy;
    }
    else {
        return poziceq.getWgs();
    }
  }


}
