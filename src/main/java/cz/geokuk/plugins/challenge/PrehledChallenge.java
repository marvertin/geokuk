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
	private final Oblast oblast;
	private final GeometrieSektoru.Model model;

	private PrehledChallenge(final Challenge challenge, final VysledekSektoru sektory, final VysledekPolicek policka, final double stredLat, final double stredLon,
			final Oblast oblast) {
		this.challenge = challenge;
		this.oblast = oblast;
		this.model = GeometrieSektoru.Model.zPravidel(challenge.zdroj());
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
		return zeSektoru(challenge, vysledek, stredLat, stredLon, null);
	}

	/** {@code oblast}: oblast, se kterou se počítalo (null = bez omezení). */
	static PrehledChallenge zeSektoru(final Challenge challenge, final VysledekSektoru vysledek, final double stredLat, final double stredLon, final Oblast oblast) {
		return new PrehledChallenge(challenge, vysledek, null, stredLat, stredLon, oblast);
	}

	static PrehledChallenge zPolicek(final Challenge challenge, final VysledekPolicek vysledek, final double stredLat, final double stredLon) {
		return new PrehledChallenge(challenge, null, vysledek, stredLat, stredLon, null);
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

	/** Největší vzdálenost, kterou {@link #sektorBodu} uvažuje: větší než půl obvodu Země, takže se vzdálenost neomezuje. */
	private static final double BEZ_OMEZENI_M = 1e8;

	/**
	 * Sektor 0 až 359 libovolného bodu (třeba vybrané keše, i když není kandidát) podle jeho azimutu od středu challenge, měřený stejným modelem (koule, u pravidel Stator
	 * elipsoid) jako výpočet. -1, když to není challenge 360°, chybí střed, souřadnice jsou neplatné nebo je bod ve středu. Oblast se nekontroluje, viz {@link #jeVOblasti}.
	 * Nealokuje.
	 */
	public int sektorBodu(final double lat, final double lon) {
		if (sektory == null || !maStred() || !VypocetSektoru.jePlatnyStred(stredLat, stredLon)) {
			return -1;
		}
		return GeometrieSektoru.sektorBodu(stredLat, stredLon, lat, lon, 0, BEZ_OMEZENI_M, model);
	}

	/**
	 * Bod leží v oblasti, se kterou se challenge počítala; bez omezení oblasti (a u challenge s políčky) vždy true. Je to test bodu v oblasti, ne konstantní operace, takže ho
	 * volat jen pro keše, které nás zajímají. Nealokuje.
	 */
	public boolean jeVOblasti(final double lat, final double lon) {
		return oblast == null || oblast.obsahuje(lat, lon);
	}

	/**
	 * Štítek políčka matice D/T („D3/T4“) z obtížnosti a terénu v půlkách 0 až 8; null mimo rozsah nebo když to není matice D/T. Nealokuje (štítky jsou předpočítané).
	 */
	public String stitekPolicka(final int obtiznostPulek, final int terenPulek) {
		return challenge.typ() == Typ.DT_MATICE ? VypocetMatice.stitekNeboNull(obtiznostPulek, terenPulek) : null;
	}

	/** Pořadí políčka matice D/T (obtížnost × 9 + terén) pro porovnání „stejné políčko“; -1 mimo rozsah nebo když to není matice D/T. */
	public int indexPolicka(final int obtiznostPulek, final int terenPulek) {
		return challenge.typ() == Typ.DT_MATICE && VypocetMatice.jePlatne(obtiznostPulek) && VypocetMatice.jePlatne(terenPulek) ? obtiznostPulek * VypocetMatice.PULEK + terenPulek : -1;
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
