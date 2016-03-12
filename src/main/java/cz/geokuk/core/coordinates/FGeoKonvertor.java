package cz.geokuk.core.coordinates;

import static java.lang.Math.PI;
import static java.lang.Math.atan;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.tan;

/**
 * Konvertor geografických souřadnic
 * @author tatinek
 *
 */
public class FGeoKonvertor {

  public static final double RZ = 6378137;
  public static final double OBVOD_ZEME = 2 * PI * RZ; // v metrech
  public static final double OBVOD_ZEME_V_MOU = 0x1_0000_0000l;

  /** Merkátoryvy metry, skutečné metry jsou to jen na rovníku */
  public static final double Q_METRY_NA_MOU = OBVOD_ZEME / OBVOD_ZEME_V_MOU;

  public static final double STUPEN_NA_ROVNIKU_V_METRECH = PI / 180 * RZ;
  
  private static UtmMgrsWgsConvertor utmWgsConvertor = new UtmMgrsWgsConvertor();

  public static double normalizujUhel(double uhel) {
    // TODO optimálněji, ale možná to v praxi stačí
    if (Math.abs(uhel) > 10000) {
      throw new RuntimeException("Proč se má normalizovat takový obludný úhel: " + uhel + " stupňů");
    }
    while (uhel >= 180) {
      uhel -= 360;
    }
    while (uhel < -180) {
      uhel += 360;
    }
    return uhel;
  }

  public static Mercator toMercator(final Wgs w) {
    final double mx = w.lon * STUPEN_NA_ROVNIKU_V_METRECH;
    final double my = log( tan(w.lat * (PI/180) / 2 + PI / 4) ) * RZ;
    return new Mercator(mx, my);
  }

  public static Wgs toWgs(final Mercator w) {
    final double lon = w.mx / STUPEN_NA_ROVNIKU_V_METRECH;
    final double lat = (atan(exp( w.my / RZ ) ) - PI / 4) * (180 / PI) * 2;
    return new Wgs(lat, lon);
  }

  public static Utm toUtm(Wgs wgs) {
    //String s = "33 U " + ux + " " + uy;
    final String s = utmWgsConvertor.latLon2UTM(wgs.lat, wgs.lon);
    final String[] utm = s.split(" ");
    //zone = Integer.parseInt(utm[0]);
    //String latBand = utm[1];
    final double easting = Double.parseDouble(utm[2]);
    final double northing = Double.parseDouble(utm[3]);
    final int polednikovaZona = Integer.parseInt(utm[0]);
    final char rovnobezkovaZona = utm[3].charAt(0);
    return new Utm(easting, northing, polednikovaZona, rovnobezkovaZona);    
  }

  public static Mercator toMercator(final Mou mou) {
    final Mercator mer = new Mercator(mou.xx * Q_METRY_NA_MOU, mou.yy * Q_METRY_NA_MOU);
    return mer;
  }

  public static Mou toMou(final Mercator mer) {
    final double xxd = mer.mx / Q_METRY_NA_MOU;
    final double yyd = mer.my / Q_METRY_NA_MOU;
    final Mou mou = new Mou((int)xxd, (int)yyd);
    return mou;
  
  }

  public static Wgs toWgs(Utm utm) {
    try {
      final double[] utm2LatLon = utmWgsConvertor.utm2LatLon(utm.toString());
      return new Wgs(utm2LatLon[0], utm2LatLon[1]);
    } catch (final Exception e) {
      throw new RuntimeException("Nelze převést na WGS: " + utm, e);
    }
  }

  public static Mou toMou(final Wgs w) {
    return toMou(toMercator(w));
  }

  public static Wgs toWgs(final Mou mou) {
    return toWgs(toMercator(mou));
  }

  public static Utm toUtm(Mercator mercator) {
    return toUtm(toWgs(mercator));
  }

  public static Mercator toMercator(Utm utm) {
    return toMercator(toWgs(utm));
  }

  public static Utm toUtm(Mou mou) {
    return toUtm(toWgs(mou));
  }

  public static Mou toMou(Utm utm) {
    return toMou(toWgs(utm));
  }


  /**
   * Kolik metrůpřipadá na jeden krok mou, na dané šířce
   * @param lat
   * @return
   */
  public static double metryNaMou(final double lat) {
    return Q_METRY_NA_MOU * Math.cos(lat / 180 * PI);

  }

  public static double dalka(final Wgs w1, final Wgs w2) {
    final double fi1 = w1.lon * PI / 180;
    final double fi2 = w2.lon * PI / 180;
    final double theta1 = w1.lat * PI / 180;
    final double theta2 = w2.lat * PI / 180;

    final double x1 = Math.cos(fi1) * Math.cos(theta1);
    final double y1 = Math.sin(fi1) * Math.cos(theta1);
    final double z1 = Math.sin(theta1);

    final double x2 = Math.cos(fi2) * Math.cos(theta2);
    final double y2 = Math.sin(fi2) * Math.cos(theta2);
    final double z2 = Math.sin(theta2);

    final double dx = x1 - x2;
    final double dy = y1 - y2;
    final double dz = z1 - z2;

    final double tetiva = Math.sqrt(dx * dx + dy * dy + dz * dz);
    final double uhel = Math.asin(tetiva / 2) * 2;
    final double dalka = uhel * RZ;
    return dalka;
  }

  public static double dalka(final Mouable mouable1, final Mouable mouable2) {
    return FGeoKonvertor.dalka(mouable1.getMou().toWgs(), mouable2.getMou().toWgs());
  }


  public static void main(final String[] args) {
    System.out.println(-3.25 % 1);
  }

}
