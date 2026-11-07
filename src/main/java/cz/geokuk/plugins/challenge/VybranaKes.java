package cz.geokuk.plugins.challenge;

/** Keš z gc.com, kterou program vybral (klik na mapě, detail): kód, poloha a obtížnost a terén v půlkách 0 až 8 (−1, když se neznají). Neměnná. */
public final class VybranaKes {

	private final String id;
	private final double lat;
	private final double lon;
	private final int obtiznostPulek;
	private final int terenPulek;

	public VybranaKes(final String id, final double lat, final double lon, final int obtiznostPulek, final int terenPulek) {
		this.id = id;
		this.lat = lat;
		this.lon = lon;
		this.obtiznostPulek = obtiznostPulek;
		this.terenPulek = terenPulek;
	}

	public String id() {
		return id;
	}

	public double lat() {
		return lat;
	}

	public double lon() {
		return lon;
	}

	public int obtiznostPulek() {
		return obtiznostPulek;
	}

	public int terenPulek() {
		return terenPulek;
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof VybranaKes)) {
			return false;
		}
		final VybranaKes k = (VybranaKes) o;
		return id.equals(k.id) && Double.compare(lat, k.lat) == 0 && Double.compare(lon, k.lon) == 0 && obtiznostPulek == k.obtiznostPulek && terenPulek == k.terenPulek;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
