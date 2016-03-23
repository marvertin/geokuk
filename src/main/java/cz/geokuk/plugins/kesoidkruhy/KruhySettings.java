package cz.geokuk.plugins.kesoidkruhy;

import java.awt.Color;

import cz.geokuk.framework.Copyable;
import cz.geokuk.framework.Preferenceble;

@Preferenceble
public class KruhySettings implements Copyable<KruhySettings> {

	private boolean	onoff				= false;
	private Color	barva				= new Color(153, 0, 220, 70);
	private int		velikost			= 20;
	private boolean	jednotkovaVelikost	= false;

	public boolean isOnoff() {
		return onoff;
	}

	public void setOnoff(final boolean onoff) {
		this.onoff = onoff;
	}

	/**
	 * @return the barva
	 */
	public Color getBarva() {
		return barva;
	}

	/**
	 * @param barva
	 *            the barva to set
	 */
	public void setBarva(final Color barva) {
		this.barva = barva;
	}

	/**
	 * @return the velikost
	 */
	public int getVelikost() {
		return velikost;
	}

	/**
	 * @param velikost
	 *            the velikost to set
	 */
	public void setVelikost(final int velikost) {
		this.velikost = velikost;
	}

	/**
	 * @return the jednotkovaVelikost
	 */
	public boolean isJednotkovaVelikost() {
		return jednotkovaVelikost;
	}

	/**
	 * @param jednotkovaVelikost
	 *            the jednotkovaVelikost to set
	 */
	public void setJednotkovaVelikost(final boolean jednotkovaVelikost) {
		this.jednotkovaVelikost = jednotkovaVelikost;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (barva == null ? 0 : barva.hashCode());
		result = prime * result + (jednotkovaVelikost ? 1231 : 1237);
		result = prime * result + (onoff ? 1231 : 1237);
		result = prime * result + velikost;
		return result;
	}

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
		final KruhySettings other = (KruhySettings) obj;
		if (barva == null) {
			if (other.barva != null) {
				return false;
			}
		} else if (!barva.equals(other.barva)) {
			return false;
		}
		if (jednotkovaVelikost != other.jednotkovaVelikost) {
			return false;
		}
		if (onoff != other.onoff) {
			return false;
		}
		if (velikost != other.velikost) {
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "KruhySettings [onoff=" + onoff + ", velikost=" + velikost + ", barva=" + barva + ", jednotkovaVelikost=" + jednotkovaVelikost + "]";
	}

	@Override
	public KruhySettings copy() {
		try {
			return (KruhySettings) super.clone();
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

}
