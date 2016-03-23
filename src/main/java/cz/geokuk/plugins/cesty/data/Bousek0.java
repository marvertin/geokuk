package cz.geokuk.plugins.cesty.data;

import java.awt.Color;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.plugins.cesty.FBarvy;

/**
 * Předek úseku a cesty
 * 
 * @author veverka
 *
 */
public abstract class Bousek0 {

	Cesta cesta;

	Bousek0(Cesta cesta) {
		this.cesta = cesta;
	}

	/**
	 * Spočítá kvadrát vzdálenosti úseku či bodu od zadaného bodu. Vrací Long.MAX_VALUE, pokud nemá smysl počítat.
	 * 
	 * @param mou
	 * @return
	 */
	public abstract long computeKvadratVzdalenosti(Mou mou);

	/** Spočítá velikost úsek, velikost boduje 0 */
	public abstract double dalka();

	public abstract double dalkaCestaVpred(Mou aMou);

	public abstract double dalkaCestaVzad(Mou aMou);

	public String dalkaHtml() {
		return Cesta.dalkaHtml(dalka(), Color.BLACK);
	}

	public String dalkaCestaVpredHtml(Mou aMou) {
		return Cesta.dalkaHtml(dalkaCestaVpred(aMou), FBarvy.CURTA_ZA_KURZOREM);
	}

	public String dalkaCestaVzadHtml(Mou aMou) {
		return Cesta.dalkaHtml(dalkaCestaVzad(aMou), FBarvy.CURTA_PRED_KURZOREM);
	}

	public String dalkaCestaRozdelenoHtml(Mou aMou) {
		return dalkaCestaVzadHtml(aMou) + " + " + dalkaCestaVpredHtml(aMou);

	}

	public String getNazevADalkaHtml(Mou aMou) {
		return cesta.getNazevHtml() + " " + dalkaCestaRozdelenoHtml(aMou);
	}

	public final boolean jeDoKvadratuVzdalenosti(Mou mou, long kvadratMaximalniVzdalenosti) {
		long kvadratVzdalenosti = computeKvadratVzdalenosti(mou);
		return kvadratVzdalenosti <= kvadratMaximalniVzdalenosti;

	}

	abstract boolean jeVOpsanemObdelniku(Mou mou);

	public Cesta getCesta() {
		return cesta;
	}

	void setChanged() {
		cesta.setChanged();
	}

	public abstract Bousek0 getBousekVpred();

	public abstract Bousek0 getBousekVzad();

	protected abstract void kontrolaKonzistence();

	/**
	 * Pokud tendo bousek představuje krajový bod nekruhové cesty, a pod tímto bodem leží opačný krajový bod jiné cesty, vrátí tento jiný bod.
	 * 
	 * @return
	 */
	public abstract Bod getKoncovyBodDruheCestyVhodnyProSpojeni();

}
