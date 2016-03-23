package cz.geokuk.core.coordinates;

public class Utm extends Misto0 {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(ux);
		result = prime * result + (int) (temp ^ temp >>> 32);
		temp = Double.doubleToLongBits(uy);
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
		final Utm other = (Utm) obj;
		if (Double.doubleToLongBits(ux) != Double.doubleToLongBits(other.ux)) {
			return false;
		}
		if (Double.doubleToLongBits(uy) != Double.doubleToLongBits(other.uy)) {
			return false;
		}
		return true;
	}



	public final double ux;
	public final double uy;
	public final int polednikovaZona;
	public final char rovnobezkovaZona;


	public Utm(final double ux, final double uy, final int polednikovaZona, final char rovnobezkovaZona) {
		this.ux = ux;
		this.uy = uy;
		this.polednikovaZona = polednikovaZona;
		this.rovnobezkovaZona = rovnobezkovaZona;
	}

	/**
	 * Vrací jiné místo na zemi, ale ve stejné zóně.
	 * @param ux
	 * @param uy
	 * @return
	 */
	public Utm toUtmInTheSameZone(final double ux, final double uy) {
		return new Utm(ux, uy, this.polednikovaZona, this.rovnobezkovaZona);
	}

	/**
	 * Vrací stejné místo na zemi, ael v souřadnicích vyjádřených v jiné zóně.
	 * @param ux
	 * @param uy
	 * @return
	 */
	public Utm toSampePlaceInAnotherZone( final int polednikovaZona, final char rovnobezkovaZona) {
		return new Utm(this.ux, this.uy, polednikovaZona, rovnobezkovaZona);
	}

	@Override
	public String toString() {
		return polednikovaZona + " " + rovnobezkovaZona + " " + ux + " " + uy;
	}

	public Wgs toWgs() {
		return FGeoKonvertor.toWgs(this);
	}

	@Override
	public Mercator toMercator() {
		return FGeoKonvertor.toMercator(this);
	}

	public Mou toMou() {
		// Takto to bylo v seznamových mapách
		//double xx = (ux + 3700000) / MOU_FACTOR;
		//double yy = (uy - 1300000) / MOU_FACTOR;
		//return new Mou((int)xx, (int)yy);
		return toWgs().toMou();
	}

	@Override
	public Utm toUtm() {
		return this;
	}


}
