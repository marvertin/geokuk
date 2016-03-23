/**
 *
 */
package cz.geokuk.util.index2d;

/**
 * Když navštíví ctverecnik, tak už uvnitř nenavštěvuje visitor
 *
 * @author Martin Veverka
 *
 */
public interface Visitor<T> {

	void visit(Ctverecnik<T> ctver);

	void visit(Sheet<T> sheet);
}
