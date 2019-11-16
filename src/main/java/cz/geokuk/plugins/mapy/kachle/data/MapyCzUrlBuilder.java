package cz.geokuk.plugins.mapy.kachle.data;

import java.net.MalformedURLException;
import java.net.URL;

class MapyCzUrlBuilder implements KachleUrlBuilder {

	private static final String URLBASE1 = "http://m1.mapserver.mapy.cz/";
	private final String kategorie;

	public MapyCzUrlBuilder(final String kategorie) {
		this.kategorie = kategorie;

	}

	@Override
	public URL buildUrl(final Ka kaOne) throws MalformedURLException {

		final StringBuilder sb = new StringBuilder();
		sb.append(URLBASE1);
		final KaLoc kaloc = kaOne.getLoc();
		sb.append(kategorie);
		sb.append('/');
		sb.append(kaloc.getMoumer());
		sb.append('-');
		sb.append(kaloc.getFromSzUnsignedX());
		sb.append('-');
		sb.append(kaloc.getFromSzUnsignedY());
		final URL url = new URL(sb.toString());
		return url;
	}

}
