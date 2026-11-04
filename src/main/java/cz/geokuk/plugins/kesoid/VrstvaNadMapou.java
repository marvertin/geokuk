package cz.geokuk.plugins.kesoid;

/** Vrstvy kruhů nad mapou, které mají vlastní vypínač a mohou být zapnuté, ale nekreslit nic. */
public enum VrstvaNadMapou {
	ZVYRAZNOVACI_KRUHY("Zvýrazňovací kruhy (F8)"), JEDNOTKOVE_KRUHY("Kruhy v metrech (Alt+F8)"), OBSAZENOST("Obsazenost (F9)");

	private final String nazev;

	VrstvaNadMapou(final String nazev) {
		this.nazev = nazev;
	}

	public String getNazev() {
		return nazev;
	}
}
