/**
 *
 */
package cz.geokuk.plugins.kesoid;

import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.genetika.Jedinec;

class GenotypBuilderCgp {

	/**
	 *
	 */
	private final Jedinec g;
	private final Genom genom;

	/**
	 *
	 */
	public GenotypBuilderCgp(final Genom genom, final Jedinec g) {
		this.genom = genom;
		this.g = g;
	}

	public void build(final CzechGeodeticPoint cgp) {
		g.put(genom.ALELA_gb);
		switch (cgp.getVztah()) {
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