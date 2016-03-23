/**
 *
 */
package cz.geokuk.core.render;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coord.JCoordPrekryvnik0;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.plugins.refbody.ReferencniBodSeZmenilEvent;

/**
 * @author veverka
 *
 */
public class JRenderNahledPrekryvnik extends JCoordPrekryvnik0 {
	private static final long serialVersionUID = -5996655830197513951L;
	private int renderedMoumer = -1;
	private Mou moustred;
	private Coord moord;

	/**
	 *
	 */
	public JRenderNahledPrekryvnik() {
		// TODO To by mělo jít jistojistě odstranit
		//    getCoord().setMoustred(new Wgs(50.284, 14.3563).toMou());
		//    getCoord().setMoumer(17);

	}

	public void onEvent(PripravaRendrovaniEvent event) {
		renderedMoumer = event.getRenderSettings().getRenderedMoumer();
		nastavto();
	}

	public void onEvent(ReferencniBodSeZmenilEvent event) {
		moustred = event.wgs.toMou();
		moord = event.getMoord();
		nastavto();
	}

	private void nastavto() {
		if (renderedMoumer < 0 || moord == null) return;
		setSoord(moord.derive(renderedMoumer, moustred));
	}



}
