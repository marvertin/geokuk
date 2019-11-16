package cz.geokuk.plugins.mapy.kachle.data;

import java.net.MalformedURLException;
import java.net.URL;

class OpenStreatMapUrlBuilder implements KachleUrlBuilder {

	private final String urlBase;

	public OpenStreatMapUrlBuilder(final String urlBase) {
		this.urlBase = urlBase;
	}

	@Override
	public URL buildUrl(final Ka kaOne) throws MalformedURLException {

		final StringBuilder sb = new StringBuilder();
		sb.append(urlBase);
		final KaLoc kaloc = kaOne.getLoc();
		sb.append(kaloc.getMoumer());
		sb.append('/');
		sb.append(kaloc.getFromSzUnsignedX());
		sb.append('/');
		sb.append(kaloc.getFromSzUnsignedY());
		sb.append(".png");
		final URL url = new URL(sb.toString());
		return url;
	}

}
