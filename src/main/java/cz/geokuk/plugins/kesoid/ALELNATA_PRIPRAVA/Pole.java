package cz.geokuk.plugins.kesoid.ALELNATA_PRIPRAVA;

import java.util.function.Supplier;

/**
 * Pole, které roste jak je indexováno, Pole nemá velikost, každý kladný index má hodnotu
 *
 * @author Martin
 *
 */
public class Pole<T> {

	@SuppressWarnings("unchecked")
	private T[] data = (T[]) new Object[0];

	/**
	 * Získá hodnotu z pole.
	 *
	 * @param i
	 * @return
	 */
	public T get(final int i) {
		return i < data.length ? data[i] : null;
	}

	/**
	 * Uložit hodnotu do pole
	 *
	 * @param i
	 *            Index >= 0
	 * @param udaj
	 *            Ukládaný údaj.
	 * @return Původní hodnota.
	 */
	public T put(final int i, final T udaj) {
		if (udaj == null && i <= data.length) {
			data[i] = null;
		}
		if (i >= data.length) {
			@SuppressWarnings("unchecked")
			final T[] pom = (T[]) new Object[i * 4 / 3 + 1];
			System.arraycopy(data, 0, pom, 0, data.length);
			data = pom;
		}
		final T last = data[i];
		data[i] = udaj;
		return last;
	}

	/**
	 * Pokud na daném místě nic není, zavolá funkci, spočítá to a vloží to tam
	 * 
	 * @param i
	 *            Index v poli
	 * @param fce
	 *            Funkce pro výpočet, když nic nemáme.
	 * @return Hodnota, která tam byla nebo nově vnořená.
	 */
	public T computeIfAbsent(final int i, final Supplier<T> fce) {
		T udaj = get(i);
		if (udaj == null) {
			udaj = fce.get();
			put(i, udaj);
		}
		return udaj;
	}
}
