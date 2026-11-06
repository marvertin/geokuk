package cz.geokuk.plugins.challenge;

import cz.geokuk.plugins.challenge.Challenge.Typ;

/** Stav pruhu plánovače na mapě v jednom okamžiku: co zobrazit a co ovládat. Neměnný. */
public final class StavPruhu {

	/** Pruh se nezobrazuje (plánovač je vypnutý nebo není aktivní challenge). */
	public static final StavPruhu SKRYTY = new StavPruhu(false, null, 0, -1, null, null, false, null);

	private final boolean viditelny;
	private final Challenge challenge;
	private final int pocetAktivnich;
	private final int badge;
	private final String stitek;
	private final VybranyKandidat kandidat;
	private final boolean jenKandidati;
	private final String chyba;

	StavPruhu(final boolean viditelny, final Challenge challenge, final int pocetAktivnich, final int badge, final String stitek, final VybranyKandidat kandidat,
			final boolean jenKandidati, final String chyba) {
		this.viditelny = viditelny;
		this.challenge = challenge;
		this.pocetAktivnich = pocetAktivnich;
		this.badge = badge;
		this.stitek = stitek;
		this.kandidat = kandidat;
		this.jenKandidati = jenKandidati;
		this.chyba = chyba;
	}

	public boolean jeViditelny() {
		return viditelny;
	}

	/** Zobrazená challenge, nebo null, když je pruh skrytý. */
	public Challenge challenge() {
		return challenge;
	}

	/** Typ zobrazené challenge, nebo null. */
	public Typ typ() {
		return challenge == null ? null : challenge.typ();
	}

	/** Text typu v pruhu (360° / Kalendář / D/T), nebo prázdný text. */
	public String stitekTypu() {
		return challenge == null ? "" : StitkyPruhu.typ(challenge.typ());
	}

	/** Kolik challenge je aktivních (pruh jich může přepínat víc). */
	public int pocetAktivnich() {
		return pocetAktivnich;
	}

	/** Počet chybějících (sektorů, políček nebo dnů) pro badge, nebo -1, dokud výsledek není. */
	public int badge() {
		return badge;
	}

	/** Štítek vybraného kandidáta, nebo null, dokud se „další chybějící“ nepoužilo. */
	public String stitek() {
		return stitek;
	}

	/** Vybraný kandidát („další chybějící“), nebo null. */
	public VybranyKandidat kandidat() {
		return kandidat;
	}

	/** Filtr „jen kandidáti“ je zapnutý pro zobrazenou challenge. */
	public boolean jenKandidati() {
		return jenKandidati;
	}

	/** Důvod, proč se výsledek nepočítá (neúplná definice), nebo null. */
	public String chyba() {
		return chyba;
	}
}
