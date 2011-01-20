package cz.geokuk.core.coordinates;

public class Wgs {

  public final double lat;
  public final double lon;


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(lat);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(lon);
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
    Wgs other = (Wgs) obj;
    if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat))
      return false;
    if (Double.doubleToLongBits(lon) != Double.doubleToLongBits(other.lon))
      return false;
    return true;
  }


  public Wgs(double lat, double lon) {
    this.lat = lat;
    this.lon = lon;
  }

  public Wgs(Wgs wgs) {
    lat = wgs.lat;
    lon = wgs.lon;
  }

  public Wgs add(double dlat, double dlon) {
    return new Wgs(lat + dlat, lon + dlon);
  }


  public Wgs add(Wgsd wgsd) {
    return new Wgs(lat + wgsd.lat, lon + wgsd.lon);
  }

  public Wgs sub(Wgsd wgsd) {
    return new Wgs(lat - wgsd.lat, lon - wgsd.lon);
  }

  public Wgsd sub(Wgs wgs) {
    return new Wgsd(lat - wgs.lat, lon - wgs.lon);
  }

  public Utm toUtm() {
    //String s = "33 U " + ux + " " + uy;
    String s = new CoordinateConversion(33).latLon2UTM(lat, lon);
    String[] utm = s.split(" ");
    //zone = Integer.parseInt(utm[0]);
    //String latZone = utm[1];
    double easting = Double.parseDouble(utm[2]);
    double northing = Double.parseDouble(utm[3]);
    return new Utm(easting, northing);
  }
  
  public Mou toMou() {
    return toUtm().toMou();
  }

  public static String toGeoFormat(double d) {
    double stupne = Math.floor(d);
    double minuty = (d - stupne) * 60.0;
    String s = String.format("%02d°%06.3f", (int)stupne, minuty);
    return s;
  }

  public static String toDdMmSsFormat(double stupne) {
    int istupne = (int)Math.floor(stupne);
    double minuty = (stupne - istupne) * 60.0;
    int iminuty = (int)Math.floor(minuty);
    double vteriny = (minuty - iminuty) * 60.0;
    int ivteriny = (int)Math.floor(vteriny);
    String s = String.format("%02d°%02d'%02d\"", istupne, iminuty, ivteriny);
    return s;
  }

  
  @Override
  public String toString() {
    return "N" + toGeoFormat(lat) + " E" + toGeoFormat(lon);
  }

	public static double vzdalenost(Wgs bod1, Wgs bod2) {
    Utm ucur = bod1.toUtm();
    Utm upoz = bod2.toUtm();
    double dalka = Math.hypot(ucur.ux - upoz.ux, ucur.uy - upoz.uy);
    return dalka;
  }

	public static String vzdalenostStr(Wgs bod1, Wgs bod2) {
    double dalka = vzdalenost(bod1, bod2);
    String result;
    if (dalka < 10000) {  // vyjádříme v metrech
      result = Math.round(dalka) + " m";
    } else if (dalka < 100000) {
      result = ((double)Math.round(dalka / 100) / 10) + " km";
    } else {
      result = Math.round(dalka / 1000) + " km";
    }
    return result;
  }

	public static double azimut(Wgs odkud, Wgs bod) {
    Utm ucur = bod.toUtm();
    Utm upoz = odkud.toUtm();
    double uhel = Math.atan2(ucur.ux - upoz.ux, ucur.uy - upoz.uy);
    uhel = uhel * 180 / Math.PI;
    if (uhel < 0) uhel += 360;
    return uhel;
  }

	public static String azimutStr(Wgs odkud, Wgs bod) {
    return Math.round(azimut(odkud, bod)) + "°";
  }

	public double vzdalenost(Wgs bod2) {
		return vzdalenost(this, bod2);
  }

	public String vzdalenostStr(Wgs bod2) {
		return vzdalenostStr(this, bod2);
  }

	public double azimut(Wgs bod) {
		return azimut(this, bod);
  }

	public String azimutStr(Wgs bod) {
		return azimutStr(this, bod);
  }
	
}
