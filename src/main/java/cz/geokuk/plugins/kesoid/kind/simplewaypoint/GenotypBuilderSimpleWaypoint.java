/**
 *
 */
package cz.geokuk.plugins.kesoid.kind.simplewaypoint;

import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.genetika.Genotyp;

class GenotypBuilderSimpleWaypoint {

	/**
	 *
	 */
	private final Genom genom;

	/**
	 *
	 */
	public GenotypBuilderSimpleWaypoint(final Genom genom) {
		this.genom = genom;
	}

	public Genotyp build(final SimpleWaypoint simpleWpt, final Genotyp g) {
		return g.with(genom.ALELA_wp);
	}

}