package cz.geokuk.plugins.mapy.kachle;

import java.net.MalformedURLException;
import java.net.URL;

public interface KachleUrlBuilder {

	/**
	 * Vytvoří URL pro sgtažení dané dlaždice.
	 * @param kaOne
	 * @return
	 * @throws MalformedURLException
	 */
	public URL buildUrl(KaOne kaOne) throws MalformedURLException;
}
