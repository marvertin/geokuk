package cz.geokuk.plugins.challenge;

/** Keš s obtížností a terénem pro challenge D/T matice. */
public interface VstupDT extends VstupKes {

	/** Obtížnost jako pořadí po půlkách: 0 = 1, 1 = 1,5, … 8 = 5; mimo 0 až 8 = neznámá. */
	int obtiznost();

	/** Terén ve stejném pořadí jako {@link #obtiznost()}. */
	int teren();
}
