/**
 *
 */
package cz.geokuk.plugins.cesty.akce.bod;

import cz.geokuk.plugins.cesty.data.Bod;

/**
 * Jde na vybranou pozici
 * 
 * @author veverka
 *
 */
public class RozdelitCestuVBoduAction extends BodAction0 {

	private static final long serialVersionUID = -2882817111560336824L;

	// private Pozice pozice;
	/**
	 * @param aBoard
	 */
	public RozdelitCestuVBoduAction(Bod bod) {
		super(bod);
		putValue(NAME, "Rozdělit cestu");
		putValue(SHORT_DESCRIPTION, "Rozdělí cestu na dvě cesty kratší.");
		// putValue(MNEMONIC_KEY, KeyEvent.VK_P);
		// putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("RIGHT"));
	}

	@Override
	protected boolean mamPovolitProBod(Bod bod) {
		return !bod.isKrajovy();
	}

	@Override
	protected void nastavJmenoAkce(Bod bod, boolean aZKontextovehoMenu) {
		putValue(NAME, "<html>Rozdělit cestu" + bod.getCesta().getNazevHtml() + " " + bod.dalkaCestaRozdelenoHtml(null));
	}

	@Override
	protected void provedProBod(Bod bod) {
		cestyModel.rozdelCestuVBode(bod, false);
	}

}
