package cz.geokuk.util.procak;

/**
 * Procák nějakým způsobem zpracuje objekt a vrátí co s tím.
 * @author veverka
 *
 * @param <T>
 */
public interface Procak<T> {

	/**
	 * Implemetnace musí provést zpracování a vrátit jak si pochodila.
	 * @param obj
	 * @return
	 */
	public EProcakResult process(T obj);

	/**
	 * Označuje konec kola.
	 */
	public void roundDone();
}
