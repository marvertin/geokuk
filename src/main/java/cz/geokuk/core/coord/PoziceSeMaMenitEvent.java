/**
 *
 */
package cz.geokuk.core.coord;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Uchopenec;
import cz.geokuk.framework.Event0;

/**
 * Event bude poslán vždy, když se má měnit pozice, aby se zjistilo, zda přesně na těchto souřadnicích nesídlí nějaké objekty. Jaké objekty tam sídlí řeknou jednotlivé modely.
 *
 */
public class PoziceSeMaMenitEvent extends Event0<PoziceModel> {

	public final Mou	mou;
	// private final List<Uchopenec> uchopenci = new ArrayList<Uchopenec>();
	private Uchopenec	uchopenec;
	private int			priorita;

	/**
	 * @param aPozice
	 * @param aMeloBySeCentrovat
	 */
	PoziceSeMaMenitEvent(final Mou mou) {
		assert mou != null;
		this.mou = mou;
	}

	public void add(final Uchopenec uchopenec, final int priorita) {
		assert uchopenec.getMou().equals(mou);
		if (priorita > this.priorita) {
			this.uchopenec = uchopenec;
			this.priorita = priorita;
		}
	}

	Uchopenec getUchopenec() {
		return uchopenec;
	}

}
