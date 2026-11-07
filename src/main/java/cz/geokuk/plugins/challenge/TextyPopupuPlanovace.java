package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Texty popupu plánovače challenge: shrnutí, řádky chybějících sektorů a upozornění na nejisté sektory. */
final class TextyPopupuPlanovace {

	private TextyPopupuPlanovace() {}

	/** „1 kandidát“, „2 kandidáti“, „5 kandidátů“. */
	static String kandidatu(final int n) {
		return n + " " + tvar(n, "kandidát", "kandidáti", "kandidátů");
	}

	/** Vzdálenost na desetiny kilometru, pod kilometr v metrech. */
	static String vzdalenost(final double metru) {
		if (metru < 1000) {
			return Math.round(metru) + " m";
		}
		return String.format(Locale.forLanguageTag("cs"), "%.1f km", metru / 1000);
	}

	/** „213°  5 kandidátů  nejbližší 2,8 km“; bez kandidátů s pomlčkou místo vzdálenosti. */
	static String radekSektoru(final int sektor, final VysledekSektoru.Kandidati k) {
		final int pocet = k == null ? 0 : k.pocet();
		final String nejblizsi = pocet == 0 ? "–" : "nejbližší " + vzdalenost(k.nejblizsiVzdalenost());
		return sektor + "°  " + kandidatu(pocet) + "  " + nejblizsi;
	}

	static String nadpis(final Challenge c) {
		return c.nazev();
	}

	static String souhrn(final VysledekSektoru v) {
		final int splneno = v.pocetObsazenych();
		return "Splněno " + splneno + " z 360 (" + procenta(v) + "), chybí " + (360 - splneno);
	}

	static String procenta(final VysledekSektoru v) {
		return Math.round(v.pocetObsazenych() * 100.0 / 360) + " %";
	}

	static String souhrn(final VysledekPolicek p) {
		return "Splněno " + (p.celkem() - p.pocetChybejicich()) + " z " + p.celkem() + " (" + procenta(p) + "), chybí " + p.pocetChybejicich();
	}

	static String procenta(final VysledekPolicek p) {
		return Math.round((p.celkem() - p.pocetChybejicich()) * 100.0 / Math.max(1, p.celkem())) + " %";
	}

	static String nadpisSeznamu(final Challenge.Typ typ) {
		switch (typ) {
		case DT_MATICE:
			return "Chybějící políčka D/T:";
		case KALENDAR:
			return "Chybějící dny:";
		default:
			return "Chybějící sektory (podle vzdálenosti nejbližšího kandidáta):";
		}
	}

	/** Když strop štítků na mapě zkrátil kandidáty ve výřezu: „Štítky na mapě: 200 nejbližších z 1 234“; jinak mezera. */
	static String omezeniStitku(final int veVyrezu) {
		if (veVyrezu <= TvarPlanovace.NEJVIC_ZNACEK) {
			return " ";
		}
		return String.format(Locale.ROOT, "Štítky na mapě: %d nejbližších z %,d", TvarPlanovace.NEJVIC_ZNACEK, veVyrezu).replace(',', '\u00a0');
	}

	static String pravidla(final Challenge.ZdrojPravidel z) {
		return z == Challenge.ZdrojPravidel.STATOR ? "Stator" : "Project-GC";
	}

	static String stred(final Challenge.Stred s) {
		switch (s.druh()) {
		case DOMOV:
			return "domov";
		case LISTING:
			return "výchozí bod " + s.kodListingu();
		default:
			return String.format(Locale.ROOT, "%.5f, %.5f", s.lat(), s.lon());
		}
	}

	/** Kolik sektorů je na hraně; prázdný text, když žádný. */
	static String nejiste(final VysledekSektoru v) {
		int n = 0;
		for (int s = 0; s < 360; s++) {
			if (v.jeNejisty(s)) {
				n++;
			}
		}
		if (n == 0) {
			return "";
		}
		final String sektoru = n + " " + tvar(n, "sektor je", "sektory jsou", "sektorů je");
		return sektoru + " na hraně (±0,05°), " + (n == 1 ? "počítá se jako nejistý." : "počítají se jako nejisté.");
	}

	/** Chybějící sektory: s kandidáty od nejbližšího, za nimi sektory bez kandidátů vzestupně. */
	static List<Integer> serazeneChybejici(final VysledekSektoru v) {
		final List<Integer> s = new ArrayList<>(v.chybejici());
		s.sort(Comparator.comparingDouble((Integer sektor) -> {
			final VysledekSektoru.Kandidati k = v.kandidati(sektor);
			return k == null || k.pocet() == 0 ? Double.MAX_VALUE : k.nejblizsiVzdalenost();
		}).thenComparingInt(sektor -> sektor));
		return s;
	}

	private static String tvar(final int n, final String jeden, final String dvaAzCtyri, final String jinak) {
		if (n == 1) {
			return jeden;
		}
		return n >= 2 && n <= 4 ? dvaAzCtyri : jinak;
	}
}
