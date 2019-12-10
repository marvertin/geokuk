/**
 *
 */
package cz.geokuk.plugins.kesoid;

import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.genetika.Genotyp;

class GenotypBuilderWaymark {

	/**
	 *
	 */
	private final Genom genom;

	/**
	 *
	 */
	public GenotypBuilderWaymark(final Genom genom) {
		this.genom = genom;
	}

	public Genotyp build(final Waymark waymark, final Genotyp g0) {
		final Genotyp g = g0 .with(genom.ALELA_wm);
		switch (waymark.getVztah()) {
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