/**
 *
 */
package cz.geokuk.core.coord;

import java.awt.event.ActionEvent;

import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.framework.Action0;

/**
 * @author veverka
 *
 */
public class ZoomPoziceAction extends Action0 {

	private static final long serialVersionUID = -8054017274338240706L;
	// private final Wgs wgs;

	/**
	 *
	 */
	public ZoomPoziceAction(final Wgs wgs) {
		// this.wgs = wgs;
		super.putValue(NAME, "Přiblížit..");
		// super.putValue(SMALL_ICON, Board.ikonizer.findIcon(wpt, "x16", true));
		// super.putValue(SMALL_ICON, ikonBag.seekIkon(wpt.getGenotyp(ikonBag.getGenom())));
		putValue(SHORT_DESCRIPTION, "Přiblíží mapu na vybranou pozici.");
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
		vyrezModel.setMeritkoMapyAutomaticky(vyrezModel.nejblizsiMeritko());
		// Board.eveman.fire(new PoziceChangedEvent(wgs, true));
	}

}
