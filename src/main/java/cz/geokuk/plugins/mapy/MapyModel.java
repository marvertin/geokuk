package cz.geokuk.plugins.mapy;

import java.util.EnumSet;

import cz.geokuk.core.program.FPref;
import cz.geokuk.framework.Model0;
import cz.geokuk.plugins.mapy.kachle.EKaType;
import cz.geokuk.plugins.mapy.kachle.KaSet;

public class MapyModel extends Model0 {

	private EKaType				podklad;

	private EnumSet<EKaType>	dekorace;

	public EnumSet<EKaType> getDekorace() {
		return dekorace.clone();
	}

	public KaSet getKaSet() {
		final EnumSet<EKaType> kts = EnumSet.of(podklad);
		kts.addAll(dekorace);
		assert kts != null;
		final KaSet kaSet = new KaSet(kts);
		return kaSet;
	}

	public EKaType getPodklad() {
		return podklad;
	}

	public void setDekorace(final EnumSet<EKaType> dekorace) {
		if (dekorace.equals(this.dekorace)) {
			return;
		}
		this.dekorace = dekorace.clone();
		currPrefe().node(FPref.NODE_KTERE_MAPY_node).putEnumSet(FPref.VALUE_MAPOVE_DEKORACE_value, dekorace);
		fajruj();
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
		final EnumSet<EKaType> dekorace = currPrefe().node(FPref.NODE_KTERE_MAPY_node).getEnumSet(FPref.VALUE_MAPOVE_DEKORACE_value, EnumSet.of(EKaType.TTUR_M), EKaType.class);
		setPodklad(podklad);
		setDekorace(dekorace);
	}

	private void fajruj() {
		if (podklad != null && dekorace != null) {
			fire(new ZmenaMapNastalaEvent(getKaSet()));
		}
	}

}
