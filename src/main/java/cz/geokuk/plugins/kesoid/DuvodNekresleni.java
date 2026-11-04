package cz.geokuk.plugins.kesoid;

/** Zda se vrstva kruhů nad mapou kreslí, a když ne, proč. */
public enum DuvodNekresleni {
	/** Kreslí se. */
	KRESLI,
	/** Ve výřezu je víc bodů než limit kreslení. */
	LIMIT_POCTU,
	/** Kruhy v pixelech by příliš zaplnily okno. */
	PRILIS_HUSTE,
	/** Kruh by měl pod nejmenší kreslený poloměr. */
	POLOMER_PRILIS_MALY
}
