package cz.geokuk.plugins.kesoid.mapicon;

import java.util.HashSet;
import java.util.Set;

import cz.geokuk.framework.Event0;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;

public class FenotypPreferencesChangedEvent extends Event0<KesoidModel> {

	private final Set<String> jmenaNefenotypovanychAlel;

	public FenotypPreferencesChangedEvent(final Set<String> jmenaNefenotypovanychAlel) {
		this.jmenaNefenotypovanychAlel = jmenaNefenotypovanychAlel;
	}

	public Set<String> getJmenaNefenotypovanychAlel() {
		return new HashSet<>(jmenaNefenotypovanychAlel);
	}
}
