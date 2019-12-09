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
	public final void visitCtverecnik(final Ctverecnik<T> aCtver) {
		visitNod(aCtver);
	}

	@Override
	public final void visitSheet(final Sheet<T> aSheet) {
		visitNod(aSheet);
	}

	@Override
	public final void visitSheetList(final SheetList<T> aSheetList) {
		visitNod(aSheetList);
	}

	protected abstract void visitNod(Node0<T> node);

}
