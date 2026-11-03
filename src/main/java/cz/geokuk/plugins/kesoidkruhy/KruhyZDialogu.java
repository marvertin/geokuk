package cz.geokuk.plugins.kesoidkruhy;

import java.awt.Color;

/** Nastavení kruhů složené z ovládacích prvků dialogu. Zapnutí kruhů dialog nikdy nemění. */
final class KruhyZDialogu {

	private static final int MAX_ALFA = 255;

	private KruhyZDialogu() {}

	/** Kopie aktuálního nastavení se změněnou barvou, průhledností, velikostí, jednotkovostí a poloměrem jednotkových kruhů. */
	static KruhySettings zmen(final KruhySettings aktualni, final Color rgb, final int pruhlednostProcent, final int velikost, final boolean jednotkove, final int polomerMetru) {
		final KruhySettings k = aktualni.copy();
		k.setBarva(slozBarvu(rgb, pruhlednostProcent, aktualni.getBarva().getAlpha()));
		k.setVelikost(velikost);
		k.setJednotkovaVelikost(jednotkove);
		k.setPolomerMetru(polomerMetru);
		return k;
	}

	/** Barva ze složek RGB a průhlednosti; průhlednost, která odpovídá současné alfě, alfu nemění. */
	static Color slozBarvu(final Color rgb, final int pruhlednostProcent, final int aktualniAlfa) {
		final int alfa = pruhlednostProcent == pruhlednostProcent(aktualniAlfa) ? aktualniAlfa : alfa(pruhlednostProcent);
		return new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue(), alfa);
	}

	/** 0 % = plně krycí, 100 % = neviditelná. */
	static int pruhlednostProcent(final int alfa) {
		return (int) Math.round((MAX_ALFA - alfa) * 100.0 / MAX_ALFA);
	}

	static int alfa(final int pruhlednostProcent) {
		return MAX_ALFA - (int) Math.round(pruhlednostProcent * MAX_ALFA / 100.0);
	}
}
