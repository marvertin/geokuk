package cz.geokuk.plugins.mrizky;

import cz.geokuk.core.coord.JSingleSlide0;

public class JMrizkaDd extends JMrizkaWgs {

	/**
	 *
	 */
	private static final long serialVersionUID = -4045265652174560904L;

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.core.coord.JSingleSlide0#createRenderableSlide()
	 */
	@Override
	public JSingleSlide0 createRenderableSlide() {
		return new JMrizkaDd();
	}

	@Override
	public String getTextX(final double x) {
		return (x < 0 ? "-" : "") + (int) Math.floor(Math.abs(x));
	}

	@Override
	public String getTextY(final double y) {
		return (y < 0 ? "-" : "") + (int) Math.floor(Math.abs(y));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see mrizka.JMrizka0#initPainting(mrizka.JMrizka0.Vykreslovac)
	 */
	@Override
	public void initPainting(final Vykreslovac v) {
		v.rastr(1, 1);
		v.rastr(5, 1);
		v.rastr(10, 1);
		v.rastr(15, 1);
		v.rastr(30, 1);
		v.rastr(45, 1);
		v.rastr(60, 1);
		v.rastr(90, 1);
		v.rastr(180, 1);
		v.rastr(360, 1);
	}

	@Override
	public boolean smimVykreslovat() {
		return true;
	}
}
