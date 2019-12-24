/**
 *
 */
package cz.geokuk.plugins.kesoid.mvc;

import java.awt.event.ActionEvent;

import cz.geokuk.framework.Action0;
import cz.geokuk.framework.AfterEventReceiverRegistrationInit;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.mapicon.IkonBag;

/**
 * @author Martin Veverka
 *
 */
public class CenterWaypointAction extends Action0 implements AfterEventReceiverRegistrationInit {

	private static final long serialVersionUID = -8054017274338240706L;
	private final Wpt wpt;
	private IkonBag ikonBag;

	/**
	 *
	 */
	public CenterWaypointAction(final Wpt wpt) {
		this.wpt = wpt;
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
		poziceModel.setPozice(wpt);
		vyrezModel.vystredovatNaPozici();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.program.AfterEventReceiverRegistrationInit#initAfterEventReceiverRegistration()
	 */
	@Override
	public void initAfterEventReceiverRegistration() {
		super.putValue(NAME, wpt.getName() + " - " + wpt.getNazev());
		// super.putValue(SMALL_ICON, Board.ikonizer.findIcon(wpt, "x16", true));
		super.putValue(SMALL_ICON, ikonBag.seekIkon(wpt.getGenotyp()));
		super.putValue(SHORT_DESCRIPTION, "Vycentruje daný waypoint keše.");

	}

	public void onEvent(final IkonyNactenyEvent event) {
		ikonBag = event.getBag();

	}
}
