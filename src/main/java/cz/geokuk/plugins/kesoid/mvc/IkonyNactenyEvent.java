/**
 *
 */
package cz.geokuk.plugins.kesoid.mvc;

import cz.geokuk.framework.Event0;
import cz.geokuk.plugins.kesoid.mapicon.ASada;
import cz.geokuk.plugins.kesoid.mapicon.IkonBag;

/**
 * @author Martin Veverka
 *
 */
public class IkonyNactenyEvent extends Event0<KesoidModel> {
	private final IkonBag vsechny;
	private final ASada jmenoAktualniSady;

	public IkonyNactenyEvent(final IkonBag vsechny, final ASada jmenoAktualniSady) {
		super();
		this.vsechny = vsechny;
		this.jmenoAktualniSady = jmenoAktualniSady;
		assert jmenoAktualniSady != null;
	}

	public IkonBag getBag() {
		return vsechny;
	}

	/**
	 * @return the jmenoAktualniSady
	 */
	public ASada getJmenoAktualniSady() {
		return jmenoAktualniSady;
	}
}
