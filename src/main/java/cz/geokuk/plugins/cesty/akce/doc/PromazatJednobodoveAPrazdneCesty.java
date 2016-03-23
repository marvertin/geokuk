package cz.geokuk.plugins.cesty.akce.doc;

import java.util.ArrayList;
import java.util.List;

import cz.geokuk.plugins.cesty.data.Cesta;
import cz.geokuk.plugins.cesty.data.Doc;

public class PromazatJednobodoveAPrazdneCesty extends DocAction0 {

	private static final long serialVersionUID = -7547868179813232769L;

	public PromazatJednobodoveAPrazdneCesty(final Doc doc) {
		super(doc);
		putValue(NAME, "Promazat jednobodové cesty");
		putValue(SHORT_DESCRIPTION, "Promaže bšechny jednobodové a prázdné vesty, pokud však nejsou nad waypointy.");
		// putValue(MNEMONIC_KEY, KeyEvent.VK_Z);
	}

	@Override
	protected boolean mamPovolitProDoc(final Doc doc) {
		return doc.getPocetJednobodovychCest() + doc.getPocetPrazdnychCest() > 0;
	}

	@Override
	protected void nastavJmenoAkce(final Doc doc, final boolean aZKontextovehoMenu) {
		putValue(NAME, "Promazat jednobodové cesty (" + (doc.getPocetJednobodovychCest() + doc.getPocetPrazdnychCest()) + ")");
	}

	@Override
	protected void provedProDoc(final Doc doc) {
		final List<Cesta> cesty = new ArrayList<>();
		for (final Cesta cesta : doc) {
			if (cesta.isEmpty() || cesta.isJednobodova()) {
				cesty.add(cesta);
			}
		}
		for (final Cesta cesta : cesty) {
			cestyModel.removeCestu(cesta);
		}
	}

}
