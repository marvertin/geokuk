/**
 *
 */
package cz.geokuk.plugins.kesoid;

import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.genetika.Jedinec;

class GenotypBuilderSimpleWaypoint {

	/**
	 *
	 */
	private final Jedinec g;
	private final Genom genom;

	/**
	 *
	 */
	public GenotypBuilderSimpleWaypoint(final Genom genom, final Jedinec g) {
		this.genom = genom;
		this.g = g;
	}

	public void build(final SimpleWaypoint simpleWpt) {
		g.put(genom.ALELA_wp);
	}

}