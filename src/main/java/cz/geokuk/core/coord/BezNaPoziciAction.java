/**
 *
 */
package cz.geokuk.core.coord;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import cz.geokuk.framework.Action0;

/**
 * Jde na vybranou pozici
 * @author veverka
 *
 */
public class BezNaPoziciAction extends Action0 {

	private static final long serialVersionUID = -2882817111560336824L;

	//  private Pozice pozice;
	/**
	 * @param aBoard
	 */
	public BezNaPoziciAction() {
		super("Na pozici");
		putValue(SHORT_DESCRIPTION, "Přesune mapu na aktuální pozici, pokud je nějaká vybrána.");
		putValue(MNEMONIC_KEY, KeyEvent.VK_P);
		//    putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("HOME"));
	}
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent aE) {
		//poziceModel.setPozice(pozice);
		vyrezModel.vystredovatNaPozici();

		//  	Board.eveman.fire(new PoziceChangedEvent(pozice, true));
	}

	public void onEvent(PoziceChangedEvent event) {
		setEnabled(! event.poziceq.isNoPosition());
	}

}
