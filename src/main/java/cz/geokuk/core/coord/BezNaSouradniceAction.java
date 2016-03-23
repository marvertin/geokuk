package cz.geokuk.core.coord;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.framework.DialogOpeningAction0;
import cz.geokuk.framework.JMyDialog0;
import cz.geokuk.plugins.kesoid.hledani.JSouradnicovyFrame;

public class BezNaSouradniceAction extends DialogOpeningAction0 {

	private static final long serialVersionUID = -2637836928166450446L;

	public BezNaSouradniceAction() {
		super("Zadat souřadnice...");
		putValue(SHORT_DESCRIPTION, "Zobrazí dialog pro zadání souřadnic a pak centruje mapu na těchto souřadnicích.");
		putValue(MNEMONIC_KEY, KeyEvent.VK_S);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke('L', InputEvent.CTRL_DOWN_MASK));
	}

	@Override
	public JMyDialog0 createDialog() {
		return new JSouradnicovyFrame();
	}

}
