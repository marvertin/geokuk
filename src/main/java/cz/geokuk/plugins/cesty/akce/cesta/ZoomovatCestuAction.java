package cz.geokuk.plugins.cesty.akce.cesta;

import cz.geokuk.core.coordinates.MouRect;
import cz.geokuk.plugins.cesty.data.Bod;
import cz.geokuk.plugins.cesty.data.Cesta;

public class ZoomovatCestuAction extends CestaAction0 {

	private static final long serialVersionUID = 1L;

	public ZoomovatCestuAction(final Cesta cesta) {
		super(cesta);
		// putValue(NAME, "<html>Odstraň cestu <i>" + jCestaMenu.getNazev() + "</i> " + (jCestaMenu.getMouDelkaCesta() + " mou"));
		putValue(NAME, "Zoomovat cestu");
		putValue(SHORT_DESCRIPTION, "Vybranou cestu nazoomuje do mapy tak, aby byla celá vidět.");
		// putValue(MNEMONIC_KEY, KeyEvent.VK_V);
		// putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F3"));
		// putValue(SMALL_ICON, ImageLoader.seekResIcon("x16/vylet/vyletAno.png"));
	}

	@Override
	protected boolean mamPovolitProCestu(final Cesta cesta) {
		return !cesta.isEmpty();
	}

	@Override
	protected void nastavJmenoAkce(final Cesta cesta, final boolean aZKontextovehoMenu) {
		putValue(NAME, "<html>Zoom cestu" + cesta.getNazevADalkaHtml());
	}

	@Override
	protected void provedProCestu(final Cesta cesta) {
		final MouRect mourect = new MouRect();
		for (final Bod bod : cesta.getBody()) {
			mourect.add(bod.getMou());
		}
		mourect.resize(1.2);
		vyrezModel.zoomTo(mourect);
	}

}
