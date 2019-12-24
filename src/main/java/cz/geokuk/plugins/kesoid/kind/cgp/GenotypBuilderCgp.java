/**
 *
 */
package cz.geokuk.plugins.kesoid.kind.cgp;

import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.genetika.Genotyp;

class GenotypBuilderCgp {

	/**
	 *
	 */
	private final Genom genom;

	/**
	 *
	 */
	public GenotypBuilderCgp(final Genom genom) {
		this.genom = genom;
	}

	public Genotyp build(final CzechGeodeticPoint cgp, final Genotyp g0) {
		final Genotyp g = g0.with(genom.ALELA_gb);
		switch (cgp.getVztah()) {
		case NORMAL:
			return g.with(genom.ALELA_hnf);
		case FOUND:
			return g.with(genom.ALELA_fnd);
		case OWN:
			return g.with(genom.ALELA_own);
		case NOT:
			return g.with(genom.ALELA_not);
		default: return g;
		}
	}

}