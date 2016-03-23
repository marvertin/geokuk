package cz.geokuk.core.coordinates;

public class Wgsd {

	public double lat;
	public double lon;

	public Wgsd() {}

	public Wgsd(final double dlat, final double dlon) {
		lat = dlat;
		lon = dlon;
	}

	public Wgsd(final Wgsd wgs) {
		lat = wgs.lat;
		lon = wgs.lon;
	}

	public Wgsd add(final double dlat, final double dlon) {
		return new Wgsd(lat + dlat, lon + dlon);
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
		final Wgsd other = (Wgsd) obj;
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

	public Wgsd sub(final double dlat, final double dlon) {
		return new Wgsd(lat - dlat, lon - dlon);
	}

	@Override
	public String toString() {
		return "WHS-D[" + lon + "," + lat + "]";
	}

}
