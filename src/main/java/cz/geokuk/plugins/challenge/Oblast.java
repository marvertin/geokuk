package cz.geokuk.plugins.challenge;

/** Zeměpisná oblast, do které keš patří, nebo nepatří; zjišťuje se podle souřadnic. */
public interface Oblast {

	/** Bod leží v oblasti (hranice se nezkoumají, bod přesně na hraně může vyjít obojí). */
	boolean obsahuje(double lat, double lon);

	/** Označení verze dat oblasti; mění-li se, uložené výsledky pro ni neplatí. */
	String verze();
}
