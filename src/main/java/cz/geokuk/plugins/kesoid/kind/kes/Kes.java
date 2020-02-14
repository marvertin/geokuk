/**
 *
 */
package cz.geokuk.plugins.kesoid.kind.kes;

import java.net.URL;

import cz.geokuk.plugins.kesoid.*;
import cz.geokuk.plugins.kesoid.data.EKesoidKind;
import cz.geokuk.plugins.kesoid.genetika.Genotyp;
import lombok.SneakyThrows;

/**
 * @author Martin Veverka
 *
 */
public class Kes extends Kesoid0 {

	public static final int NENI_HODNOCENI = -1;
	private int bestOf = Kes.NENI_HODNOCENI;
	private int hodnoceni = Kes.NENI_HODNOCENI;

	private int hodnoceniPocet = Kes.NENI_HODNOCENI;
	private int znamka = Kes.NENI_HODNOCENI;
	private int favorit = Kes.NENI_HODNOCENI;
	private String hint;
	private EKesSize size;
	private EKesDiffTerRating difficulty;
	private EKesDiffTerRating terrain;
	// private String guid;
	private Wpt finalWpt;

	private String iFountTime;

	private Wpt mainWpt;

	@Override
	public void addWpt(final Wpti wpti) {
		super.addWpt(wpti);
		if (wpti == null) {
			return;
		}
		if (EKesWptType.FINAL_LOCATION == EKesWptType.decode(wpti.getSym())) {
			finalWpt = wpti;
		}
	}

	@Override
	public Genotyp buildGenotyp(final Genotyp g) {
		final GenotypBuilderKes genotypBuilder = new GenotypBuilderKes(g.getGenom());
		return genotypBuilder.build(this, g);
	}

	/**
	 * @return the bestOf
	 */
	public int getBestOf() {
		return bestOf;
	}

	public EKesDiffTerRating getDifficulty() {
		return difficulty;
	}

	public int getFavorit() {
		return favorit;
	}

	/**
	 * @return
	 */
	public Wpt getFinal() {
		return finalWpt;
	}

	/**
	 * @return the fountTime
	 */
	public String getFountTime() {
		return iFountTime;
	}

	/**
	 * @return the hint
	 */
	public String getHint() {
		return hint;
	}

	/**
	 * @return the hodnoceni
	 */
	public int getHodnoceni() {
		return hodnoceni;
	}

	/**
	 * @return the hodnoceniPocet
	 */
	public int getHodnoceniPocet() {
		return hodnoceniPocet;
	}

	/**
	 * Vrátí takové to čtyřznakové info TR3A
	 *
	 * @return
	 */
	public String getInfo() {
		return String.valueOf(getOneLetterType()) + getOneLetterSize() + getOneLetterDifficulty() + getOneLetterTerrain();
	}

	@Override
	public EKesoidKind getKesoidKind() {
		return EKesoidKind.KES;
	}

	// public Wpt getGc() {
	// return gc;
	// }

	/**
	 * @return the mainWpt
	 */
	@Override
	public Wpt getFirstWpt() {
		if (mainWpt != null) {
			return mainWpt;
		}
		return super.getFirstWpt();
	}

	/**
	 * @return
	 */
	public char getOneLetterDifficulty() {
		return getDifficulty().toSingleChar();
	}

	/**
	 * @return
	 */
	public char getOneLetterSize() {
		return getSize().getOneLetterSize();
	}

	/**
	 * @return
	 */
	public char getOneLetterTerrain() {
		return getTerrain().toSingleChar();
	}

	/**
	 * @return
	 */
	public char getOneLetterType() {
		final String sym = getFirstWpt().getSym();
		if (sym == null || sym.length() == 0) {
			return '?';
		}
		return sym.charAt(0);
	}

	public EKesSize getSize() {
		return size;
	}

	public EKesDiffTerRating getTerrain() {
		return terrain;
	}


	/**
	 * @return the znamka
	 */
	public int getZnamka() {
		return znamka;
	}

	/**
	 * @return
	 */
	public Boolean hasValidFinal() {
		return finalWpt != null && !finalWpt.hasEmptyCoords();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.kes.Kesoid#prispejDoTooltipu(java.lang.StringBuilder)
	 */
	@Override
	public void prispejDoTooltipu(final StringBuilder sb, final Wpt wpt) {
		sb.append("<b>");
		sb.append(getNazev());
		sb.append("</b>");
		sb.append("<small>");
		sb.append(" - ");
		sb.append(getFirstWpt().getSym());
		sb.append("  (").append(getIdentifier()).append(")");
		sb.append("</small>");
		sb.append("<br>");
		if (wpt != getFirstWpt()) {
			if (!getNazev().contains(wpt.getNazev())) {
				sb.append(wpt.isRucnePridany() ? "+ " : "");
				sb.append("<i>");
				sb.append(wpt.getIdentifier().substring(0, 2));
				sb.append(": ");
				sb.append(wpt.getNazev());
				sb.append("</i>");
			}
			// if (! getSym().equals(wpt.getSym())) {
			sb.append("<small>");
			sb.append(" - ");
			sb.append(wpt.getSym());
			sb.append("  (").append(wpt.getIdentifier()).append(")");
			sb.append("</small>");
		}
		// }
		// }

	}

	/**
	 * @param aBestOf
	 *            the bestOf to set
	 */
	public void setBestOf(final int aBestOf) {
		bestOf = aBestOf;
	}

	public void setDifficulty(final EKesDiffTerRating difficulty) {
		this.difficulty = difficulty;
	}

	public void setFavorit(final int favorit) {
		this.favorit = favorit;
	}

	/**
	 * @param aFound
	 */
	public void setFoundTime(final String fountTime) {
		iFountTime = fountTime;
	}

	/**
	 * @param aHint
	 *            the hint to set
	 */
	public void setHint(final String aHint) {
		hint = aHint;
	}

	/**
	 * @param aHodnoceni
	 *            the hodnoceni to set
	 */
	public void setHodnoceni(final int aHodnoceni) {
		hodnoceni = aHodnoceni;
	}

	/**
	 * @param aHodnoceniPocet
	 *            the hodnoceniPocet to set
	 */
	public void setHodnoceniPocet(final int aHodnoceniPocet) {
		hodnoceniPocet = aHodnoceniPocet;
	}

	/**
	 * @param aMainWpt
	 *            the mainWpt to set
	 */
	public void setMainWpt(final Wpt aMainWpt) {
		if (!aMainWpt.hasEmptyCoords()) {
			mainWpt = aMainWpt;
		}
	}

	public void setSize(final EKesSize size) {
		this.size = size;
	}

	public void setTerrain(final EKesDiffTerRating terrain) {
		this.terrain = terrain;
	}

	/**
	 * @param aZnamka
	 *            the znamka to set
	 */
	public void setZnamka(final int aZnamka) {
		znamka = aZnamka;
	}

	@Override
	public String toString() {
		return "Kes [mainWpt=" + mainWpt + ", finalWpt=" + finalWpt + "]";
	}

	@Override
	@SneakyThrows
	public URL getUrlProPridaniDoSeznamuVGeogetu() {
		return new URL("https://coord.info/" + getIdentifier());
	}
}
