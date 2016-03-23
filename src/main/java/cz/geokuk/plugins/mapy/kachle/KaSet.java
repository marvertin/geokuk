package cz.geokuk.plugins.mapy.kachle;

import java.util.EnumSet;

public class KaSet {

	private final EnumSet<EKaType> kts;

	public KaSet(final EnumSet<EKaType> kts) {
		this.kts = kts;
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
		final KaSet other = (KaSet) obj;
		if (kts == null) {
			if (other.kts != null) {
				return false;
			}
		} else if (!kts.equals(other.kts)) {
			return false;
		}
		return true;
	}

	public EnumSet<EKaType> getKts() {
		return kts.clone();
	}

	// public boolean isExaclyOnePodklad() {
	// if (kts == null) return false;
	// int n = 0;
	// for (EKachloType kt : kts) {
	// if (kt.isPodklad()) n++;
	// }
	// return n == 1;
	// }

	public EKaType getPodklad() {
		if (kts == null) {
			return null;
		}
		for (final EKaType kt : kts) {
			if (kt.isPodklad()) {
				return kt;
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (kts == null ? 0 : kts.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return kts.toString();
	}

}
