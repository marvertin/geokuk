package cz.geokuk.plugins.challenge;

/** Společné vlastnosti akcí plánovače a tlačítek pruhu: stav se bere z {@link ChallengeModel}. */
final class ChallengeAkce {

	private ChallengeAkce() {}

	/** Akce „další/předchozí chybějící“ a filtr jsou dostupné, když je vidět pruh plánovače. */
	static boolean dostupne(final ChallengeModel model) {
		return model != null && model.jePruhViditelny();
	}

	/**
	 * Přejde na další nebo předchozí chybějící u hlavní challenge: vybere kandidáta a vystředí na něj mapu. Bez modelu, plánovače nebo aktivní challenge nedělá nic a vrací null.
	 */
	static VybranyKandidat prejdi(final ChallengeModel model, final PlanovacChallenge planovac, final boolean dopredu) {
		if (model == null || planovac == null) {
			return null;
		}
		final Challenge c = model.zobrazena();
		if (c == null) {
			return null;
		}
		return dopredu ? planovac.dalsiChybejici(c) : planovac.predchoziChybejici(c);
	}
}
