/**
 *
 */
package cz.geokuk.plugins.cesty.akce.bod;

import cz.geokuk.plugins.cesty.data.Bod;

/**
 * Jde na vybranou pozici
 *
 * @author Martin Veverka
 *
 */
public class UriznoutCestuVBoduAction extends BodAction0 {

	private static final long serialVersionUID = -2882817111560336824L;

	// private Pozice pozice;
	/**
	 * @param aBoard
	 */
	public UriznoutCestuVBoduAction(final Bod bod) {
		super(bod);
		putValue(NAME, "Uříznout cestu");
		putValue(SHORT_DESCRIPTION, "Uřízne konec cesty od daného bodu dál. Tento bod v cestě zůstane.");
		// putValue(MNEMONIC_KEY, KeyEvent.VK_P);
		// putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("RIGHT"));
	}

	@Override
	protected boolean mamPovolitProBod(final Bod bod) {
		return !bod.isKrajovy();
	}

	@Override
	protected void nastavJmenoAkce(final Bod bod, final boolean aZKontextovehoMenu) {
		putValue(NAME, "<html>Uříznout cestu" + bod.getCesta().getNazevHtml() + " " + bod.dalkaCestaRozdelenoHtml(null));
	}

	@Override
	protected void provedProBod(final Bod bod) {
		cestyModel.rozdelCestuVBode(bod, true);
	}

}
