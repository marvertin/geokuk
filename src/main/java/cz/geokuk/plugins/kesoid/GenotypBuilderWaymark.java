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
	private final Genotyp g;
	private final Genom genom;

	/**
	 *
	 */
	public GenotypBuilderWaymark(final Genom genom, final Genotyp g) {
		this.genom = genom;
		this.g = g;
	}

	public void build(final Waymark waymark) {
		g.put(genom.ALELA_wm);
		switch (waymark.getVztah()) {
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
	}

}