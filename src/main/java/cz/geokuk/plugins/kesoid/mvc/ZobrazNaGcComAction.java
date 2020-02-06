/**
 *
 */
package cz.geokuk.plugins.kesoid.mvc;

import javax.swing.Action;

import cz.geokuk.framework.OpenUrlAction0;
import cz.geokuk.plugins.kesoid.Wpt;

/**
 * @author Martin Veverka
 *
 */
public class ZobrazNaGcComAction extends OpenUrlAction0 {

	private static final long serialVersionUID = -8054017274338240706L;

	/**
	 *
	 */
	public ZobrazNaGcComAction(final Wpt wpt) {
		super(wpt.getKesoid().getUrlShow());
		putValue(Action.NAME, "Zobrazení na webu");
		putValue(SMALL_ICON, wpt.getKesoid().getUrlIcon());
		putValue(SHORT_DESCRIPTION, "Zobrazí listing keše na geocaching COM.");
		// putValue(MNEMONIC_KEY, InputEvent.)
		// putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));
	}

}
