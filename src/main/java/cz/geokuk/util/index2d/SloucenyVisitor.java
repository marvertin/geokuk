/**
 *
 */
package cz.geokuk.util.index2d;

/**
 * @author Martin Veverka
 *
 */
@FunctionalInterface
public interface SloucenyVisitor<T> extends Visitor<T> {

	@Override
	public default void visitCtverecnik(final Ctverecnik<T> aCtver) {
		visitNod(aCtver);
	}

	@Override
	public default void visitSheet(final Sheet<T> aSheet) {
		visitNod(aSheet);
	}

	@Override
	public default void visitSheetList(final SheetList<T> aSheetList) {
		visitNod(aSheetList);
	}

	void visitNod(Node0<T> node);

}
