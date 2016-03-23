package cz.geokuk.core.coordinates;

public class Wgsd {

	public double	lat;
	public double	lon;

	public Wgsd() {
	}

	public Wgsd(final double dlat, final double dlon) {
		this.lat = dlat;
		this.lon = dlon;
	}

	public Wgsd(final Wgsd wgs) {
		lat = wgs.lat;
		lon = wgs.lon;
	}

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

	public Wgsd add(final double dlat, final double dlon) {
		return new Wgsd(this.lat + dlat, this.lon + dlon);
	}

	public Wgsd sub(final double dlat, final double dlon) {
		return new Wgsd(this.lat - dlat, this.lon - dlon);
	}

	@Override
	public String toString() {
		return "WHS-D[" + lon + "," + lat + "]";
	}

}
