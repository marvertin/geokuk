package cz.geokuk.core.render;

import cz.geokuk.plugins.mapy.kachle.data.EKaType;
import cz.geokuk.plugins.mapy.kachle.gui.JKachlovnik;
import cz.geokuk.plugins.mapy.kachle.podklady.Priority;

public class JRenderNahledKachlovnik extends JKachlovnik {

	private static final long serialVersionUID = -7897332661428146095L;

	public JRenderNahledKachlovnik() {
		super("Náhled rendrovacího kachlovníku", Priority.KACHLE);
	}

	@Override
	public void setKachloType(final EKaType katype) {
		super.setKachloType(katype);
	}

}
