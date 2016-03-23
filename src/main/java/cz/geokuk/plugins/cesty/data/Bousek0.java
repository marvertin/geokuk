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

	Bousek0(final Cesta cesta) {
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

	public String dalkaCestaRozdelenoHtml(final Mou aMou) {
		return dalkaCestaVzadHtml(aMou) + " + " + dalkaCestaVpredHtml(aMou);

	}

	public abstract double dalkaCestaVpred(Mou aMou);

	public String dalkaCestaVpredHtml(final Mou aMou) {
		return Cesta.dalkaHtml(dalkaCestaVpred(aMou), FBarvy.CURTA_ZA_KURZOREM);
	}

	public abstract double dalkaCestaVzad(Mou aMou);

	public String dalkaCestaVzadHtml(final Mou aMou) {
		return Cesta.dalkaHtml(dalkaCestaVzad(aMou), FBarvy.CURTA_PRED_KURZOREM);
	}

	public String dalkaHtml() {
		return Cesta.dalkaHtml(dalka(), Color.BLACK);
	}

	public abstract Bousek0 getBousekVpred();

	public abstract Bousek0 getBousekVzad();

	public Cesta getCesta() {
		return cesta;
	}

	/**
	 * Pokud tendo bousek představuje krajový bod nekruhové cesty, a pod tímto bodem leží opačný krajový bod jiné cesty, vrátí tento jiný bod.
	 *
	 * @return
	 */
	public abstract Bod getKoncovyBodDruheCestyVhodnyProSpojeni();

	public String getNazevADalkaHtml(final Mou aMou) {
		return cesta.getNazevHtml() + " " + dalkaCestaRozdelenoHtml(aMou);
	}

	public final boolean jeDoKvadratuVzdalenosti(final Mou mou, final long kvadratMaximalniVzdalenosti) {
		final long kvadratVzdalenosti = computeKvadratVzdalenosti(mou);
		return kvadratVzdalenosti <= kvadratMaximalniVzdalenosti;

	}

	protected abstract void kontrolaKonzistence();

	abstract boolean jeVOpsanemObdelniku(Mou mou);

	void setChanged() {
		cesta.setChanged();
	}

}
