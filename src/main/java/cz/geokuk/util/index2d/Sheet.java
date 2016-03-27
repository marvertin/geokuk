package cz.geokuk.util.index2d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Sheet<T> extends Node0<T> {

	private static final Logger log = LogManager.getLogger(Sheet.class.getSimpleName());

	private final int xx;
	private final int yy;
	private final T obj;

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

	@Override
	boolean isSheet() {
		return true;
	}

	@Override
	void visit(final BoundingRect rect, final Visitor<T> aVisitor) {
		if (rect == null || xx >= rect.xx1 && xx < rect.xx2 & yy >= rect.yy1 && yy < rect.yy2) {
			// jsem uvnitr
			aVisitor.visit(this);
		}
	}

	@Override
	void vypis(final String aPrefix, final int aLevel) {
		final String mezery = String.format("%" + aLevel * 2 + "s", " ");
		log.debug("{}{}: [{},{}] {}", mezery, aPrefix, xx, yy, obj);
	}

	@Override
	public String toString() {
		return "{" + xx + " " + yy + " " + obj + "}";
	}
}
