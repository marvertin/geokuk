package cz.geokuk.plugins.challenge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import cz.geokuk.plugins.challenge.OblastPolygony.Cast;
import cz.geokuk.plugins.challenge.OblastPolygony.Okraj;

/**
 * Kompaktní textový formát oblasti: první řádek {@code GKOBL 1 id verze}, potom pro každou část řádek {@code C} a za ním řádky {@code O} (vnější okraj) a
 * {@code D} (díry). Okraj jsou celá čísla v setinách tisícin stupně (1e-5°, asi metr): první šířka a délka absolutně, další body jako rozdíly proti předchozímu.
 */
public final class FormatOblasti {

	private static final double MERITKO = 1e5;

	private FormatOblasti() {}

	public static OblastPolygony nacti(final Reader reader) throws IOException {
		final BufferedReader r = new BufferedReader(reader);
		final String hlavicka = r.readLine();
		if (hlavicka == null) {
			throw new IOException("Prázdná oblast");
		}
		final String[] h = hlavicka.trim().split("\\s+");
		if (h.length != 4 || !h[0].equals("GKOBL") || !h[1].equals("1")) {
			throw new IOException("Neznámý formát oblasti: " + hlavicka);
		}
		final List<Cast> casti = new ArrayList<>();
		Okraj vnejsi = null;
		List<Okraj> diry = new ArrayList<>();
		for (String radek = r.readLine(); radek != null; radek = r.readLine()) {
			radek = radek.trim();
			if (radek.isEmpty() || radek.startsWith("#")) {
				continue;
			}
			final char druh = radek.charAt(0);
			if (druh == 'C') {
				if (vnejsi != null) {
					casti.add(vytvorCast(vnejsi, diry));
				}
				vnejsi = null;
				diry = new ArrayList<>();
			} else if (druh == 'O' || druh == 'D') {
				final Okraj okraj;
				try {
					okraj = nactiOkraj(radek.substring(1));
				} catch (final IllegalArgumentException e) {
					throw new IOException(e.getMessage(), e);
				}
				if (druh == 'O') {
					if (vnejsi != null) {
						throw new IOException("Část má víc vnějších okrajů");
					}
					vnejsi = okraj;
				} else {
					diry.add(okraj);
				}
			} else {
				throw new IOException("Neznámý řádek oblasti: " + radek);
			}
		}
		if (vnejsi != null) {
			casti.add(vytvorCast(vnejsi, diry));
		}
		if (casti.isEmpty()) {
			throw new IOException("Oblast " + h[2] + " nemá žádnou část");
		}
		try {
			return new OblastPolygony(h[2], h[3], casti);
		} catch (final IllegalArgumentException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	private static Cast vytvorCast(final Okraj vnejsi, final List<Okraj> diry) throws IOException {
		try {
			return new Cast(vnejsi, diry);
		} catch (final IllegalArgumentException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

	private static Okraj nactiOkraj(final String text) throws IOException {
		final String[] c = text.trim().split("\\s+");
		if (c.length < 6 || c.length % 2 != 0) {
			throw new IOException("Okraj má mít aspoň 3 body (sudý počet čísel)");
		}
		final int n = c.length / 2;
		final double[] lat = new double[n];
		final double[] lon = new double[n];
		long la = 0;
		long lo = 0;
		try {
			for (int i = 0; i < n; i++) {
				la += Long.parseLong(c[2 * i]);
				lo += Long.parseLong(c[2 * i + 1]);
				lat[i] = la / MERITKO;
				lon[i] = lo / MERITKO;
			}
		} catch (final NumberFormatException e) {
			throw new IOException("Neplatné číslo v okraji oblasti: " + e.getMessage(), e);
		}
		return new Okraj(lat, lon);
	}

	public static void zapis(final OblastPolygony oblast, final String verzeDat, final Writer out) throws IOException {
		out.write("GKOBL 1 " + oblast.id() + " " + verzeDat + "\n");
		for (final Cast c : oblast.casti()) {
			out.write("C\n");
			boolean vnejsi = true;
			for (final Okraj o : c.okraje()) {
				out.write(vnejsi ? "O" : "D");
				vnejsi = false;
				long pla = 0;
				long plo = 0;
				for (int i = 0; i < o.pocetBodu(); i++) {
					final long la = Math.round(o.lat(i) * MERITKO);
					final long lo = Math.round(o.lon(i) * MERITKO);
					out.write(" " + (la - pla) + " " + (lo - plo));
					pla = la;
					plo = lo;
				}
				out.write("\n");
			}
		}
	}
}
