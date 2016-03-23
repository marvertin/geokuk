/**
 *
 */
package cz.geokuk.core.coord;

/**
 * @author veverka
 *
 */
public enum EJakOtacetPriRendrovani {

    /** Otočení předáme do COORD a grafiku neotáčíme. Vhodné u pokisků, waypointů a poodbně */
	COORD,

	/** Otočíme grafiku, aby se otočilo vše. Nutné u MAP */
	GRAPH2D
}
