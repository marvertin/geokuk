package cz.geokuk.plugins.mapy.kachle.data;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

import static cz.geokuk.util.lang.StringUtils.isBlank;

public class ConfigurableMapUrlBuilder implements KachleUrlBuilder {

	private final String urlBase;
	private final String subdomains;

	public ConfigurableMapUrlBuilder(final String urlBase, final String subdomains, int tileSize) {
		this.urlBase = urlBase;
		this.subdomains = subdomains;
		_validate();
	}

	private void _validate() {
		if (isBlank(urlBase)) {
			throw new IllegalArgumentException("urlBase cannot be blank.");
		}
		if (urlBase.contains("{s}") && isBlank(subdomains)) {
			throw new IllegalArgumentException("If urlBase contains \"{s}\" placeholder, at least one subdomain character must be specfied: urlBase="+urlBase);
		}
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
				.replace("{s}", _subDomain())
				.replace("{x}", Objects.toString(kaloc.getFromSzUnsignedX()))
				.replace("{y}", Objects.toString(kaloc.getFromSzUnsignedY()))
				.replace("{z}", Objects.toString(kaloc.getMoumer()))
		;
		return new URL(urlString);
	}

	private String _subDomain() {
		return Optional.ofNullable(subdomains).map(s->s.substring(0,1)).orElse("");
	}

}
