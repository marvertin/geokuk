package cz.geokuk.plugins.challenge;

/** Operace se seznamem challenge se nepovedla; zpráva je určená uživateli. */
public final class ChallengeChyba extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ChallengeChyba(final String zprava) {
		super(zprava);
	}
}
