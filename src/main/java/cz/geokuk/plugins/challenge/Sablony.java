package cz.geokuk.plugins.challenge;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cz.geokuk.plugins.challenge.Challenge.OblastDef;
import cz.geokuk.plugins.challenge.Challenge.Stred;
import cz.geokuk.plugins.challenge.Challenge.Typ;
import cz.geokuk.plugins.challenge.Challenge.ZdrojPravidel;

/** Přednastavené definice; uživatel je může upravit, protože jde o obyčejná data {@link Challenge}. */
public final class Sablony {

	/** Označení oblastí v {@link PoskytovatelOblasti}. */
	public static final String STAT_CESKO = "stat-cz";
	/** Hlavní město Praha a okresy Praha-východ a Praha-západ jako jeden obrys. */
	public static final String PRAHA = "praha";

	/** Název keše na gc.com tak, jak je v originále, s kódem před ním. */
	public static final String NAZEV_CESKO = "GC4PCQB – Challenge - 360 degrees of the Czech Republic";
	public static final String NAZEV_PRAHA = "GC56R49 – Challenge - 360 Degrees of Prague";

	/** Starší názvy šablon, které se v uloženém seznamu aktivních převedou na současné. */
	private static final java.util.Map<String, String> STARE_NAZVY = new java.util.HashMap<>();

	static {
		STARE_NAZVY.put("360° of the Czech Republic", NAZEV_CESKO);
		STARE_NAZVY.put("360° of Prague", NAZEV_PRAHA);
	}

	private Sablony() {}

	/** Současný název šablony pro název uložený dřív; ostatní názvy beze změny. */
	static String soucasnyNazev(final String nazev) {
		final String novy = STARE_NAZVY.get(nazev);
		return novy == null ? nazev : novy;
	}

	/** PGC „360 from home“ je odznak bez GC kódu; střed je domov a počítá se po kouli. */
	public static Challenge od360Domova() {
		return new Challenge("360° od domova", Typ.SEKTORY_360, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC);
	}

	/** GC4PCQB: střed je výchozí bod listingu, oblast stát ČR. */
	public static Challenge cesko360() {
		return new Challenge(NAZEV_CESKO, Typ.SEKTORY_360, Stred.listing("GC4PCQB"), OblastDef.stat(STAT_CESKO), ZdrojPravidel.PGC);
	}

	/** GC56R49: střed je výchozí bod listingu, oblast hl. m. Praha a okresy Praha-východ a Praha-západ. */
	public static Challenge praha360() {
		return new Challenge(NAZEV_PRAHA, Typ.SEKTORY_360, Stred.listing("GC56R49"), OblastDef.sada(PRAHA), ZdrojPravidel.PGC);
	}

	/** Všech 81 kombinací obtížnosti a terénu po půlkách od 1 do 5, střed v domově. */
	public static Challenge maticeDT() {
		return new Challenge("Matice D/T", Typ.DT_MATICE, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC);
	}

	public static List<Challenge> vse() {
		return Collections.unmodifiableList(Arrays.asList(od360Domova(), cesko360(), praha360(), maticeDT()));
	}
}
