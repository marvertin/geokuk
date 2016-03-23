/**
 *
 */
package cz.geokuk.plugins.kesoid;

import cz.geokuk.framework.*;
import cz.geokuk.plugins.vylety.EVylet;

/**
 * @author veverka
 *
 */
@Preferenceble
public class FilterDefinition implements Copyable<FilterDefinition> {

	private int prahHodnoceni = 0;
	private int prahBestOf = 0;
	private int prahFavorit = 0;

	private EVylet prahVyletu = EVylet.NEVIM;
	private boolean jenDoTerenuUNenalezenych = true;
	private boolean jenFinalUNalezenych = true;

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.framework.Copyable#copy()
	 */
	@Override
	public FilterDefinition copy() {
		try {
			return (FilterDefinition) super.clone();
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return the prahBestOf
	 */
	public int getPrahBestOf() {
		return prahBestOf;
	}

	public int getPrahFavorit() {
		return prahFavorit;
	}

	/**
	 * @return the prahHodnoceni
	 */
	public int getPrahHodnoceni() {
		return prahHodnoceni;
	}

	/**
	 * @return the prahVyletu
	 */
	@PreferencebleIgnore
	public EVylet getPrahVyletu() {
		return prahVyletu;
	}

	/**
	 * @return the jenDoTerenuUNenalezenych
	 */
	@PreferencebleProperty(name = "jenFinal")
	public boolean isJenDoTerenuUNenalezenych() {
		return jenDoTerenuUNenalezenych;
	}

	/**
	 * @return the jenFinalUNalezenych
	 */
	@PreferencebleProperty(name = "jenJedenUNalezenych")
	public boolean isJenFinalUNalezenych() {
		return jenFinalUNalezenych;
	}

	/**
	 * @param jenDoTerenuUNenalezenych
	 *            the jenDoTerenuUNenalezenych to set
	 */
	@PreferencebleProperty(name = "jenFinal")
	public void setJenDoTerenuUNenalezenych(final boolean jenDoTerenuUNenalezenych) {
		this.jenDoTerenuUNenalezenych = jenDoTerenuUNenalezenych;
	}

	/**
	 * @param jenFinalUNalezenych
	 *            the jenFinalUNalezenych to set
	 */
	@PreferencebleProperty(name = "jenJedenUNalezenych")
	public void setJenFinalUNalezenych(final boolean jenFinalUNalezenych) {
		this.jenFinalUNalezenych = jenFinalUNalezenych;
	}

	/**
	 * @param prahBestOf
	 *            the prahBestOf to set
	 */
	public void setPrahBestOf(final int prahBestOf) {
		this.prahBestOf = prahBestOf;
	}

	public void setPrahFavorit(final int prahFavorit) {
		this.prahFavorit = prahFavorit;
	}

	/**
	 * @param prahHodnoceni
	 *            the prahHodnoceni to set
	 */
	public void setPrahHodnoceni(final int prahHodnoceni) {
		this.prahHodnoceni = prahHodnoceni;
	}

	/**
	 * @param prahVyletu
	 *            the prahVyletu to set
	 */
	@PreferencebleIgnore
	public void setPrahVyletu(final EVylet prahVyletu) {
		this.prahVyletu = prahVyletu;
	}

}
