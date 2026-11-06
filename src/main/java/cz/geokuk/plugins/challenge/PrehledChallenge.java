package cz.geokuk.plugins.challenge;

import cz.geokuk.plugins.challenge.Challenge.Typ;

/**
 * Neměnný přehled aktivní challenge pro kreslení štítků keší na mapě a pro tooltip: barva, kolik je splněno, střed a štítek kandidáta podle kódu keše. Nový přehled vzniká
 * s každým novým výsledkem, takže změnu pozná kreslení porovnáním identity s přehledem z minula. Čtení nealokuje a lze je volat z kteréhokoli vlákna.
 */
public final class PrehledChallenge {

	private final Challenge challenge;
	private final Object zdroj;
	private final VysledekSektoru sektory;
	private final VysledekPolicek policka;
	private final double stredLat;
	private final double stredLon;
	private final int splneno;
	private final int celkem;

	private PrehledChallenge(final Challenge challenge, final VysledekSektoru sektory, final VysledekPolicek policka, final double stredLat, final double stredLon) {
		this.challenge = challenge;
		this.sektory = sektory;
		this.policka = policka;
		this.zdroj = sektory != null ? sektory : policka;
		this.stredLat = stredLat;
		this.stredLon = stredLon;
		if (sektory != null) {
			this.celkem = 360;
			this.splneno = sektory.pocetObsazenych();
		} else {
			this.celkem = policka.celkem();
			this.splneno = policka.celkem() - policka.pocetChybejicich();
		}
	}

	static PrehledChallenge zeSektoru(final Challenge challenge, final VysledekSektoru vysledek, final double stredLat, final double stredLon) {
		return new PrehledChallenge(challenge, vysledek, null, stredLat, stredLon);
	}

	static PrehledChallenge zPolicek(final Challenge challenge, final VysledekPolicek vysledek, final double stredLat, final double stredLon) {
		return new PrehledChallenge(challenge, null, vysledek, stredLat, stredLon);
	}

	public Challenge challenge() {
		return challenge;
	}

	public Typ typ() {
		return challenge.typ();
	}

	/** Barva 0xRRGGBB podle typu challenge, viz {@link PaletaChallenge}. */
	public int barva() {
		return PaletaChallenge.barva(challenge.typ());
	}

	/** Kolik sektorů, políček nebo dnů je splněno. */
	public int splneno() {
		return splneno;
	}

	/** Kolik jich je celkem (360, 81, 365 nebo 366). */
	public int celkem() {
		return celkem;
	}

	/** Střed je znám (výsledek vznikl výpočtem plánovače). */
	public boolean maStred() {
		return !Double.isNaN(stredLat) && !Double.isNaN(stredLon);
	}

	public double stredLat() {
		return stredLat;
	}

	public double stredLon() {
		return stredLon;
	}

	/** Popis středu pro značku na mapě: „domov“, kód listingu, „zadané souřadnice“; u D/T a kalendáře „střed mapy“. */
	public String stredPopis() {
		if (challenge.typ() != Typ.SEKTORY_360) {
			return "střed mapy";
		}
		switch (challenge.stred().druh()) {
		case DOMOV:
			return "domov";
		case LISTING:
			return challenge.stred().kodListingu();
		default:
			return "zadané souřadnice";
		}
	}

	/** Štítek kandidáta pro chybějící sektor nebo políčko podle kódu keše; null, když to není kandidát. */
	public StitekKese stitek(final String idKese) {
		return sektory != null ? sektory.stitek(idKese) : policka.stitek(idKese);
	}

	/** Výsledek, ze kterého přehled vznikl. */
	Object zdroj() {
		return zdroj;
	}
}
