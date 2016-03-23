/**
 *
 */
package cz.geokuk.plugins.kesoid.filtr;

import java.util.Set;

import cz.geokuk.framework.Event0;
import cz.geokuk.plugins.kesoid.FilterDefinition;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;

/**
 * @author veverka
 *
 */
public class FilterDefinitionChangedEvent extends Event0<KesoidModel> {

	private final FilterDefinition	filterDefinition;
	private final Set<String>		jmenaNechtenychAlel;
	private final Set<String>		jmenaAlelNaToolbaru;

	public FilterDefinitionChangedEvent(final FilterDefinition filterDefinition, final Set<String> jmenaNechtenychAlel, final Set<String> jmenaAlelNaToolbaru) {
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
	public Set<String> getJmenaAlelNaToolbaru() {
		return jmenaAlelNaToolbaru;
	}

	/**
	 * @return the jmenaNechtenychAlel
	 */
	public Set<String> getJmenaNechtenychAlel() {
		return jmenaNechtenychAlel;
	}

}
