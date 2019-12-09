/**
 *
 */
package cz.geokuk.util.index2d;

/**
 * @author Martin Veverka
 *
 */
public abstract class SloucenyVisitor<T> implements Visitor<T> {

	@Override
	public final void visit(final Ctverecnik<T> aCtver) {
		visitNod(aCtver);
	}

	@Override
	public final void visit(final Sheet<T> aSheet) {
		visitNod(aSheet);
	}

	@Override
	public final void visit(final SheetList<T> aSheetList) {
		visitNod(aSheetList);
	}

	protected abstract void visitNod(Node0<T> node);

}
