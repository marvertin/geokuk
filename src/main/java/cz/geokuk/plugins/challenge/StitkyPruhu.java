package cz.geokuk.plugins.challenge;

import java.util.Locale;

import cz.geokuk.plugins.challenge.Challenge.Typ;

/** Texty pruhu plánovače na mapě; čisté funkce bez závislosti na Swingu. */
public final class StitkyPruhu {

	private StitkyPruhu() {}

	/** Název typu v pruhu. */
	public static String typ(final Typ typ) {
		switch (typ) {
		case DT_MATICE:
			return "D/T";
		case KALENDAR:
			return "Kalendář";
		default:
			return "360°";
		}
	}

	/** Štítek vybraného chybějícího sektoru: {@code Sektor 213° · 5 kandidátů · 2,8 km}; bez kandidátů {@code Sektor 213° · bez kandidátů}. */
	public static String sektor(final int sektor, final int pocetKandidatu, final double vzdalenostM) {
		final String zaklad = "Sektor " + sektor + "°";
		if (pocetKandidatu <= 0) {
			return zaklad + " · bez kandidátů";
		}
		return zaklad + " · " + kandidatu(pocetKandidatu) + " · " + vzdalenost(vzdalenostM);
	}

	/** Počet kandidátů se správným tvarem slova. */
	public static String kandidatu(final int n) {
		final String slovo;
		if (n == 1) {
			slovo = "kandidát";
		} else if (n >= 2 && n <= 4) {
			slovo = "kandidáti";
		} else {
			slovo = "kandidátů";
		}
		return n + " " + slovo;
	}

	/** Vzdálenost: do 995 m v metrech zaokrouhlených na 10, do 99,95 km na desetiny kilometru s čárkou, dál na celé kilometry. */
	public static String vzdalenost(final double metru) {
		if (!(metru >= 0)) {
			return "";
		}
		if (metru < 995) {
			return Math.round(metru / 10) * 10 + " m";
		}
		if (metru < 99_950) {
			return String.format(Locale.ROOT, "%.1f", metru / 1000).replace('.', ',') + " km";
		}
		return Math.round(metru / 1000) + " km";
	}
}
