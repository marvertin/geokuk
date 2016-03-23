package cz.geokuk.plugins.mapy.kachle;

import java.net.MalformedURLException;
import java.net.URL;

public class MapyCzUrlBuilder implements KachleUrlBuilder {

	private static final String URLBASE1 = "http://m1.mapserver.mapy.cz/";
	private final String kategorie;

	public MapyCzUrlBuilder(String kategorie) {
		this.kategorie = kategorie;

	}
	@Override
	public URL buildUrl(KaOne kaOne) throws MalformedURLException {

		StringBuilder sb = new StringBuilder();
		sb.append(URLBASE1);
		KaLoc kaloc = kaOne.getLoc();
		sb.append(kategorie);
		sb.append('/');
		sb.append(kaloc.getMoumer());
		sb.append('-');
		sb.append(kaloc.getFromSzUnsignedX());
		sb.append('-');
		sb.append(kaloc.getFromSzUnsignedY());
		URL url = new URL(sb.toString());
		return url;
	}

}
