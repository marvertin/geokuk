package cz.geokuk.plugins.challenge;

/**
 * Azimut a vzdálenost po povrchu Země pro challenge s úhlovými sektory. Na rozdíl od {@code Wgs.azimut} (směr v mercatorově rovině) jde o počáteční směr po
 * kouli nebo po elipsoidu WGS84.
 */
public final class Geodezie {

	/** Poloměr koule pro vzdálenosti v metrech. */
	static final double POLOMER_ZEME = 6_371_008.8;

	/** Sektor, jehož azimut leží blíž k hranici celého stupně než tato hodnota, je nejistý. */
	public static final double NEJISTOTA_STUPNU = 0.05;

	private static final double A = 6378137.0;
	private static final double F = 1 / 298.257223563;

	private Geodezie() {}

	/** Počáteční azimut po kružnici (koule) ve stupních, 0 až méně než 360. */
	public static double azimutKoule(final double lat1, final double lon1, final double lat2, final double lon2) {
		final double p1 = Math.toRadians(lat1);
		final double p2 = Math.toRadians(lat2);
		final double dl = Math.toRadians(lon2 - lon1);
		final double a = Math.atan2(Math.sin(dl) * Math.cos(p2), Math.cos(p1) * Math.sin(p2) - Math.sin(p1) * Math.cos(p2) * Math.cos(dl));
		return normalizuj(Math.toDegrees(a));
	}

	/** Vzdálenost po kružnici (koule) v metrech. */
	public static double vzdalenostKoule(final double lat1, final double lon1, final double lat2, final double lon2) {
		final double p1 = Math.toRadians(lat1);
		final double p2 = Math.toRadians(lat2);
		final double dp = p2 - p1;
		final double dl = Math.toRadians(lon2 - lon1);
		final double h = Math.sin(dp / 2) * Math.sin(dp / 2) + Math.cos(p1) * Math.cos(p2) * Math.sin(dl / 2) * Math.sin(dl / 2);
		return 2 * POLOMER_ZEME * Math.asin(Math.min(1, Math.sqrt(h)));
	}

	/**
	 * Počáteční azimut na elipsoidu WGS84 (Vincenty) ve stupních, 0 až méně než 360. Pro shodné body 0. Iterace nekonverguje u téměř protilehlých bodů (do asi 20 km od
	 * protinožce); pak vrací {@code NaN} a volající použije kouli s příznakem nejistoty.
	 */
	public static double azimutElipsoid(final double lat1, final double lon1, final double lat2, final double lon2) {
		final double u1 = Math.atan((1 - F) * Math.tan(Math.toRadians(lat1)));
		final double u2 = Math.atan((1 - F) * Math.tan(Math.toRadians(lat2)));
		final double velkeL = Math.toRadians(lon2 - lon1);
		double lam = velkeL;
		final double sinU1 = Math.sin(u1);
		final double cosU1 = Math.cos(u1);
		final double sinU2 = Math.sin(u2);
		final double cosU2 = Math.cos(u2);
		boolean konverguje = false;
		for (int i = 0; i < 100; i++) {
			final double sinLam = Math.sin(lam);
			final double cosLam = Math.cos(lam);
			final double ss = Math.hypot(cosU2 * sinLam, cosU1 * sinU2 - sinU1 * cosU2 * cosLam);
			if (ss == 0) {
				return 0;
			}
			final double cs = sinU1 * sinU2 + cosU1 * cosU2 * cosLam;
			final double sigma = Math.atan2(ss, cs);
			final double sinAlfa = cosU1 * cosU2 * sinLam / ss;
			final double cos2Alfa = 1 - sinAlfa * sinAlfa;
			final double cos2Sm = cos2Alfa == 0 ? 0 : cs - 2 * sinU1 * sinU2 / cos2Alfa;
			final double c = F / 16 * cos2Alfa * (4 + F * (4 - 3 * cos2Alfa));
			final double nova = velkeL + (1 - c) * F * sinAlfa * (sigma + c * ss * (cos2Sm + c * cs * (-1 + 2 * cos2Sm * cos2Sm)));
			final boolean konec = Math.abs(nova - lam) < 1e-13;
			lam = nova;
			if (konec) {
				konverguje = true;
				break;
			}
		}
		if (!konverguje) {
			return Double.NaN;
		}
		final double az = Math.atan2(cosU2 * Math.sin(lam), cosU1 * sinU2 - sinU1 * cosU2 * Math.cos(lam));
		return normalizuj(Math.toDegrees(az));
	}

	/**
	 * Vzdálenost po elipsoidu WGS84 v metrech (Vincenty, inverzní úloha). U téměř protilehlých bodů, kde iterace nekonverguje, vrací {@code NaN}; volající použije
	 * kouli.
	 */
	public static double vzdalenostElipsoid(final double lat1, final double lon1, final double lat2, final double lon2) {
		final double b = (1 - F) * A;
		final double u1 = Math.atan((1 - F) * Math.tan(Math.toRadians(lat1)));
		final double u2 = Math.atan((1 - F) * Math.tan(Math.toRadians(lat2)));
		final double velkeL = Math.toRadians(lon2 - lon1);
		double lam = velkeL;
		final double sinU1 = Math.sin(u1);
		final double cosU1 = Math.cos(u1);
		final double sinU2 = Math.sin(u2);
		final double cosU2 = Math.cos(u2);
		for (int i = 0; i < 100; i++) {
			final double sinLam = Math.sin(lam);
			final double cosLam = Math.cos(lam);
			final double ss = Math.hypot(cosU2 * sinLam, cosU1 * sinU2 - sinU1 * cosU2 * cosLam);
			if (ss == 0) {
				return 0;
			}
			final double cs = sinU1 * sinU2 + cosU1 * cosU2 * cosLam;
			final double sigma = Math.atan2(ss, cs);
			final double sinAlfa = cosU1 * cosU2 * sinLam / ss;
			final double cos2Alfa = 1 - sinAlfa * sinAlfa;
			final double cos2Sm = cos2Alfa == 0 ? 0 : cs - 2 * sinU1 * sinU2 / cos2Alfa;
			final double c = F / 16 * cos2Alfa * (4 + F * (4 - 3 * cos2Alfa));
			final double nova = velkeL + (1 - c) * F * sinAlfa * (sigma + c * ss * (cos2Sm + c * cs * (-1 + 2 * cos2Sm * cos2Sm)));
			final boolean konec = Math.abs(nova - lam) < 1e-13;
			lam = nova;
			if (konec) {
				final double sinLam2 = Math.sin(lam);
				final double cosLam2 = Math.cos(lam);
				final double ss2 = Math.hypot(cosU2 * sinLam2, cosU1 * sinU2 - sinU1 * cosU2 * cosLam2);
				final double cs2 = sinU1 * sinU2 + cosU1 * cosU2 * cosLam2;
				final double sigma2 = Math.atan2(ss2, cs2);
				final double sinAlfa2 = cosU1 * cosU2 * sinLam2 / ss2;
				final double cos2Alfa2 = 1 - sinAlfa2 * sinAlfa2;
				final double cos2Sm2 = cos2Alfa2 == 0 ? 0 : cs2 - 2 * sinU1 * sinU2 / cos2Alfa2;
				final double u2k = cos2Alfa2 * (A * A - b * b) / (b * b);
				final double kA = 1 + u2k / 16384 * (4096 + u2k * (-768 + u2k * (320 - 175 * u2k)));
				final double kB = u2k / 1024 * (256 + u2k * (-128 + u2k * (74 - 47 * u2k)));
				final double deltaSigma = kB * ss2 * (cos2Sm2 + kB / 4 * (cs2 * (-1 + 2 * cos2Sm2 * cos2Sm2) - kB / 6 * cos2Sm2 * (-3 + 4 * ss2 * ss2) * (-3 + 4 * cos2Sm2 * cos2Sm2)));
				return b * kA * (sigma2 - deltaSigma);
			}
		}
		return Double.NaN;
	}

	/** Sektor 0 až 359: azimut uříznutý dolů na celé stupně. */
	public static int sektor(final double azimut) {
		return (int) Math.floor(azimut) % 360;
	}

	/** Azimut je tak blízko hranici sektoru, že ho drobný rozdíl ve výpočtu nebo v souřadnicích přesune jinam. */
	public static boolean jeNejisty(final double azimut) {
		final double zlomek = azimut - Math.floor(azimut);
		return zlomek < NEJISTOTA_STUPNU || zlomek > 1 - NEJISTOTA_STUPNU;
	}

	private static double normalizuj(final double stupne) {
		final double v = stupne % 360;
		final double r = v < 0 ? v + 360 : v;
		return r >= 360 ? 0 : r;
	}
}
