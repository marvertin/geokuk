package cz.geokuk.plugins.mapy.kachle;

import cz.geokuk.core.coordinates.Mou;


public class KaLoc {

  private final Mou mou;  // souřadnice rohu jihozápadního
  private final int moumer;




  public KaLoc(Mou mou, int moumer) {
    super();
    this.mou = mou;
    this.moumer = moumer;
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((mou == null) ? 0 : mou.hashCode());
    result = prime * result + getMoumer();
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
    KaLoc other = (KaLoc) obj;
    if (mou == null) {
      if (other.mou != null)
        return false;
    } else if (!mou.equals(other.mou))
      return false;
    if (getMoumer() != other.getMoumer())
      return false;
    return true;
  }




  public void addToUrl(StringBuilder sb) {
    sb.append(getMoumer());
    sb.append('_');
    sb.append(Integer.toHexString(mou.xx));
    sb.append('_');
    sb.append(Integer.toHexString(mou.yy));
  }


  /**
   * Vrátí souřdnice jihozápadního rohu
   * @return
   */
  public Mou getMou() {
    return mou;
  }



  public int getMoumer() {
    return moumer;
  }


  @Override
  public String toString() {
    return "KaLoc [mou=" + mou + ", moumer=" + moumer + "]";
  }

}
