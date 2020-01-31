package cz.geokuk.plugins.kesoid.importek;

import cz.geokuk.plugins.kesoid.Wpt;

public interface WptReceiver {

	/**
	 * Hlavní metoda, kterou přijímáme hotové waypointy.
	 */
	void expose(Wpt wpt);

}