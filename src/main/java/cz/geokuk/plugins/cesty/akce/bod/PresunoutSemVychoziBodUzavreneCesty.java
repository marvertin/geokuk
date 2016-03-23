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
public class PresunoutSemVychoziBodUzavreneCesty extends BodAction0 {

	private static final long serialVersionUID = -2882817111560336824L;

	// private Pozice pozice;
	/**
	 * @param aBoard
	 */
	public PresunoutSemVychoziBodUzavreneCesty(final Bod bod) {
		super(bod);
		putValue(NAME, "Přesunout výchozí bod");
		putValue(SHORT_DESCRIPTION, "Přesune sem výchozí bod uzavřené cesty.");
		// putValue(MNEMONIC_KEY, KeyEvent.VK_P);
		// putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("RIGHT"));
	}

	@Override
	protected boolean mamPovolitProBod(final Bod bod) {
		return bod.getCesta().isKruh() && !bod.isKrajovy();
	}

	@Override
	protected void nastavJmenoAkce(final Bod bod, final boolean aZKontextovehoMenu) {
		putValue(NAME, "<html>Přesunout výchozí bod" + bod.getCesta().getNazevHtml() + " " + bod.dalkaCestaRozdelenoHtml(null));
	}

	@Override
	protected void provedProBod(final Bod bod) {
		cestyModel.presunoutVyhoziBod(bod);
	}

}
