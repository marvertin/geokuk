/**
 *
 */
package cz.geokuk.plugins.kesoid;

import cz.geokuk.plugins.kesoid.mapicon.Genom;
import cz.geokuk.plugins.kesoid.mapicon.Genotyp;

class GenotypBuilderWpt {

	/**
	 *
	 */
	final Genotyp g;
	private final Genom genom;

	/**
	 *
	 */
	public GenotypBuilderWpt(final Genom genom, final Genotyp g) {
		this.g = g;
		this.genom = genom;
	}

	void build(final Wpt wpt) {
		g.put(wpt.isMainWpt() ? genom.ALELA_h : genom.ALELA_v);
		g.put(genom.alelaSym(wpt.getSym(), null));
	}

}