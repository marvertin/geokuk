package cz.geokuk.plugins.kesoid.kind;

import java.io.File;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.plugins.kesoid.*;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.genetika.Genotyp;
import cz.geokuk.plugins.kesoid.mapicon.Sklivec;
import lombok.*;

@ToString
@NoArgsConstructor
public class Wpt00 extends WptDefaults implements Wpt {

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
	public Sklivec getSklivec() {
		if (sklivecValidityCode != currentSklivecValidityCode) {
			sklivec = null;
		}
		return sklivec;
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

	public void setStatus(@NonNull final EWptStatus status) {
		final Genom genom = genotyp.getGenom();
		switch (status) {
		case ACTIVE: genotyp = genotyp.with(genom.ALELA_actv); break;
		case DISABLED: genotyp = genotyp.with(genom.ALELA_dsbl); break;
		case ARCHIVED: genotyp = genotyp.with(genom.ALELA_arch); break;
		}
	}

////////////////////////////////////////////////////////////////////////////////////
}
