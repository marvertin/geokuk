package cz.geokuk.plugins.mapy;

import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.Model0;
import cz.geokuk.plugins.mapy.kachle.data.EKaType;

public class MapyModel extends Model0 {

	private EKaType podklad;

	public EKaType getKaSet() {
		return podklad;
	}

	public EKaType getPodklad() {
		return podklad;
	}

	public void setPodklad(final EKaType podklad) {
		if (podklad == this.podklad) {
			return;
		}
		this.podklad = podklad;
		currPrefe().node(FPref.NODE_KTERE_MAPY_node).putEnum(FPref.VALUE_MAPOVE_PODKLADY_value, podklad);
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
