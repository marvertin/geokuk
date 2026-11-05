package cz.geokuk.plugins.challenge;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * Výběr „dalšího chybějícího“ pro challenge s políčky: nejdřív kandidát s nejbližším datem (kalendář), při shodě nebo bez data nejbližší zadanému bodu. Už vybraní se
 * v jednom kole přeskakují; po projití všech začne kolo znovu, stejně jako při změně výsledku.
 */
public final class DalsiPolicko {

	private final Set<String> vybrane = new HashSet<>();
	private VysledekPolicek vysledek;

	/** Další kandidát; null, když kandidáti nejsou. */
	public synchronized KandidatPolicka dalsi(final VysledekPolicek vysledek, final double lat, final double lon) {
		if (vysledek != this.vysledek) {
			this.vysledek = vysledek;
			vybrane.clear();
		}
		if (vysledek == null || vysledek.kandidati().isEmpty()) {
			return null;
		}
		KandidatPolicka k = nejlepsi(vysledek, lat, lon);
		if (k == null) {
			vybrane.clear();
			k = nejlepsi(vysledek, lat, lon);
		}
		vybrane.add(k.id());
		return k;
	}

	private KandidatPolicka nejlepsi(final VysledekPolicek v, final double lat, final double lon) {
		final Comparator<KandidatPolicka> poradi = Comparator.comparingInt((final KandidatPolicka k) -> k.dniDo() == null ? 0 : k.dniDo())
				.thenComparingDouble(k -> Geodezie.vzdalenostKoule(lat, lon, k.lat(), k.lon()));
		KandidatPolicka nejlepsi = null;
		for (final KandidatPolicka k : v.kandidati()) {
			if (!vybrane.contains(k.id()) && (nejlepsi == null || poradi.compare(k, nejlepsi) < 0)) {
				nejlepsi = k;
			}
		}
		return nejlepsi;
	}

	/** Začne kolo znovu. */
	public synchronized void reset() {
		vybrane.clear();
	}
}
