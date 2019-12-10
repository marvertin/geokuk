package cz.geokuk.plugins.kesoid.mapicon;

import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;

/**
 * Převod legacy jmen alel na kvalifikvoaná jména jménem genu.
 * Do tohot seznamu se nic nebude přidávat, je jen z důvodu zpětné kompatibility.
 * @author Martin
 *
 */
public class LegacyAlelaNames {

	private static Map<String, String> prekladMap = new HashMap<>();


	private static String V = "";

	static {
		gen("sym")
		.alela(V, "Waypoint");

		gen("mouse")
		.alela(V, "nomouse")
		.alela("mousean")
		.alela("mouseon");

		gen("postaveni")
		.alela(V, "h")
		.alela("v");

		gen("druh")
		.alela(V, "00")
		.alela("gc")
		.alela("wm")
		.alela("mz")
		.alela("pic")
		.alela("gb")
		.alela("wp");

		gen("vztah")
		.alela(V, "hnf")
		.alela("fnd")
		.alela("own")
		.alela("not")
		.alela("cpt")
		.alela("dpl");

		gen("stav")
		.alela("actv")
		.alela("dsbl")
		.alela("arch");

		gen("velikost")
		.alela(V, "00000")
		.alela("nlist")
		.alela("micro")
		.alela("small")
		.alela("regul")
		.alela("large")
		.alela("hugex")
		.alela("virtu")
		.alela("other");

		gen("vylet")
		.alela(V, "nevime")
		.alela("lovime")
		.alela("ignoru");

		gen("cesty")
		.alela(V, "mimocesticu")
		.alela("nacestejsou");

		gen("vybranost")
		.alela(V, "noselect")
		.alela("selected");

		gen("vylustenost")
		.alela(V, "nevyluste")
		.alela("vylusteno");

		gen("zdroj")
		.alela(V, "pqimported")
		.alela("handedited");

		gen("ter")
		.alela(V, "ter0")
		.alela("10", "ter10")
		.alela("15", "ter15")
		.alela("20", "ter20")
		.alela("25", "ter25")
		.alela("30", "ter30")
		.alela("35", "ter35")
		.alela("40", "ter40")
		.alela("45", "ter45")
		.alela("50", "ter50");

		gen("dif")
		.alela(V, "dif0")
		.alela("10", "dif10")
		.alela("15", "dif15")
		.alela("20", "dif20")
		.alela("25", "dif25")
		.alela("30", "dif30")
		.alela("35", "dif35")
		.alela("40", "dif40")
		.alela("45", "dif45")
		.alela("50", "dif50");
	}

	public static String preloz(final String nazev) {
		if (nazev.contains("-")) {
			return nazev;
		} else {
			final String novy = prekladMap.get(nazev);
			if (novy == null) {
				throw new IllegalArgumentException("Nazev '" + nazev + "' není platným legacy jednoduchý názvem alely, použij kvalifkovaný název 'gen-alela', nebo platný legacy název");
			}
			return novy;
		}
	}

	@RequiredArgsConstructor
	private static class X {

		private final String nazevGenu;

		public X alela(final String staryNazev) {
			alela(staryNazev, staryNazev);
			return this;
		}

		public X alela(final String novyNazevAlely, final String staryNazev) {
			// TODO připojit nový název alely, až budou nové názvy všude promítnuty
			prekladMap.put(staryNazev, nazevGenu + "-" + staryNazev);
			return this;
		}

	}

	private static X gen(final String nazevGenu) {
		return new X(nazevGenu);
	}


}
