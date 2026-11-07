package cz.geokuk.plugins.challenge;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

import cz.geokuk.plugins.kesoid.Kesoid;

/** Filtr „jen kandidáti“: kešoid projde, je-li mezi kandidáty challenge; kandidáti se porovnávají podle označení bez ohledu na velikost písmen. */
public final class FiltrKandidatu implements Predicate<Kesoid> {

	private final Set<String> identifikatory;

	FiltrKandidatu(final Set<String> identifikatory) {
		this.identifikatory = new HashSet<>();
		for (final String id : identifikatory) {
			this.identifikatory.add(id.toUpperCase(Locale.ROOT));
		}
	}

	/** Filtr pro zadanou challenge podle jejího posledního výsledku, u matice D/T zúžený na vybrané políčko; null, dokud výsledek není (pak se nefiltruje). */
	static FiltrKandidatu pro(final ChallengeModel model, final Challenge c) {
		final Set<String> ids = new HashSet<>();
		if (c.typ() == Challenge.Typ.SEKTORY_360) {
			final VysledekSektoru v = model.vysledek(c);
			if (v == null) {
				return null;
			}
			for (final VysledekSektoru.KandidatKes k : v.vsichniKandidati()) {
				ids.add(k.id());
			}
		} else {
			final VysledekPolicek v = model.vysledekPolicek(c);
			if (v == null) {
				return null;
			}
			final String policko = model.vybranePolicko(c);
			for (final KandidatPolicka k : v.kandidati()) {
				if (policko == null || policko.equals(k.stitek())) {
					ids.add(k.id());
				}
			}
		}
		return new FiltrKandidatu(ids);
	}

	int pocet() {
		return identifikatory.size();
	}

	@Override
	public boolean test(final Kesoid kesoid) {
		final String id = kesoid == null ? null : kesoid.getIdentifier();
		return id != null && identifikatory.contains(id.toUpperCase(Locale.ROOT));
	}

	@Override
	public boolean equals(final Object o) {
		return o instanceof FiltrKandidatu && identifikatory.equals(((FiltrKandidatu) o).identifikatory);
	}

	@Override
	public int hashCode() {
		return identifikatory.hashCode();
	}
}
