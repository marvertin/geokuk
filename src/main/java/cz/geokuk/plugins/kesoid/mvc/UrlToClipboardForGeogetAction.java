/**
 *
 */
package cz.geokuk.plugins.kesoid.mvc;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import cz.geokuk.core.coord.PoziceChangedEvent;
import cz.geokuk.core.coord.Poziceq;
import cz.geokuk.framework.Action0;
import cz.geokuk.img.ImageLoader;
import cz.geokuk.plugins.kesoid.Wpt;

/**
 * @author Martin Veverka
 *
 */
public class UrlToClipboardForGeogetAction extends Action0 {

	private static final long serialVersionUID = -8054017274338240706L;
	private final Wpt iWpt;
	private Poziceq poziceq = new Poziceq();
	private KesoidModel kesoidModel;

	/**
	 *
	 */
	public UrlToClipboardForGeogetAction(final Wpt wpt) {
		super("Url do Geogetu", ImageLoader.seekResIcon("x16/geoget.png"));
		iWpt = wpt;
		putValue(SHORT_DESCRIPTION, "Do systémového clipboardu vloží URL listingu keše, na nějž se chytne spuštěný GEOGET (a možná i GSAK).");
		// putValue(MNEMONIC_KEY, InputEvent.)
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F7"));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */

	@Override
	public void actionPerformed(final ActionEvent e) {
		Wpt wpt = iWpt;
		if (wpt == null) {
			wpt = poziceq.getWpt();
			if (wpt == null) {
				return;
			}
		}
		kesoidModel.pridejDoSeznamuVGeogetu(wpt);
	}

	public void inject(final KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
	}

	public void onEvent(final PoziceChangedEvent event) {
		poziceq = event.poziceq;
		setEnabled(iWpt != null || poziceq.getWpt() != null);
	}

	@Override
	public boolean shouldBeVisible() {
		return iWpt != null && iWpt.getUrlProPridaniDoSeznamuVGeogetu() != null;
	}
}
