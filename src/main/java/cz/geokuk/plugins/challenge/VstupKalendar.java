package cz.geokuk.plugins.challenge;

import java.time.LocalDate;

/** Keš nebo event s daty pro challenge kalendář. */
public interface VstupKalendar extends VstupKes {

	/** Datum nálezu v lokálním čase uživatele; null = neznámé. */
	LocalDate datumNalezu();

	/** Datum eventu, u ostatních keší datum zveřejnění; null = neznámé. */
	LocalDate datum();

	boolean event();
}
