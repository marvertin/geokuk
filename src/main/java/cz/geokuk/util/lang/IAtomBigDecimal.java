/**
 *
 */
package cz.geokuk.util.lang;

import java.math.BigDecimal;

/**
 * Rozhraní označující atomické typy převeditelné na BigDecimal.
 *
 * @author Martin Veverka
 *
 */
public interface IAtomBigDecimal extends IAtom {

	/**
	 * Převede atomický typ na číslo typu BigDecimal. Protože atomický typ nemůže obsahoivat null, je pouit primitivní typ a ne obálka.
	 *
	 * @return
	 */
	public BigDecimal toBigDecimal();

}
