package cz.geokuk.core.coordinates;

import cz.geokuk.util.index2d.BoundingRect;

public class MouRect {

	private int	xx1;
	private int	yy1;
	private int	xx2;
	private int	yy2;

	public Mou	sstre;

	public MouRect() {
	}

	public MouRect(final Mou mou) {
		add(mou);
	}

	public MouRect(final Mou roh1, final Mou roh2) {
		add(roh1);
		add(roh2);
		sstre = new Mou((roh1.xx + roh2.xx) / 2, (roh1.yy + roh2.yy) / 2);
		// assert roh1.xx == xx1;
		// assert roh2.xx == xx2;
		// assert roh1.yy == yy1 : roh1.yy + " " + yy1;
		// assert roh2.yy == yy2;
	}

	public Mou getStred() {
		return new Mou((xx1 + xx2) / 2, (yy1 + yy2) / 2);
	}

	public Mou getJz() {
		return new Mou(xx1, yy1);
	}

	public Mou getSv() {
		return new Mou(xx2, yy2);
	}

	public Mou getJv() {
		return new Mou(xx2, yy1);
	}

	public Mou getSz() {
		return new Mou(xx1, yy2);
	}

	public boolean isRecangle() {
		return xx1 < xx2 && yy1 < yy2;
	}

	public boolean isVerticalLine() {
		return xx1 == xx2 && yy1 < yy2;
	}

	public boolean isHorizontalLine() {
		return yy1 == yy2 && xx1 < xx2;
	}

	public int getMouWidth() {
		return xx2 - xx1;
	}

	public int getMouHeight() {
		return yy2 - yy1;
	}

	public boolean isLine() {
		return isVerticalLine() || isHorizontalLine();
	}

	public boolean isPoint() {
		return !isEmpty() && xx1 == xx2 && yy1 == yy2;
	}

	public boolean isEmpty() {
		return xx1 == 0 && xx2 == 0 & yy1 == 0 & yy2 == 0;
	}

	public BoundingRect getBoundingRect() {
		return new BoundingRect(xx1, yy1, xx2, yy2);
	}

	public void add(final Mou mou) {
		if (isEmpty()) { // tak jsou všechny nulové
			xx1 = mou.xx;
			xx2 = mou.xx;
			yy1 = mou.yy;
			yy2 = mou.yy;
		} else {
			if (mou.xx < xx1)
				xx1 = mou.xx;
			if (mou.xx > xx2)
				xx2 = mou.xx;
			if (mou.yy < yy1)
				yy1 = mou.yy;
			if (mou.yy > yy2)
				yy2 = mou.yy;
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + xx1;
		result = prime * result + xx2;
		result = prime * result + yy1;
		result = prime * result + yy2;
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MouRect other = (MouRect) obj;
		if (xx1 != other.xx1)
			return false;
		if (xx2 != other.xx2)
			return false;
		if (yy1 != other.yy1)
			return false;
		if (yy2 != other.yy2)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MouRect [xx1=" + xx1 + ", xx2=" + xx2 + ", yy1=" + yy1 + ", yy2=" + yy2 + "]";
	}

	/**
	 * Zvětší nebo zmenší velikost v daném poměru
	 */
	public void resize(final double pomer) {
		final int dx = (int) ((getMouWidth() * pomer - getMouWidth()) / 2);
		final int dy = (int) ((getMouHeight() * pomer - getMouHeight()) / 2);
		xx1 -= dx;
		xx2 += dx;
		yy1 -= dy;
		yy2 += dy;
	}

}
