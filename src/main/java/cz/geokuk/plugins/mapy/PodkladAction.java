package cz.geokuk.plugins.mapy;

import cz.geokuk.plugins.mapy.kachle.data.KaType;

public class PodkladAction extends MapyAction0 {

	private static final long serialVersionUID = 8106696486908484270L;

	public PodkladAction(final KaType katype) {
		super(katype);
	}

	public KaType getPodklad() {
		return super.getKaType();
	}

	public void onEvent(final ZmenaMapNastalaEvent event) {
		setSelected(getMapyModel().getPodklad() == getPodklad());
	}

	@Override
	protected void onSlectedChange(final boolean nastaveno) {
		if (nastaveno) {
			getMapyModel().setPodklad(getPodklad());
		}
	}

}
