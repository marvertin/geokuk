package cz.geokuk.core.coordinates;

public class Wgs extends Misto0 {

	public final double lat;
	public final double lon;

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

	public static String toDdMmSsFormat(final double stupne) {
		final int istupne = (int) Math.floor(stupne);
		final double minuty = (stupne - istupne) * 60.0;
		final int iminuty = (int) Math.floor(minuty);
		final double vteriny = (minuty - iminuty) * 60.0;
		final int ivteriny = (int) Math.floor(vteriny);
		final String s = String.format("%02d°%02d'%02d\"", istupne, iminuty, ivteriny);
		return s;
	}

	public static String toGeoFormat(final double d) {
		final double stupne = Math.floor(d);
		final double minuty = (d - stupne) * 60.0;
		final String s = String.format("%02d°%06.3f", (int) stupne, minuty).replace(',', '.');
		return s;
	}

	public static double vzdalenost(final Wgs bod1, final Wgs bod2) {
		return FGeoKonvertor.dalka(bod1, bod2);
	}

	public static String vzdalenostStr(final Wgs bod1, final Wgs bod2) {
		final double dalka = vzdalenost(bod1, bod2);
		String result;
		if (dalka < 10000) { // vyjádříme v metrech
			result = Math.round(dalka) + " m";
		} else if (dalka < 100000) {
			result = (double) Math.round(dalka / 100) / 10 + " km";
		} else {
			result = Math.round(dalka / 1000) + " km";
		}
		return result;
	}

	/**
	 * Souřdnice se normalizují na interval <-180,180)
	 *
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

	public double azimut(final Wgs bod) {
		return azimut(this, bod);
	}

	public String azimutStr(final Wgs bod) {
		return azimutStr(this, bod);
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

	/**
	 * Vrátí, kolik metrů vychází na mou souřanici na dané šířce.
	 *
	 * @return
	 */
	public double metryNaMou() {
		return FGeoKonvertor.metryNaMou(lat);
	}

	public Wgsd sub(final Wgs wgs) {
		return new Wgsd(lat - wgs.lat, lon - wgs.lon);
	}

	public Wgs sub(final Wgsd wgsd) {
		return new Wgs(lat - wgsd.lat, lon - wgsd.lon);
	}

	@Override
	public Mercator toMercator() {
		return FGeoKonvertor.toMercator(this);
	}

	@Override
	public Mou toMou() {
		return FGeoKonvertor.toMou(this);
	}

	@Override
	public String toString() {
		// Úprava pro záporné souřadnice
		return (lat >= 0 ? "N" : "S") + toGeoFormat(Math.abs(lat)) + " " + (lon >= 0 ? "E" : "W") + toGeoFormat(Math.abs(lon));
	}

	@Override
	public Utm toUtm() {
		return FGeoKonvertor.toUtm(this);
	}

	@Override
	public Wgs toWgs() {
		return this;
	}

	public double vzdalenost(final Wgs bod2) {
		return vzdalenost(this, bod2);
	}

	public String vzdalenostStr(final Wgs bod2) {
		return vzdalenostStr(this, bod2);
	}

}
