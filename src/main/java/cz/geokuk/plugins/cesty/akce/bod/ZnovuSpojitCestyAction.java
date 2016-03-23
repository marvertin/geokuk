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
public class ZnovuSpojitCestyAction extends BodAction0 {

	private static final long serialVersionUID = -2882817111560336824L;

	// private Pozice pozice;
	/**
	 * @param aBoard
	 */
	public ZnovuSpojitCestyAction(final Bod bod) {
		super(bod);
		putValue(NAME, "Znovu spojit cesty");
		putValue(SHORT_DESCRIPTION, "Pokud došlo v některém bodě k rozdělení cest, je možné je zde znovu spojit.");
		// putValue(MNEMONIC_KEY, KeyEvent.VK_P);
		// putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("RIGHT"));
	}

	@Override
	protected boolean mamPovolitProBod(final Bod bod) {
		return bod.getKoncovyBodDruheCestyVhodnyProSpojeni() != null;
	}

	@Override
	protected void nastavJmenoAkce(final Bod bod, final boolean aZKontextovehoMenu) {
		putValue(NAME, "<html>Znovu spojit cesty" + bod.getCesta().getNazevHtml() + " " + bod.getCesta().getNazevADalkaHtml());
	}

	@Override
	protected void provedProBod(final Bod bod) {
		cestyModel.spojCestyVPrekryvnemBode(bod);
	}

}
