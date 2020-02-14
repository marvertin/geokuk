package cz.geokuk.plugins.kesoid.kind;

import java.io.File;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.plugins.kesoid.Kepodr;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.genetika.Genotyp;
import cz.geokuk.plugins.kesoid.mapicon.Sklivec;
import lombok.*;

@Getter @Setter @ToString
@NoArgsConstructor
public abstract class Wpt00 implements Wpt {

	public static int currentSklivecValidityCode;


	@NonNull
	private String identifier;

	@NonNull
	private String nazev;

	@Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
	private double lat;

	@Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
	private double lon;
	@Override
	public Wgs getWgs() {
		return new Wgs(lat, lon);
	}
	public void setWgs(final Wgs aWgs) {
		lat = aWgs.lat;
		lon = aWgs.lon;
	}

	@NonNull
	private String sym;

	private int elevation;

	@NonNull
	private Genotyp genotyp;

	private boolean rucnePridany = false;

	@NonNull
	private File sourceFile;

	@NonNull
	private EZOrder zorder = EZOrder.OTHER;

	// Podpora vykreslování
	private Sklivec sklivec;

	private int sklivecValidityCode;

	private KesoidPlugin kesoidPlugin;

	/** Poddruh kešoidu */
	@Getter @Setter
	private Kepodr kepodr;


	public static void invalidateAllSklivec() {
		currentSklivecValidityCode++;
	}

	private int xx = -1;
	private int yy = -1;
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

/////////////////// Na vyhození ////////////
	@Override
	public void computeGenotypIfNotExistsForAllRing(final Genom genom) {
	}

	@Override
	public void removeMeFromRing() {
	}

}
