package cz.geokuk.core.hledani;

import cz.geokuk.core.coordinates.Wgs;

public abstract class HledaciPodminka0 {
  private Wgs stredHledani = new Wgs(49.284, 16.3563);
  private String vzorek;

  public Wgs getStredHledani() {
    return stredHledani;
  }

  public void setStredHledani(Wgs stredHledani) {
    this.stredHledani = stredHledani;
  }

  /**
   * @return the vzorek
   */
  public String getVzorek() {
    return vzorek;
  }

  /**
   * @param aVzorek the vzorek to set
   */
  public void setVzorek(String aVzorek) {
    vzorek = aVzorek;
  }


}
