/**
 *
 */
package cz.geokuk.plugins.cesty.akce.cesta;

import java.awt.Color;

import cz.geokuk.core.coordinates.FGeoKonvertor;
import cz.geokuk.plugins.cesty.data.Cesta;

/**
 * Jde na vybranou pozici
 *
 * @author Martin Veverka
 *
 */
public class PripojitVybranouCestu extends CestaAction0 {

	private static final long serialVersionUID = -2882817111560336824L;

	// private Pozice pozice;
	/**
	 * @param aBoard
	 */
	public PripojitVybranouCestu(final Cesta cesta) {
		super(cesta);
		putValue(NAME, "Připojit vybranou cestu");
		putValue(SHORT_DESCRIPTION, "Za tuto cestu připojí vybranou cestu spojením koncového bodu této cesty a počátečního bodu vybrané cesty.");
		// putValue(MNEMONIC_KEY, KeyEvent.VK_P);
		// putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("RIGHT"));
	}

	@Override
	protected boolean mamPovolitProCestu(final Cesta cesta) {
		return curta() != null && curta() != cesta;
	}

	@Override
	protected void nastavJmenoAkce(final Cesta cesta, final boolean aZKontextovehoMenu) {
		putValue(NAME, "<html>Připojit k" + cesta.getNazevADalkaHtml() + " vybranou cestu" + curta().getNazevADalkaHtml() + " usekem "
		        + Cesta.dalkaHtml(FGeoKonvertor.dalka(cesta.getCil(), curta().getStart()), Color.BLACK));
	}

	@Override
	protected void provedProCestu(final Cesta cesta) {
		cesta.kontrolaKonzistence();
		curta().kontrolaKonzistence();
		cestyModel.pripojitCestuZa(cesta, curta());
		cesta.kontrolaKonzistence();
		curta().kontrolaKonzistence();
	}

}
