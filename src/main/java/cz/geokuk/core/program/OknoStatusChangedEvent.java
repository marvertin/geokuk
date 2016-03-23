/**
 *
 */
package cz.geokuk.core.program;

import cz.geokuk.framework.Event0;

/**
 * @author Martin Veverka
 *
 */
public class OknoStatusChangedEvent extends Event0<OknoModel> {
	private final int stavOkna;
	private final OknoUmisteniDto oknoUmisteni;

	/**
	 * @param oknoUmisteni
	 */
	public OknoStatusChangedEvent(final int stavOkna, final OknoUmisteniDto oknoUmisteni) {
		this.stavOkna = stavOkna;
		this.oknoUmisteni = oknoUmisteni;
	}

	/**
	 * @return the oknoStatus
	 */
	public OknoUmisteniDto getOknoStatus() {
		return oknoUmisteni;
	}

	/**
	 * @return the stavOkna
	 */
	public int getStavOkna() {
		return stavOkna;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "OknoStatusChangedEvent [oknoUmisteni=" + oknoUmisteni + ", stavOkna=" + stavOkna + "]";
	}

}
