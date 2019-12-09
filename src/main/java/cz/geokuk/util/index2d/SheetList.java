package cz.geokuk.util.index2d;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SheetList<T> extends Sheet0<T> {

	private static final Logger log = LogManager.getLogger(SheetList.class.getSimpleName());

	private final List<T> objs = new LinkedList<>();

	/**
	 * Převede sheet na sheetlist, aby se dal přidat víc objektů s duplicitními souřadnicemi.
	 *
	 * @param sheet
	 */
	public SheetList(final Sheet<T> sheet) {
		super(sheet.getXx(), sheet.getYy());
		count = 1;
		objs.add(sheet.get());
	}

	@Override
	boolean isSheet() {
		return true;
	}

	@Override
	void visit(final BoundingRect rect, final Visitor<T> aVisitor) {
		if (rect == null || xx >= rect.xx1 && xx < rect.xx2 & yy >= rect.yy1 && yy < rect.yy2) {
			// jsem uvnitr
			aVisitor.visitSheetList(this);
		}
	}

	@Override
	void vypis(final String aPrefix, final int aLevel) {
		final String mezery = String.format("%" + aLevel * 2 + "s", " ");
		log.debug("{}{}: [{},{}] {}", mezery, aPrefix, xx, yy, objs);
	}

	@Override
	public String toString() {
		return "{" + xx + " " + yy + " " + objs + "}";
	}

	public void add(final Sheet<T> aSheet) {
		objs.add(aSheet.get());
		count++;
		assert aSheet.getXx() == xx && aSheet.getYy() == yy : aSheet.getXx() + ": " + xx + " /= " + aSheet.getYy() + ": " + yy;
	}

	List<T> getObjs() {
		return objs;
	}
}
