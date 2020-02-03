/**
 *
 */
package cz.geokuk.plugins.kesoid.mvc;

import java.awt.event.ActionEvent;
import java.net.URL;

import cz.geokuk.framework.Action0;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.util.process.BrowserOpener;

/**
 * @author Martin Veverka
 *
 */
public class TiskniNaGcComAction extends Action0 {

	private static final long serialVersionUID = -8054017274338240706L;
	private final URL url;

	/**
	 *
	 */
	public TiskniNaGcComAction(final Wpt wpt) {
		super("Tisk na webu", wpt.getKesoid().getUrlIcon());
		url = wpt.getKesoid().getUrlPrint();
		putValue(SHORT_DESCRIPTION, "Zobrazí listing keše na geocaching COM v tisknutelné podobě.");
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
		BrowserOpener.displayURL(url);
	}

	@Override
	public boolean shouldBeVisible() {
		return url != null;
	}

}
