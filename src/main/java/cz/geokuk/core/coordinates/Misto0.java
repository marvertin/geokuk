package cz.geokuk.core.coordinates;

/**
 * Objekt nesoucí mějaké místo na zemi, tedy souřadnice v nějakém formátu.
 *
 * @author tatinek
 *
 */
public abstract class Misto0 implements Mouable {

	@Override
	public final Mou getMou() {
		return toMou();
	}

	public abstract Mercator toMercator();

	public abstract Mou toMou();

	public abstract Utm toUtm();

	public abstract Wgs toWgs();

}
