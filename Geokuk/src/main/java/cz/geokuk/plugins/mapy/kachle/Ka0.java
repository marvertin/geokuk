package cz.geokuk.plugins.mapy.kachle;

abstract class Ka0 {

  private final KaLoc loc;

  public Ka0(KaLoc loc) {
    this.loc = loc;
  }

  public abstract String typToString();

  public KaLoc getLoc() {
    return loc;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((loc == null) ? 0 : loc.hashCode());
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
    Ka0 other = (Ka0) obj;
    if (loc == null) {
      if (other.loc != null)
        return false;
    } else if (!loc.equals(other.loc))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Ka0 [loc=" + loc + "]";
  }
}
