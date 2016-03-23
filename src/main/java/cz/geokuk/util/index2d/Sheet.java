package cz.geokuk.util.index2d;

public class Sheet<T> extends Node0<T> {

	final int	xx;
	final int	yy;
	final T		obj;

	/**
	 * @param aMapobj
	 */
	public Sheet(final int xx, final int yy, final T aMapobj) {
		this.xx = xx;
		this.yy = yy;
		count = 1;
		obj = aMapobj;
	}

	public T get() {
		return obj;
	}

	/**
	 * @return the xx
	 */
	public int getXx() {
		return xx;
	}

	/**
	 * @return the yy
	 */
	public int getYy() {
		return yy;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see objekty.Node0#isSheet()
	 */
	@Override
	boolean isSheet() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see objekty.Node0#visit(objekty.BoundingRect, objekty.Visitor)
	 */
	@Override
	void visit(final BoundingRect rect, final Visitor<T> aVisitor) {
		if (rect == null || xx >= rect.xx1 && xx < rect.xx2 & yy >= rect.yy1 && yy < rect.yy2) {
			// jsem uvnitr
			aVisitor.visit(this);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see objekty.Node0#vypis(java.lang.String, int)
	 */
	@Override
	void vypis(final String aPrefix, final int aLevel) {
		final String mezery = String.format("%" + aLevel * 2 + "s", " ");
		System.out.printf("%s%s: [%d,%d] %s\n", mezery, aPrefix, xx, yy, obj);
	}

}
