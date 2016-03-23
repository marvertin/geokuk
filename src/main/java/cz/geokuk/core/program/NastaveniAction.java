/**
 *
 */
package cz.geokuk.core.program;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import cz.geokuk.framework.Action0;
import cz.geokuk.framework.Dlg;

/**
 * @author veverka
 *
 */
public class NastaveniAction extends Action0 {

	private static final long serialVersionUID = -2882817111560336824L;

	/**
	 * @param aBoard
	 */
	public NastaveniAction() {
		super("Nastavení...");
		putValue(SHORT_DESCRIPTION, "Umožní nastavit nejhrůznější parametry.");
		putValue(MNEMONIC_KEY, KeyEvent.VK_N);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(final ActionEvent aE) {
		Dlg.info("Akce ještě nebyla implementována.", "Informace");
		setEnabled(false);
	}

}
