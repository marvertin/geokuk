package cz.geokuk.plugins.challenge;

import java.util.List;

import cz.geokuk.core.coord.Coord;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Wgs;

/**
 * Neměnný podklad pro kreslení plánovače na mapě: střed, kandidáti a vybraný sektor v souřadnicích mapy. Staví se při změně výsledku nebo výběru, ne při každém překreslení,
 * a kreslení z něj čte bez alokací.
 */
final class TvarPlanovace {

	/** Nejvíc značek kandidátů, které se v jednom výřezu kreslí. */
	static final int NEJVIC_ZNACEK = 200;

	final PrehledChallenge prehled;
	final VybranyKandidat vybrany;
	/** Střed 360° nebo null (D/T a kalendář střed nekreslí). */
	final Mou stred;
	final Wgs stredWgs;
	final String popisStredu;
	final GeometrieSektoru.Model model;
	final Mou[] pozice;
	final String[] texty;
	/** Vybraný sektor: štítek, poloha kandidáta a sektor; jinak null / -1. */
	final String popisVybraneho;
	final Mou vybranyPozice;
	final int vybranySektor;
	private double polomerPrstence = Double.NaN;
	private List<GeometrieSektoru.Usek> prstenec;
	private List<GeometrieSektoru.Bod> klin;

	private TvarPlanovace(final PrehledChallenge prehled, final VybranyKandidat vybrany, final java.util.function.BooleanSupplier zruseno) {
		this.prehled = prehled;
		this.vybrany = vybrany;
		final Challenge c = prehled.challenge();
		model = GeometrieSektoru.Model.zPravidel(c.zdroj());
		final boolean sektory = c.typ() == Challenge.Typ.SEKTORY_360;
		if (sektory && prehled.maStred()) {
			stredWgs = new Wgs(prehled.stredLat(), prehled.stredLon());
			stred = PixelyMapy.mou(stredWgs.lat, stredWgs.lon);
			popisStredu = prehled.stredPopis();
		} else {
			stredWgs = null;
			stred = null;
			popisStredu = null;
		}
		final Object zdroj = prehled.zdroj();
		if (zdroj instanceof VysledekSektoru) {
			final List<VysledekSektoru.KandidatKes> k = ((VysledekSektoru) zdroj).vsichniKandidati();
			pozice = new Mou[k.size()];
			texty = new String[k.size()];
			for (int i = 0; i < pozice.length; i++) {
				zkontrolujZruseni(i, zruseno);
				final VysledekSektoru.KandidatKes x = k.get(i);
				pozice[i] = PixelyMapy.mou(x.lat(), x.lon());
				texty[i] = x.text() + "°";
			}
		} else {
			final List<KandidatPolicka> k = ((VysledekPolicek) zdroj).kandidati();
			pozice = new Mou[k.size()];
			texty = new String[k.size()];
			for (int i = 0; i < pozice.length; i++) {
				zkontrolujZruseni(i, zruseno);
				final KandidatPolicka x = k.get(i);
				pozice[i] = PixelyMapy.mou(x.lat(), x.lon());
				texty[i] = x.text();
			}
		}
		if (sektory && vybrany != null && vybrany.sektor() >= 0 && zdroj instanceof VysledekSektoru) {
			final VysledekSektoru.Kandidati k = ((VysledekSektoru) zdroj).kandidati(vybrany.sektor());
			vybranySektor = vybrany.sektor();
			vybranyPozice = PixelyMapy.mou(vybrany.lat(), vybrany.lon());
			popisVybraneho = TextyKresleni.vybranySektor(vybranySektor, k == null ? 1 : k.pocet(), k == null ? vybrany.vzdalenost() : k.nejblizsiVzdalenost());
		} else {
			vybranySektor = -1;
			vybranyPozice = null;
			popisVybraneho = null;
		}
	}

	static TvarPlanovace z(final PrehledChallenge prehled, final VybranyKandidat vybrany) {
		return z(prehled, vybrany, () -> false);
	}

	/** Staví se po částech a kontroluje {@code zruseno}; zrušená stavba skončí {@link java.util.concurrent.CancellationException}. */
	static TvarPlanovace z(final PrehledChallenge prehled, final VybranyKandidat vybrany, final java.util.function.BooleanSupplier zruseno) {
		return prehled == null ? null : new TvarPlanovace(prehled, vybrany, zruseno);
	}

	private static void zkontrolujZruseni(final int i, final java.util.function.BooleanSupplier zruseno) {
		if ((i & 4095) == 0 && zruseno.getAsBoolean()) {
			throw new java.util.concurrent.CancellationException("Stavba podkladu plánovače byla zrušena.");
		}
	}

	/** Poloměr prstence v metrech pro dané měřítko: 1–2–5 × 10^n, ať prstenec zabere asi 0,2 až 0,6 menšího rozměru mapy. */
	static double polomerPrstence(final Coord coord) {
		final double px = 0.375 * Math.min(coord.getWidth(), coord.getHeight());
		final double m = px / coord.getPixluNaMetr();
		final double dekada = Math.pow(10, Math.floor(Math.log10(m)));
		final double[] kroky = { 1, 2, 5, 10 };
		double nejlepsi = dekada;
		for (final double k : kroky) {
			if (Math.abs(Math.log(k * dekada / m)) < Math.abs(Math.log(nejlepsi / m))) {
				nejlepsi = k * dekada;
			}
		}
		return nejlepsi;
	}

	/** Úseky prstence stejného stavu pro daný poloměr; pamatuje si poslední poloměr. */
	List<GeometrieSektoru.Usek> prstenec(final double polomer) {
		if (prstenec == null || polomer != polomerPrstence) {
			polomerPrstence = polomer;
			klin = null;
			final Object zdroj = prehled.zdroj();
			prstenec = stredWgs == null || !(zdroj instanceof VysledekSektoru) ? java.util.Collections.<GeometrieSektoru.Usek>emptyList()
					: GeometrieSektoru.prstenec((VysledekSektoru) zdroj, stredWgs.lat, stredWgs.lon, polomer, model);
		}
		return prstenec;
	}

	/** Klín vybraného sektoru až k prstenci daného poloměru, nebo prázdný. */
	List<GeometrieSektoru.Bod> klin(final double polomer) {
		prstenec(polomer);
		if (klin == null) {
			klin = stredWgs == null || vybranySektor < 0 ? java.util.Collections.<GeometrieSektoru.Bod>emptyList()
					: GeometrieSektoru.klin(stredWgs.lat, stredWgs.lon, vybranySektor, 0, polomer, model);
		}
		return klin;
	}
}
