/**
 *
 */
package cz.geokuk.plugins.kesoid.mvc;

import java.awt.event.ActionEvent;

import cz.geokuk.core.coord.PoziceChangedEvent;
import cz.geokuk.core.coord.Poziceq;
import cz.geokuk.framework.Action0;
import cz.geokuk.plugins.kesoid.Wpt;

/**
 * @author Martin Veverka
 *
 */
public class KesoidCodeToClipboard extends Action0 {

	private static final long serialVersionUID = -8054017274338240706L;
	private final Wpt wpt;
	private Poziceq poziceq = new Poziceq();
	private KesoidModel kesoidModel;

	/**
	 *
	 */
	public KesoidCodeToClipboard(final Wpt wpt) {
		super("<html>Identifikátor <i>" + wpt.getIdentifier() + "</i> do schráky");
		this.wpt = wpt;
		putValue(SHORT_DESCRIPTION, "Do systémového clipboardu vloží kód waypointu.");

		// putValue(MNEMONIC_KEY, InputEvent.)
		// putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F7"));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */

	@Override
	public void actionPerformed(final ActionEvent e) {
		Wpt kes = wpt;
		if (kes == null) {
			kes = poziceq.getWpt();
			if (kes == null) {
				return;
			}
		}
		kesoidModel.pridejKodKesoiduDoClipboardu(kes);
	}

	public void inject(final KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
	}

	public void onEvent(final PoziceChangedEvent event) {
		poziceq = event.poziceq;
		setEnabled(wpt != null || poziceq.getWpt() != null);
	}


	@Override
	public boolean shouldBeVisible() {
		return wpt != null && wpt.getIdentifier() != null;
	}
}
