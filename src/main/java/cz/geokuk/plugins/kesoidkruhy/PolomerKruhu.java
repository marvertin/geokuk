package cz.geokuk.plugins.kesoidkruhy;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Locale;

/** Poloměr jednotkových kruhů v metrech: přednastavené hodnoty posuvníku, převod z textu a do textu, přepočet na pixely. */
final class PolomerKruhu {

	static final int VYCHOZI_METRU = 100;
	static final int MIN_METRU = 1;
	static final int MAX_METRU = 50_000;
	/** Menší kruh se nekreslí. */
	static final int MIN_PIXLU = 3;
	/** Hodnoty, ke kterým se posuvník přichytává. */
	static final int[] PRESETY = { 10, 25, 50, 100, 250, 500, 1000, 2000, 5000 };

	private static final Locale CS = new Locale("cs");

	private PolomerKruhu() {}

	/** Poloměr v pixelech, při přiblížení plynule bez horní hranice; 0, když je kruh příliš malý ke kreslení. */
	static double pixlu(final int metru, final double pixluNaMetr) {
		final double r = metru * pixluNaMetr;
		return r < MIN_PIXLU ? 0 : r;
	}

	/** Jak se kruh nakreslí do výřezu. */
	enum Kresleni {
		/** Kruh výřez nezasahuje. */
		NIC,
		/** Výřez leží celý uvnitř kruhu, okraj není vidět, stačí vyplnit výřez. */
		VYPLN_VYREZ,
		/** Okraj kruhu protíná výřez. */
		KRUH
	}

	/** Co s kruhem o středu ({@code sx}, {@code sy}) a poloměru {@code r} ve výřezu daném rohy {@code x1, y1, x2, y2}. */
	static Kresleni kresleni(final double sx, final double sy, final double r, final double x1, final double y1, final double x2, final double y2) {
		final double nejblizsiX = Math.max(x1, Math.min(sx, x2));
		final double nejblizsiY = Math.max(y1, Math.min(sy, y2));
		final double dnx = nejblizsiX - sx;
		final double dny = nejblizsiY - sy;
		if (dnx * dnx + dny * dny > r * r) {
			return Kresleni.NIC;
		}
		final double nejdalnejsiX = Math.max(Math.abs(x1 - sx), Math.abs(x2 - sx));
		final double nejdalnejsiY = Math.max(Math.abs(y1 - sy), Math.abs(y2 - sy));
		return nejdalnejsiX * nejdalnejsiX + nejdalnejsiY * nejdalnejsiY <= r * r ? Kresleni.VYPLN_VYREZ : Kresleni.KRUH;
	}

	/**
	 * Rozpočet na součet ploch kruhů s hranou ve výřezu, v pixelech. Vyplnění průhledné plochy stojí kolem 10 ns na pixel, takže 15 milionů pixelů je řádově desetiny sekundy
	 * i s dotazem do indexu a výběrem kruhů, zhruba sedm kruhů velkých jako celé okno 1920×1080.
	 */
	static final long ROZPOCET_PLOCHY = 15_000_000L;
	/** Nejvíc kruhů s hranou ve výřezu bez ohledu na plochu; i malý kruh má režii (tvar, průhlednost, bílý okraj). */
	static final int MAX_CASTECNYCH_KRUHU = 2000;

	/**
	 * Kolik kruhů o poloměru {@code r} px s hranou ve výřezu se smí nakreslit. Cena kruhu roste s jeho plochou ve výřezu, omezenou plochou výřezu, takže malé kruhy se kreslí
	 * všechny (až {@link #MAX_CASTECNYCH_KRUHU}) a velké nejvýš pár. Vždy aspoň jeden.
	 */
	static int limitKruhu(final double r, final double plochaVyrezu, final long rozpocet) {
		final double plocha = Math.max(1, Math.min(Math.PI * r * r, plochaVyrezu));
		return (int) Math.max(1, Math.min(MAX_CASTECNYCH_KRUHU, Math.floor(rozpocet / plocha)));
	}

	static int limitKruhu(final double r, final double plochaVyrezu) {
		return limitKruhu(r, plochaVyrezu, ROZPOCET_PLOCHY);
	}

	/** Nejvýš {@code limit} bodů nejbližších bodu ({@code cx}, {@code cy}), od nejbližšího; při menším počtu všechny beze změny pořadí. Výběr je O(n log limit), ne řazení všech. */
	static List<int[]> nejblizsi(final List<int[]> body, final double cx, final double cy, final int limit) {
		if (limit <= 0) {
			return new ArrayList<>();
		}
		if (body.size() <= limit) {
			return body;
		}
		// Halda s nejvzdálenějším vybraným bodem navrchu: nový bod ji vytlačí, jen když je blíž.
		final PriorityQueue<int[]> halda = new PriorityQueue<>(limit + 1, (a, b) -> Double.compare(vzdalenost2(b, cx, cy), vzdalenost2(a, cx, cy)));
		for (final int[] b : body) {
			if (halda.size() < limit) {
				halda.add(b);
			} else if (vzdalenost2(b, cx, cy) < vzdalenost2(halda.peek(), cx, cy)) {
				halda.poll();
				halda.add(b);
			}
		}
		final List<int[]> vybrane = new ArrayList<>(halda);
		vybrane.sort((a, b) -> Double.compare(vzdalenost2(a, cx, cy), vzdalenost2(b, cx, cy)));
		return vybrane;
	}

	private static double vzdalenost2(final int[] b, final double cx, final double cy) {
		final double dx = b[0] - cx;
		final double dy = b[1] - cy;
		return dx * dx + dy * dy;
	}

	/** Výsledná průhlednost, když se přes sebe položí {@code pocet} vrstev stejné barvy s krytím {@code alfa} (0 až 255). */
	static int slozAlfu(final int alfa, final int pocet) {
		if (pocet <= 0) {
			return 0;
		}
		return (int) Math.round(255 * (1 - Math.pow(1 - alfa / 255.0, pocet)));
	}

	/** Poloha posuvníku nejbližší hodnotě (v logaritmické míře, hodnota mimo presety dostane sousední pozici). */
	static int pozice(final int metru) {
		int nejblizsi = 0;
		double nejmensi = Double.MAX_VALUE;
		for (int i = 0; i < PRESETY.length; i++) {
			final double rozdil = Math.abs(Math.log((double) Math.max(metru, 1) / PRESETY[i]));
			if (rozdil < nejmensi) {
				nejmensi = rozdil;
				nejblizsi = i;
			}
		}
		return nejblizsi;
	}

	static int metru(final int pozice) {
		return PRESETY[Math.max(0, Math.min(PRESETY.length - 1, pozice))];
	}

	/** Hodnota z pole: číslo s volitelnou jednotkou m nebo km, čárka i tečka; mimo rozsah nebo nesrozumitelné vrátí 0. */
	static int parsuj(final String text) {
		if (text == null) {
			return 0;
		}
		String t = text.trim().toLowerCase(CS).replace(' ', ' ').replace(',', '.');
		double nasobek = 1;
		if (t.endsWith("km")) {
			nasobek = 1000;
			t = t.substring(0, t.length() - 2).trim();
		} else if (t.endsWith("m")) {
			t = t.substring(0, t.length() - 1).trim();
		}
		if (!t.matches("\\d+(\\.\\d+)?")) {
			return 0;
		}
		final double metru;
		try {
			metru = Double.parseDouble(t) * nasobek;
		} catch (final NumberFormatException e) {
			return 0;
		}
		final long zaokrouhleno = Math.round(metru);
		if (zaokrouhleno < MIN_METRU || zaokrouhleno > MAX_METRU) {
			return 0;
		}
		return (int) zaokrouhleno;
	}

	static final int MIN_PIXLU_VELIKOST = 0;
	static final int MAX_PIXLU_VELIKOST = 100;

	/** Velikost kruhu v pixelech z pole, s volitelným „px“; mimo rozsah nebo nesrozumitelné vrátí -1. */
	static int parsujPixely(final String text) {
		if (text == null) {
			return -1;
		}
		String t = text.trim().toLowerCase(CS);
		if (t.endsWith("px")) {
			t = t.substring(0, t.length() - 2).trim();
		}
		if (!t.matches("\\d{1,4}")) {
			return -1;
		}
		try {
			final int px = Integer.parseInt(t);
			return px > MAX_PIXLU_VELIKOST ? -1 : px;
		} catch (final NumberFormatException e) {
			return -1;
		}
	}

	static String formatPixely(final int pixlu) {
		return pixlu + " px";
	}

	/** Text do pole: do 999 m v metrech, od 1 km v kilometrech s desetinnou čárkou. */
	static String format(final int metru) {
		if (metru < 1000) {
			return metru + " m";
		}
		final NumberFormat nf = NumberFormat.getNumberInstance(CS);
		nf.setMaximumFractionDigits(3);
		nf.setGroupingUsed(false);
		return nf.format(metru / 1000.0) + " km";
	}
}
