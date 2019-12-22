package cz.geokuk.plugins.kesoid;

import cz.geokuk.plugins.kesoid.genetika.QualAlelaNames;
import cz.geokuk.plugins.vylety.VyletModel;

public class KesoidFilterModel {

	private FilterDefinition filterDefinition;
	private QualAlelaNames jmenaNechtenychAlel;

	private VyletModel vyletModel;

	public KesoidFilterModel() {}

	KesoidFilter createKesoidFilter() {
		return new KesoidFilter(filterDefinition, jmenaNechtenychAlel, vyletModel);
	}

	/**
	 * @return the filterDefinition
	 */
	public FilterDefinition getFilterDefinition() {
		return filterDefinition;
	}

	/**
	 * @return the jmenaNechtenychAlel
	 */
	public QualAlelaNames getJmenaNechtenychAlel() {
		return jmenaNechtenychAlel;
	}

	public void inject(final VyletModel vyletModel) {
		this.vyletModel = vyletModel;
	}



	public void setDefaults() {
		// Atom.of(AWptType.FINAL_LOCATION);
		filterDefinition = new FilterDefinition();
	}

	/**
	 * @param filterDefinition
	 *            the filterDefinition to set
	 */
	public void setFilterDefinition(final FilterDefinition filterDefinition) {
		this.filterDefinition = filterDefinition;
	}

	/**
	 * @param jmenaNechtenychAlel
	 *            the jmenaNechtenychAlel to set
	 */
	public synchronized void setJmenaNechtenychAlel(final QualAlelaNames jmenaNechtenychAlel) {
		this.jmenaNechtenychAlel = jmenaNechtenychAlel;
	}

}
