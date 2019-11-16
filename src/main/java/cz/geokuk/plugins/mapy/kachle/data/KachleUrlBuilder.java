package cz.geokuk.plugins.mapy.kachle.data;

import java.net.MalformedURLException;
import java.net.URL;

interface KachleUrlBuilder {

	/**
	 * Vytvoří URL pro sgtažení dané dlaždice.
	 *
	 * @param kaOne
	 * @return
	 * @throws MalformedURLException
	 */
	public URL buildUrl(Ka kaOne) throws MalformedURLException;
}
