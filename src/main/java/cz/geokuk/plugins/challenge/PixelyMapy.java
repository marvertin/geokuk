package cz.geokuk.plugins.challenge;

import java.awt.Point;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Wgs;

/** Převod souřadnic na pixely mapy pro kreslení plánovače: přes antimeridián a u pólů. */
final class PixelyMapy {

	/** Nejvyšší šířka, kterou Mercatorova mapa ukazuje; dál (póly) se bod zploští na okraj mapy. */
	private static final double NEJVYSSI_SIRKA = 85.0;

	private PixelyMapy() {
	}

	/** Mou bodu; délka se zalomí do -180 až 180 a šířka omezí na ±85°, aby se bod nezkreslil mimo svět. */
	static Mou mou(final double lat, final double lon) {
		final double la = Math.max(-NEJVYSSI_SIRKA, Math.min(NEJVYSSI_SIRKA, lat));
		final double lo = ((lon + 180) % 360 + 360) % 360 - 180;
		return new Wgs(la, lo).toMou();
	}

	/**
	 * Pixel bodu v mapě; bod za antimeridiánem leží vedle středu mapy, ne o šířku světa dál. U otočené mapy se svět nezalamuje (kreslení je v něm jen přibližné).
	 */
	static Point transform(final Coord c, final Mou m, final Point p) {
		c.transform(m, p);
		if (c.getNatoceni() == 0) {
			final long svet = 1L << (c.getMoumer() + Coord.MOUMER_0_BITS);
			final long stred = c.getWidth() / 2;
			p.x = (int) (Math.floorMod(p.x - stred + svet / 2, svet) - svet / 2 + stred);
		}
		return p;
	}
}
