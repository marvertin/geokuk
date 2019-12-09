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

	void visitSheet(Sheet<T> sheet);

	void visitSheetList(SheetList<T> sheetList);

	void visitCtverecnik(Ctverecnik<T> ctver);

}
