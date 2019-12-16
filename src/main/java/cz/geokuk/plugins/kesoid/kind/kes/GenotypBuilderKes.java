/**
 *
 */
package cz.geokuk.plugins.kesoid.kind.kes;

import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.genetika.Genotyp;

class GenotypBuilderKes {

	/**
	 *
	 */
	private final Genom genom;

	/**
	 *
	 */
	public GenotypBuilderKes(final Genom genom) {
		this.genom = genom;
	}

	public Genotyp build(final Kes kes, final Genotyp g0) {
		Genotyp g = g0.with(genom.ALELA_gc);
		switch (kes.getVztah()) {
		case NORMAL:
			g = g.with(genom.ALELA_hnf);
			break;
		case FOUND:
			g = g.with(genom.ALELA_fnd);
			break;
		case OWN:
			g = g.with(genom.ALELA_own);
			break;
		case NOT:
			g = g.with(genom.ALELA_not);
			break;
		}
		switch (kes.getStatus()) {
		case ACTIVE:
			g = g.with(genom.ALELA_actv);
			break;
		case DISABLED:
			g = g.with(genom.ALELA_dsbl);
			break;
		case ARCHIVED:
			g = g.with(genom.ALELA_arch);
			break;
		}
		switch (kes.getSize()) {
		case UNKNOWN:
			g = g.with(genom.ALELA_00000);
			break;
		case NOT_CHOSEN:
			g = g.with(genom.ALELA_nlist);
			break;
		case MICRO:
			g = g.with(genom.ALELA_micro);
			break;
		case SMALL:
			g = g.with(genom.ALELA_small);
			break;
		case REGULAR:
			g = g.with(genom.ALELA_regul);
			break;
		case LARGE:
			g = g.with(genom.ALELA_large);
			break;
		case VIRTUAL:
			g = g.with(genom.ALELA_virtu);
			break;
		case HUGE:
			g = g.with(genom.ALELA_hugex);
			break;
		case OTHER:
			g = g.with(genom.ALELA_other);
			break;
		}
		if (kes.hasValidFinal()) {
			g = g.with(genom.ALELA_vylusteno);
		}

		g = g
				.with(genom.GEN_teren.alela("ter" + kes.getTerrain().to2DigitNumberString()))
				.with(genom.GEN_obtiznost.alela("dif" + kes.getDifficulty().to2DigitNumberString()));
		return g;
	}
}