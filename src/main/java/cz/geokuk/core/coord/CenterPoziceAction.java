/**
 *
 */
package cz.geokuk.core.coord;

import java.awt.event.ActionEvent;

import cz.geokuk.framework.Action0;

/**
 * @author Martin Veverka
 *
 */
public class CenterPoziceAction extends Action0 {

	private static final long serialVersionUID = -8054017274338240706L;

	/**
	 *
	 */
	public CenterPoziceAction() {
		super();
		super.putValue(NAME, "Centerovat...");
		// super.putValue(SMALL_ICON, Board.ikonizer.findIcon(wpt, "x16", true));
		// super.putValue(SMALL_ICON, ikonBag.seekIkon(wpt.getGenotyp(ikonBag.getGenom())));
		putValue(SHORT_DESCRIPTION, "Vycentruje označenou pozici.");
		// putValue(MNEMONIC_KEY, InputEvent.)
		// putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(final ActionEvent e) {
		vyrezModel.vystredovatNaPozici();
	}

}
