package cz.geokuk.plugins.cesty.akce.doc;

import java.awt.event.KeyEvent;

import cz.geokuk.core.coordinates.MouRect;
import cz.geokuk.plugins.cesty.data.Bod;
import cz.geokuk.plugins.cesty.data.Doc;

public class CestyZoomAction extends DocAction0 {

	private static final long serialVersionUID = -7547868179813232769L;

	public CestyZoomAction(final Doc doc) {
		super(doc);
		putValue(NAME, "Zoomovat cesty");
		putValue(SHORT_DESCRIPTION, "Zobrazí měřítko a výřez mapy tak, aby na mapě byly všechny nakreslené cesty.");
		putValue(MNEMONIC_KEY, KeyEvent.VK_Z);
	}

	@Override
	protected boolean mamPovolitProDoc(final Doc doc) {
		return doc.getPocetCest() > 0;
	}

	@Override
	protected void nastavJmenoAkce(final Doc doc, final boolean aZKontextovehoMenu) {
		// Není nutno nic speciálního
	}

	@Override
	protected void provedProDoc(final Doc doc) {
		final MouRect mourect = new MouRect();
		for (final Bod bod : doc.getBody()) {
			mourect.add(bod.getMou());
		}
		mourect.resize(1.2);
		vyrezModel.zoomTo(mourect);
	}

}
