package cz.geokuk.plugins.mapy.kachle.gui;

import cz.geokuk.plugins.mapy.kachle.data.EKaType;
import cz.geokuk.plugins.mapy.kachle.podklady.Priority;

public class JKachlovnikDoRohu extends JKachlovnik {

	private static final long serialVersionUID = -7897332661428146095L;

	public JKachlovnikDoRohu() {
		super("Levý dolní roh - kachlovník", Priority.KACHLE);
	}

	@Override
	public void setKachloType(final EKaType aKachloSet) {
		// nehezké takto přepsat metodu
		super.setKachloType(EKaType.OPHOTO_M);
	}

}
