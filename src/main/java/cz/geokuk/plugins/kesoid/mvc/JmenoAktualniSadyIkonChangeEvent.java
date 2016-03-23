/**
 *
 */
package cz.geokuk.plugins.kesoid.mvc;

import cz.geokuk.plugins.kesoid.mapicon.ASada;

/**
 * @author Martin Veverka
 *
 */
public class JmenoAktualniSadyIkonChangeEvent extends KesoidModelEvent0 {
	private final ASada jmenoSadyIkon;

	/**
	 * @param jmenoSadyIkon
	 */
	public JmenoAktualniSadyIkonChangeEvent(final ASada jmenoSadyIkon) {
		super();
		this.jmenoSadyIkon = jmenoSadyIkon;
	}

	/**
	 * @return the jmenoSadyIkon
	 */
	public ASada getJmenoSadyIkon() {
		return jmenoSadyIkon;
	}

}
