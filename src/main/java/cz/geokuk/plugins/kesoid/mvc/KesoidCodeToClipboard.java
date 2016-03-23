/**
 *
 */
package cz.geokuk.plugins.kesoid.mvc;

import java.awt.event.ActionEvent;

import cz.geokuk.core.coord.PoziceChangedEvent;
import cz.geokuk.core.coord.Poziceq;
import cz.geokuk.framework.Action0;
import cz.geokuk.plugins.kesoid.Kesoid;

/**
 * @author veverka
 *
 */
public class KesoidCodeToClipboard extends Action0 {

	private static final long	serialVersionUID	= -8054017274338240706L;
	private final Kesoid		kesoid;
	private Poziceq				poziceq				= new Poziceq();
	private KesoidModel			kesoidModel;

	/**
	 *
	 */
	public KesoidCodeToClipboard(final Kesoid aKesoid) {
		super("<html>Identifikátor <i>" + aKesoid.getIdentifier() + "</i> do schráky");
		kesoid = aKesoid;
		putValue(SHORT_DESCRIPTION, "Do systémového clipboardu vloží kód kešoidu.");
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
		Kesoid kes = kesoid;
		if (kes == null) {
			kes = poziceq.getKesoid();
			if (kes == null) {
				return;
			}
		}
		kesoidModel.pridejKodKesoiduDoClipboardu(kes);
	}

	public void onEvent(final PoziceChangedEvent event) {
		poziceq = event.poziceq;
		setEnabled(kesoid != null || poziceq.getWpt() != null);
	}

	public void inject(final KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
	}
}
