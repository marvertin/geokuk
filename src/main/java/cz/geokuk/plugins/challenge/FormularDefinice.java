package cz.geokuk.plugins.challenge;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import cz.geokuk.plugins.challenge.Challenge.DruhOblasti;
import cz.geokuk.plugins.challenge.Challenge.DruhStredu;
import cz.geokuk.plugins.challenge.Challenge.OblastDef;
import cz.geokuk.plugins.challenge.Challenge.Stred;
import cz.geokuk.plugins.challenge.Challenge.ZdrojPravidel;
import cz.geokuk.plugins.challenge.Challenge.ZdrojeNalezu;

/** Převod mezi definicí challenge a hodnotami formuláře dialogu; hodnoty jsou texty tak, jak je uživatel napsal. */
public final class FormularDefinice {

	/** Volba oblasti ve formuláři: označení v {@link PoskytovatelOblasti}, nebo null pro „bez omezení“. */
	public static final class Oblast {
		public final String id;
		public final String popis;

		Oblast(final String id, final String popis) {
			this.id = id;
			this.popis = popis;
		}

		@Override
		public String toString() {
			return popis;
		}
	}

	private static final Oblast ZADNA = new Oblast(null, "Bez omezení");
	private static final Oblast CESKO = new Oblast(Sablony.STAT_CESKO, "Česká republika");
	private static final Oblast PRAHA = new Oblast(Sablony.PRAHA, "Praha a okolí");
	private static final Pattern KOD_LISTINGU = Pattern.compile("GC[0-9A-Z]{2,7}");

	private FormularDefinice() {}

	public static List<Oblast> oblasti() {
		return Collections.unmodifiableList(Arrays.asList(ZADNA, CESKO, PRAHA));
	}

	/** Volba oblasti pro definici; oblast, kterou formulář nezná (sada okresů), se mapuje na volbu se stejným označením, jinak na „bez omezení“. */
	public static Oblast oblastDefinice(final Challenge c) {
		final OblastDef o = c.oblast();
		if (o.druh() == DruhOblasti.ZADNA || o.idOblasti().size() != 1) {
			return ZADNA;
		}
		for (final Oblast v : oblasti()) {
			if (o.idOblasti().get(0).equals(v.id)) {
				return v;
			}
		}
		return ZADNA;
	}

	/** Má formulář definici úplně vyjádřit? Ne, když má oblast, kterou nezná (uživatelova sada okresů); pak se oblast při uložení nesmí tiše změnit. */
	public static boolean zachovaOblast(final Challenge c) {
		final OblastDef o = c.oblast();
		return o.druh() == DruhOblasti.ZADNA || o.idOblasti().size() == 1 && oblastDefinice(c).id != null;
	}

	/** Souřadnice z textu: tečka nebo čárka jako desetinná čárka; mimo rozsah je {@link ChallengeChyba}. */
	static double souradnice(final String text, final String co, final double nejmene, final double nejvice) {
		final String t = text == null ? "" : text.trim().replace(',', '.');
		final double d;
		try {
			d = Double.parseDouble(t);
		} catch (final NumberFormatException e) {
			throw new ChallengeChyba(co + " musí být číslo.");
		}
		if (Double.isNaN(d) || d < nejmene || d > nejvice) {
			throw new ChallengeChyba(co + " musí být mezi " + (int) nejmene + " a " + (int) nejvice + ".");
		}
		return d;
	}

	/**
	 * Definice z hodnot formuláře. Souřadnice a kód listingu se čtou jen u druhu středu, který je potřebuje. Chybný vstup je {@link ChallengeChyba} se zprávou pro uživatele.
	 *
	 * @param puvodni definice, ze které formulář vychází; z ní se přebírá typ a oblast, kterou formulář neumí vyjádřit; bez ní vzniká typ 360°
	 */
	public static Challenge sestav(final Challenge puvodni, final String nazev, final ZdrojPravidel pravidla, final DruhStredu druhStredu, final String lat, final String lon, final String kod,
			final Oblast oblast, final ZdrojeNalezu nalezy) {
		if (puvodni != null && puvodni.typ() == Challenge.Typ.DT_MATICE) {
			// D/T se počítá z pevných pravidel podle středu mapy: z formuláře se bere jen název a zdroj nálezů, ostatní zůstává, jak bylo.
			return new Challenge(nazev, Challenge.Typ.DT_MATICE, puvodni.stred(), puvodni.oblast(), puvodni.zdroj(), nalezy);
		}
		final Stred stred;
		switch (druhStredu) {
		case DOMOV:
			stred = Stred.domov();
			break;
		case SOURADNICE:
			stred = Stred.souradnice(souradnice(lat, "Zeměpisná šířka", -90, 90), souradnice(lon, "Zeměpisná délka", -180, 180));
			break;
		default:
			final String k = kod == null ? "" : kod.trim().toUpperCase(java.util.Locale.ROOT);
			if (!KOD_LISTINGU.matcher(k).matches()) {
				throw new ChallengeChyba("Kód keše musí vypadat jako GC1234.");
			}
			stred = Stred.listing(k);
			break;
		}
		final OblastDef def;
		if (puvodni != null && !zachovaOblast(puvodni) && oblast == oblastDefinice(puvodni)) {
			def = puvodni.oblast();
		} else if (oblast.id == null) {
			def = OblastDef.zadna();
		} else if (Sablony.PRAHA.equals(oblast.id)) {
			def = OblastDef.sada(oblast.id);
		} else {
			def = OblastDef.stat(oblast.id);
		}
		return new Challenge(nazev, puvodni == null ? Challenge.Typ.SEKTORY_360 : puvodni.typ(), stred, def, pravidla, nalezy);
	}

	private static String typ(final Challenge.Typ t) {
		switch (t) {
		case DT_MATICE:
			return "D/T";
		case KALENDAR:
			return "Kalendář";
		default:
			return "360°";
		}
	}

	/** Jednořádkový popis definice pro seznam a tooltip. */
	public static String souhrn(final Challenge c) {
		final String stred;
		switch (c.stred().druh()) {
		case DOMOV:
			stred = "domov";
			break;
		case SOURADNICE:
			stred = String.format(java.util.Locale.ROOT, "%.5f, %.5f", c.stred().lat(), c.stred().lon());
			break;
		default:
			stred = c.stred().kodListingu();
			break;
		}
		if (c.typ() == Challenge.Typ.DT_MATICE) {
			return "D/T · matice obtížnost × terén podle keší na mapě";
		}
		final String pravidla = c.zdroj() == ZdrojPravidel.PGC ? "Project-GC" : "Stator";
		return typ(c.typ()) + " · " + pravidla + " · střed: " + stred + " · oblast: " + oblastDefinice(c).popis.toLowerCase(java.util.Locale.ROOT);
	}
}
