package cz.geokuk.core.coordinates;


/**
 * Viditelná masa v mou souřadnicích.
 * @author tatinek
 *
 */
public class MouFrame {
	public Mou moujz;
	public Mou mousv;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((moujz == null) ? 0 : moujz.hashCode());
		result = prime * result + ((mousv == null) ? 0 : mousv.hashCode());
		return result;
	}
	
	public MouFrame(Mou moujz, Mou mousv) {
		super();
		this.moujz = moujz;
		this.mousv = mousv;
	}

	@Override
	public String toString() {
		return "MouFrame [moujz=" + moujz + ", mousv=" + mousv + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MouFrame other = (MouFrame) obj;
		if (moujz == null) {
			if (other.moujz != null)
				return false;
		} else if (!moujz.equals(other.moujz))
			return false;
		if (mousv == null) {
			if (other.mousv != null)
				return false;
		} else if (!mousv.equals(other.mousv))
			return false;
		return true;
	}
	
	
	

}
