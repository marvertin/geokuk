/**
 *
 */
package cz.geokuk.core.render;

import cz.geokuk.core.coord.Coord;

/**
 * @author veverka
 *
 */
public class RenderParams {
	// vstupy
	Coord roord;
	boolean pruhledne;
	boolean natacetDoSeveru = true;
	int drawOrder;

	RenderParams() {

	}
	//
	//  String computePureFileName() {
	//    Mou moustred = coord.getMoustred();
	//    int moumer = coord.getMoumer();
	//    String result = String.format(Locale.ENGLISH, "n%7fe%7f%d", moustred.toWgs().lat, moustred.toWgs().lon, moumer).replace(".", "");
	//    return result;
	//  }

}
