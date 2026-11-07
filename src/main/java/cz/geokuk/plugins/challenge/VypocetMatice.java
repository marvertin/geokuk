package cz.geokuk.plugins.challenge;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Výpočet challenge D/T matice: 81 kombinací obtížnosti a terénu po půlkách od 1 do 5. */
public final class VypocetMatice {

	static final int PULEK = 9;

	private VypocetMatice() {}

	/**
	 * @param lat
	 *            referenční bod (třeba střed mapy), ke kterému se vybírají kandidáti; výsledek platí pro něj
	 * @param kese
	 *            prvky nesmí být null
	 */
	public static VysledekPolicek spocti(final PravidlaTypu pravidla, final double lat, final double lon, final Iterable<? extends VstupDT> kese) {
		Objects.requireNonNull(pravidla, "pravidla");
		Objects.requireNonNull(kese, "kese");
		if (!VypocetSektoru.jePlatnyStred(lat, lon)) {
			throw new IllegalArgumentException("Bod mimo rozsah souřadnic: " + lat + ", " + lon);
		}
		final int[] nalezu = new int[PULEK * PULEK];
		final OmezenaFronta[] hledatelne = new OmezenaFronta[PULEK * PULEK];
		for (int i = 0; i < hledatelne.length; i++) {
			hledatelne[i] = new OmezenaFronta(pravidla.maxKandidatu());
		}
		for (final VstupDT k : kese) {
			Objects.requireNonNull(k, "keš");
			if (!jePlatne(k.obtiznost()) || !jePlatne(k.teren()) || !pravidla.lzePocitat(k)) {
				continue;
			}
			if (PravidlaTypu.platnyNalez(k)) {
				nalezu[k.obtiznost() * PULEK + k.teren()]++;
			} else if (PravidlaTypu.hledatelna(k)) {
				hledatelne[k.obtiznost() * PULEK + k.teren()].pridej(k, 0, lat, lon);
			}
		}
		final List<String> chybejici = new ArrayList<>();
		final List<KandidatPolicka> kandidati = new ArrayList<>();
		for (int d = 0; d < PULEK; d++) {
			for (int t = 0; t < PULEK; t++) {
				if (nalezu[d * PULEK + t] >= pravidla.opakovani()) {
					continue;
				}
				final String stitek = stitek(d, t);
				chybejici.add(stitek);
				for (final OmezenaFronta.Kandidat c : hledatelne[d * PULEK + t].serazene()) {
					kandidati.add(new KandidatPolicka(c.keska.id(), c.keska.lat(), c.keska.lon(), stitek, stitek, null, c.vzdalenost));
				}
			}
		}
		return new VysledekPolicek(PULEK * PULEK, chybejici, kandidati, pravidla.maxKandidatu(), pravidla.opakovani(), nalezu);
	}

	private static final String[] STITKY = new String[PULEK * PULEK];

	static {
		for (int d = 0; d < PULEK; d++) {
			for (int t = 0; t < PULEK; t++) {
				STITKY[d * PULEK + t] = "D" + hodnota(d) + "/T" + hodnota(t);
			}
		}
	}

	/** Štítek políčka z pořadí po půlkách, například „D4/T1“ nebo „D1,5/T2,5“. */
	public static String stitek(final int obtiznost, final int teren) {
		return jePlatne(obtiznost) && jePlatne(teren) ? STITKY[obtiznost * PULEK + teren] : "D" + hodnota(obtiznost) + "/T" + hodnota(teren);
	}

	private static final java.util.Map<String, Integer> INDEX_STITKU = new java.util.HashMap<>();

	static {
		for (int i = 0; i < STITKY.length; i++) {
			INDEX_STITKU.put(STITKY[i], i);
		}
	}

	/** Pořadí políčka (obtížnost × 9 + terén) podle štítku, nebo -1 pro neznámý štítek. */
	static int indexStitku(final String stitek) {
		final Integer i = INDEX_STITKU.get(stitek);
		return i == null ? -1 : i;
	}

	/** Předpočítaný štítek políčka, nebo null mimo 0 až 8. */
	static String stitekNeboNull(final int obtiznost, final int teren) {
		return jePlatne(obtiznost) && jePlatne(teren) ? STITKY[obtiznost * PULEK + teren] : null;
	}

	private static String hodnota(final int pulky) {
		return pulky % 2 == 0 ? String.valueOf(1 + pulky / 2) : (1 + pulky / 2) + ",5";
	}

	static boolean jePlatne(final int pulky) {
		return pulky >= 0 && pulky < PULEK;
	}
}
