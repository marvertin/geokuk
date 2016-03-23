/**
 *
 */
package cz.geokuk.util.lang;


/**
 * Rozhraní označuje atomické typy převeditelné na String, což jsou všechny.
 * @author veverka
 *
 */
public interface IAtomString extends IAtom {

	/**
	 * Převede atomický typ na řetězec v kanonickém tvaru.
	 * @return
	 */
	public String toString();

}
