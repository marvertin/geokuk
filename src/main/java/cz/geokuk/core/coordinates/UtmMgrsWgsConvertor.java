/*
 * Author: Sami Salkosuo, sami.salkosuo@fi.ibm.com
 *
 * (c) Copyright IBM Corp. 2007
 */
package cz.geokuk.core.coordinates;

import java.util.Hashtable;
import java.util.Map;

class UtmMgrsWgsConvertor {

	double[] utm2LatLon(final String UTM) {
		final UTM2LatLon c = new UTM2LatLon();
		return c.convertUTMToLatLong(UTM);
	}

	String latLon2UTM(final double latitude, final double longitude) {
		final LatLon2UTM c = new LatLon2UTM();
		return c.convertLatLonToUTM(latitude, longitude);

	}

	private void validate(final double latitude, final double longitude) {
		if (latitude < -90.0 || latitude > 90.0 || longitude < -180.0 || longitude >= 180.0) {
			// throw new IllegalArgumentException(
			// "Legal ranges: latitude [-90,90], longitude [-180,180).");
		}

	}

	String latLon2MGRUTM(final double latitude, final double longitude) {
		final LatLon2MGRUTM c = new LatLon2MGRUTM();
		return c.convertLatLonToMGRUTM(latitude, longitude);

	}

	double[] mgrutm2LatLon(final String MGRUTM) {
		final MGRUTM2LatLon c = new MGRUTM2LatLon();
		return c.convertMGRUTMToLatLong(MGRUTM);
	}

	private double degreeToRadian(final double degree) {
		return degree * Math.PI / 180;
	}

	@SuppressWarnings("unused")
	private double radianToDegree(final double radian) {
		return radian * 180 / Math.PI;
	}

	private double POW(final double a, final double b) {
		return Math.pow(a, b);
	}

	private double SIN(final double value) {
		return Math.sin(value);
	}

	private double COS(final double value) {
		return Math.cos(value);
	}

	private double TAN(final double value) {
		return Math.tan(value);
	}

	private class LatLon2UTM {

		public String convertLatLonToUTM(final double latitude, final double longitude) {
			validate(latitude, longitude);
			String UTM = "";

			setVariables(latitude, longitude);

			final String longZone = getLongZone(longitude);
			final LatBands latBands = new LatBands();
			final String latBand = latBands.getLatBand(latitude);

			final double _easting = getEasting();
			final double _northing = getNorthing(latitude);

			UTM = longZone + " " + latBand + " " + (int) _easting + " " + (int) _northing;
			// UTM = longZone + " " + latBand + " " + decimalFormat.format(_easting) +
			// " "+ decimalFormat.format(_northing);

			return UTM;

		}

		protected void setVariables(double latitude, final double longitude) {
			latitude = degreeToRadian(latitude);
			// rho = equatorialRadius * (1 - e * e)
			// / POW(1 - POW(e * SIN(latitude), 2), 3 / 2.0);

			nu = equatorialRadius / POW(1 - POW(e * SIN(latitude), 2), 1 / 2.0);

			double var1;
			if (longitude < 0.0) {
				var1 = (int) ((180 + longitude) / 6.0) + 1;
			} else {
				var1 = (int) (longitude / 6) + 31;
			}
			final double var2 = 6 * var1 - 183;
			final double var3 = longitude - var2;
			p = var3 * 3600 / 10000;

			S = A0 * latitude - B0 * SIN(2 * latitude) + C0 * SIN(4 * latitude) - D0 * SIN(6 * latitude) + E0 * SIN(8 * latitude);

			K1 = S * k0;
			K2 = nu * SIN(latitude) * COS(latitude) * POW(sin1, 2) * k0 * 100000000 / 2;
			K3 = POW(sin1, 4) * nu * SIN(latitude) * Math.pow(COS(latitude), 3) / 24 * (5 - POW(TAN(latitude), 2) + 9 * e1sq * POW(COS(latitude), 2) + 4 * POW(e1sq, 2) * POW(COS(latitude), 4)) * k0
					* 10000000000000000L;

			K4 = nu * COS(latitude) * sin1 * k0 * 10000;

			K5 = POW(sin1 * COS(latitude), 3) * (nu / 6) * (1 - POW(TAN(latitude), 2) + e1sq * POW(COS(latitude), 2)) * k0 * 1000000000000L;

			// A6 = (POW(p * sin1, 6) * nu * SIN(latitude) * POW(COS(latitude), 5) / 720)
			// * (61 - 58 * POW(TAN(latitude), 2) + POW(TAN(latitude), 4) + 270
			// * e1sq * POW(COS(latitude), 2) - 330 * e1sq
			// * POW(SIN(latitude), 2)) * k0 * (1E+24);

		}

		protected String getLongZone(final double longitude) {
			double longZone = 0;
			if (longitude < 0.0) {
				longZone = (180.0 + longitude) / 6 + 1;
			} else {
				longZone = longitude / 6 + 31;
			}
			String val = String.valueOf((int) longZone);
			if (val.length() == 1) {
				val = "0" + val;
			}
			return val;
		}

		protected double getNorthing(final double latitude) {
			double northing = K1 + K2 * p * p + K3 * POW(p, 4);
			if (latitude < 0.0) {
				northing = 10000000 + northing;
			}
			return northing;
		}

		protected double getEasting() {
			return 500000 + (K4 * p + K5 * POW(p, 3));
		}

		// Lat Lon to UTM variables

		// equatorial radius
		double	equatorialRadius	= 6378137;

		// polar radius
		double	polarRadius			= 6356752.314;

		// flattening
		// double flattening = 0.00335281066474748;// (equatorialRadius-polarRadius)/equatorialRadius;

		// inverse flattening 1/flattening
		// double inverseFlattening = 298.257223563;// 1/flattening;

		// Mean radius
		// double rm = POW(equatorialRadius * polarRadius, 1 / 2.0);

		// scale factor
		double	k0					= 0.9996;

		// eccentricity
		double	e					= Math.sqrt(1 - POW(polarRadius / equatorialRadius, 2));

		double	e1sq				= e * e / (1 - e * e);

		// double n = (equatorialRadius - polarRadius)
		// / (equatorialRadius + polarRadius);

		// r curv 1
		// double rho = 6368573.744;

		// r curv 2
		double	nu					= 6389236.914;

		// Calculate Meridional Arc Length
		// Meridional Arc
		double	S					= 5103266.421;

		double	A0					= 6367449.146;

		double	B0					= 16038.42955;

		double	C0					= 16.83261333;

		double	D0					= 0.021984404;

		double	E0					= 0.000312705;

		// Calculation Constants
		// Delta Long
		double	p					= -0.483084;

		double	sin1				= 4.84814E-06;

		// Coefficients for UTM Coordinates
		double	K1					= 5101225.115;

		double	K2					= 3750.291596;

		double	K3					= 1.397608151;

		double	K4					= 214839.3105;

		double	K5					= -2.995382942;

		// double A6 = -1.00541E-07;

	}

	private class LatLon2MGRUTM extends LatLon2UTM {
		public String convertLatLonToMGRUTM(final double latitude, final double longitude) {
			validate(latitude, longitude);
			String mgrUTM = "";

			setVariables(latitude, longitude);

			final String longZone = getLongZone(longitude);
			final LatBands latBands = new LatBands();
			final String latBand = latBands.getLatBand(latitude);

			final double _easting = getEasting();
			final double _northing = getNorthing(latitude);
			final Digraphs digraphs = new Digraphs();
			final String digraph1 = digraphs.getDigraph1(Integer.parseInt(longZone), _easting);
			final String digraph2 = digraphs.getDigraph2(Integer.parseInt(longZone), _northing);

			String easting = String.valueOf((int) _easting);
			if (easting.length() < 5) {
				easting = "00000" + easting;
			}
			easting = easting.substring(easting.length() - 5);

			String northing;
			northing = String.valueOf((int) _northing);
			if (northing.length() < 5) {
				northing = "0000" + northing;
			}
			northing = northing.substring(northing.length() - 5);

			mgrUTM = longZone + latBand + digraph1 + digraph2 + easting + northing;
			return mgrUTM;
		}
	}

	private class MGRUTM2LatLon extends UTM2LatLon {
		public double[] convertMGRUTMToLatLong(final String mgrutm) {
			final double[] latlon = { 0.0, 0.0 };
			// 02CNR0634657742
			final int zone = Integer.parseInt(mgrutm.substring(0, 2));
			final String latBand = mgrutm.substring(2, 3);

			final String digraph1 = mgrutm.substring(3, 4);
			final String digraph2 = mgrutm.substring(4, 5);
			easting = Double.parseDouble(mgrutm.substring(5, 10));
			northing = Double.parseDouble(mgrutm.substring(10, 15));

			final LatBands lz = new LatBands();
			final double latBandDegree = lz.getLatBandDegree(latBand);

			final double a1 = latBandDegree * 40000000 / 360.0;
			final double a2 = 2000000 * Math.floor(a1 / 2000000.0);

			final Digraphs digraphs = new Digraphs();

			final double digraph2Index = digraphs.getDigraph2Index(digraph2);

			double startindexEquator = 1;
			if (1 + zone % 2 == 1) {
				startindexEquator = 6;
			}

			double a3 = a2 + (digraph2Index - startindexEquator) * 100000;
			if (a3 <= 0) {
				a3 = 10000000 + a3;
			}
			northing = a3 + northing;

			zoneCM = -183 + 6 * zone;
			final double digraph1Index = digraphs.getDigraph1Index(digraph1);
			final int a5 = 1 + zone % 3;
			final double[] a6 = { 16, 0, 8 };
			final double a7 = 100000 * (digraph1Index - a6[a5 - 1]);
			easting = easting + a7;

			setVariables();

			double latitude = 0;
			latitude = 180 * (phi1 - fact1 * (fact2 + fact3 + fact4)) / Math.PI;

			if (latBandDegree < 0) {
				latitude = 90 - latitude;
			}

			final double d = _a2 * 180 / Math.PI;
			final double longitude = zoneCM - d;

			if (getHemisphere(latBand).equals("S")) {
				latitude = -latitude;
			}

			latlon[0] = latitude;
			latlon[1] = longitude;
			return latlon;
		}
	}

	private class UTM2LatLon {
		double	easting;

		double	northing;

		int		zone;

		String	southernHemisphere	= "ACDEFGHJKLM";

		protected String getHemisphere(final String latBand) {
			String hemisphere = "N";
			if (southernHemisphere.contains(latBand)) {
				hemisphere = "S";
			}
			return hemisphere;
		}

		public double[] convertUTMToLatLong(final String UTM) {
			final double[] latlon = { 0.0, 0.0 };
			final String[] utm = UTM.split(" ");
			zone = Integer.parseInt(utm[0]);
			final String latBand = utm[1];
			easting = Double.parseDouble(utm[2]);
			northing = Double.parseDouble(utm[3]);
			final String hemisphere = getHemisphere(latBand);
			double latitude = 0.0;
			double longitude = 0.0;

			if (hemisphere.equals("S")) {
				northing = 10000000 - northing;
			}
			setVariables();
			latitude = 180 * (phi1 - fact1 * (fact2 + fact3 + fact4)) / Math.PI;

			if (zone > 0) {
				zoneCM = 6 * zone - 183.0;
			} else {
				zoneCM = 3.0;

			}

			longitude = zoneCM - _a3;
			if (hemisphere.equals("S")) {
				latitude = -latitude;
			}

			latlon[0] = latitude;
			latlon[1] = longitude;
			return latlon;

		}

		protected void setVariables() {
			arc = northing / k0;
			mu = arc / (a * (1 - POW(e, 2) / 4.0 - 3 * POW(e, 4) / 64.0 - 5 * POW(e, 6) / 256.0));

			ei = (1 - POW(1 - e * e, 1 / 2.0)) / (1 + POW(1 - e * e, 1 / 2.0));

			ca = 3 * ei / 2 - 27 * POW(ei, 3) / 32.0;

			cb = 21 * POW(ei, 2) / 16 - 55 * POW(ei, 4) / 32;
			cc = 151 * POW(ei, 3) / 96;
			cd = 1097 * POW(ei, 4) / 512;
			phi1 = mu + ca * SIN(2 * mu) + cb * SIN(4 * mu) + cc * SIN(6 * mu) + cd * SIN(8 * mu);

			n0 = a / POW(1 - POW(e * SIN(phi1), 2), 1 / 2.0);

			r0 = a * (1 - e * e) / POW(1 - POW(e * SIN(phi1), 2), 3 / 2.0);
			fact1 = n0 * TAN(phi1) / r0;

			_a1 = 500000 - easting;
			dd0 = _a1 / (n0 * k0);
			fact2 = dd0 * dd0 / 2;

			t0 = POW(TAN(phi1), 2);
			Q0 = e1sq * POW(COS(phi1), 2);
			fact3 = (5 + 3 * t0 + 10 * Q0 - 4 * Q0 * Q0 - 9 * e1sq) * POW(dd0, 4) / 24;

			fact4 = (61 + 90 * t0 + 298 * Q0 + 45 * t0 * t0 - 252 * e1sq - 3 * Q0 * Q0) * POW(dd0, 6) / 720;

			//
			lof1 = _a1 / (n0 * k0);
			lof2 = (1 + 2 * t0 + Q0) * POW(dd0, 3) / 6.0;
			lof3 = (5 - 2 * Q0 + 28 * t0 - 3 * POW(Q0, 2) + 8 * e1sq + 24 * POW(t0, 2)) * POW(dd0, 5) / 120;
			_a2 = (lof1 - lof2 + lof3) / COS(phi1);
			_a3 = _a2 * 180 / Math.PI;

		}

		double	arc;

		double	mu;

		double	ei;

		double	ca;

		double	cb;

		double	cc;

		double	cd;

		double	n0;

		double	r0;

		double	_a1;

		double	dd0;

		double	t0;

		double	Q0;

		double	lof1;

		double	lof2;

		double	lof3;

		double	_a2;

		double	phi1;

		double	fact1;

		double	fact2;

		double	fact3;

		double	fact4;

		double	zoneCM;

		double	_a3;

		// double b = 6356752.314;

		double	a		= 6378137;

		double	e		= 0.081819191;

		double	e1sq	= 0.006739497;

		double	k0		= 0.9996;

	}

	private class Digraphs {
		private final Map<Integer, String>	digraph1		= new Hashtable<>();

		private final Map<Integer, String>	digraph2		= new Hashtable<>();

		private final String[]				digraph1Array	= { "A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

		private final String[]				digraph2Array	= { "V", "A", "B", "C", "D", "E", "F", "G", "H", "J", "K", "L", "M", "N", "P", "Q", "R", "S", "T", "U", "V" };

		public Digraphs() {
			for (int i = 0; i < digraph1Array.length; ++i) {
				digraph1.put(i + 1, digraph1Array[i]);
			}

			for (int i = 0; i < digraph2Array.length; ++i) {
				digraph2.put(i, digraph2Array[i]);
			}
		}

		public int getDigraph1Index(final String letter) {
			for (int i = 0; i < digraph1Array.length; i++) {
				if (digraph1Array[i].equals(letter)) {
					return i + 1;
				}
			}

			return -1;
		}

		public int getDigraph2Index(final String letter) {
			for (int i = 0; i < digraph2Array.length; i++) {
				if (digraph2Array[i].equals(letter)) {
					return i;
				}
			}

			return -1;
		}

		public String getDigraph1(final int longZone, final double easting) {
			final int a1 = longZone;
			final double a2 = 8 * ((a1 - 1) % 3) + 1;

			final double a3 = easting;
			final double a4 = a2 + (int) (a3 / 100000) - 1;
			return digraph1.get((int) Math.floor(a4));
		}

		public String getDigraph2(final int longZone, final double northing) {
			final int a1 = longZone;
			final double a2 = 1 + 5 * ((a1 - 1) % 2);
			final double a3 = northing;
			double a4 = a2 + (int) (a3 / 100000);
			a4 = (a2 + (int) (a3 / 100000.0)) % 20;
			a4 = Math.floor(a4);
			if (a4 < 0) {
				a4 = a4 + 19;
			}
			return digraph2.get((int) Math.floor(a4));

		}

	}

	private class LatBands {
		private final char[]	letters		= { 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Z' };

		private final int[]		degrees		= { -90, -84, -72, -64, -56, -48, -40, -32, -24, -16, -8, 0, 8, 16, 24, 32, 40, 48, 56, 64, 72, 84 };

		private final char[]	negLetters	= { 'A', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M' };

		private final int[]		negDegrees	= { -90, -84, -72, -64, -56, -48, -40, -32, -24, -16, -8 };

		private final char[]	posLetters	= { 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Z' };

		private final int[]		posDegrees	= { 0, 8, 16, 24, 32, 40, 48, 56, 64, 72, 84 };

		private final int		arrayLength	= 22;

		public LatBands() {
		}

		public int getLatBandDegree(final String letter) {
			final char ltr = letter.charAt(0);
			for (int i = 0; i < arrayLength; i++) {
				if (letters[i] == ltr) {
					return degrees[i];
				}
			}
			return -100;
		}

		public String getLatBand(final double latitude) {
			int latIndex = -2;
			final int lat = (int) latitude;

			if (lat >= 0) {
				final int len = posLetters.length;
				for (int i = 0; i < len; i++) {
					if (lat == posDegrees[i]) {
						latIndex = i;
						break;
					}

					if (lat > posDegrees[i]) {
						continue;
					} else {
						latIndex = i - 1;
						break;
					}
				}
			} else {
				final int len = negLetters.length;
				for (int i = 0; i < len; i++) {
					if (lat == negDegrees[i]) {
						latIndex = i;
						break;
					}

					if (lat < negDegrees[i]) {
						latIndex = i - 1;
						break;
					} else {
						continue;
					}

				}

			}

			if (latIndex == -1) {
				latIndex = 0;
			}
			if (lat >= 0) {
				if (latIndex == -2) {
					latIndex = posLetters.length - 1;
				}
				return String.valueOf(posLetters[latIndex]);
			} else {
				if (latIndex == -2) {
					latIndex = negLetters.length - 1;
				}
				return String.valueOf(negLetters[latIndex]);

			}
		}

	}

}
