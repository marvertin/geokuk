package cz.geokuk.plugins.mapy;

import java.util.EnumSet;

import cz.geokuk.plugins.mapy.kachle.EKaType;

public class DekoraceAction extends MapyAction0 {

	private static final long serialVersionUID = 8106696486908484270L;

	public DekoraceAction(final EKaType aKatype) {
		super(aKatype);
	}

	public void onEvent(final ZmenaMapNastalaEvent event) {
		final EnumSet<EKaType> dekoraces = getMapyModel().getDekorace();
		final boolean nastaveno = dekoraces.contains(getDekorace());
		setSelected(nastaveno);
	}

	protected EKaType getDekorace() {
		return super.getKaType();
	}

	protected void nastavDekoraci(final boolean onoff) {
		final EnumSet<EKaType> dekoraces = getMapyModel().getDekorace();
		if (onoff) {
			dekoraces.add(getDekorace());
		} else {
			dekoraces.remove(getDekorace());
		}
		getMapyModel().setDekorace(dekoraces);
	}

	@Override
	protected void onSlectedChange(final boolean nastaveno) {
		nastavDekoraci(nastaveno);
	}

}
