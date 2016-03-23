/**
 *
 */
package cz.geokuk.plugins.kesoid.mvc;

import cz.geokuk.framework.DialogOpeningAction0;
import cz.geokuk.framework.JMyDialog0;

/**
 * @author veverka
 *
 */
public class NickEditAction extends DialogOpeningAction0 {

	private static final long serialVersionUID = -2882817111560336824L;

	/**
	 * @param aBoard
	 */
	public NickEditAction() {
		super("Nastavit nick...");
		putValue(SHORT_DESCRIPTION, "Nastaví nick, který používáš na geocaching.com, nutné pro označení tebou založených kešíků.");
		// putValue(MNEMONIC_KEY, KeyEvent.VK_D);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public JMyDialog0 createDialog() {
		return new JNickEditDialog();
	}

}
