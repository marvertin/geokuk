package cz.geokuk.plugins.challenge;

import java.util.Locale;

/** Texty značek plánovače na mapě. */
final class TextyKresleni {

	private TextyKresleni() {
	}

	/** Vzdálenost: „850 m“, „2,8 km“, „25 km“. */
	static String vzdalenost(final double metru) {
		if (Double.isNaN(metru)) {
			return "";
		}
		if (metru < 1000) {
			return Math.round(metru) + " m";
		}
		if (metru < 10_000) {
			return String.format(Locale.ROOT, "%.1f", metru / 1000).replace('.', ',') + " km";
		}
		return Math.round(metru / 1000) + " km";
	}

	static String kandidatu(final int pocet) {
		if (pocet == 1) {
			return "1 kandidát";
		}
		return pocet + (pocet >= 2 && pocet <= 4 ? " kandidáti" : " kandidátů");
	}

	/** Štítek vybraného sektoru: „Sektor 213° · 5 kandidátů · 2,8 km“. */
	static String vybranySektor(final int sektor, final int pocet, final double metru) {
		return "Sektor " + sektor + "° · " + kandidatu(pocet) + (Double.isNaN(metru) ? "" : " · " + vzdalenost(metru));
	}

	/** Štítek vybrané keše: „Sektor 213° · 5 kandidátů · 46 km“, u sektoru bez kandidátů jen „Sektor 213°“. */
	static String sektorVybrane(final int sektor, final PrehledChallenge prehled) {
		final Object zdroj = prehled.zdroj();
		final VysledekSektoru.Kandidati k = zdroj instanceof VysledekSektoru ? ((VysledekSektoru) zdroj).kandidati(sektor) : null;
		return k != null && k.pocet() > 0 ? vybranySektor(sektor, k.pocet(), k.nejblizsiVzdalenost()) : "Sektor " + sektor + "°";
	}

	/** Štítek středu: „Střed: domov“, „Střed: GC56R49“. */
	static String stred(final String popis) {
		return "Střed: " + popis;
	}

	/** Šipka u okraje mapy: „Střed: domov · 25 km“. */
	static String stredMimoVyrez(final String popis, final double metru) {
		return stred(popis) + " · " + vzdalenost(metru);
	}
}
