package cz.geokuk.plugins.mapy.kachle.gui;

import java.awt.Point;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.plugins.mapy.kachle.data.KaLoc;

/**
 * počítač všeho, co souvisí s kachlemi.
 *
 * Statické metody obsahují, co lez dělat přímo. Instanční, to co souvisí s navázaností na Coord.
 *
 * @author Martin Veverka
 *
 */
public class Kaputer {

	// private static final Logger log = LogManager.getLogger(Kaputer.class.getSimpleName());

	public static final int KACHLE_BITS = Coord.MOUMER_0_BITS;
	public static final int KACHLE_PIXELS = 1 << KACHLE_BITS; // 256
	public static final int KACHLE_MASKA = KACHLE_PIXELS - 1; // 255

	private final Coord soord;

	private final Point p0;

	private final int pocetKachliX;
	private final int pocetKachliY;

	public Kaputer(final Coord aCoord) {
		soord = aCoord;

		final int xn = soord.getWidth();
		final int yn = soord.getHeight();

		final Mou moustred = soord.getMoustred();
		final int moumer = soord.getMoumer();
		// int moukrok = getMoukrok(); // o kolik mou je to od kachle ke kachli (pro moumer=0 je to 2^32, tedy v integeru 0, což odpovídá, že se stále zobrazuje stejná kachle)
		// int maskaHorni = ~ (moukrok - 1);
		// int xx0 = moustred.xx & maskaHorni; // posuneme od středu nalevo na nejbližší hranici kachlí
		// int yy0 = moustred.yy & maskaHorni; // posuneme od středu dolů na nejbližší hranici kachlí
		final int xd = moustred.xx >> Coord.MAX_MOUMER - moumer & KACHLE_MASKA; // o tolik pixlů nalevo od středu bude svislá hranice kachlí
		final int yd = moustred.yy >> Coord.MAX_MOUMER - moumer & KACHLE_MASKA; // o tolik pixklů dolů od středu bude vodorovná hranice kachlí
		assert xd >= 0 && xd < KACHLE_PIXELS;
		assert yd >= 0 && yd < KACHLE_PIXELS;
		// if (log.isTraceEnabled()) {
		// log.trace("moukrok ={} moumer={} - [{},{}] maskaHorni={}", Integer.toHexString(moukrok), moumer, Integer.toHexString(xx0), Integer.toHexString(yy0), Integer.toHexString(maskaHorni));
		// }
		int x0, y0;
		// for (x0 = xn / 2 - xd; x0 > 0; x0 -= KACHLE_PIXELS, xx0-=moukrok); // nastavit x0 i xx zleva před kreslenou plochu (- je zde, protože nalevo od středu)
		// for (y0 = yn / 2 + yd; y0 > 0; y0 -= KACHLE_PIXELS, yy0+=moukrok); // nastavit x0 i yy shora před kreslenou plochu (+ je zde, protože dolů od středu, druhé plus, ptotož mouy jde sdola nahoru)
		for (x0 = xn / 2 - xd; x0 > 0; x0 -= KACHLE_PIXELS) {
			; // nastavit x0 i xx zleva před kreslenou plochu (- je zde, protože nalevo od středu)
		}
		for (y0 = yn / 2 + yd; y0 > 0; y0 -= KACHLE_PIXELS) {
			; // nastavit x0 i yy shora před kreslenou plochu (+ je zde, protože dolů od středu, druhé plus, ptotož mouy jde sdola nahoru)
		}
		assert x0 <= 0 && y0 <= 0;
		// nyní máme [x0,y0] a [xx0, yy0] souřadnice styku čtyř kachlí. S tím, že kachle, která od tohoto bodu jde
		// dolů (jižně) a vpravo (východně) zasáhne nejméně jedním pixlem do levého horního roku okna.
		// a teď jedeme
		p0 = new Point(x0, y0);
		pocetKachliX = (soord.getWidth() - p0.x + KACHLE_PIXELS - 1) / KACHLE_PIXELS;
		pocetKachliY = (soord.getHeight() - p0.y + KACHLE_PIXELS - 1) / KACHLE_PIXELS;
	}

	public Coord getCoord() {
		return soord;
	}

	/**
	 * Mouřadnice levého horního rohu kachle, které má být umístěna na daném indexu.
	 *
	 * @param xi
	 *            index kachle ve vřezu 0 .. pocetKachliX-1
	 * @param yi
	 *            index kachle ve vřezu 0 .. pocetKachliY-1
	 * @return
	 */
	public Mou getKachleMou(final int xi, final int yi) {
		return soord.transform(getKachlePoint(xi, yi));
	}

	/**
	 * Bod, na který se má vykreslit levý horní roh kachle pro zadané indexy uvnitř výřezu. Kachle [0,0] vždy postihne levý horní roh výřezu. Kachle [pocetKachliX - 1, pocetKachliY -1 ] vždy postihne pravý dolní roh výřezu.
	 *
	 * @param xi
	 *            index kachle ve vřezu 0 .. pocetKachliX-1
	 * @param yi
	 *            index kachle ve vřezu 0 .. pocetKachliY-1
	 */
	public Point getKachlePoint(final int xi, final int yi) {
		return new Point(p0.x + xi * KACHLE_PIXELS, p0.y + yi * KACHLE_PIXELS);
	}

	/**
	 * Lokace kachle, která má být zobrazena na daném indexu ve výřezu.
	 *
	 * @param xi
	 *            index kachle ve vřezu 0 .. pocetKachliX-1
	 * @param yi
	 *            index kachle ve vřezu 0 .. pocetKachliY-1
	 * @return
	 */
	public KaLoc getKaloc(final int xi, final int yi) {
		return KaLoc.ofSZ(getKachleMou(xi, yi), soord.getMoumer());
	}

	public int getMoukrok() {
		return soord.getMoumer() == 0 ? 0 : 1 << Coord.MOU_BITS - soord.getMoumer(); // o kolik mou je to od kachle ke kachli (pro moumer=0 je to 2^32, tedy v integeru 0, což odpovídá, že se stále zobrazuje stejná kachle)
	}

	/**
	 * Počet kachlí ve směru osy X, které zasahují do daného výřezu.
	 *
	 * @return
	 */
	public int getPocetKachliX() {
		return pocetKachliX;
	}

	/**
	 * Počet kachlí ve směru osy X, které zasahují do daného výřezu.
	 *
	 * @return
	 */
	public int getPocetKachliY() {
		return pocetKachliY;
	}

	public int getVzdalenostKachleOdStredu(final Mou moupoc) {
		final int mouHranaPul = getMoukrok() / 2;
		final Mou moustredx = soord.getMoustred();
		final double hypot = Math.hypot(moupoc.xx + mouHranaPul - moustredx.xx, moupoc.yy + mouHranaPul - moustredx.yy);
		return (int) hypot;
	}

	public void pocitani() {

	}

	@Override
	public String toString() {
		return "Kaputer [pocetKachliX=" + pocetKachliX + ", pocetKachliY=" + pocetKachliY + ", p0=" + p0 + ", soord=" + soord + "]";
	}
}
