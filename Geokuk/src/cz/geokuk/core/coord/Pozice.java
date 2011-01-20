/**
 * 
 */
package cz.geokuk.core.coord;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.plugins.kesoid.Wpt;

/**
 * @author veverka
 *
 */
public class Pozice {

  private final Wpt wpt;
  private final Wgs wgs;

  /*
   * Bez pozice
   */
  public Pozice () {
    wpt = null;
    wgs = null;
  }
  
  /**
   * @param aWgs
   */
  public Pozice(Wgs aWgs) {
    super();
    wgs = aWgs;
    wpt = null;
  }
  
  /**
   * @param aWpt
   */
  public Pozice(Wpt aWpt) {
    super();
    wpt = aWpt;
    wgs = wpt.getWgs();
  }

  /**
   * @return the wpt
   */
  public Wpt getWpt() {
    return wpt;
  }

  /**
   * @return the wgs
   */
  public Wgs getWgs() {
    return wgs;
  }
  
  public boolean isWaypoint() {
    return wpt != null;
  }
  
  public boolean isNoPosition() {
    return wgs == null;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((wgs == null) ? 0 : wgs.hashCode());
    result = prime * result + ((wpt == null) ? 0 : wpt.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Pozice other = (Pozice) obj;
    if (wgs == null) {
      if (other.wgs != null)
        return false;
    } else if (!wgs.equals(other.wgs))
      return false;
    if (wpt == null) {
      if (other.wpt != null)
        return false;
    } else if (!wpt.equals(other.wpt))
      return false;
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Pozice [wgs=" + wgs + ", wpt=" + wpt + "]";
  }
  
  
}
