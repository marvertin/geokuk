package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Výběr „dalšího chybějícího“ pro challenge s políčky: nejdřív kandidát s nejbližším datem (kalendář), při shodě nebo bez data nejbližší zadanému bodu. Už vybraní se
 * v jednom kole přeskakují; po projití všech začne kolo znovu nejbližším jiným kandidátem, než je poslední vybraný (na tom mapa už stojí). Kolo pokračuje i přes
 * nový výsledek téhož vstupu (přepočet podle jiného středu mapy, viz {@link VysledekPolicek#sVstupem(Object)}); výsledek z jiného vstupu začne kolo od začátku.
 */
public final class DalsiPolicko {

	private final Set<String> vybrane = new HashSet<>();
	/** Vybraní v tomto kole v pořadí výběru, kvůli „předchozímu“. */
	private final List<KandidatPolicka> poradi = new ArrayList<>();
	private Object vstup;

	/** Další kandidát; null, když kandidáti nejsou. */
	public synchronized KandidatPolicka dalsi(final VysledekPolicek vysledek, final double lat, final double lon) {
		final Object klic = vysledek == null ? null : vysledek.vstup();
		if (klic != vstup) {
			vstup = klic;
			vybrane.clear();
			poradi.clear();
		}
		if (vysledek == null || vysledek.kandidati().isEmpty()) {
			return null;
		}
		KandidatPolicka k = nejlepsi(vysledek, lat, lon);
		if (k == null) {
			final String posledni = poradi.isEmpty() ? null : poradi.get(poradi.size() - 1).id();
			vybrane.clear();
			poradi.clear();
			if (posledni != null && vysledek.kandidati().size() > 1) {
				vybrane.add(posledni);
				k = nejlepsi(vysledek, lat, lon);
				vybrane.clear();
			} else {
				k = nejlepsi(vysledek, lat, lon);
			}
		}
		vybrane.add(k.id());
		poradi.add(k);
		return k;
	}

	/** Předchozí vybraný kandidát v tomto kole; null, když před posledním vybraným žádný není. Aktuální se tím vrátí mezi nevybrané. */
	public synchronized KandidatPolicka predchozi(final VysledekPolicek vysledek) {
		if (vysledek == null || vysledek.vstup() != vstup || poradi.size() < 2) {
			return null;
		}
		vybrane.remove(poradi.remove(poradi.size() - 1).id());
		return poradi.get(poradi.size() - 1);
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
		poradi.clear();
	}
}
