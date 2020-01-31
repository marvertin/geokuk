/**
 *
 */
package cz.geokuk.plugins.kesoid;

import cz.geokuk.framework.*;
import cz.geokuk.plugins.vylety.EVylet;

/**
 * @author Martin Veverka
 *
 */
@Preferenceble
public class FilterDefinition implements Copyable<FilterDefinition> {


	private EVylet prahVyletu = EVylet.NEVIM;

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
	public void setPrahVyletu(final EVylet prahVyletu) {
		this.prahVyletu = prahVyletu;
	}

}
