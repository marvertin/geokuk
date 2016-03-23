package cz.geokuk.plugins.mapy.kachle;

/**
 * Umožní zkanclovat probíhající download.
 *
 * @author veverka
 *
 */
public interface Kanceler {

	public static Kanceler EMPTY = () -> {
	};

	public void cancel();
}
