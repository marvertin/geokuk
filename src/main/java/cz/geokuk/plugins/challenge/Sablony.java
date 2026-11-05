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

	private Sablony() {}

	public static Challenge od360Domova() {
		return new Challenge("360° od domova", Typ.SEKTORY_360, Stred.domov(), OblastDef.zadna(), ZdrojPravidel.PGC);
	}

	/** GC4PCQB: střed je výchozí bod listingu, oblast stát ČR. */
	public static Challenge cesko360() {
		return new Challenge("360° of the Czech Republic", Typ.SEKTORY_360, Stred.listing("GC4PCQB"), OblastDef.stat(STAT_CESKO), ZdrojPravidel.PGC);
	}

	/** GC56R49: střed je výchozí bod listingu, oblast hl. m. Praha a okresy Praha-východ a Praha-západ. */
	public static Challenge praha360() {
		return new Challenge("360° of Prague", Typ.SEKTORY_360, Stred.listing("GC56R49"), OblastDef.sada(PRAHA), ZdrojPravidel.PGC);
	}

	public static List<Challenge> vse() {
		return Collections.unmodifiableList(Arrays.asList(od360Domova(), cesko360(), praha360()));
	}
}
