package cz.geokuk.plugins.kesoid.kind;

import java.io.File;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.plugins.kesoid.*;
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
	protected Genotyp genotyp;

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


	///////////////// Implementace odvozené z genotypu ////////////////////////////////

	@Override
	public EWptVztah getVztah() {
		final Genom genom = genotyp.getGenom();
		if (genotyp.has(genom.ALELA_fnd)) {
			return EWptVztah.FOUND;
		}
		if (genotyp.has(genom.ALELA_own)) {
			return EWptVztah.OWN;
		}
		if (genotyp.has(genom.ALELA_not)) {
			return EWptVztah.NOT;
		}
		return EWptVztah.NORMAL;
	}

	@Override
	public EWptStatus getStatus() {
		final Genom genom = genotyp.getGenom();
		if (genotyp.has(genom.ALELA_dsbl)) {
			return EWptStatus.DISABLED;
		}
		if (genotyp.has(genom.ALELA_arch)) {
			return EWptStatus.ARCHIVED;
		}
		return EWptStatus.ACTIVE;
	}

	public void setStatus(@NonNull final EWptStatus status) {
		final Genom genom = genotyp.getGenom();
		switch (status) {
		case ACTIVE: genotyp = genotyp.with(genom.ALELA_actv); break;
		case DISABLED: genotyp = genotyp.with(genom.ALELA_dsbl); break;
		case ARCHIVED: genotyp = genotyp.with(genom.ALELA_arch); break;
		}
	}
/////////////////// Na vyhození ////////////
	@Override
	public void computeGenotypIfNotExistsForAllRing(final Genom genom) {
	}

	@Override
	public void removeMeFromRing() {
	}

}
