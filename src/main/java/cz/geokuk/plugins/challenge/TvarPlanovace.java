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
	/** Štítek políčka každého kandidáta; null u sektorů. */
	final String[] stitkyPolicek;
	/** Vybraný sektor: štítek, poloha kandidáta a sektor; jinak null / -1. */
	final String popisVybraneho;
	final Mou vybranyPozice;
	final int vybranySektor;
	/** Poloměr kružnice v metrech; 0, když se nekreslí. */
	final double polomerKruznice;
	private static final double MAX_KLIN_M = 15_000_000;
	private static final double KLIN_USEK_M = 25_000;
	private static final int KLIN_USEKU_MAX = 48;
	private double polomerPrstence = Double.NaN;
	private List<GeometrieSektoru.Usek> prstenec;

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
		polomerKruznice = zdroj instanceof VysledekSektoru && stredWgs != null ? polomerKruznice((VysledekSektoru) zdroj) : 0;
		if (zdroj instanceof VysledekSektoru) {
			final List<VysledekSektoru.KandidatKes> k = ((VysledekSektoru) zdroj).vsichniKandidati();
			pozice = new Mou[k.size()];
			texty = new String[k.size()];
			stitkyPolicek = null;
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
			stitkyPolicek = new String[k.size()];
			for (int i = 0; i < pozice.length; i++) {
				zkontrolujZruseni(i, zruseno);
				final KandidatPolicka x = k.get(i);
				stitkyPolicek[i] = x.stitek();
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

	/** Nejmenší poloměr kružnice, když nejsou kandidáti; a nejmenší vůbec. */
	static final double MIN_POLOMER_M = 1000;
	/** Poloměr kružnice bez kandidátů: 1–2–5 × 10ⁿ, tady 5 km. */
	static final double POLOMER_BEZ_KANDIDATU_M = 5000;

	/**
	 * Pevný poloměr kružnice v metrech, nezávislý na zvětšení mapy: vzdálenost nejvzdálenějšího z nejbližších kandidátů chybějících sektorů, aspoň 1 km; bez kandidátů 5 km.
	 */
	static double polomerKruznice(final VysledekSektoru v) {
		double nejvic = 0;
		for (final int s : v.chybejici()) {
			final VysledekSektoru.Kandidati k = v.kandidati(s);
			if (k != null && k.pocet() > 0) {
				nejvic = Math.max(nejvic, k.nejblizsiVzdalenost());
			}
		}
		return nejvic > 0 ? Math.max(MIN_POLOMER_M, nejvic) : POLOMER_BEZ_KANDIDATU_M;
	}

	/** Úseky prstence stejného stavu pro daný poloměr; pamatuje si poslední poloměr. */
	List<GeometrieSektoru.Usek> prstenec(final double polomer) {
		if (prstenec == null || polomer != polomerPrstence) {
			polomerPrstence = polomer;
			final Object zdroj = prehled.zdroj();
			prstenec = stredWgs == null || !(zdroj instanceof VysledekSektoru) ? java.util.Collections.<GeometrieSektoru.Usek>emptyList()
					: GeometrieSektoru.prstenec((VysledekSektoru) zdroj, stredWgs.lat, stredWgs.lon, polomer, model);
		}
		return prstenec;
	}

	/** Nejvzdálenější roh okna od středu challenge v metrech s rezervou; klín delší než tohle už v okně nic nepřidá. */
	double polomerZaOknem(final Coord coord) {
		if (stredWgs == null) {
			return 0;
		}
		final int w = coord.getWidth();
		final int h = coord.getHeight();
		double nejvic = 0;
		for (final int[] roh : new int[][] { { 0, 0 }, { w, 0 }, { 0, h }, { w, h } }) {
			final Wgs wgs = coord.transform(new java.awt.Point(roh[0], roh[1])).toWgs();
			nejvic = Math.max(nejvic, Geodezie.vzdalenostKoule(stredWgs.lat, stredWgs.lon, wgs.lat, wgs.lon));
		}
		return Math.min(MAX_KLIN_M, nejvic * 1.15 + 1000);
	}

	/**
	 * Klín sektoru od středu až za okraj okna: dva geodetické paprsky po úsecích a oblouk ve vzdálenosti za oknem; sektor 0 až 359, jinak prázdný. Počet bodů nezávisí na zvětšení
	 * mapy, jen na vzdálenosti za oknem.
	 */
	List<GeometrieSektoru.Bod> klinZaOkno(final int sektor, final double polomer) {
		if (stredWgs == null || sektor < 0 || sektor > 359 || !(polomer > 0)) {
			return java.util.Collections.<GeometrieSektoru.Bod>emptyList();
		}
		final int useku = Math.max(1, Math.min(KLIN_USEKU_MAX, (int) Math.ceil(polomer / KLIN_USEK_M)));
		final List<GeometrieSektoru.Bod> body = new java.util.ArrayList<>();
		for (int i = 0; i <= useku; i++) {
			body.add(GeometrieSektoru.bod(stredWgs.lat, stredWgs.lon, sektor, polomer * i / useku, model));
		}
		final List<GeometrieSektoru.Bod> oblouk = GeometrieSektoru.oblouk(stredWgs.lat, stredWgs.lon, sektor, sektor + 1, polomer, model);
		body.addAll(oblouk.subList(1, oblouk.size()));
		for (int i = useku - 1; i >= 0; i--) {
			body.add(GeometrieSektoru.bod(stredWgs.lat, stredWgs.lon, sektor + 1, polomer * i / useku, model));
		}
		return body;
	}
}
