package cz.geokuk.plugins.mapy;

import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.Model0;
import cz.geokuk.plugins.mapy.kachle.data.EKaType;
import cz.geokuk.plugins.mapy.kachle.data.KaType;

public class MapyModel extends Model0 {

	private KaType podklad;

	public KaType getKaSet() {
		return podklad;
	}

	public KaType getPodklad() {
		return podklad;
	}

	public void setPodklad(final KaType podklad) {
		if (podklad == this.podklad) {
			return;
		}
		fajruj();
	}

	@Override
	protected void initAndFire() {
		final EKaType podklad = currPrefe().node(FPref.NODE_KTERE_MAPY_node).getEnum(FPref.VALUE_MAPOVE_PODKLADY_value, EKaType.TURIST_M, EKaType.class);
		setPodklad(podklad);
	}

	private void fajruj() {
		if (podklad != null) {
			fire(new ZmenaMapNastalaEvent(getKaSet()));
		}
	}

}
