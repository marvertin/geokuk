/**
 *
 */
package cz.geokuk.util.index2d;

/**
 * Když navštíví ctverecnik, tak už uvnitř nenavštěvuje visitor
 * 
 * @author veverka
 *
 */
public interface Visitor<T> {

	void visit(Sheet<T> sheet);

	void visit(Ctverecnik<T> ctver);
}
