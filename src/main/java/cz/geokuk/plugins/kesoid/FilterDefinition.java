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

	private int		prahHodnoceni				= 0;
	private int		prahBestOf					= 0;
	private int		prahFavorit					= 0;

	private EVylet	prahVyletu					= EVylet.NEVIM;
	private boolean	jenDoTerenuUNenalezenych	= true;
	private boolean	jenFinalUNalezenych			= true;

	public int getPrahFavorit() {
		return prahFavorit;
	}

	public void setPrahFavorit(int prahFavorit) {
		this.prahFavorit = prahFavorit;
	}

	/**
	 * @return the prahHodnoceni
	 */
	public int getPrahHodnoceni() {
		return prahHodnoceni;
	}

	/**
	 * @param prahHodnoceni
	 *            the prahHodnoceni to set
	 */
	public void setPrahHodnoceni(int prahHodnoceni) {
		this.prahHodnoceni = prahHodnoceni;
	}

	/**
	 * @return the prahBestOf
	 */
	public int getPrahBestOf() {
		return prahBestOf;
	}

	/**
	 * @param prahBestOf
	 *            the prahBestOf to set
	 */
	public void setPrahBestOf(int prahBestOf) {
		this.prahBestOf = prahBestOf;
	}

	/**
	 * @return the prahVyletu
	 */
	@PreferencebleIgnore
	public EVylet getPrahVyletu() {
		return prahVyletu;
	}

	/**
	 * @param prahVyletu
	 *            the prahVyletu to set
	 */
	@PreferencebleIgnore
	public void setPrahVyletu(EVylet prahVyletu) {
		this.prahVyletu = prahVyletu;
	}

	/**
	 * @return the jenDoTerenuUNenalezenych
	 */
	@PreferencebleProperty(name = "jenFinal")
	public boolean isJenDoTerenuUNenalezenych() {
		return jenDoTerenuUNenalezenych;
	}

	/**
	 * @param jenDoTerenuUNenalezenych
	 *            the jenDoTerenuUNenalezenych to set
	 */
	@PreferencebleProperty(name = "jenFinal")
	public void setJenDoTerenuUNenalezenych(boolean jenDoTerenuUNenalezenych) {
		this.jenDoTerenuUNenalezenych = jenDoTerenuUNenalezenych;
	}

	/**
	 * @return the jenFinalUNalezenych
	 */
	@PreferencebleProperty(name = "jenJedenUNalezenych")
	public boolean isJenFinalUNalezenych() {
		return jenFinalUNalezenych;
	}

	/**
	 * @param jenFinalUNalezenych
	 *            the jenFinalUNalezenych to set
	 */
	@PreferencebleProperty(name = "jenJedenUNalezenych")
	public void setJenFinalUNalezenych(boolean jenFinalUNalezenych) {
		this.jenFinalUNalezenych = jenFinalUNalezenych;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cz.geokuk.framework.Copyable#copy()
	 */
	@Override
	public FilterDefinition copy() {
		try {
			return (FilterDefinition) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

}
