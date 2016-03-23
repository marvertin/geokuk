/**
 *
 */
package cz.geokuk.util.index2d;

/**
 * @author veverka
 *
 */
public abstract class SloucenyVisitor<T> implements Visitor<T> {

	/*
	 * (non-Javadoc)
	 *
	 * @see objekty.Visitor#visit(objekty.Ctverecnik)
	 */
	@Override
	public final void visit(final Ctverecnik<T> aCtver) {
		visitNod(aCtver);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see objekty.Visitor#visit(objekty.Sheet)
	 */
	@Override
	public final void visit(final Sheet<T> aSheet) {
		visitNod(aSheet);
	}

	protected abstract void visitNod(Node0<T> node);

}
