/**
 *
 */
package cz.geokuk.plugins.kesoid;

import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.genetika.Genotyp;

class GenotypBuilderWpt {

	/**
	 *
	 */
	private final Genom genom;

	/**
	 *
	 */
	public GenotypBuilderWpt(final Genom genom) {
		this.genom = genom;
	}

	Genotyp build(final Wpt wpt, final Genotyp g) {
		return g
				.with(genom.alelaSym(wpt.getSym(), null, null))
				.with(wpt.isMainWpt() ? genom.ALELA_h : genom.ALELA_v);
	}

}