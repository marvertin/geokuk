/**
 *
 */
package cz.geokuk.plugins.kesoid;

import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.genetika.Jedinec;

class GenotypBuilderWpt {

	/**
	 *
	 */
	final Jedinec g;
	private final Genom genom;

	/**
	 *
	 */
	public GenotypBuilderWpt(final Genom genom, final Jedinec g) {
		this.g = g;
		this.genom = genom;
	}

	void build(final Wpt wpt) {
		g.put(wpt.isMainWpt() ? genom.ALELA_h : genom.ALELA_v);
		g.put(genom.alelaSym(wpt.getSym(), null, null));
	}

}