package cz.geokuk.plugins.kesoid.kind;

import java.net.URL;
import java.util.Collections;
import java.util.Optional;

import cz.geokuk.plugins.kesoid.*;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.util.lang.StringUtils;
import lombok.SneakyThrows;

public abstract class WptDefaults implements Wpt {

	public static final String DEFAULT_SYM = "Waypoint";

	public WptDefaults() {
		super();
	}

	@Override
	public String getSym() {
		return DEFAULT_SYM;
	}

	@Override
	public boolean hasEmptyCoords() {
		return getMou().xx == 0 && getMou().yy == 0;
	}

	@Override
	public EWptVztah getVztah() {
		return getGenotyp().ifuj( (i, genom) -> i
				.ifa(genom.ALELA_fnd, EWptVztah.FOUND)
				.ifa(genom.ALELA_own, EWptVztah.OWN)
				.ifa(genom.ALELA_not, EWptVztah.NOT)
				.els(EWptVztah.NORMAL)
				);
	}

	@Override
	public EWptStatus getStatus() {
		return getGenotyp().ifuj( (i, genom) -> i
				.ifa(genom.ALELA_dsbl, EWptStatus.DISABLED)
				.ifa(genom.ALELA_arch, EWptStatus.ARCHIVED)
				.els(EWptStatus.ACTIVE)
				);
	}

	@Override
	public String getNazev() {
		return "";
	}

	@Override
	public int getElevation() {
		return 0;
	}

	@Override
	public boolean isRucnePridany() {
		return false;
	}

	@Override
	public int getPolomerObsazenosti() {
		return 0;
	}

	@Override
	public Iterable<Wpt> getRelatedWpts() {
		return Collections.singleton(this);
	}

	@Override
	public EZOrder getZorder() {
		return EZOrder.OTHER;
	}

	@Override
	public Optional<String> getAuthor() {
		return Optional.empty();
	}

	@Override
	public Optional<String> getCreationDate() {
		return Optional.empty();
	}

	@Override
	public Optional<URL> getUrlProOtevreniListinguVGeogetu() {
		return Optional.empty();
	}

	@Override
	@SneakyThrows
	public Optional<URL> getUrlProPridaniDoSeznamuVGeogetu() {
		return Optional.of(new URL("https://coord.info/" + getIdentifier()));
	}

	@Override
	public String getTextToolTipu() {
		final StringBuilder sb = new StringBuilder();
		sb.append("<hml>");
		if (!StringUtils.isBlank(getNazev())) {
			sb.append("<b>");
			sb.append(getNazev());
			sb.append("</b>");
			sb.append("<small>");
			sb.append(" - ");
			sb.append("</small>");
		}
		sb.append("<small>");
		sb.append(getSym());
		sb.append("  (").append(getIdentifier()).append(")");
		sb.append("</small>");
		sb.append(isRucnePridany() ? "+ " : "");
		return sb.toString();
	}

	@Override
	public int getPrioritaUchopovani() {
		// Půvoodě byl hlavní 50 změnšwný o typ kešoidu, pro keš největší tedy 49.
		// Tak chceme hodnotu, která pokud nebude přepsána je nižší než hlavní, ale vedlejší než původní dodatečné.
		return 42;
	}

	@Override
	public void invalidate() {
		setSklivec(null);
	}


	/////////////////// Na vyhození ////////////
	@Override
	public void computeGenotypIfNotExistsForAllRing(final Genom genom) {
	}

	@Override
	public void removeMeFromRing() {
	}

}