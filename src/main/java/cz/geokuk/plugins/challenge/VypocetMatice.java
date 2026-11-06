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
		final boolean[][] nalezeno = new boolean[PULEK][PULEK];
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
				nalezeno[k.obtiznost()][k.teren()] = true;
			} else if (PravidlaTypu.hledatelna(k)) {
				hledatelne[k.obtiznost() * PULEK + k.teren()].pridej(k, 0, lat, lon);
			}
		}
		final List<String> chybejici = new ArrayList<>();
		final List<KandidatPolicka> kandidati = new ArrayList<>();
		for (int d = 0; d < PULEK; d++) {
			for (int t = 0; t < PULEK; t++) {
				if (nalezeno[d][t]) {
					continue;
				}
				final String stitek = stitek(d, t);
				chybejici.add(stitek);
				for (final OmezenaFronta.Kandidat c : hledatelne[d * PULEK + t].serazene()) {
					kandidati.add(new KandidatPolicka(c.keska.id(), c.keska.lat(), c.keska.lon(), stitek, stitek, null, c.vzdalenost));
				}
			}
		}
		return new VysledekPolicek(PULEK * PULEK, chybejici, kandidati);
	}

	/** Štítek políčka z pořadí po půlkách, například „D4/T1“ nebo „D1,5/T2,5“. */
	public static String stitek(final int obtiznost, final int teren) {
		return "D" + hodnota(obtiznost) + "/T" + hodnota(teren);
	}

	private static String hodnota(final int pulky) {
		return pulky % 2 == 0 ? String.valueOf(1 + pulky / 2) : (1 + pulky / 2) + ",5";
	}

	private static boolean jePlatne(final int pulky) {
		return pulky >= 0 && pulky < PULEK;
	}
}
