/**
 *
 */
package cz.geokuk.util.lang;

/**
 * Rozhraní označující atomické typy převeditelné na DOuble.
 *
 * @author Martin Veverka
 *
 */
public interface IAtomDouble extends IAtom {

	/**
	 * Převede atomický typ na číslo typu Double. Protože atomický typ nemůže obsahoivat null, je pouit primitivní typ a ne obálka.
	 *
	 * @return
	 */
	public double toDouble();

}
