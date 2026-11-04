package cz.geokuk.plugins.challenge;

import cz.geokuk.framework.Event0;

/** Změna stavu plánovače challenge; událost se doručuje ve vlákně Swingu. */
public class ChallengeZmenenyEvent extends Event0<ChallengeModel> {

	public enum Druh {
		/** Nastavení načteno (start, změna profilu). */
		NACTENO,
		/** Plánovač zapnut nebo vypnut. */
		ZAPNUTO,
		/** Seznam definic se změnil (nová, upravená, přejmenovaná, smazaná). */
		DEFINICE,
		/** Změnila se množina aktivních challenge. */
		AKTIVNI,
		/** Je nový výsledek výpočtu některé challenge. */
		VYSLEDEK,
		/** Změnil se stav předpočtu některé challenge. */
		PREDPOCET,
		/** Změnil se stav filtru „jen kandidáti“. */
		JEN_KANDIDATI,
		/** Změnil se vybraný kandidát některé challenge. */
		KANDIDAT
	}

	private final Druh druh;
	private final Challenge challenge;

	public ChallengeZmenenyEvent(final Druh druh, final Challenge challenge) {
		this.druh = druh;
		this.challenge = challenge;
	}

	public Druh druh() {
		return druh;
	}

	/** Challenge, které se změna týká; null, týká-li se všech nebo plánovače jako celku. */
	public Challenge challenge() {
		return challenge;
	}

	@Override
	public String toString() {
		return "ChallengeZmenenyEvent " + druh + (challenge == null ? "" : " " + challenge.nazev());
	}
}
