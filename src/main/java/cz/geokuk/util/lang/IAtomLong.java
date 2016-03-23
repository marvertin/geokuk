/**
 *
 */
package cz.geokuk.util.lang;


/**
 * Rozhraní označující atomické typy převeditelné na long.
 * @author veverka
 *
 */
public interface IAtomLong extends IAtom {

	/**
	 * Převede atomický typ na číslo typu long.
	 * Protože atomický typ nemůže obsahoivat null, je pouit primitivní typ a ne obálka.
	 * @return
	 */
	public long toLong();

}
