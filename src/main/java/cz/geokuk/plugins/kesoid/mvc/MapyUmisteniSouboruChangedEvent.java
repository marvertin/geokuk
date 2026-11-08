package cz.geokuk.plugins.kesoid.mvc;

import cz.geokuk.framework.Event0;

public class MapyUmisteniSouboruChangedEvent extends Event0<KesoidModel> {

	private final MapyUmisteniSouboru umisteniSouboru;

	public MapyUmisteniSouboruChangedEvent(final MapyUmisteniSouboru umisteniSouboru) {
		super();
		this.umisteniSouboru = umisteniSouboru;
	}
	public MapyUmisteniSouboru getUmisteniSouboru() {
		return umisteniSouboru;
	}

}
