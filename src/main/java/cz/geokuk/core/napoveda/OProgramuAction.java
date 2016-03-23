/**
 *
 */
package cz.geokuk.core.napoveda;

import java.awt.event.KeyEvent;

import cz.geokuk.core.program.JOProgramuDialog;
import cz.geokuk.framework.DialogOpeningAction0;
import cz.geokuk.framework.JMyDialog0;

/**
 * @author veverka
 *
 */
public class OProgramuAction extends DialogOpeningAction0 {

	private static final long serialVersionUID = -2882817111560336824L;

	/**
	 * @param aBoard
	 */
	public OProgramuAction() {
		super("O programu");
		putValue(SHORT_DESCRIPTION, "Zobrazí obrazovku s verzí programu, autorem a dalšími informacemi.");
		putValue(MNEMONIC_KEY, KeyEvent.VK_O);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cz.geokuk.framework.DialogOpeningAction0#createDialog()
	 */
	@Override
	public JMyDialog0 createDialog() {
		JOProgramuDialog dlg = new JOProgramuDialog();
		dlg.setModal(true);
		return dlg;
	}

}
