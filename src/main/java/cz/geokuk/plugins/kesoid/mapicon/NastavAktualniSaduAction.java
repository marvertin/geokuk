package cz.geokuk.plugins.kesoid.mapicon;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import cz.geokuk.plugins.kesoid.mvc.IkonyNactenyEvent;

/**
 * @author veverka
 *
 */
public class NastavAktualniSaduAction extends AbstractAction {

	private static final long serialVersionUID = -8054017274338240706L;

	/**
	 *
	 */
	public NastavAktualniSaduAction(final String jmenoSady) {
		super(jmenoSady);
		putValue(SHORT_DESCRIPTION, "Nastaví aktuální sadu mapových ikon.");
		// putValue(MNEMONIC_KEY, KeyEvent.VK_R);
		// Board.eveman.register(this);
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */

	@Override
	public void actionPerformed(final ActionEvent e) {
		System.out.println("Sada nastavena");
	}

	public void onEvent(final IkonyNactenyEvent event) {
		// setEnabled(true);
	}

}
