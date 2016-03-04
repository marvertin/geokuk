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

  public static final double Q_METRY_NA_MOU = OBVOD_ZEME / OBVOD_ZEME_V_MOU;

  public static final double STUPEN_NA_ROVNIKU_V_METRECH = PI / 180 * RZ;

  public static double normalizujUhel(double uhel) {
    // TODO optimálněji, ale možná to v praxi stačí
    while (uhel >= 180) uhel -= 360;
    while (uhel < -180) uhel += 360;
    return uhel;
  }
  
  public static Mercator toMercator(Wgs w) {
    double mx = w.lon * STUPEN_NA_ROVNIKU_V_METRECH;
    double my = log( tan(w.lat * (PI/180) / 2 + PI / 4) ) * RZ;
    return new Mercator(mx, my);
  }
  
  public static Wgs toWgs(Mercator w) {
    double lon = w.mx / STUPEN_NA_ROVNIKU_V_METRECH;
    double lat = (atan(exp( w.my / RZ ) ) - PI / 4) * (180 / PI) * 2;
    return new Wgs(lat, lon);
  }
  
  public static Mou toMou(Wgs w) {
    return toMou(toMercator(w));
  }
  
  public static Wgs toWgs(Mou mou) {
    return toWgs(toMercator(mou));
  }

  public static Mercator toMercator(Mou mou) {
    Mercator mer = new Mercator(mou.xx * Q_METRY_NA_MOU, mou.yy * Q_METRY_NA_MOU);
    return mer;
  }
  
  public static Mou toMou(Mercator mer) {
    double xxd = mer.mx / Q_METRY_NA_MOU;
    double yyd = mer.my / Q_METRY_NA_MOU;
    Mou mou = new Mou((int)xxd, (int)yyd);
    return mou;

  }

  
  public static void main(String[] args) {
    System.out.println(-3.25 % 1);
  }

}
