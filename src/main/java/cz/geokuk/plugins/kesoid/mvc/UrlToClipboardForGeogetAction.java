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

/**
 * @author veverka
 *
 */
public class UrlToClipboardForGeogetAction extends Action0 {

	private static final long	serialVersionUID	= -8054017274338240706L;
	private final Kesoid		kesoid;
	private Poziceq				poziceq				= new Poziceq();
	private KesoidModel			kesoidModel;

	/**
	 *
	 */
	public UrlToClipboardForGeogetAction(Kesoid aKesoid) {
		super("Url do Geogetu", ImageLoader.seekResIcon("x16/geoget.png"));
		kesoid = aKesoid;
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
	public void actionPerformed(ActionEvent e) {
		Kesoid kes = kesoid;
		if (kes == null) {
			kes = poziceq.getKesoid();
			if (kes == null)
				return;
		}
		kesoidModel.pridejDoSeznamuVGeogetu(kes);
	}

	public void onEvent(PoziceChangedEvent event) {
		poziceq = event.poziceq;
		setEnabled(kesoid != null || poziceq.getWpt() != null);
	}

	public void inject(KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
	}
}
