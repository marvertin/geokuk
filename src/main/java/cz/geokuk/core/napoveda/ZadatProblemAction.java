/**
 *
 */
package cz.geokuk.core.napoveda;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;

import cz.geokuk.core.program.FConst;
import cz.geokuk.framework.Action0;
import cz.geokuk.util.process.BrowserOpener;

/**
 * @author Martin Veverka
 *
 */
public class ZadatProblemAction extends Action0 {

	private static final long serialVersionUID = -2882817111560336824L;

	/**
	 * @param aBoard
	 */
	public ZadatProblemAction() {
		super("Zadat problém ...");
		putValue(SHORT_DESCRIPTION, "Zobrazí stránku na code.google.com, která umožní zadat chybu v Geokuku nebo požadavek na novou funkcionalitu.");
		putValue(MNEMONIC_KEY, KeyEvent.VK_P);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(final ActionEvent aE) {
		try {
			BrowserOpener.displayURL(new URL(FConst.POST_PROBLEM_URL));
		} catch (final MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

}
