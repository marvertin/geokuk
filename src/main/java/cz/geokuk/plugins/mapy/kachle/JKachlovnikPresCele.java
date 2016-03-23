package cz.geokuk.plugins.mapy.kachle;

import cz.geokuk.core.coord.JSingleSlide0;

public class JKachlovnikPresCele extends JKachlovnik {

	public JKachlovnikPresCele() {
		super("Hlavní kachlovník", Priority.KACHLE);
	}

	private static final long serialVersionUID = -3170605712662727739L;
	/*
	 * (non-Javadoc)
	 * 
	 * @see cz.geokuk.core.coord.JSingleSlide0#render(java.awt.Graphics)
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see cz.geokuk.core.coord.JSingleSlide0#createRenderableSlide()
	 */
	@Override
	public JSingleSlide0 createRenderableSlide() {
		return new JKachlovnikRendrovaci();
	}

}
