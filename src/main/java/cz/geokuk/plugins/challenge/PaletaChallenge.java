package cz.geokuk.plugins.challenge;

import cz.geokuk.plugins.challenge.Challenge.Typ;

/**
 * Barvy challenge na mapě podle typu. Zelená a červená jsou vyhrazené pro stav sektorů, proto se tu nepoužívají; barvy mají čitelný kontrast vůči mapě i lištám ve všech
 * motivech.
 */
public final class PaletaChallenge {

	private static final int FIALOVA = 0x7B3FC4;
	private static final int MODRA = 0x1E6FD9;
	private static final int ORANZOVA = 0xD98200;

	private PaletaChallenge() {}

	/** Barva 0xRRGGBB pro typ challenge. */
	public static int barva(final Typ typ) {
		switch (typ) {
		case SEKTORY_360:
			return FIALOVA;
		case DT_MATICE:
			return MODRA;
		default:
			return ORANZOVA;
		}
	}
}
