package cz.geokuk.core.coordinates;

/**
 * Objekt nesoucí mějaké místo na zemi, tedy souřadnice v nějakém formátu.
 * @author tatinek
 *
 */
public abstract class Misto0 implements Mouable {

	public abstract Wgs toWgs();

	public abstract Mercator toMercator();

	public abstract Mou toMou();

	public abstract Utm toUtm();

	@Override
	public final Mou getMou() {
		return toMou();
	}

}
