package cz.geokuk.plugins.challenge;

/** Nenalezená keš vybraná pro chybějící sektor („další chybějící“); vzdálenost je od bodu, podle kterého se vybíralo (třeba střed mapy). */
public final class VybranyKandidat {

	private final String id;
	private final double lat;
	private final double lon;
	private final int sektor;
	private final double vzdalenost;
	private final String stitek;

	public VybranyKandidat(final String id, final double lat, final double lon, final int sektor, final double vzdalenost) {
		this(id, lat, lon, sektor, vzdalenost, null);
	}

	/** Kandidát D/T matice nebo kalendáře: {@code sektor} je -1 a {@code stitek} text políčka nebo dne k zobrazení. */
	public VybranyKandidat(final String id, final double lat, final double lon, final int sektor, final double vzdalenost, final String stitek) {
		this.stitek = stitek;
		this.id = id;
		this.lat = lat;
		this.lon = lon;
		this.sektor = sektor;
		this.vzdalenost = vzdalenost;
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

	/** Chybějící sektor 0 až 359, který keš zaplní. */
	public int sektor() {
		return sektor;
	}

	/** Vzdálenost v metrech od bodu, podle kterého se vybíralo. */
	public double vzdalenost() {
		return vzdalenost;
	}

	/** Text políčka nebo dne ({@code D4/T1}, {@code 14. 3. (za 131 dní)}) u D/T a kalendáře, jinak null. */
	public String stitek() {
		return stitek;
	}
}
