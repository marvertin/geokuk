package cz.geokuk.plugins.challenge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.geokuk.plugins.challenge.OblastPolygony.Cast;
import cz.geokuk.plugins.challenge.OblastPolygony.Okraj;

/**
 * Soubor hranic oblastí z RÚIAN: řádky {@code oblast KÓD NÁZEV}, {@code okruh N}, N řádků {@code dx dy} a {@code konec}. Souřadnice jsou délka a šířka v setinách
 * tisícin stupně (1e-5°); první bod okruhu je absolutní, další jsou rozdíly proti předchozímu. Řádky s {@code #} jsou komentáře; verze dat se bere z komentáře
 * {@code staženo RRRR-MM-DD}. Okruhy jedné oblasti jsou samostatné části, vnořený okruh (díra) se nepodporuje a čtení ho odmítne.
 */
public final class FormatRuian {

	/** Největší velikost souboru ve znacích. */
	static final int NEJVIC_ZNAKU = 4 << 20;
	static final int NEJVIC_OBLASTI = 64;
	static final int NEJVIC_OKRUHU = 256;
	static final int NEJVIC_BODU_OKRUHU = 200_000;
	private static final int NEJDELSI_RADEK = 80;
	private static final long MERITKO_MAX = 18_000_000L;
	private static final double MERITKO = 1e5;

	private static final Pattern KOD = Pattern.compile("[a-z0-9][a-z0-9-]*");
	private static final Pattern DATUM = Pattern.compile("staženo (\\d{4}-\\d{2}-\\d{2})");

	private FormatRuian() {}

	/** Přečte všechny oblasti souboru podle kódu; chybný nebo příliš velký soubor je {@link IOException}. */
	public static Map<String, OblastPolygony> nacti(final Reader reader) throws IOException {
		final BufferedReader r = new BufferedReader(reader);
		final Map<String, List<Okraj>> okruhy = new LinkedHashMap<>();
		String verze = null;
		String kod = null;
		List<Okraj> aktualni = null;
		long znaku = 0;
		int cislo = 0;
		for (String radek = r.readLine(); radek != null; radek = r.readLine()) {
			cislo++;
			znaku += radek.length() + 1L;
			if (znaku > NEJVIC_ZNAKU) {
				throw new IOException("Soubor oblastí je příliš velký");
			}
			if (radek.length() > NEJDELSI_RADEK && !radek.startsWith("#")) {
				throw new IOException("Příliš dlouhý řádek " + cislo);
			}
			radek = radek.trim();
			if (radek.isEmpty()) {
				continue;
			}
			if (radek.charAt(0) == '#') {
				final Matcher m = DATUM.matcher(radek);
				if (verze == null && m.find()) {
					verze = m.group(1);
				}
				continue;
			}
			try {
				if (radek.startsWith("oblast ")) {
					if (kod != null) {
						throw new IOException("Chybí konec oblasti " + kod);
					}
					final String[] c = radek.split("\\s+", 3);
					if (c.length < 3 || !KOD.matcher(c[1]).matches()) {
						throw new IOException("Neplatná hlavička oblasti");
					}
					if (okruhy.containsKey(c[1]) || okruhy.size() >= NEJVIC_OBLASTI) {
						throw new IOException("Opakovaná nebo příliš mnoho oblastí");
					}
					kod = c[1];
					aktualni = new ArrayList<>();
					okruhy.put(kod, aktualni);
				} else if (radek.startsWith("okruh ")) {
					if (aktualni == null || aktualni.size() >= NEJVIC_OKRUHU) {
						throw new IOException("Okruh mimo oblast nebo příliš mnoho okruhů");
					}
					final int n = Integer.parseInt(radek.substring(6).trim());
					if (n < 3 || n > NEJVIC_BODU_OKRUHU) {
						throw new IOException("Počet bodů okruhu mimo rozsah: " + n);
					}
					aktualni.add(nactiOkruh(r, n));
					cislo += n;
				} else if (radek.equals("konec")) {
					if (kod == null || aktualni.isEmpty()) {
						throw new IOException("Konec bez oblasti nebo oblast bez okruhu");
					}
					kod = null;
					aktualni = null;
				} else {
					throw new IOException("Neznámý řádek");
				}
			} catch (final NumberFormatException e) {
				throw new IOException("Neplatné číslo na řádku " + cislo, e);
			} catch (final IOException e) {
				throw new IOException("Řádek " + cislo + ": " + e.getMessage(), e);
			}
		}
		if (kod != null) {
			throw new IOException("Chybí konec oblasti " + kod);
		}
		if (verze == null) {
			throw new IOException("Soubor oblastí nemá verzi dat (komentář „staženo RRRR-MM-DD“)");
		}
		final Map<String, OblastPolygony> vysledek = new LinkedHashMap<>();
		for (final Map.Entry<String, List<Okraj>> e : okruhy.entrySet()) {
			vysledek.put(e.getKey(), vytvor(e.getKey(), "ruian-" + verze, e.getValue()));
		}
		return Collections.unmodifiableMap(vysledek);
	}

	private static long cislo(final String text) throws IOException {
		try {
			return Long.parseLong(text);
		} catch (final NumberFormatException e) {
			throw new IOException("Neplatné číslo: " + text, e);
		}
	}

	private static Okraj nactiOkruh(final BufferedReader r, final int n) throws IOException {
		final double[] lat = new double[n];
		final double[] lon = new double[n];
		long x = 0;
		long y = 0;
		for (int i = 0; i < n; i++) {
			final String radek = r.readLine();
			if (radek == null || radek.length() > NEJDELSI_RADEK) {
				throw new IOException("Okruh je kratší, než hlásí, nebo má příliš dlouhý řádek");
			}
			final String[] c = radek.trim().split("\\s+");
			if (c.length != 2) {
				throw new IOException("Bod okruhu má mít dvě čísla");
			}
			x += cislo(c[0]);
			y += cislo(c[1]);
			if (Math.abs(x) > MERITKO_MAX || Math.abs(y) > MERITKO_MAX / 2) {
				throw new IOException("Bod okruhu mimo rozsah souřadnic");
			}
			lon[i] = x / MERITKO;
			lat[i] = y / MERITKO;
		}
		try {
			return new Okraj(lat, lon);
		} catch (final IllegalArgumentException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	private static OblastPolygony vytvor(final String kod, final String verze, final List<Okraj> okruhy) throws IOException {
		final List<Cast> casti = new ArrayList<>();
		for (final Okraj o : okruhy) {
			casti.add(new Cast(o, Collections.<Okraj> emptyList()));
		}
		if (casti.size() > 1) {
			for (int i = 0; i < casti.size(); i++) {
				final OblastPolygony ostatni = new OblastPolygony(kod, verze, Collections.singletonList(casti.get(i)));
				for (int j = 0; j < casti.size(); j++) {
					final Okraj o = okruhy.get(j);
					if (i != j && ostatni.obsahuje(o.lat(0), o.lon(0))) {
						throw new IOException("Oblast " + kod + " má vnořený okruh, což se nepodporuje");
					}
				}
			}
		}
		try {
			return new OblastPolygony(kod, verze, casti);
		} catch (final IllegalArgumentException e) {
			throw new IOException("Oblast " + kod + ": " + e.getMessage(), e);
		}
	}
}
