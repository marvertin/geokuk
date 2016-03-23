/**
 *
 */
package cz.geokuk.util.lang;

import java.math.BigInteger;

/**
 * Rozhraní označující atomické typy převeditelné na BigInteger.
 *
 * @author Martin Veverka
 *
 */
public interface IAtomBigInteger extends IAtom {

	/**
	 * Převede atomický typ na číslo typu BigInteger. Protože atomický typ nemůže obsahoivat null, je pouit primitivní typ a ne obálka.
	 *
	 * @return
	 */
	public BigInteger toBigInteger();

}
