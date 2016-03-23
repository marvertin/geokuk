package cz.geokuk.plugins.kesoid.mvc;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.framework.DialogOpeningAction0;
import cz.geokuk.framework.JMyDialog0;
import cz.geokuk.plugins.kesoid.hledani.JTextoveHledaniDialog;

public class HledejKesAction extends DialogOpeningAction0 {

	private static final long serialVersionUID = -2637836928166450446L;

	public HledejKesAction() {
		super("Keš ...");
		putValue(SHORT_DESCRIPTION, "Zobrazí dialog s možností vyhledat keš podle podřetězců v názvu a podle regulárních výrazů.");
		putValue(MNEMONIC_KEY, KeyEvent.VK_K);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('F', InputEvent.CTRL_DOWN_MASK));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cz.geokuk.framework.DialogOpeningAction0#createDialog()
	 */
	@Override
	public JMyDialog0 createDialog() {
		return new JTextoveHledaniDialog();
	}

}
