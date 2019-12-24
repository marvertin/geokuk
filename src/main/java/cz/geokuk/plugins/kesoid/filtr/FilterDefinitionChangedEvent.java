/**
 *
 */
package cz.geokuk.plugins.kesoid.filtr;

import cz.geokuk.framework.Event0;
import cz.geokuk.plugins.kesoid.FilterDefinition;
import cz.geokuk.plugins.kesoid.genetika.QualAlelaNames;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;

/**
 * @author Martin Veverka
 *
 */
public class FilterDefinitionChangedEvent extends Event0<KesoidModel> {

	private final FilterDefinition filterDefinition;
	private final QualAlelaNames jmenaNechtenychAlel;
	private final QualAlelaNames jmenaAlelNaToolbaru;

	public FilterDefinitionChangedEvent(final FilterDefinition filterDefinition, final QualAlelaNames jmenaNechtenychAlel, final QualAlelaNames jmenaAlelNaToolbaru) {
		this.filterDefinition = filterDefinition;
		this.jmenaNechtenychAlel = jmenaNechtenychAlel;
		this.jmenaAlelNaToolbaru = jmenaAlelNaToolbaru;
	}

	/**
	 * @return the filterDefinition
	 */
	public FilterDefinition getFilterDefinition() {
		return filterDefinition;
	}

	/**
	 *
	 * /**
	 *
	 * @return the jmenaAlelNaToolbaru
	 */
	public QualAlelaNames getJmenaAlelNaToolbaru() {
		return jmenaAlelNaToolbaru;
	}

	/**
	 * @return the jmenaNechtenychAlel
	 */
	public QualAlelaNames getJmenaNechtenychAlel() {
		return jmenaNechtenychAlel;
	}

}
