package cz.geokuk.core.coord;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Mouable;
import cz.geokuk.core.coordinates.Utm;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.vylety.data.Bod;

public class Poziceq {

  /** Sem pozice vede */
  private final Mouable mouable;

  /** Tak sem to bylo dáno bez ohledu na to, jak se MOu mění v mouabblu */
  private final Mou originalMou;

  public Poziceq() {
    mouable = null;
    originalMou = null;
  }

  /**
   * @param aWgs
   */
  public Poziceq(Mouable mouable) {
    this.mouable = mouable;
    originalMou = mouable.getMou();
    assert this.mouable != null;
    assert originalMou != null;
  }

  public boolean isNoPosition() {
    return mouable == null;
  }


  public Mouable getPoziceMouable() {
    if (isNoPosition()) return null;
    return mouable.getMou().equals(originalMou) ? mouable : originalMou;
  }

  public Mou getPoziceMou() {
    Mouable poziceMouable = getPoziceMouable();
    return poziceMouable == null ? null : poziceMouable.getMou();
  }

  public Wpt getWpt() {
    Mouable mouable = getPoziceMouable();
    if (mouable instanceof Wpt) {
      Wpt wpt = (Wpt) mouable;
      return wpt;
    }
    return null;
  }

  public Bod getBod() {
    Mouable mouable = getPoziceMouable();
    if (mouable instanceof Bod) {
      Bod bod = (Bod) mouable;
      return bod;
    }
    return null;
  }


  public Kesoid getKesoid() {
    Wpt wpt = getWpt();
    if (wpt == null) return null;
    return wpt.getKesoid();
  }


  public Wgs getWgs() {
    Mou mou = getPoziceMou();
    return mou == null ? null : mou.toWgs();
  }

  public Utm getUtm() {
    Mou mou = getPoziceMou();
    return mou == null ? null : mou.toUtm();
  }


}
