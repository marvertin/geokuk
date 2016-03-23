/**
 *
 */
package cz.geokuk.plugins.kesoid.mvc;

import java.awt.event.ActionEvent;
import java.net.URL;

import javax.swing.AbstractAction;

import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.util.process.BrowserOpener;

/**
 * @author veverka
 *
 */
public class TiskniNaGcComAction extends AbstractAction {

	private static final long	serialVersionUID	= -8054017274338240706L;
	private final URL			url;

	/**
	 *
	 */
	public TiskniNaGcComAction(Kesoid kesoid) {
		super("Tisk na webu", kesoid.getUrlIcon());
		url = kesoid.getUrlPrint();
		putValue(SHORT_DESCRIPTION, "Zobrazí listing keše na geocaching COM v tisknutelné podobě.");
		// putValue(MNEMONIC_KEY, InputEvent.)
		// putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));

	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */

	public void actionPerformed(ActionEvent e) {
		BrowserOpener.displayURL(url);
	}

}
