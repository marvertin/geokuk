/**
 *
 */
package cz.geokuk.plugins.cesty.akce;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

/**
 * Jde na vybranou pozici
 *
 * @author veverka
 *
 */
public class BezNaKonecCestyAction extends CestyAction0 {

	private static final long serialVersionUID = -2882817111560336824L;

	// private Pozice pozice;
	/**
	 * @param aBoard
	 */
	public BezNaKonecCestyAction() {
		putValue(NAME, "Na konec cesty");
		putValue(SHORT_DESCRIPTION, "Přesune mapu na konec vybrané cesty. Pokud žádná cesta není vybraná, vybere nejbližší cestu.");
		// putValue(MNEMONIC_KEY, KeyEvent.VK_P);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("END"));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(final ActionEvent aE) {
		cestyModel.bezNaKonecCesty(curta());
		vyrezModel.vystredovatNaPozici();
	}

	@Override
	protected void vyletChanged() {
		setEnabled(!curdoc().isEmpty());
		super.vyletChanged();
	}

}
