package cz.geokuk.plugins.cesty.data;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Mouable;

public class Updator {

	public void odeberBod(final Bod bod) {
		bod.remove();
	}

	public void pipojitCestuPred(final Cesta cesta1, final Cesta cesta2) {
		cesta1.pripojitPred(cesta2);
	}

	public void pipojitCestuZa(final Cesta cesta1, final Cesta cesta2) {
		assert cesta1.getDoc() == cesta2.getDoc();
		assert cesta1.getStart() != null;
		assert cesta1.getCil() != null;
		cesta1.pripojitZa(cesta2);
		assert cesta1.getStart() != null;
		assert cesta1.getCil() != null;
	}

	public void pospojujVzdusneUseky(final Cesta cesta) {
		cesta.pospojujVzdusneUseky();
	}

	public Bod pridejNaKonec(final Cesta cesta, final Mouable mouable) {
		cesta.setChanged();
		return cesta.pridejNaKonec(mouable);
	}

	// public void odeberBod(Cesta cesta, Mouable mouable) {
	// cesta.setChanged();
	// cesta.odeberBod(mouable);
	// }

	public Bod pridejNaMisto(final Cesta cesta, final Mouable mouable) {
		cesta.setChanged();
		return cesta.pridejNaMisto(mouable);
	}

	public void remove(final Cesta cesta) {
		cesta.remove();
	}

	public Usek removeBod(final Bod bod) {
		bod.setChanged();
		return bod.remove();
	}

	public void reverse(final Cesta cesta) {
		cesta.reverse();
	}

	public Cesta rozdelCestuVBode(final Bod bod) {
		bod.setChanged();
		return bod.rozdelCestu();
	}

	public Cesta rozdelCestuVUseku(final Usek usek, final Mou mou) {
		usek.setChanged();
		final Bod bod = usek.rozdelAZanikni(mou);
		return bod.rozdelCestu();
	}

	public Bod rozdelUsekNaDvaNove(final Usek usek, final Mou mouNovy) {
		usek.setChanged();
		return usek.rozdelAZanikni(mouNovy);
	}

	public void setMouable(final Bod bod, final Mouable mouable) {
		bod.setChanged();
		bod.setMouable(mouable);
	}

	public void setMouableButNoChange(final Bod bod, final Mouable mouable) {
		bod.setMouable(mouable);
	}

	public void setNazev(final Cesta cesta, final String aTrackName) {
		cesta.setNazev(aTrackName);
	}

	public void setVzdusny(final Usek usek, final boolean vzdusny) {
		usek.setChanged();
		usek.setVzdusny(vzdusny);
	}

	public void smazatUsekAOtevritNeboRozdelitCestu(final Usek usek) {
		final Cesta cesta = usek.getCesta();
		final boolean jeKruh = cesta.isKruh();
		final Cesta druhaPulka = usek.getBvzad().rozdelCestu();
		druhaPulka.getStart().remove();
		if (jeKruh) {
			cesta.pripojitPred(druhaPulka);
		}
	}

	public void xadd(final Doc doc, final Cesta cesta) {
		doc.setChanged();
		doc.xadd(cesta);
	}

}
