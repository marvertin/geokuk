package cz.geokuk.plugins.lovim;

import cz.geokuk.framework.Event0;
import cz.geokuk.plugins.kesoid.Kesoid;

public class VyletChangeEvent extends Event0<VyletModel> {

  private final VyletModel vyletModel;

  private final Kesoid kes;
  private final EVylet evyl;
  private final EVylet evylPuvodni;


  VyletChangeEvent(VyletModel vylet, Kesoid kes, EVylet evyl, EVylet evylPuvodni) {
    super();
    this.vyletModel = vylet;
    this.evyl = evyl;
    this.kes = kes;
    this.evylPuvodni = evylPuvodni;
  }

  /**
   * Zda se změnil celý výlet, pokud ano, tak se nsmí brát jednotlivé změny 
   * Většinout to bude znamenat načtení výletu.
   * @return
   */
  public boolean isVelkaZmena() {
    return evyl == null || kes == null;
  }

  public EVylet getEvyl() {
    return evyl;
  }

  public Kesoid getKes() {
    return kes;
  }

  public Kesoid getAno() {
    return evyl == EVylet.ANO ? kes : null;
  }

  public Kesoid getNe() {
    return evyl == EVylet.NE ? kes : null;
  }

  public VyletModel getVyletModel() {
    return vyletModel;
  }

  public EVylet getEvylPuvodni() {
    return evylPuvodni;
  }

}
