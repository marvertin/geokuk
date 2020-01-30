package cz.geokuk.plugins.kesoid.kind.kes;

import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.kind.PluginFilter;

public class KesFilter implements PluginFilter<KesFilterDefinition> {

	@Override
	public KesFilterDefinition getDefaultFilterDefinition() {
		return null;
	}

	@Override
	public boolean filter(final KesFilterDefinition definition, final Wpt waypoint) {
		return true;
	}

}
