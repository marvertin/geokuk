/**
 *
 */
package cz.geokuk.plugins.kesoid.mvc;

import cz.geokuk.framework.OpenUrlAction0;
import cz.geokuk.plugins.kesoid.Wpt;

/**
 * @author Martin Veverka
 *
 */
public class TiskniNaGcComAction extends OpenUrlAction0 {

	private static final long serialVersionUID = -8054017274338240706L;

	/**
	 *
	 */
	public TiskniNaGcComAction(final Wpt wpt) {
		super(wpt.getKesoid().getUrlPrint(),"Tisk na webu", wpt.getKesoid().getUrlIcon());
		putValue(SHORT_DESCRIPTION, "Zobrazí listing keše na geocaching COM v tisknutelné podobě.");
		// putValue(MNEMONIC_KEY, InputEvent.)
		// putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));

	}


}
