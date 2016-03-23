/**
 *
 */
package cz.geokuk.plugins.kesoid;

import cz.geokuk.plugins.kesoid.mapicon.Genom;
import cz.geokuk.plugins.kesoid.mapicon.Genotyp;

class GenotypBuilderSimpleWaypoint {

	/**
	 *
	 */
	private final Genotyp	g;
	private final Genom		genom;

	/**
	 *
	 */
	public GenotypBuilderSimpleWaypoint(final Genom genom, final Genotyp g) {
		this.genom = genom;
		this.g = g;
	}

	public void build(final SimpleWaypoint simpleWpt) {
		g.put(genom.ALELA_wp);
	}

}