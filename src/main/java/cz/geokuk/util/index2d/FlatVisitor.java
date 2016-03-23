/**
 *
 */
package cz.geokuk.util.index2d;

/**
 * Visitne všechny cílové objekty.
 * 
 * @author veverka
 *
 */
public abstract class FlatVisitor<T> implements Visitor<T> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see objekty.Visitor#visit(objekty.Ctverecnik)
	 */
	public final void visit(Ctverecnik<T> ctver) {
		// dovisitnout to, co nadřízený nevisitnoul
		ctver.visit(null, this);
	}

}
