package cz.geokuk.plugins.kesoid;

import java.util.function.Predicate;

import cz.geokuk.plugins.kesoid.genetika.QualAlelaNames;
import cz.geokuk.plugins.vylety.VyletModel;

public class KesoidFilterModel {

	// Mění se z EDT, čte i vlákno filtrování; stačí viditelnost nových referencí.
	private volatile FilterDefinition filterDefinition;
	private volatile QualAlelaNames jmenaNechtenychAlel;

	/** Další podmínka přidaná ke všem ostatním; null = žádná. */
	private volatile Predicate<Kesoid> doplnkovyFiltr;

	private VyletModel vyletModel;

	public KesoidFilterModel() {}

	KesoidFilter createKesoidFilter() {
		return new KesoidFilter(filterDefinition, jmenaNechtenychAlel, vyletModel).doplnkovy(doplnkovyFiltr);
	}

	public Predicate<Kesoid> getDoplnkovyFiltr() {
		return doplnkovyFiltr;
	}

	public void setDoplnkovyFiltr(final Predicate<Kesoid> doplnkovyFiltr) {
		this.doplnkovyFiltr = doplnkovyFiltr;
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
	public void setJmenaNechtenychAlel(final QualAlelaNames jmenaNechtenychAlel) {
		this.jmenaNechtenychAlel = jmenaNechtenychAlel;
	}

}
