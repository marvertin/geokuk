package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;

/** Výsledek výpočtu 360°: obsazené a chybějící sektory, kandidáti a sektory s nejistým stavem. */
public final class VysledekSektoru {

	/** Nenalezené keše v oblasti, které by zaplnily jeden chybějící sektor. */
	public static final class Kandidati {
		private final int sektor;
		private int pocet;
		private String nejblizsiId;
		private double nejblizsiVzdalenost = Double.MAX_VALUE;

		Kandidati(final int sektor) {
			this.sektor = sektor;
		}

		void pridej(final String id, final double vzdalenost) {
			pocet++;
			if (vzdalenost < nejblizsiVzdalenost) {
				nejblizsiVzdalenost = vzdalenost;
				nejblizsiId = id;
			}
		}

		public int sektor() {
			return sektor;
		}

		public int pocet() {
			return pocet;
		}

		/** Kód nejbližší kandidátní keše (podle vzdálenosti od středu). */
		public String nejblizsiId() {
			return nejblizsiId;
		}

		/** Vzdálenost nejbližšího kandidáta od středu v metrech. */
		public double nejblizsiVzdalenost() {
			return nejblizsiVzdalenost;
		}
	}

	private final BitSet obsazene;
	private final BitSet nejiste;
	private final Kandidati[] kandidati = new Kandidati[360];
	private final int pocetNalezu;

	VysledekSektoru(final BitSet obsazene, final BitSet nejiste, final Kandidati[] kandidati, final int pocetNalezu) {
		this.obsazene = obsazene;
		this.nejiste = nejiste;
		System.arraycopy(kandidati, 0, this.kandidati, 0, 360);
		this.pocetNalezu = pocetNalezu;
	}

	public int pocetObsazenych() {
		return obsazene.cardinality();
	}

	public boolean jeObsazeny(final int sektor) {
		return obsazene.get(sektor);
	}

	/** Chybějící sektory vzestupně. */
	public List<Integer> chybejici() {
		final List<Integer> v = new ArrayList<>();
		for (int s = obsazene.nextClearBit(0); s < 360; s = obsazene.nextClearBit(s + 1)) {
			v.add(s);
		}
		return Collections.unmodifiableList(v);
	}

	/** Stav sektoru by se mohl změnit drobnou odchylkou ve výpočtu: obsazen jen nálezy u hranice, nebo chybí vedle takového nálezu. */
	public boolean jeNejisty(final int sektor) {
		return nejiste.get(sektor);
	}

	/** Kandidáti pro chybějící sektor; null, když sektor není chybějící. */
	public Kandidati kandidati(final int sektor) {
		return kandidati[sektor];
	}

	/** Kolik nálezů se do výpočtu započetlo (v oblasti a podle pravidel). */
	public int pocetZapocitanychNalezu() {
		return pocetNalezu;
	}
}
