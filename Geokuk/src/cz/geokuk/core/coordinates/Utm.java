package cz.geokuk.core.coordinates;

public class Utm {

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(ux);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(uy);
    result = prime * result + (int) (temp ^ (temp >>> 32));
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
    Utm other = (Utm) obj;
    if (Double.doubleToLongBits(ux) != Double.doubleToLongBits(other.ux))
      return false;
    if (Double.doubleToLongBits(uy) != Double.doubleToLongBits(other.uy))
      return false;
    return true;
  }



  public double ux;
  public double uy;

  public Utm() {}

  public Utm(double ux, double uy) {
    this.ux = ux;
    this.uy = uy;
  }

  public Utm(Utm mou) {
    ux = mou.ux;
    uy = mou.uy;
  }

  public Utm add(double dxx, double dyy) {
    return new Utm(ux + dxx, uy + dyy);
  }


  public Utm add(Utmd moud) {
    return new Utm(ux + moud.dux, uy + moud.duy);
  }

  public Utm sub(Utmd moud) {
    return new Utm(ux - moud.dux, uy - moud.duy);
  }

  public Utmd sub(Utm mou) {
    return new Utmd(ux - mou.ux, uy - mou.uy);
  }


  public Wgs toWgs() {
    String s = "33 U " + ux + " " + uy;
    double[] utm2LatLon = new CoordinateConversion(33).utm2LatLon(s);
    return new Wgs(utm2LatLon[0], utm2LatLon[1]);
  }

  public Mou toMou() {
    double xx = (ux + 3700000) / 0.03125;
    double yy = (uy - 1300000) / 0.03125;
    return new Mou((int)xx, (int)yy);
  }

  @Override
  public String toString() {
    return "UTM[" + ux  + "," + uy  + "]";
  }

}
