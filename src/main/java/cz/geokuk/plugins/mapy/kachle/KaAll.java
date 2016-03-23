package cz.geokuk.plugins.mapy.kachle;

import java.util.Set;
import java.util.stream.Collectors;

public class KaAll extends Ka0 {

	// private int DOPLNKOVAC = 1<<28;

	public KaSet kaSet;

	public KaAll(final KaLoc loc, final KaSet kaSet) {
		super(loc);
		this.kaSet = kaSet;
	}

	@Override
	public String typToString() {
		final StringBuilder sb = new StringBuilder();
		for (final EKaType kt : kaSet.getKts()) {
			sb.append(kt.name());
			sb.append('_');
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return super.getLoc().toString() + "*" + kaSet;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (kaSet == null ? 0 : kaSet.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final KaAll other = (KaAll) obj;
		if (kaSet == null) {
			if (other.kaSet != null) {
				return false;
			}
		} else if (!kaSet.equals(other.kaSet)) {
			return false;
		}
		return true;
	}

	public EKaType getPodkladType() {
		return kaSet.getPodklad();
	}

	public KaOne getPodklad() {
		return new KaOne(getLoc(), getPodkladType());
	}

	/**
	 * Rozpad na jednotlivé rekvesty
	 * 
	 * @return
	 */
	public Set<KaOne> getKaOnes() {
		return kaSet.getKts().stream().map(kaa -> new KaOne(super.getLoc(), kaa)).collect(Collectors.toSet());
	}

}
