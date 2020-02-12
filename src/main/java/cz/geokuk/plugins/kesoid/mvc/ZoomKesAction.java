/**
 *
 */
package cz.geokuk.plugins.kesoid.mvc;

import java.awt.event.ActionEvent;

import com.google.common.collect.Lists;

import cz.geokuk.core.coordinates.MouRect;
import cz.geokuk.framework.Action0;
import cz.geokuk.plugins.kesoid.Wpt;

/**
 * @author Martin Veverka
 *
 */
public class ZoomKesAction extends Action0 {

	private static final long serialVersionUID = -8054017274338240706L;
	private final Wpt wpt;

	/**
	 *
	 */
	public ZoomKesAction(final Wpt wpt) {
		super("Zoom keš (" + Lists.newArrayList(wpt.getRelatedWpts()).size() + ")");
		this.wpt = wpt;
		putValue(SHORT_DESCRIPTION, "Nastaví výřez a měřítko mapy tak ,aby na ní byla celá keška včetně všech multin. V záborce je počet waypointů keše.");
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
		final MouRect mourect = new MouRect();
		for (final Wpt wpt : wpt.getRelatedWpts()) {
			mourect.add(wpt.getWgs().toMou());
		}
		if (!mourect.isEmpty()) {
			mourect.resize(1.2);
			vyrezModel.zoomTo(mourect);
		}
	}

}
