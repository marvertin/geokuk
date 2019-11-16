package cz.geokuk.plugins.mapy;

import cz.geokuk.framework.ToggleAction0;
import cz.geokuk.plugins.mapy.kachle.data.EKaType;

public abstract class MapyAction0 extends ToggleAction0 {

	private static final long serialVersionUID = 8106696486908484270L;
	private MapyModel mapyModel;
	private final EKaType katype;

	public MapyAction0(final EKaType katype) {
		super(katype.getNazev());
		this.katype = katype;
		putValue(SHORT_DESCRIPTION, katype.getPopis());
		if (katype.getKlavesa() != 0) {
			putValue(MNEMONIC_KEY, katype.getKlavesa());
		}
		if (katype.getKeyStroke() != null) {
			putValue(ACCELERATOR_KEY, katype.getKeyStroke());
		}
	}

	public MapyModel getMapyModel() {
		return mapyModel;
	}

	public void inject(final MapyModel mapyModel) {
		this.mapyModel = mapyModel;
	}

	protected EKaType getKaType() {
		return katype;
	}

}
