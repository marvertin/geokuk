package cz.geokuk.plugins.kesoid.kind;

import cz.geokuk.plugins.kesoid.Wpt;

/**
 * Umožňuje specifické filtrování.
 * @author veverka
 *
 */
public interface PluginFilter<T> {

	/** Vrací implicitní filtr definici, která bude použita, pokud žádná není v preferences */
	T getDefaultFilterDefinition();


	/** Implemetnace musí rozhodnout, zda daný waypoint má či nemá být zařazen. Posílá sem pouze waypointy, které plugin sám stvoři a jen ty, které nebyly
	 * vyfiltrovány globálně pomocí alel. Filter nesmí předpokládat, že sem projdou všechny waypointy. */
	boolean filter(T definition, Wpt waypoint);

}
