package cz.geokuk.core.render;

import cz.geokuk.plugins.mapy.kachle.*;

public class JRenderNahledKachlovnik extends JKachlovnik {

	private static final long serialVersionUID = -7897332661428146095L;

	public JRenderNahledKachlovnik() {
		super("Náhled rendrovacího kachlovníku", Priority.KACHLE);
	}

	@Override
	public void setKachloTypes(final KaSet aKachloSet) {
		super.setKachloTypes(aKachloSet);
	}

}
