/**
 *
 */
package cz.geokuk.util.index2d;

/**
 * Visitne všechny cílové objekty.
 *
 * @author Martin Veverka
 *
 */
@FunctionalInterface
public interface FlatVisitor<T> extends Visitor<T> {

	/*
	 * (non-Javadoc)
	 *
	 * @see objekty.Visitor#visit(objekty.Ctverecnik)
	 */
	@Override
	public default void visitCtverecnik(final Ctverecnik<T> ctver) {
		// dovisitnout to, co nadřízený nevisitnoul
		ctver.visit(null, this);
	}

	@Override
	public default void visitSheetList(final SheetList<T> sheetList) {
		// když flatujeme, tak visitujeme sheety.
		sheetList.getObjs().forEach(obj -> visitSheet(new Sheet<T>(sheetList.xx, sheetList.yy, obj)));
	}

}
