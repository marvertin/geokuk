/**
 *
 */
package cz.geokuk.util.lang;

/**
 * Předek všech rozhraní implementovaných atomickými typy.
 *
 * @author Martin Veverka
 */
public interface IAtom {

	/**
	 * Určuje, zda je hodnota atomického typu z jistého pohledu validní.
	 *
	 * @return Vrací boolean.
	 */
	public boolean isValid();

}
