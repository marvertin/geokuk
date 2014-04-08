package cz.geokuk.plugins.mapy.kachle;

import java.util.EnumSet;


public class KaSet {

  private final EnumSet<EKaType> kts;

  public KaSet(EnumSet<EKaType> kts) {
    this.kts = kts;
  }

  public EnumSet<EKaType> getKts() {
    return kts.clone();
  }

  public EKaType getPodklad() {
    if (kts == null) return null;
    for (EKaType  kt : kts) {
      if (kt.isPodklad()) return kt;
    }
    return null;
  }

//  public boolean isExaclyOnePodklad() {
//    if (kts == null) return false;
//    int n = 0;
//    for (EKachloType  kt : kts) {
//      if (kt.isPodklad()) n++;
//    }
//    return n == 1;
//  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((kts == null) ? 0 : kts.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    KaSet other = (KaSet) obj;
    if (kts == null) {
      if (other.kts != null)
        return false;
    } else if (!kts.equals(other.kts))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "KachloSet [kts=" + kts + "]";
  }


}
