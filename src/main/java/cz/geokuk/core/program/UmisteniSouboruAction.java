/**
 *
 */
package cz.geokuk.core.program;

import java.awt.event.KeyEvent;

import cz.geokuk.framework.DialogOpeningAction0;
import cz.geokuk.framework.JMyDialog0;

/**
 * @author veverka
 *
 */
public class UmisteniSouboruAction extends DialogOpeningAction0 {

	private static final long serialVersionUID = -2882817111560336824L;
	private final ESouborPanelName souborPanelName;

	/**
	 * @param aBoard
	 */
	public UmisteniSouboruAction(final ESouborPanelName souborPanelName) {
		super("Umístění souborů...");
		this.souborPanelName = souborPanelName;
		putValue(SHORT_DESCRIPTION, "Nastavení umístění souborů a složek, které program používá.");
		putValue(MNEMONIC_KEY, KeyEvent.VK_U);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public JMyDialog0 createDialog() {
		final JUmisteniSouboruDialog jPrehledSouboruDialog = new JUmisteniSouboruDialog();
		if (souborPanelName != null) {
			jPrehledSouboruDialog.fokusni(souborPanelName);
		}
		return jPrehledSouboruDialog;
	}

}
