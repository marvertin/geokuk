/**
 *
 */
package cz.geokuk.plugins.cesty.akce.cesta;


import java.awt.Color;

import cz.geokuk.core.coordinates.FGeoKonvertor;
import cz.geokuk.plugins.cesty.data.Cesta;

/**
 * Jde na vybranou pozici
 * @author veverka
 *
 */
public class PredraditVybranouCestu extends CestaAction0 {

	private static final long serialVersionUID = -2882817111560336824L;

	//  private Pozice pozice;
	/**
	 * @param aBoard
	 */
	public PredraditVybranouCestu(Cesta cesta) {
		super(cesta);
		putValue(NAME, "Předřadit vybranou cestu");
		putValue(SHORT_DESCRIPTION, "Před tuto cestu předřadí vybranou cestu spojením startovního bodu této cesty a cílového bodu vybrané cesty.");
		//putValue(MNEMONIC_KEY, KeyEvent.VK_P);
		//putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("RIGHT"));
	}

	@Override
	protected boolean mamPovolitProCestu(Cesta cesta) {
		return curta() != null && curta() != cesta;
	}

	@Override
	protected void nastavJmenoAkce(Cesta cesta, boolean aZKontextovehoMenu) {
		putValue(NAME, "<html>Předřadit před" + cesta.getNazevADalkaHtml() + " vybranou cestu" +
				curta().getNazevADalkaHtml() + " usekem " + Cesta.dalkaHtml(FGeoKonvertor.dalka(cesta.getCil(), curta().getStart()), Color.BLACK));
	}

	@Override
	protected void provedProCestu(Cesta cesta) {
		cesta.kontrolaKonzistence();
		curta().kontrolaKonzistence();
		cestyModel.pripojitCestuPred(cesta, curta());
		cesta.kontrolaKonzistence();
		curta().kontrolaKonzistence();
	}

}
