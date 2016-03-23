/**
 *
 */
package cz.geokuk.plugins.cesty.akce.usek;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.plugins.cesty.data.Usek;

/**
 * Jde na vybranou pozici
 *
 * @author Martin Veverka
 *
 */
public class UriznoutCestuVUsekuAction extends UsekAction0 {

	private static final long serialVersionUID = -2882817111560336824L;

	// private Pozice pozice;
	/**
	 * @param aBoard
	 */
	public UriznoutCestuVUsekuAction(final Usek usek, final Mou mouMysi) {
		super(usek, mouMysi);
		putValue(NAME, "Uříznout cestu");
		putValue(SHORT_DESCRIPTION, "Uřízne konec od daného místa na úseku a vyhodí pryč.");
		// putValue(MNEMONIC_KEY, KeyEvent.VK_P);
		// putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("RIGHT"));
	}

	@Override
	protected boolean mamPovolitProUsek(final Usek usek, final Mou mou) {
		return true;
	}

	@Override
	protected void nastavJmenoAkce(final Usek usek, final Mou mou) {
		putValue(NAME, "<html>Uříznout cestu" + usek.getCesta().getNazevHtml() + " " + usek.dalkaCestaRozdelenoHtml(mou));
	}

	@Override
	protected void provedProUsek(final Usek usek, final Mou mou) {
		cestyModel.rozdelCestuVUseku(usek, mou, true);
	}

}
