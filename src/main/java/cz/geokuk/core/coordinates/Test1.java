/**
 *
 */
package cz.geokuk.core.coordinates;

/**
 * @author veverka
 *
 */
public class Test1 {

	public static void main(String[] args) {
		int spravne = 0;
		int spatne = 0;
		int n = 10000;
		double rozptyl = 0;
		for (int i = 0; i < 10000; i++) {
			double lat = Math.random() + 49;
			double lon = Math.random() + 18;
			Wgs wgs1 = new Wgs(lat, lon);
			Wgs wgs2 = wgs1.toMou().toWgs();
			//      if (wgs1.toString().equals(wgs2.toString())) {
			//        spravne ++;
			//      } else {
			//        spatne ++;
			//      }
			double difflat = (wgs2.lat - wgs1.lat) * 60 * 1000;
			double difflon = (wgs2.lon - wgs1.lon) * 60 * 1000 ;
			double ctverec = difflat * difflat + difflon * difflon;
			rozptyl += ctverec;
			if (Math.abs(difflat) < 1 && Math.abs(difflon) < 1) {
				spravne ++;
			} else {
				spatne ++;
			}
			System.out.printf("%s -- %s\n", wgs1, new UtmMgrsWgsConvertor().latLon2UTM(lat, lon));

			System.out.printf("difflat: %f  ---- difflon: %f  %s => %s\n", difflat, difflon, wgs1, wgs2);
		}
		rozptyl = rozptyl / n;
		System.out.printf("spravne: %d\nspatne: %d\nrozptyl: %f\nodchylka: %f\n", spravne, spatne, rozptyl, Math.sqrt(rozptyl));
	}
}
