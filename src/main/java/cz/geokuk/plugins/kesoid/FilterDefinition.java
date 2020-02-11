/**
 *
 */
package cz.geokuk.plugins.kesoid;

import cz.geokuk.framework.*;
import cz.geokuk.plugins.vylety.EPrahVyletu;

/**
 * @author Martin Veverka
 *
 */
@Preferenceble
public class FilterDefinition implements Copyable<FilterDefinition> {


	private EPrahVyletu prahVyletu = EPrahVyletu.BEZ_VYNECHANYCH;

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
	public EPrahVyletu getPrahVyletu() {
		return prahVyletu;
	}


	/**
	 * @param prahVyletu
	 *            the prahVyletu to set
	 */
	@PreferencebleIgnore
	public void setPrahVyletu(final EPrahVyletu prahVyletu) {
		this.prahVyletu = prahVyletu;
	}

}
