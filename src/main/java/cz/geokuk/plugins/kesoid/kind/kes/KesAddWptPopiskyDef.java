package cz.geokuk.plugins.kesoid.kind.kes;

public class KesAddWptPopiskyDef extends KesPopiskyDef0 {

	@Override
	public void init() {
		super.init();

		label = "Geokeš additional waypoint";
		defaultPattern = "{info} - {nazev} ({wpt})";

	}


}
