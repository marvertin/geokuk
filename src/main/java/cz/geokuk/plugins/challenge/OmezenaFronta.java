package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/** Drží nejvýš {@code max} nejlepších kandidátů: nejdřív s nejbližším datem, při shodě nejblíž referenčnímu bodu. */
final class OmezenaFronta {

	/** Kandidát s počtem dní do jeho data (0 bez času) a vzdáleností od referenčního bodu v metrech. */
	static final class Kandidat {
		final VstupKes keska;
		final int dni;
		final double vzdalenost;

		Kandidat(final VstupKes keska, final int dni, final double vzdalenost) {
			this.keska = keska;
			this.dni = dni;
			this.vzdalenost = vzdalenost;
		}
	}

	/** Datum, vzdálenost a při úplné shodě kód keše, takže výběr nezávisí na pořadí vstupu. */
	private static final Comparator<Kandidat> PORADI = Comparator.comparingInt((final Kandidat k) -> k.dni).thenComparingDouble(k -> k.vzdalenost)
			.thenComparing((final Kandidat k) -> k.keska.id());

	private final int max;
	private final PriorityQueue<Kandidat> nejhorsiNahore = new PriorityQueue<>(PORADI.reversed());

	OmezenaFronta(final int max) {
		this.max = max;
	}

	/** Keš s neplatnými souřadnicemi se nepřidá. */
	void pridej(final VstupKes keska, final int dni, final double lat, final double lon) {
		if (!VypocetSektoru.jePlatnyStred(keska.lat(), keska.lon())) {
			return;
		}
		final double vzdalenost = Geodezie.vzdalenostKoule(lat, lon, keska.lat(), keska.lon());
		if (nejhorsiNahore.size() < max) {
			nejhorsiNahore.add(new Kandidat(keska, dni, vzdalenost));
			return;
		}
		final Kandidat novy = new Kandidat(keska, dni, vzdalenost);
		if (PORADI.compare(novy, nejhorsiNahore.peek()) < 0) {
			nejhorsiNahore.poll();
			nejhorsiNahore.add(novy);
		}
	}

	/** Od nejlepšího. */
	List<Kandidat> serazene() {
		final List<Kandidat> v = new ArrayList<>(nejhorsiNahore);
		Collections.sort(v, PORADI);
		return v;
	}
}
