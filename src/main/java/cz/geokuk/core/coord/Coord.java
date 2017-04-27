package cz.geokuk.core.coord;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

import cz.geokuk.core.coordinates.*;
import cz.geokuk.util.index2d.BoundingRect;

/**
 * Objekt je immuitable.
 *
 * @author Martin Veverka
 *
 */
public class Coord {

	/** Počet bitů mou 2^MOU_BITS obtáčí rovník kolem dokola (odpovídá 40 000 km */
	public static final int MOU_BITS = 32;

	/**
	 * Počet bitů, na kterém je zobrazena celá země v merkátoru, když je nejmenší možné měřítko a to. 0 2^ MOUMER_0_BITS je hrana čtverce nejmenší kachle (ale, o kachlích se zde mluvi nesmí). Takže je to počet pixlů, do kterého se vejde celá země při měřítkui 0.
	 */
	public static final int MOUMER_0_BITS = 8;

	/**
	 * Teoreticky možné největší měřítko, kdy jedomu pixlu odpovídá jedna mouřadnice.
	 */
	public static final int MAX_MOUMER = MOU_BITS - MOUMER_0_BITS; // 24

	// X_UTM=(sX*0,03125)-3700000 = 292576
	// Y_UTM=(sY*0,03125)+1300000 = 5570080

	// Základní parametry
	private final int moumer; // měřítko
	private final Mou moustred; // střed
	private final Dimension dim; // velikost
	private final double natoceni; // natočení v radiánech

	// odvozené
	// poměr mezi mou a pixlama, kolik mou souřadnic je na pixel. U teoreticky nejvyššího měřítka 24 to bude 1:1.
	// Jinak vždy jsou mou drobnější než pisly a jeden z pixl se kládá z mnoha mou (mocniny dvou).
	// Vždy to bude rozumné číslo v intu (maximálně 16 milionů, takže se s tím dá normálně počítat)
	private final int mouNaPixl;

	private final int mpShift;

	private AffineTransform tam;
	private AffineTransform zpet;

	public static Coord prozatimniInicializacni() {
		return new Coord(0, new Mou(0x0800_000, 0x0800_000), new Dimension(100, 100), 0.0);
	}

	/**
	 *
	 */
	public Coord(final int moumer, final Mou moustred, final Dimension dim, final double natoceni) {
		// moumer
		this.moumer = moumer;

		/**
		 * mou - pixle shift. << .... z pixklů udělá mou >> .... z mou uděllá pixly
		 */
		mpShift = MAX_MOUMER - moumer;
		mouNaPixl = 1 << mpShift;

		// moustred
		this.moustred = new Mou(moustred);

		// dim
		this.dim = dim;

		// natoceni
		this.natoceni = natoceni;
		computeAffineTransforms();
	}

	/**
	 * Spočítá nový moustred, který musí být pokud se zazůmuje na danou veliksot, ab zůstal zadaný střed na svém místě obrazovky.
	 */
	public Mou computeZoom(final int newMoumer, final Mou zoomMouStred) {
		if (newMoumer == moumer) {
			return getMoustred();
		}
		// Point zommstred = zoomMouStred == null ? new Point(cur) : transform(zoomMouStred);
		// získat střed zoomování na obrazovce, podle tohoto zoomujeme
		final Point pointZoomStred = zoomMouStred == null ? transform(getMoustred()) : transform(zoomMouStred); // střed zůmování v bodech
		final Mou mouZoomStred = transform(pointZoomStred); // a střed v mou
		final Coord c = derive(newMoumer); // pokusne tam nastavit nové měřítko
		final Moud rozdil = c.transform(pointZoomStred).sub(mouZoomStred); // korigovat střed map, aby zůstal střed zůmování
		// System.out.println("CCCCCCCCCCCCCCCCCCC: " + rozdil + " " + pointZoomStred + " " + zoomMouStred);
		return getMoustred().sub(rozdil);
	}

	/**
	 * Úhel, o který je mapa v daném bodě pootočena oproti normálnímu směru na sever.
	 *
	 * @return
	 */
	public double computNataceciUhel() {
		if (getMouSize().isAnyRozmerEmpty()) {
			return 0;
		}
		final Mou mouS = getMouS();
		final Mou mouJ = getMouJ();
		final Wgs wgsS = mouS.toWgs();
		final Wgs wgsJ = mouJ.toWgs();
		final double lon = (wgsS.lon + wgsJ.lon) / 2; // stredova délka
		final Mou mouSq = new Wgs(wgsS.lat, lon).toMou();
		final Mou mouJq = new Wgs(wgsJ.lat, lon).toMou();
		return Math.atan((double) (mouSq.xx - mouJq.xx) / (double) (mouSq.yy - mouJq.yy));
	}

	public Coord derive(final Dimension dim) {
		return create(moumer, moustred, dim, natoceni);
	}

	public Coord derive(final Dimension dim, final double natoceni) {
		return create(moumer, moustred, dim, natoceni);
	}

	public Coord derive(final double natoceni) {
		return create(moumer, moustred, dim, natoceni);
	}

	public Coord derive(final int moumer) {
		return create(moumer, moustred, dim, natoceni);
	}

	public Coord derive(final int moumer, final Dimension dim) {
		return create(moumer, moustred, dim, natoceni);
	}

	public Coord derive(final int moumer, final Dimension dim, final double natoceni) {
		return create(moumer, moustred, dim, natoceni);
	}

	public Coord derive(final int moumer, final double natoceni) {
		return create(moumer, moustred, dim, natoceni);
	}

	public Coord derive(final int moumer, final Mou moustred) {
		return create(moumer, moustred, dim, natoceni);
	}

	public Coord derive(final int moumer, final Mou moustred, final Dimension dim) {
		return create(moumer, moustred, dim, natoceni);
	}

	public Coord derive(final int moumer, final Mou moustred, final Dimension dim, final double natoceni) {
		return create(moumer, moustred, dim, natoceni);
	}

	public Coord derive(final int moumer, final Mou moustred, final double natoceni) {
		return create(moumer, moustred, dim, natoceni);
	}

	public Coord derive(final Mou moustred) {
		return create(moumer, moustred, dim, natoceni);
	}

	public Coord derive(final Mou moustred, final Dimension dim) {
		return create(moumer, moustred, dim, natoceni);
	}

	public Coord derive(final Mou moustred, final Dimension dim, final double natoceni) {
		return create(moumer, moustred, dim, natoceni);
	}

	public Coord derive(final Mou moustred, final double natoceni) {
		return create(moumer, moustred, dim, natoceni);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Coord other = (Coord) obj;
		if (dim == null) {
			if (other.dim != null) {
				return false;
			}
		} else if (!dim.equals(other.dim)) {
			return false;
		}
		if (moumer != other.moumer) {
			return false;
		}
		if (moustred == null) {
			if (other.moustred != null) {
				return false;
			}
		} else if (!moustred.equals(other.moustred)) {
			return false;
		}
		return Double.doubleToLongBits(natoceni) == Double.doubleToLongBits(other.natoceni);
	}

	public BoundingRect getBoundingRect() {
		final Mou jz = getMouJZ();
		final Mou sv = getMouSV();
		return new BoundingRect(jz.xx, jz.yy, sv.xx, sv.yy);
	}

	/**
	 * @return the dim
	 */
	public Dimension getDim() {
		return dim;
	}

	public int getHeight() {
		return dim.height;
	}

	public double getHeightMetru() {
		return dim.height / getPixluNaMetr();
	}

	/**
	 *
	 */
	public double getMouboduNaMetr() {
		return getPixluNaMetr() * mouNaPixl;
	}

	public Mou getMouJ() {
		return getMoustred().add(0, -getMouSize().dyy / 2);
	}

	public Mou getMouJV() {
		return getMouJZ().add(getMouSize().dxx, 0);
	}

	/**
	 * Je vždy v levém dlolním oruhu okna, na rozdíl od ostatních.
	 *
	 * @return
	 */
	public Mou getMouJZ() {
		final Moud d = new Moud(dim.width / 2 << mpShift, dim.height / 2 << mpShift);
		final Mou mou = moustred.sub(d);
		// System.out.printf("mpShift=%d, dim=%s | moustred=%s=%s + %s = moujz=%s=%s%n", mpShift, dim, moustred, moustred.toWgs(), d, mou, mou.toWgs());
		return mou;
	}

	public int getMoumer() {
		return moumer;
	}

	public Mou getMouS() {
		return getMoustred().add(0, getMouSize().dyy / 2);
	}

	public Moud getMouSize() {
		// pozor na to, že pokud půjde moumer k nule a v mapš se bude opakovat motiv, tak to přeteče
		// a vlastně je rozměr menší
		return new Moud(dim.width << mpShift, dim.height << mpShift);
	}

	public Mou getMoustred() {
		// Mou moustred = new Mou(moupoc.xx + width * pomer / 2, moupoc.yy + height * pomer / 2);
		return moustred;
	}

	public Mou getMouSV() {
		return getMouJZ().add(getMouSize());
	}

	public Mou getMouSZ() {
		return getMouJZ().add(0, getMouSize().dyy);
	}

	public Mou getMouV() {
		return getMoustred().add(getMouSize().dxx / 2, 0);
	}

	public Mou getMouZ() {
		return getMoustred().add(-getMouSize().dxx / 2, 0);
	}

	/**
	 * @return the natoceni
	 */
	public double getNatoceni() {
		return natoceni;
	}

	/**
	 * Vrací koli metrů odpovídá jednomu pixklu uprostřed plochy.
	 *
	 */
	public double getPixluNaMetr() {

		final Wgs wgs = getMoustred().toWgs();
		final double pixlyNaMetr = 1 / (wgs.metryNaMou() * mouNaPixl);
		// System.out.println("PIXLU NA METR: " + metry + " " + 1 / metry);
		return pixlyNaMetr;
	}

	public int getWidth() {
		return dim.width;
	}

	public double getWidthMetru() {
		return dim.width / getPixluNaMetr();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (dim == null ? 0 : dim.hashCode());
		result = prime * result + moumer;
		result = prime * result + (moustred == null ? 0 : moustred.hashCode());
		long temp;
		temp = Double.doubleToLongBits(natoceni);
		result = prime * result + (int) (temp ^ temp >>> 32);
		return result;
	}

	public double pixleDalka(final Mou mou1, final Mou mou2) {
		final Point p1 = transform(mou1);
		final Point p2 = transform(mou2);
		return Math.hypot(p1.x - p2.x, p1.y - p2.y);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Coord [moumer=" + moumer + ", moustred=" + moustred + ", dim=" + dim + ", natoceni=" + natoceni + "]";
	}

	public Point transform(final Mou mou) {
		// pro moumer blížící se k nule nemusí být transformace jednoznačná, protože plocha v bodech
		// je větší než rozsah mou, mapa je zobrazena vícekrát, takže jednem mou odpovídá více pointů.
		// Zde se spočítá nejjihozápadnější bod.

		// souřadnice mohou být i záporné
		final Point p = new Point();
		final Mou moujz = getMouJZ();
		p.x = mou.xx - moujz.xx >> mpShift;
		p.y = dim.height - (mou.yy - moujz.yy >> mpShift);
		if (tam != null) {
			tam.transform(p, p);
		}
		return p;
	}

	public Rectangle transform(final MouRect mourect) {
		final Point p1 = transform(mourect.getSz());
		final Point p2 = transform(mourect.getJv());
		// System.err.println("RORERO: " + rect + " --- " + mourect + " / " + p1 + "+" + p2);
		return new Rectangle(p1, new Dimension(p2.x - p1.x, p2.y - p1.y));
	}

	/**
	 * Vypočítá obdélník vyhovující souřadnicovému obdélníku a přidá nějaké pixly na stranách podle incestu.
	 *
	 * @param mourect
	 * @param insets
	 * @return
	 */
	public Rectangle transform(final MouRect mourect, final Insets insets) {
		if (mourect == null) {
			return null;
		}

		final Rectangle r = transform(mourect);
		// System.out.println("Kompjuted rektangle: " + rect + " | " + r + " -- " + mourect);
		return new Rectangle(r.x - insets.left, r.y - insets.top, r.width + insets.left + insets.right, r.height + insets.top + insets.bottom);
	}

	public Mou transform(final Point p) {
		if (zpet != null) {
			zpet.transform(p, p);
		}
		// pozor na to, že pro moumer blížící se k nule může dojít k přetečení při posunu
		// pro případ, kdy se v okně objeví celá mapa světa vícekrát
		final Mou moujz = getMouJZ();
		// System.out.println("TRANSUJEME: " + p + " / " + moustred + " -/- " + moujz + " -+++- " + moustred.toWgs() + " -/- " + moujz.toWgs());

		return new Mou(moujz.xx + (p.x << mpShift), moujz.yy + (dim.height - p.y << mpShift));
	}

	public MouRect transform(final Rectangle rect) {
		return new MouRect(transform(new Point(rect.x, rect.y)), transform(new Point(rect.x + rect.width, rect.y + rect.height)));

	}

	/*
	 * Transformuje vzdálnost v pixlech na vzdálenost v mouřadnicích.
	 */
	public int transformPoindDiff(final int pointDiff) {
		return pointDiff << mpShift;
	}

	/** transformuje posun v pixlech na posun v mouřadnicích */
	public Moud transformShift(final int dx, final int dy) {
		// minus zde je proto, že na obrazovce jdou souadnice shora dolu a mouřadnice naopak
		return new Moud(transformPoindDiff(dx), -transformPoindDiff(dy));
	}

	public BoundingRect transforToBounding(final Rectangle rect) {
		final Point p1 = new Point(rect.x, rect.y);
		final Point p2 = new Point(rect.x + rect.width, rect.y + rect.height);
		final Mou mou1 = transform(p1);
		final Mou mou2 = transform(p2);
		// To je spravne, protoze souradnice jdou opacne
		return new BoundingRect(mou1.xx, mou2.yy, mou2.xx, mou1.yy);
	}

	private void computeAffineTransforms() {
		if (natoceni == 0) {
			tam = null;
			zpet = null;
			return;
		}
		final AffineTransform tra = new AffineTransform();
		final double widthPul = (double) dim.width / 2;
		final double heightPul = (double) dim.height / 2;
		tra.translate(widthPul, heightPul);
		tra.rotate(natoceni);
		tra.translate(-widthPul, -heightPul);
		tam = tra;
		try {
			zpet = tam.createInverse();
		} catch (final NoninvertibleTransformException e) {
			throw new RuntimeException(e);
		}
	}

	private Coord create(final int moumer, final Mou moustred, final Dimension dim, final double natoceni) {
		if (moumer == getMoumer() && moustred.equals(this.moustred) && dim.equals(this.dim) && natoceni == this.natoceni) {
			return this; // vrátíme stejnou instanci, podle čehož se pozná, že nedošlo ke změně
		}
		return new Coord(moumer, moustred, dim, natoceni);
	}

}
