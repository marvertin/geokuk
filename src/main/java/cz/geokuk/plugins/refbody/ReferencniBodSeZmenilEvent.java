/**
 *
 */
package cz.geokuk.plugins.refbody;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.Event0;

/**
 * Pokud se změní bod, od kterého se cosi odvozuje. Buď je to aktuální pozice nebo střed mapy.
 *
 * @author veverka
 *
 */
public class ReferencniBodSeZmenilEvent extends Event0<RefbodyModel> {

	public final Wgs wgs;
	private final Coord moord;

	/**
	 * @param aWgs
	 */
	public ReferencniBodSeZmenilEvent(final Wgs aWgs, final Coord moord) {
		wgs = aWgs;
		this.moord = moord;
	}

	public Coord getMoord() {
		return moord;
	}

}
