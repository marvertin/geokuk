package cz.geokuk.plugins.mapy.kachle;

/**
 * Umožní zkanclovat probíhající download.
 *
 * @author Martin Veverka
 *
 */
public interface Kanceler {

	public static Kanceler EMPTY = () -> {};

	public void cancel();
}
