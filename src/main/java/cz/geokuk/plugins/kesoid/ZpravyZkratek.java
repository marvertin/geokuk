package cz.geokuk.plugins.kesoid;

import java.util.List;

import cz.geokuk.plugins.kesoid.importek.StavZdroju.StavVyberu;

/**
 * Texty krátkých zpráv po zkratkách; vždy „akce – stav“, případně s parametrem akce (velikost, poloměr), a to při zapnutí i vypnutí, aby bylo vidět, co zkratka dělá a že jde přepnout.
 * Když je zapnutá vrstva přesto skrytá, zpráva řekne proč.
 */
public final class ZpravyZkratek {

	static final String ZAPNUTO = "Zapnuto";
	static final String VYPNUTO = "Vypnuto";
	private static final String ODDELOVAC = " – ";

	private ZpravyZkratek() {}

	/** „Akce – Zapnuto, parametr“ nebo „Akce – Vypnuto, parametr“; parametr je volitelný. */
	static String akceStav(final String akce, final boolean zapnuto, final String parametr) {
		return akce + ODDELOVAC + (zapnuto ? ZAPNUTO : VYPNUTO) + (parametr == null || parametr.isEmpty() ? "" : ", " + parametr);
	}

	/** Zapnutá vrstva, která se přesto nekreslí: za stav se přidá důvod. */
	private static String sDuvodem(final String zprava, final boolean zapnuto, final StavVrstev.Skryta skryta, final VrstvaNadMapou vrstva) {
		return zapnuto && skryta != null ? zprava + ", ale skryté: " + StavVrstev.textDuvodu(vrstva, skryta.getDuvod(), skryta.getLimit()) : zprava;
	}

	/** F8; {@code velikost} je velikost kruhu, třeba „20 px“ nebo „161 m“. */
	public static String zvyraznovaciKruhy(final boolean zapnuto, final String velikost, final StavVrstev.Skryta skryta) {
		return sDuvodem(akceStav("Zvýrazňovací kruhy", zapnuto, velikost), zapnuto, skryta, VrstvaNadMapou.ZVYRAZNOVACI_KRUHY);
	}

	/** Alt+F8: zapnutí bez zapnutého F8 zapne obojí; {@code polomer} je poloměr kruhu, třeba „161 m“. */
	public static String jednotkoveKruhy(final boolean zapnuto, final boolean zvyrazneniBylyZapnute, final String polomer, final StavVrstev.Skryta skryta) {
		final String zprava = akceStav("Kruhy v metrech", zapnuto, polomer);
		final String sDuvodem = sDuvodem(zprava, zapnuto, skryta, VrstvaNadMapou.JEDNOTKOVE_KRUHY);
		return zapnuto && !zvyrazneniBylyZapnute ? sDuvodem + " (zapnuly se i zvýrazňovací kruhy)" : sDuvodem;
	}

	/** F5. */
	public static String kesoidy(final boolean zapnuto) {
		return akceStav("Kešoidy", zapnuto, zapnuto ? null : "včetně kruhů, obsazenosti a popisků (F5 zapne)");
	}

	/** F9; v příliš malém měřítku se Obsazenost nekreslí; {@code polomer} je poloměr kruhu, třeba „161 m“. */
	public static String obsazenost(final boolean zapnuto, final String polomer, final StavVrstev.Skryta skryta) {
		final String zprava = akceStav("Obsazenost", zapnuto, polomer);
		if (!zapnuto || skryta == null) {
			return zprava;
		}
		if (skryta.getDuvod() == DuvodNekresleni.POLOMER_PRILIS_MALY) {
			return zprava + ", ale kreslí se až po přiblížení mapy";
		}
		return sDuvodem(zprava, zapnuto, skryta, VrstvaNadMapou.OBSAZENOST);
	}

	/** Ctrl+1 až Ctrl+4: zdroj, který se přepnul, jeho stav po přepnutí a zdroje, které jsou po přepnutí zapnuté (i částečně). */
	public static String zdroj(final String nazevZdroje, final StavVyberu stav, final List<String> zapnute) {
		final String akce = "Zdroj keší " + nazevZdroje;
		final String zprava = stav == StavVyberu.CASTECNE ? akceStav(akce, true, "částečně") : akceStav(akce, stav == StavVyberu.ZAPNUTO, null);
		return zapnute.isEmpty() ? zprava + ", žádný zdroj keší není zapnutý" : zprava;
	}

	/** F4: filtr plánovače challenge „jen kandidáti“. */
	public static String challengeJenKandidati(final boolean zapnuto) {
		return akceStav("Challenge: jen kandidáti", zapnuto, null);
	}

	/** Shift+šipka, když ve směru šipky žádný kandidát challenge není; {@code smer} je třeba „na sever“. */
	public static String challengeZadnyKandidat(final String smer) {
		return "Challenge – žádný kandidát " + smer;
	}

	/** Ctrl+1 až Ctrl+4 u zdroje, který nemá žádné načtené soubory a proto jde vypnutý. */
	public static String zdrojBezSouboru(final String nazevZdroje) {
		return "Zdroj keší " + nazevZdroje + " – nemá načtené soubory";
	}
}
