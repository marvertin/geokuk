package cz.geokuk.plugins.cesty.akce.cesta;

import cz.geokuk.plugins.cesty.data.Cesta;

public class PospojovatVzdusneUseky extends CestaAction0 {

	private static final long serialVersionUID = 1L;

	public PospojovatVzdusneUseky(final Cesta cesta) {
		super(cesta);

		// putValue(NAME, "<html>Odstraň cestu <i>" + jCestaMenu.getNazev() + "</i> " + (jCestaMenu.getMouDelkaCesta() + " mou"));
		putValue(NAME, "Pospojovat vzdušné úseky");
		putValue(SHORT_DESCRIPTION, "Nalezne všechny vzdušné úseky v cestě a pospojuje je.");
		// putValue(MNEMONIC_KEY, KeyEvent.VK_V);
		// putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F3"));
		// putValue(SMALL_ICON, ImageLoader.seekResIcon("x16/vylet/vyletAno.png"));
	}

	@Override
	protected boolean mamPovolitProCestu(final Cesta cesta) {
		final int pocetVzdusnychUseku = cesta.getPocetVzdusnychUseku();
		return pocetVzdusnychUseku > 1 && cesta.getPocetPodcestVzdusnychUseku() < pocetVzdusnychUseku;
	}

	@Override
	protected void nastavJmenoAkce(final Cesta cesta, final boolean aZKontextovehoMenu) {
		putValue(NAME, "<html>Pospojovat vzdušné úseky " + cesta.getPocetVzdusnychUseku() + " => " + cesta.getPocetPodcestVzdusnychUseku());
	}

	@Override
	protected void provedProCestu(final Cesta cesta) {
		cestyModel.pospojujVzdusneUseky(cesta);
	}

}
