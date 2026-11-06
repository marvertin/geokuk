package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Neměnný výsledek challenge s políčky (D/T matice, kalendář): chybějící políčka a kandidáti. */
public final class VysledekPolicek {

	private final int celkem;
	private final List<String> stitky;
	private final List<KandidatPolicka> kandidati;
	private final Object vstup;

	VysledekPolicek(final int celkem, final List<String> chybejici, final List<KandidatPolicka> kandidati) {
		this(celkem, chybejici, kandidati, null);
	}

	private VysledekPolicek(final int celkem, final List<String> chybejici, final List<KandidatPolicka> kandidati, final Object vstup) {
		this.celkem = celkem;
		this.stitky = Collections.unmodifiableList(new ArrayList<>(chybejici));
		this.kandidati = Collections.unmodifiableList(new ArrayList<>(kandidati));
		this.vstup = vstup == null ? this : vstup;
	}

	/**
	 * Tentýž výsledek označený vstupem, ze kterého vznikl. Výsledky téhož vstupu (třeba přepočet podle jiného středu mapy) mají stejný klíč a {@link DalsiPolicko} u nich
	 * pokračuje v kole; výsledek bez označení je vlastním vstupem.
	 */
	VysledekPolicek sVstupem(final Object klic) {
		return new VysledekPolicek(celkem, stitky, kandidati, Objects.requireNonNull(klic, "klic"));
	}

	Object vstup() {
		return vstup;
	}

	private volatile Map<String, KandidatPolicka> index;

	/** Štítek kandidáta pro chybějící políčko; null, když keš není mezi kandidáty. Index kódů se staví při prvním dotazu, potom jde o čtení z mapy bez alokace. */
	public StitekKese stitek(final String idKese) {
		Map<String, KandidatPolicka> m = index;
		if (m == null) {
			synchronized (this) {
				m = index;
				if (m == null) {
					m = new HashMap<>(Math.max(16, kandidati.size() * 4 / 3 + 1));
					for (final KandidatPolicka k : kandidati) {
						m.putIfAbsent(k.id(), k);
					}
					index = m;
				}
			}
		}
		return m.get(idKese);
	}

	/** Kolik políček je potřeba zaplnit. */
	public int celkem() {
		return celkem;
	}

	public int pocetChybejicich() {
		return stitky.size();
	}

	/** Štítky chybějících políček v pořadí podle challenge (matice: D, pak T; kalendář: nejbližší den první). */
	public List<String> stitkyChybejicich() {
		return stitky;
	}

	/** Všichni kandidáti pro chybějící políčka. */
	public List<KandidatPolicka> kandidati() {
		return kandidati;
	}
}
