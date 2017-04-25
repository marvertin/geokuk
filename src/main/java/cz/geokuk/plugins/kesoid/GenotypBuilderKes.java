/**
 *
 */
package cz.geokuk.plugins.kesoid;

import cz.geokuk.plugins.kesoid.mapicon.Genom;
import cz.geokuk.plugins.kesoid.mapicon.Genotyp;

class GenotypBuilderKes {

	/**
	 *
	 */
	private final Genotyp g;
	private final Genom genom;

	/**
	 *
	 */
	public GenotypBuilderKes(final Genom genom, final Genotyp g) {
		this.genom = genom;
		this.g = g;
	}

	public void build(final Kes kes) {
		g.put(genom.ALELA_gc);
		switch (kes.getVztah()) {
		case NORMAL:
			g.put(genom.ALELA_hnf);
			break;
		case FOUND:
			g.put(genom.ALELA_fnd);
			break;
		case OWN:
			g.put(genom.ALELA_own);
			break;
		case NOT:
			g.put(genom.ALELA_not);
			break;
		}
		switch (kes.getStatus()) {
		case ACTIVE:
			g.put(genom.ALELA_actv);
			break;
		case DISABLED:
			g.put(genom.ALELA_dsbl);
			break;
		case ARCHIVED:
			g.put(genom.ALELA_arch);
			break;
		}
		switch (kes.getSize()) {
		case UNKNOWN:
			g.put(genom.ALELA_00000);
			break;
		case NOT_CHOSEN:
			g.put(genom.ALELA_nlist);
			break;
		case MICRO:
			g.put(genom.ALELA_micro);
			break;
		case SMALL:
			g.put(genom.ALELA_small);
			break;
		case REGULAR:
			g.put(genom.ALELA_regul);
			break;
		case LARGE:
			g.put(genom.ALELA_large);
			break;
		case VIRTUAL:
			g.put(genom.ALELA_virtu);
			break;
		case HUGE:
			g.put(genom.ALELA_hugex);
			break;
		case OTHER:
			g.put(genom.ALELA_other);
			break;
		}
		if (kes.hasValidFinal()) {
			g.put(genom.ALELA_vylusteno);
		}

		g.put(genom.seekAlela("ter" + kes.getTerrain().to2DigitNumberString()));
		g.put(genom.seekAlela("dif" + kes.getDifficulty().to2DigitNumberString()));
	}
}