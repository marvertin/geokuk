/**
 * 
 */
package cz.geokuk.util.lang;


/**
 * Rozhraní označující atomické typy převeditelné na int.
 * @author veverka
 *
 */
public interface IAtomInt extends IAtom {
  
  /**
   * Převede atomický typ na číslo typu int.
   * Protože atomický typ nemůže obsahoivat null, je pouit primitivní typ a ne obálka.
   * @return
   */
  public int toInt();

}
