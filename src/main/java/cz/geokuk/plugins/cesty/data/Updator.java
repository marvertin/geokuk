package cz.geokuk.plugins.cesty.data;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Mouable;

public class Updator {

	public void remove(Cesta cesta) {
		cesta.remove();
	}

	public void xadd(Doc doc, Cesta cesta) {
		doc.setChanged();
		doc.xadd(cesta);
	}

	public Bod pridejNaKonec(Cesta cesta, Mouable mouable) {
		cesta.setChanged();
		return cesta.pridejNaKonec(mouable);
	}

	public Bod pridejNaMisto(Cesta cesta, Mouable mouable) {
		cesta.setChanged();
		return cesta.pridejNaMisto(mouable);
	}

	public void odeberBod(Bod bod) {
		bod.remove();
	}

	// public void odeberBod(Cesta cesta, Mouable mouable) {
	// cesta.setChanged();
	// cesta.odeberBod(mouable);
	// }

	public Bod rozdelUsekNaDvaNove(Usek usek, Mou mouNovy) {
		usek.setChanged();
		return usek.rozdelAZanikni(mouNovy);
	}

	public void setVzdusny(Usek usek, boolean vzdusny) {
		usek.setChanged();
		usek.setVzdusny(vzdusny);
	}

	public Usek removeBod(Bod bod) {
		bod.setChanged();
		return bod.remove();
	}

	public void setMouable(Bod bod, Mouable mouable) {
		bod.setChanged();
		bod.setMouable(mouable);
	}

	public void setMouableButNoChange(Bod bod, Mouable mouable) {
		bod.setMouable(mouable);
	}

	public void reverse(Cesta cesta) {
		cesta.reverse();
	}

	public Cesta rozdelCestuVBode(Bod bod) {
		bod.setChanged();
		return bod.rozdelCestu();
	}

	public Cesta rozdelCestuVUseku(Usek usek, Mou mou) {
		usek.setChanged();
		Bod bod = usek.rozdelAZanikni(mou);
		return bod.rozdelCestu();
	}

	public void pipojitCestuZa(Cesta cesta1, Cesta cesta2) {
		assert cesta1.getDoc() == cesta2.getDoc();
		assert cesta1.getStart() != null;
		assert cesta1.getCil() != null;
		cesta1.pripojitZa(cesta2);
		assert cesta1.getStart() != null;
		assert cesta1.getCil() != null;
	}

	public void pipojitCestuPred(Cesta cesta1, Cesta cesta2) {
		cesta1.pripojitPred(cesta2);
	}

	public void pospojujVzdusneUseky(Cesta cesta) {
		cesta.pospojujVzdusneUseky();
	}

	public void smazatUsekAOtevritNeboRozdelitCestu(Usek usek) {
		Cesta cesta = usek.getCesta();
		boolean jeKruh = cesta.isKruh();
		Cesta druhaPulka = usek.getBvzad().rozdelCestu();
		druhaPulka.getStart().remove();
		if (jeKruh) {
			cesta.pripojitPred(druhaPulka);
		}
	}

	public void setNazev(Cesta cesta, String aTrackName) {
		cesta.setNazev(aTrackName);
	}

}
