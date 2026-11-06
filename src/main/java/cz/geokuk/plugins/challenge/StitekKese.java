package cz.geokuk.plugins.challenge;

/**
 * Štítek keše na mapě: proč se zobrazuje jako kandidát chybějícího sektoru nebo políčka challenge. Objekty vznikají při výpočtu výsledku a sdílejí se, čtení z nich nic
 * nealokuje.
 */
public interface StitekKese {

	/** Text štítku: u 360° číslo sektoru (třeba „123“), u D/T „D3/T4“ (půlky „D3,5/T4“). */
	String text();

	/** Sektor 0 až 359 u 360°, jinak -1. */
	int sektor();

	/** U 360° leží keš u hranice stupně, nebo sektor chybí vedle nejistého nálezu, takže drobná odchylka ve výpočtu může výsledek změnit; jinak false. */
	boolean nejisty();

	/** Vzdálenost keše od středu challenge (D/T: od středu mapy při výpočtu) v metrech; NaN, když se neví. */
	double vzdalenostM();
}
