package cz.geokuk.plugins.kesoidkruhy;


import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.framework.Action0;



public class NastavParametryZvyraznovacichKruhuAction extends Action0 {

	private static final long serialVersionUID = -2637836928166450446L;

	public NastavParametryZvyraznovacichKruhuAction() {
		super("Nastavení kruhů...");
		putValue(SHORT_DESCRIPTION, "Zobrazí dialog pro nastavení barvy a velikosti zvýrazňovaíchkruhů.");
		putValue(MNEMONIC_KEY, KeyEvent.VK_K);
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl F8"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JKruhyDialog parametryZvyraznovacichKruhuFrame = factory.init(new JKruhyDialog());
		parametryZvyraznovacichKruhuFrame.setVisible(true);
	}


}
