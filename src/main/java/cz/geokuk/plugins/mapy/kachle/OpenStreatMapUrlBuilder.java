package cz.geokuk.plugins.mapy.kachle;

import java.net.MalformedURLException;
import java.net.URL;

public class OpenStreatMapUrlBuilder implements KachleUrlBuilder {

	private String urlBase;

	public OpenStreatMapUrlBuilder(String urlBase) {
		this.urlBase = urlBase;
	}

	@Override
	public URL buildUrl(KaOne kaOne) throws MalformedURLException {

		StringBuilder sb = new StringBuilder();
		sb.append(urlBase);
		KaLoc kaloc = kaOne.getLoc();
		sb.append(kaloc.getMoumer());
		sb.append('/');
		sb.append(kaloc.getFromSzUnsignedX());
		sb.append('/');
		sb.append(kaloc.getFromSzUnsignedY());
		sb.append(".png");
		URL url = new URL(sb.toString());
		return url;
	}

}
