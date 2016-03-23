/**
 *
 */
package cz.geokuk.core.coordinates;

/**
 * @author Martin Veverka
 *
 */
public class Test1 {

	public static void main(final String[] args) {
		int spravne = 0;
		int spatne = 0;
		final int n = 10000;
		double rozptyl = 0;
		for (int i = 0; i < 10000; i++) {
			final double lat = Math.random() + 49;
			final double lon = Math.random() + 18;
			final Wgs wgs1 = new Wgs(lat, lon);
			final Wgs wgs2 = wgs1.toMou().toWgs();
			// if (wgs1.toString().equals(wgs2.toString())) {
			// spravne ++;
			// } else {
			// spatne ++;
			// }
			final double difflat = (wgs2.lat - wgs1.lat) * 60 * 1000;
			final double difflon = (wgs2.lon - wgs1.lon) * 60 * 1000;
			final double ctverec = difflat * difflat + difflon * difflon;
			rozptyl += ctverec;
			if (Math.abs(difflat) < 1 && Math.abs(difflon) < 1) {
				spravne++;
			} else {
				spatne++;
			}
			System.out.printf("%s -- %s\n", wgs1, new UtmMgrsWgsConvertor().latLon2UTM(lat, lon));

			System.out.printf("difflat: %f  ---- difflon: %f  %s => %s\n", difflat, difflon, wgs1, wgs2);
		}
		rozptyl = rozptyl / n;
		System.out.printf("spravne: %d\nspatne: %d\nrozptyl: %f\nodchylka: %f\n", spravne, spatne, rozptyl, Math.sqrt(rozptyl));
	}
}
