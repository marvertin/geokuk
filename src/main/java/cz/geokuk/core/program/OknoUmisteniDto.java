/**
 *
 */
package cz.geokuk.core.program;

import java.awt.Dimension;
import java.awt.Point;

import cz.geokuk.framework.Copyable;
import cz.geokuk.framework.Preferenceble;

/**
 * @author veverka
 *
 */
@Preferenceble
public class OknoUmisteniDto implements Copyable<OknoUmisteniDto> {

	private Point pozice = new Point(Integer.MIN_VALUE,Integer.MIN_VALUE);
	private Dimension  velikost = new Dimension(Integer.MIN_VALUE, Integer.MIN_VALUE);

	/**
	 * @return the velikost
	 */
	public Dimension getVelikost() {
		return velikost;
	}
	/**
	 * @param velikost the velikost to set
	 */
	public void setVelikost(Dimension velikost) {
		this.velikost = velikost;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pozice == null) ? 0 : pozice.hashCode());
		result = prime * result + ((velikost == null) ? 0 : velikost.hashCode());
		return result;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OknoUmisteniDto other = (OknoUmisteniDto) obj;
		if (pozice == null) {
			if (other.pozice != null)
				return false;
		} else if (!pozice.equals(other.pozice))
			return false;
		if (velikost == null) {
			if (other.velikost != null)
				return false;
		} else if (!velikost.equals(other.velikost))
			return false;
		return true;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "OknoStatusDto [pozice=" + pozice +  ", velikost=" + velikost + "]";
	}
	/**
	 * @param pozice the pozice to set
	 */
	public void setPozice(Point pozice) {
		this.pozice = pozice;
	}
	/**
	 * @return the pozice
	 */
	public Point getPozice() {
		return pozice;
	}
	/* (non-Javadoc)
	 * @see cz.geokuk.framework.Copyable#copy()
	 */

	@Override
	public OknoUmisteniDto copy() {
		try {
			return (OknoUmisteniDto) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}


}
