package cz.geokuk.plugins.kesoidpopisky;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.framework.Action0;



public class PopiskyNastavParametryAction extends Action0 {

	private static final long serialVersionUID = -2637836928166450446L;

	public PopiskyNastavParametryAction(Void v) {
		super("Nastavení popisků...");
		putValue(SHORT_DESCRIPTION, "Zobrazí dialog pro nastavení fontu a barev popisků na mapě.");
		putValue(MNEMONIC_KEY, KeyEvent.VK_P);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl F12"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JPopiskyDialog frame = factory.init(new JPopiskyDialog());
		frame.setVisible(true);
	}


}
