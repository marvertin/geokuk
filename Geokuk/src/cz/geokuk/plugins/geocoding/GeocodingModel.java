/**
 * 
 */
package cz.geokuk.plugins.geocoding;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.core.hledani.HledaciSluzba;
import cz.geokuk.core.hledani.RefreshorVysledkuHledani;
import cz.geokuk.core.napoveda.NapovedaModelChangedEvent;
import cz.geokuk.framework.Model0;
import cz.geokuk.plugins.refbody.ReferencniBodSeZmenilEvent;

/**
 * @author veverka
 *
 */
public class GeocodingModel extends Model0  {

  private final Hledac hledac = new Hledac();
  private HledaciSluzba hledaciSluzba;
  private boolean onlineMode;
  private Wgs referencniBod;

  public synchronized void spustHledani(Wgs wgs, RefreshorVysledkuHledani<Nalezenec> refreshor) {
    spustHledani(wgs.lat + "," + wgs.lon, refreshor);
  }

  public synchronized void spustHledani(String coHledat, RefreshorVysledkuHledani<Nalezenec> refreshor) {
    if (onlineMode) {
      HledaciPodminka hledaciPodminka = new HledaciPodminka();
      if (referencniBod == null) return;
      hledaciPodminka.setStredHledani(referencniBod);
      hledaciPodminka.setVzorek(coHledat);
      hledaciSluzba.spustHledani(hledac, hledaciPodminka, refreshor);
    }
  }

  /* (non-Javadoc)
   * @see cz.geokuk.framework.Model0#initAndFire()
   */
  @Override
  protected void initAndFire() {
    //asi není co dělat
  }


  public void onEvent(NapovedaModelChangedEvent event) {
    onlineMode = event.getModel().isOnlineMode();
  }

  public void inject(HledaciSluzba hledaciSluzba) {
    this.hledaciSluzba = hledaciSluzba;
  }

  public void onEvent(ReferencniBodSeZmenilEvent event) {
    referencniBod = event.wgs;
  }
}
