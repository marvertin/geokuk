package cz.geokuk.core.coordinates;

public class Wgs implements Mouable {

  public final double lat;
  public final double lon;


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    long temp;
    temp = Double.doubleToLongBits(lat);
    result = prime * result + (int) (temp ^ temp >>> 32);
    temp = Double.doubleToLongBits(lon);
    result = prime * result + (int) (temp ^ temp >>> 32);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Wgs other = (Wgs) obj;
    if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat)) {
      return false;
    }
    if (Double.doubleToLongBits(lon) != Double.doubleToLongBits(other.lon)) {
      return false;
    }
    return true;
  }


  /**
   * Souřdnice se normalizují na interval <-180,180)
   * @param lat
   * @param lon
   */
  public Wgs(final double lat, final double lon) {
    this.lat = FGeoKonvertor.normalizujUhel(lat);
    this.lon = FGeoKonvertor.normalizujUhel(lon);
  }

  public Wgs(final Wgs wgs) {
    lat = wgs.lat;
    lon = wgs.lon;
  }

  public Wgs add(final double dlat, final double dlon) {
    return new Wgs(lat + dlat, lon + dlon);
  }


  public Wgs add(final Wgsd wgsd) {
    return new Wgs(lat + wgsd.lat, lon + wgsd.lon);
  }

  public Wgs sub(final Wgsd wgsd) {
    return new Wgs(lat - wgsd.lat, lon - wgsd.lon);
  }

  public Wgsd sub(final Wgs wgs) {
    return new Wgsd(lat - wgs.lat, lon - wgs.lon);
  }

  public Utm toUtm() {
    //String s = "33 U " + ux + " " + uy;
    final String s = new CoordinateConversion().latLon2UTM(lat, lon);
    final String[] utm = s.split(" ");
    //zone = Integer.parseInt(utm[0]);
    //String latZone = utm[1];
    final double easting = Double.parseDouble(utm[2]);
    final double northing = Double.parseDouble(utm[3]);
    final int polednikovaZona = Integer.parseInt(utm[0]);
    final char rovnobezkovaZona = utm[3].charAt(0);
    return new Utm(easting, northing, polednikovaZona, rovnobezkovaZona);
  }

  public Mou toMou() {
    return FGeoKonvertor.toMou(this);
  }

  public Mercator toMercator() {
    return FGeoKonvertor.toMercator(this);
  }


  public static String toGeoFormat(final double d) {
    final double stupne = Math.floor(d);
    final double minuty = (d - stupne) * 60.0;
    final String s = String.format("%02d°%06.3f", (int)stupne, minuty).replace(',', '.');
    return s;
  }

  public static String toDdMmSsFormat(final double stupne) {
    final int istupne = (int)Math.floor(stupne);
    final double minuty = (stupne - istupne) * 60.0;
    final int iminuty = (int)Math.floor(minuty);
    final double vteriny = (minuty - iminuty) * 60.0;
    final int ivteriny = (int)Math.floor(vteriny);
    final String s = String.format("%02d°%02d'%02d\"", istupne, iminuty, ivteriny);
    return s;
  }


  @Override
  public String toString() {
    return "N" + toGeoFormat(lat) + " E" + toGeoFormat(lon);
  }

  public static double vzdalenost(final Wgs bod1, final Wgs bod2) {
    return FGeoKonvertor.dalka(bod1, bod2);
  }

  public static String vzdalenostStr(final Wgs bod1, final Wgs bod2) {
    final double dalka = vzdalenost(bod1, bod2);
    String result;
    if (dalka < 10000) {  // vyjádříme v metrech
      result = Math.round(dalka) + " m";
    } else if (dalka < 100000) {
      result = (double)Math.round(dalka / 100) / 10 + " km";
    } else {
      result = Math.round(dalka / 1000) + " km";
    }
    return result;
  }

  public static double azimut(final Wgs odkud, final Wgs bod) {
    final Mou ucur = bod.toMou();
    final Mou upoz = odkud.toMou();
    double uhel = Math.atan2(ucur.xx - upoz.xx, ucur.yy - upoz.yy);
    uhel = uhel * 180 / Math.PI;
    if (uhel < 0) {
      uhel += 360;
    }
    return uhel;
  }

  public static String azimutStr(final Wgs odkud, final Wgs bod) {
    return Math.round(azimut(odkud, bod)) + "°";
  }

  public double vzdalenost(final Wgs bod2) {
    return vzdalenost(this, bod2);
  }

  public String vzdalenostStr(final Wgs bod2) {
    return vzdalenostStr(this, bod2);
  }

  public double azimut(final Wgs bod) {
    return azimut(this, bod);
  }

  public String azimutStr(final Wgs bod) {
    return azimutStr(this, bod);
  }

  @Override
  public Mou getMou() {
    return toMou();
  }

  /**
   * Vrátí, kolik metrů vychází na mou souřanici na dané šířce.
   * @return
   */
  public double metryNaMou() {
    return FGeoKonvertor.metryNaMou(lat);
  }


}
