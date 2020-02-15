package cz.geokuk.plugins.kesoid.kind;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.Optional;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.plugins.kesoid.*;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.genetika.Genotyp;
import cz.geokuk.plugins.kesoid.mapicon.Sklivec;
import cz.geokuk.util.lang.StringUtils;
import lombok.*;

@ToString
@NoArgsConstructor
public class Wpt00 implements Wpt {

	public static final String DEFAULT_SYM = "Waypoint";

	@Getter @NonNull private String identifier;

	// Wgs souřadnice
	private double lat;
	private double lon;

	// Mou souřadnice, odvozují se případně z WGS, když jsou potřeba.
	private int xx = -1;
	private int yy = -1;

	@NonNull private KesoidPlugin kesoidPlugin;
	@Getter @NonNull private File sourceFile;
	@Getter @NonNull private Kepodr kepodr;

	@Getter @NonNull protected Genotyp genotyp;

	// Podpora vykreslování
	public static int currentSklivecValidityCode;
	private Sklivec sklivec;
	private int sklivecValidityCode;

	@Override
	public Wgs getWgs() {
		return new Wgs(lat, lon);
	}
	public void setWgs(final Wgs aWgs) {
		lat = aWgs.lat;
		lon = aWgs.lon;
	}

	@Override
	public String getSym() {
		return DEFAULT_SYM;
	}

	public static void invalidateAllSklivec() {
		currentSklivecValidityCode++;
	}

	@Override
	public Mou getMou() {
		if (yy == -1) { // testovat yy, protože se nastavuje později
			final Mou mou = getWgs().toMou();
			xx = mou.xx;
			yy = mou.yy;
			return mou;
		} else {
			// System.out.println("kesnuto " + xx + " " + yy);
			return new Mou(xx, yy);
		}
	}

	@Override
	public boolean hasEmptyCoords() {
		return getMou().xx == 0 && getMou().yy == 0;
	}

	@Override
	public Sklivec getSklivec() {
		if (sklivecValidityCode != currentSklivecValidityCode) {
			sklivec = null;
		}
		return sklivec;
	}

	/**
	 *
	 */
	@Override
	public void invalidate() {
		setSklivec(null);
	}

	@Override
	public void setSklivec(final Sklivec sklivec) {
		this.sklivec = sklivec;
		sklivecValidityCode = currentSklivecValidityCode;
	}

	@Override
	public KesoidPlugin getKesoidPlugin() {
		if (kesoidPlugin == null) {
			throw new NullPointerException(this.getNazev() + " nemá kesoidPlugin");
		}
		return kesoidPlugin;
	}


	///////////////// Implementace odvozené z genotypu ////////////////////////////////

	@Override
	public EWptVztah getVztah() {
		return genotyp.ifuj( (i, genom) -> i
				.ifa(genom.ALELA_fnd, EWptVztah.FOUND)
				.ifa(genom.ALELA_own, EWptVztah.OWN)
				.ifa(genom.ALELA_not, EWptVztah.NOT)
				.els(EWptVztah.NORMAL)
				);
	}

	@Override
	public EWptStatus getStatus() {
		return genotyp.ifuj( (i, genom) -> i
				.ifa(genom.ALELA_dsbl, EWptStatus.DISABLED)
				.ifa(genom.ALELA_arch, EWptStatus.ARCHIVED)
				.els(EWptStatus.ACTIVE)
				);
	}


	public void setStatus(@NonNull final EWptStatus status) {
		final Genom genom = genotyp.getGenom();
		switch (status) {
		case ACTIVE: genotyp = genotyp.with(genom.ALELA_actv); break;
		case DISABLED: genotyp = genotyp.with(genom.ALELA_dsbl); break;
		case ARCHIVED: genotyp = genotyp.with(genom.ALELA_arch); break;
		}
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
	public Optional<String> getCreatinDate() {
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

/////////////////// Na vyhození ////////////
	@Override
	public void computeGenotypIfNotExistsForAllRing(final Genom genom) {
	}

	@Override
	public void removeMeFromRing() {
	}

////////////////////////////////////////////////////////////////////////////////////
}
