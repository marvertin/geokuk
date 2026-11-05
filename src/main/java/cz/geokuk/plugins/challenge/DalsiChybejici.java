package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.geokuk.plugins.challenge.VysledekSektoru.KandidatKes;

/**
 * Výběr „dalšího chybějícího“: kandidát nejbližší zadanému bodu (třeba středu mapy), cyklicky. Už vybraní se v jednom kole přeskakují, takže opakované volání po
 * vystředění mapy na vybranou keš vybere další nejbližší a po projití všech začne kolo znovu. Změní-li se výsledek, kolo se začne od začátku.
 */
public final class DalsiChybejici {

	private final Set<String> vybrane = new HashSet<>();
	private final List<String> poradi = new ArrayList<>();
	private VysledekSektoru vysledek;

	/** Další kandidát podle vzdálenosti od bodu; null, když kandidáti nejsou. */
	public synchronized VybranyKandidat dalsi(final VysledekSektoru vysledek, final double lat, final double lon) {
		if (vysledek != this.vysledek) {
			this.vysledek = vysledek;
			vybrane.clear();
			poradi.clear();
		}
		if (vysledek == null || vysledek.vsichniKandidati().isEmpty()) {
			return null;
		}
		KandidatKes nejblizsi = nejblizsi(vysledek, lat, lon);
		if (nejblizsi == null) {
			vybrane.clear();
			poradi.clear();
			nejblizsi = nejblizsi(vysledek, lat, lon);
		}
		vybrane.add(nejblizsi.id());
		poradi.add(nejblizsi.id());
		return new VybranyKandidat(nejblizsi.id(), nejblizsi.lat(), nejblizsi.lon(), nejblizsi.sektor(), Geodezie.vzdalenostKoule(lat, lon, nejblizsi.lat(), nejblizsi.lon()));
	}

	/** Předchozí vybraný kandidát v tomto kole; null, když před posledním vybraným žádný není. Aktuální se tím vrátí mezi nevybrané. */
	public synchronized VybranyKandidat predchozi(final VysledekSektoru vysledek, final double lat, final double lon) {
		if (vysledek != this.vysledek || poradi.size() < 2) {
			return null;
		}
		vybrane.remove(poradi.remove(poradi.size() - 1));
		final String id = poradi.get(poradi.size() - 1);
		for (final KandidatKes k : vysledek.vsichniKandidati()) {
			if (k.id().equals(id)) {
				return new VybranyKandidat(k.id(), k.lat(), k.lon(), k.sektor(), Geodezie.vzdalenostKoule(lat, lon, k.lat(), k.lon()));
			}
		}
		return null;
	}

	private KandidatKes nejblizsi(final VysledekSektoru v, final double lat, final double lon) {
		KandidatKes nejlepsi = null;
		double nejlepsiVzdalenost = Double.MAX_VALUE;
		for (final KandidatKes k : v.vsichniKandidati()) {
			if (vybrane.contains(k.id())) {
				continue;
			}
			final double d = Geodezie.vzdalenostKoule(lat, lon, k.lat(), k.lon());
			if (d < nejlepsiVzdalenost) {
				nejlepsiVzdalenost = d;
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
