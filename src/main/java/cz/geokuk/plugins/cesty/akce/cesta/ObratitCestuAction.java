package cz.geokuk.plugins.cesty.akce.cesta;

import cz.geokuk.plugins.cesty.data.Cesta;

public class ObratitCestuAction extends CestaAction0 {

	private static final long serialVersionUID = 1L;

	public ObratitCestuAction(Cesta cesta) {
		super(cesta);

		// putValue(NAME, "<html>Odstraň cestu <i>" + jCestaMenu.getNazev() + "</i> " + (jCestaMenu.getMouDelkaCesta() + " mou"));
		putValue(NAME, "Obrátit cestu");
		putValue(SHORT_DESCRIPTION, "Změní směr cesty, koncový bod se stane počátečním bodem, počáteční bod koncovým bodem.");
		// putValue(MNEMONIC_KEY, KeyEvent.VK_V);
		// putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F3"));
		// putValue(SMALL_ICON, ImageLoader.seekResIcon("x16/vylet/vyletAno.png"));
	}

	@Override
	protected boolean mamPovolitProCestu(Cesta cesta) {
		return !cesta.isJednobodova();
	}

	@Override
	protected void nastavJmenoAkce(Cesta cesta, boolean aZKontextovehoMenu) {
		putValue(NAME, "<html>Obrátit cestu" + cesta.getNazevHtml() + " " + cesta.dalkaHtml());
	}

	@Override
	protected void provedProCestu(Cesta cesta) {
		cestyModel.reverseCestu(cesta);
	}

}
