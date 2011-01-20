package cz.geokuk.core.coordinates;

public class Moud {

  public int dxx;
  public int dyy;

  public Moud() {}

  public Moud(int dxx, int dyy) {
    this.dxx = dxx;
    this.dyy = dyy;
  }

  public Moud(Moud mou) {
    dxx = mou.dxx;
    dyy = mou.dyy;
  }

  public Moud add(int dxx, int dyy) {
    return new Moud(this.dxx + dxx, this.dyy + dyy);
  }

  public Moud sub(int dxx, int dyy) {
    return new Moud(this.dxx - dxx, this.dyy - dyy);
  }

  @Override
  public int hashCode() {
    return dxx ^ dyy;
  }


  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (! (obj instanceof Moud)) return false;
    Moud m = (Moud) obj;
    return dxx == m.dxx && dyy == m.dyy;
  }


  @Override
  public String toString() {
    return "[" + Integer.toHexString(dxx)  + "," + Integer.toHexString(dyy)  + "]";
  }

  public boolean isAnyRozmerEmpty() {
    return dxx <= 0 || dyy <= 0;
  }
}
