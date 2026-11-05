package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Neměnný výsledek challenge s políčky (D/T matice, kalendář): chybějící políčka a kandidáti. */
public final class VysledekPolicek {

	private final int celkem;
	private final List<String> stitky;
	private final List<KandidatPolicka> kandidati;

	VysledekPolicek(final int celkem, final List<String> chybejici, final List<KandidatPolicka> kandidati) {
		this.celkem = celkem;
		this.stitky = Collections.unmodifiableList(new ArrayList<>(chybejici));
		this.kandidati = Collections.unmodifiableList(new ArrayList<>(kandidati));
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
