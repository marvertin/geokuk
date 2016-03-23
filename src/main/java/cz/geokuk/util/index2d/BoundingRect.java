/**
 *
 */
package cz.geokuk.util.index2d;

/**
 * @author veverka
 *
 */
public class BoundingRect {

	public static final BoundingRect	ALL	= new BoundingRect(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);

	public final int					xx1;
	public final int					yy1;
	public final int					xx2;
	public final int					yy2;

	/**
	 * @param aXx1
	 * @param aXx2
	 * @param aYy1
	 * @param aYy2
	 */
	public BoundingRect(final int aXx1, final int aYy1, final int aXx2, final int aYy2) {
		xx1 = aXx1;
		yy1 = aYy1;
		xx2 = aXx2;
		yy2 = aYy2;

		if (xx2 < xx1 || yy2 < yy1) {
			// throw new RuntimeException("Spatne presahy " + this);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "BoundingRect [xx1=" + xx1 + ", xx2=" + xx2 + ", yy1=" + yy1 + ", yy2=" + yy2 + "]";
	}

	public BoundingRect rozsir(final int okolik) {
		return new BoundingRect(xx1 - okolik, yy1 - okolik, xx2 + okolik, yy2 + okolik);
	}

	public boolean isPoint() {
		return xx1 == xx2 && yy1 == yy2;
	}

}
