package cz.geokuk.plugins.kesoidpopisky;

import cz.geokuk.plugins.kesoid.Wpt;

/**
 * Implementace Nahrazuje nějakou hodnotu.
 * Každá implementace ví, co má hahradit a do
 * předaného sb předá nějaká data, která vytáhne z waypointu.
 * @author Martin
 *
 */
public interface PopiskyNahrazovac {
	void pridej(StringBuilder sb, Wpt wpt);

}