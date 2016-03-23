/**
 *
 */
package cz.geokuk.core.program;

import cz.geokuk.framework.Event0;

/**
 * @author veverka
 *
 */
public class OknoStatusChangedEvent extends Event0<OknoModel> {
	private final int				stavOkna;
	private final OknoUmisteniDto	oknoUmisteni;

	/**
	 * @param oknoUmisteni
	 */
	public OknoStatusChangedEvent(int stavOkna, OknoUmisteniDto oknoUmisteni) {
		this.stavOkna = stavOkna;
		this.oknoUmisteni = oknoUmisteni;
	}

	/**
	 * @return the oknoStatus
	 */
	public OknoUmisteniDto getOknoStatus() {
		return oknoUmisteni;
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

	/**
	 * @return the stavOkna
	 */
	public int getStavOkna() {
		return stavOkna;
	}

}
