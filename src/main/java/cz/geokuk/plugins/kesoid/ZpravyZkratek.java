package cz.geokuk.plugins.kesoid;

import java.util.List;

/** Texty krátkých zpráv po zkratkách, jejichž výsledek není na první pohled vidět. Metody vracejí null, když zpráva není potřeba. */
public final class ZpravyZkratek {

	private ZpravyZkratek() {}

	/** F8 nebo Alt+F8 zapnuly kruhy, které se přesto nekreslí. */
	private static String zapnuteAleSkryte(final String kdo, final StavVrstev.Skryta skryta, final VrstvaNadMapou vrstva) {
		return kdo + ": " + StavVrstev.textDuvodu(vrstva, skryta.getDuvod(), skryta.getLimit());
	}

	/** F8: zpráva jen když zapnuté kruhy nejsou vidět. */
	public static String zvyraznovaciKruhy(final boolean zapnuto, final StavVrstev.Skryta skryta) {
		return zapnuto && skryta != null ? zapnuteAleSkryte("Kruhy zapnuté, ale skryté", skryta, VrstvaNadMapou.ZVYRAZNOVACI_KRUHY) : null;
	}

	/** Alt+F8: zapnutí bez zapnutého F8 zapne obojí; skryté kruhy mají přednost. */
	public static String jednotkoveKruhy(final boolean zapnuto, final boolean zvyrazneniBylyZapnute, final StavVrstev.Skryta skryta) {
		if (!zapnuto) {
			return null;
		}
		if (skryta != null) {
			return zapnuteAleSkryte("Kruhy zapnuté, ale skryté", skryta, VrstvaNadMapou.JEDNOTKOVE_KRUHY);
		}
		return zvyrazneniBylyZapnute ? null : "Zapnuté zvýrazňovací kruhy s poloměrem v metrech";
	}

	/** F5. */
	public static String kesoidy(final boolean zapnuto) {
		return zapnuto ? "Kešoidy zobrazené" : "Kešoidy skryté včetně kruhů, obsazenosti a popisků";
	}

	/** F9: v příliš malém měřítku se Obsazenost nekreslí. */
	public static String obsazenost(final boolean zapnuto, final StavVrstev.Skryta skryta) {
		if (!zapnuto || skryta == null) {
			return null;
		}
		if (skryta.getDuvod() == DuvodNekresleni.POLOMER_PRILIS_MALY) {
			return "Obsazenost se kreslí až po přiblížení mapy";
		}
		return zapnuteAleSkryte("Obsazenost zapnutá, ale skrytá", skryta, VrstvaNadMapou.OBSAZENOST);
	}

	/** Ctrl+1 až Ctrl+4: které zdroje keší jsou po přepnutí zapnuté (i částečně). */
	public static String zdroje(final List<String> zapnute) {
		if (zapnute.isEmpty()) {
			return "Zdroje keší: žádný";
		}
		return (zapnute.size() == 1 ? "Zdroj keší: " : "Zdroje keší: ") + String.join(", ", zapnute);
	}
}
