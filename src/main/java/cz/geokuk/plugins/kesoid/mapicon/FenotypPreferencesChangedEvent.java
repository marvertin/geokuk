package cz.geokuk.plugins.kesoid.mapicon;

import cz.geokuk.framework.Event0;
import cz.geokuk.plugins.kesoid.genetika.QualAlelaNames;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;

public class FenotypPreferencesChangedEvent extends Event0<KesoidModel> {

	private final QualAlelaNames jmenaNefenotypovanychAlel;

	public FenotypPreferencesChangedEvent(final QualAlelaNames jmenaNefenotypovanychAlel) {
		this.jmenaNefenotypovanychAlel = jmenaNefenotypovanychAlel;
	}

	public QualAlelaNames getJmenaNefenotypovanychAlel() {
		return jmenaNefenotypovanychAlel;
	}
}
