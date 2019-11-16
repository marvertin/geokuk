package cz.geokuk.plugins.mapy.kachle.podklady;

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
