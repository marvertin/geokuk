package cz.geokuk.plugins.mapy;

import java.util.EnumSet;

import cz.geokuk.plugins.mapy.kachle.EKaType;

public class DekoraceAction extends MapyAction0 {

	private static final long serialVersionUID = 8106696486908484270L;

	public DekoraceAction(EKaType aKatype) {
		super(aKatype);
	}

	protected EKaType getDekorace() {
		return super.getKaType();
	}

	protected void nastavDekoraci(boolean onoff) {
		EnumSet<EKaType> dekoraces = getMapyModel().getDekorace();
		if (onoff) {
			dekoraces.add(getDekorace());
		} else {
			dekoraces.remove(getDekorace());
		}
		getMapyModel().setDekorace(dekoraces);
	}

	@Override
	protected void onSlectedChange(boolean nastaveno) {
		nastavDekoraci(nastaveno);
	}

	public void onEvent(ZmenaMapNastalaEvent event) {
		EnumSet<EKaType> dekoraces = getMapyModel().getDekorace();
		boolean nastaveno = dekoraces.contains(getDekorace());
		setSelected(nastaveno);
	}

}
