package cz.geokuk.plugins.kesoid.mvc;

import java.util.*;

public class GsakParametryNacitani {
	private Set<String> casNalezu = Collections.emptySet();
	/** Nové databáze GSAKu se načtou hned; vypnuto = zůstanou blokované, dokud je uživatel nevybere v tabulce zdrojů. */
	private boolean noveDatabazeHned = false;
	/** Totéž pro databáze OpenSAKu. */
	private boolean noveDatabazeOpensakuHned = true;

	public Set<String> getCasNalezu() {
		return casNalezu;
	}

	public void setCasNalezu(final Collection<String> aCasNalezu) {
		this.casNalezu = Collections.unmodifiableSet(new LinkedHashSet<>(aCasNalezu));
	}

	public boolean isNoveDatabazeHned() {
		return noveDatabazeHned;
	}

	public void setNoveDatabazeHned(final boolean noveDatabazeHned) {
		this.noveDatabazeHned = noveDatabazeHned;
	}

	public boolean isNoveDatabazeOpensakuHned() {
		return noveDatabazeOpensakuHned;
	}

	public void setNoveDatabazeOpensakuHned(final boolean noveDatabazeOpensakuHned) {
		this.noveDatabazeOpensakuHned = noveDatabazeOpensakuHned;
	}

	@Override
	public String toString() {
		return "GsakParametryNacitani [casNalezu=" + casNalezu + ", noveDatabazeHned=" + noveDatabazeHned + ", noveDatabazeOpensakuHned=" + noveDatabazeOpensakuHned + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(casNalezu, noveDatabazeHned, noveDatabazeOpensakuHned);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		final GsakParametryNacitani other = (GsakParametryNacitani) obj;
		return Objects.equals(casNalezu, other.casNalezu) && noveDatabazeHned == other.noveDatabazeHned && noveDatabazeOpensakuHned == other.noveDatabazeOpensakuHned;
	}
}
