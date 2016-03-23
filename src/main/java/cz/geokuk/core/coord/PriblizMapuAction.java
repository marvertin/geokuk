/**
 *
 */
package cz.geokuk.core.coord;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import cz.geokuk.framework.Action0;

/**
 * @author veverka
 *
 */
public class PriblizMapuAction extends Action0 {

	private static final long serialVersionUID = -8054017274338240706L;

	/**
	 *
	 */
	public PriblizMapuAction() {
		super("Přibliž mapu");
		putValue(SHORT_DESCRIPTION, "Změna měřítka mapy o jeden stupeň.");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));

	}
	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */

	@Override
	public void actionPerformed(final ActionEvent e) {
		vyrezModel.setMeritkoMapy(vyrezModel.getMoord().getMoumer() + 1);
	}

	public void onEvent(final VyrezChangedEvent event) {
		setEnabled(!event.getModel().jeNejblizsiMeritko());
	}

}
