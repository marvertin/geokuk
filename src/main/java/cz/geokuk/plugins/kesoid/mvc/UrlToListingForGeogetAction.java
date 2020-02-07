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
import cz.geokuk.plugins.kesoid.Kesoid;
import cz.geokuk.plugins.kesoid.Wpt;

/**
 * @author Martin Veverka
 *
 */
public class UrlToListingForGeogetAction extends Action0 {

	private static final long serialVersionUID = -8054017274338240706L;
	private final Kesoid iKes;
	private Poziceq poziceq = new Poziceq();
	private KesoidModel kesoidModel;

	/**
	 *
	 */
	public UrlToListingForGeogetAction(final Wpt wpt) {
		super("Listing do Geogetu", ImageLoader.seekResIcon("x16/geoget.png"));
		iKes = wpt == null ? null : wpt.getKesoid();
		putValue(SHORT_DESCRIPTION, "Do systémového clipboardu vloží URL tiskové podoby listingu keše, na nějž se chytne spuštěný GEOGET a otevře listing (a možná i GSAK).");
		// putValue(MNEMONIC_KEY, InputEvent.)
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F3"));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */

	@Override
	public void actionPerformed(final ActionEvent e) {
		Kesoid kes = iKes;
		if (kes == null) {
			kes = poziceq.getKesoid();
			if (kes == null) {
				return;
			}
		}
		kesoidModel.otevriListingVGeogetu(kes);
	}

	public void inject(final KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
	}

	public void onEvent(final PoziceChangedEvent event) {
		poziceq = event.poziceq;
		setEnabled(iKes != null || poziceq.getWpt() != null);
	}

	@Override
	public boolean shouldBeVisible() {
		return iKes != null && iKes.getUrlProOtevreniListinguVGeogetu() != null;
	}
}
