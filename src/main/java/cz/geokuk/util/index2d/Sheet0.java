package cz.geokuk.util.index2d;

public abstract class Sheet0<T> extends Node0<T> {

	protected final int xx;
	protected final int yy;

	public Sheet0(final int xx, final int yy) {
		super();
		this.xx = xx;
		this.yy = yy;
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

}
