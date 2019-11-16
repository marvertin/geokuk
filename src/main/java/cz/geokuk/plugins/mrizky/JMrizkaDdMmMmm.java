package cz.geokuk.plugins.mrizky;

import cz.geokuk.core.coord.JSingleSlide0;
import cz.geokuk.core.coordinates.Wgs;

public class JMrizkaDdMmMmm extends JMrizkaWgs {

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
		return new JMrizkaDdMmMmm();
	}

	@Override
	public String getTextX(final double x) {
		return (x < 0 ? "-" : "") + Wgs.toGeoFormat(Math.abs(x));
	}

	@Override
	public String getTextY(final double y) {
		return (y < 0 ? "-" : "") + Wgs.toGeoFormat(Math.abs(y));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see mrizka.JMrizka0#initPainting(mrizka.JMrizka0.Vykreslovac)
	 */
	@Override
	public void initPainting(final Vykreslovac v) {
		v.rastr(MINUTA * 0.01, 1);
		v.rastr(MINUTA * 0.02, 5);
		v.rastr(MINUTA * 0.05, 2);
		v.rastr(MINUTA * 0.1, 1);
		v.rastr(MINUTA * 0.2, 5);
		v.rastr(MINUTA * 0.5, 2);
		v.rastr(MINUTA * 1, 1);
		v.rastr(MINUTA * 2, 5);
		v.rastr(MINUTA * 5, 2);
		v.rastr(MINUTA * 10, 1);
		v.rastr(MINUTA * 20, 3);
		v.rastr(MINUTA * 30, 2);
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
