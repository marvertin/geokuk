package cz.geokuk.plugins.mapy.kachle.data;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

public class ConfigurableMapUrlBuilder implements KachleUrlBuilder {

	private final String urlBase;

	public ConfigurableMapUrlBuilder(final String urlBase, final String subdomains, int tileSize) {
		this.urlBase = urlBase;
	}
	public ConfigurableMapUrlBuilder(final String urlBase, int tileSize) {
		this(urlBase, null, -1);
	}
	public ConfigurableMapUrlBuilder(final String urlBase, final String subdomains) {
		this(urlBase, subdomains, -1);
	}
	public ConfigurableMapUrlBuilder(final String urlBase) {
		this(urlBase, null);
	}

	@Override
	public URL buildUrl(final Ka kaOne) throws MalformedURLException {
		final KaLoc kaloc = kaOne.getLoc();
		String urlString = urlBase
				.replace("{x}", Objects.toString(kaloc.getFromSzUnsignedX()))
				.replace("{y}", Objects.toString(kaloc.getFromSzUnsignedY()))
				.replace("{z}", Objects.toString(kaloc.getMoumer()))
		;
		return new URL(urlString);
	}

}
