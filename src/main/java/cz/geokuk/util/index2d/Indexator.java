/**
 *
 */
package cz.geokuk.util.index2d;

import java.util.ArrayList;
import java.util.List;

import cz.geokuk.util.index2d.Ctverecnik.DuplikHlidac;

/**
 * Drží celý index všech objektů na mapě, tedy se dají přes něj dostat i ty objekty.
 *
 * @author Martin Veverka
 *
 */
public class Indexator<T> {

	/**
	 *
	 */
	Ctverecnik<T> root;

	public Indexator() {
		this(new BoundingRect(0, 0, Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2));
	}

	public Indexator(final BoundingRect br) {
		root = new Ctverecnik<>(br.xx1, br.yy1, br.xx2, br.yy2);
	}

	public int count(final BoundingRect boundingRect) {
		final int[] counta = new int[1];
		root.visit(boundingRect, new SloucenyVisitor<T>() {
			@Override
			protected void visitNod(final Node0<T> aNode) {
				counta[0] += aNode.count;
			}
		});
		return counta[0];
	}

	public List<Sheet<T>> deepList(final BoundingRect boundingRect) {
		final List<Sheet<T>> list = new ArrayList<>(100);
		root.visit(boundingRect, new FlatVisitor<T>() {
			@Override
			public void visit(final Sheet<T> aSheet) {
				list.add(aSheet);
			}
		});
		return list;
	}

	public boolean checkRozsah(final int xx, final int yy) {
		return !(xx < root.getXx1() || xx >= root.getXx2() || yy < root.getYy1() || yy >= root.getYy2());
	}

	public Sheet<T> locateAnyOne(final BoundingRect br) {
		try {
			visit(br, new FlatVisitor<T>() {
				@Override
				public void visit(final Sheet<T> sheet) {
					throw new XNalezeno(sheet);
				}
			});
			return null; // nevypadla výjimka, nebylo nalezeno
		} catch (final XNalezeno e) { // bylo něco nalezeno
			@SuppressWarnings("unchecked") // vyjimka nemuze byt genericka
			final Sheet<T> sheet = (Sheet<T>) e.sheet;
			return sheet;
		}

	}

	public Sheet<T> locateNearestOne(final BoundingRect br, final int xx, final int yy) {
		class Drzak {
			Sheet<T> tt;
			long d2 = Long.MAX_VALUE;
		}
		final Drzak drzak = new Drzak();
		visit(br, new FlatVisitor<T>() {
			@Override
			public void visit(final Sheet<T> sheet) {
				final long dx = sheet.xx - xx;
				final long dy = sheet.yy - yy;
				final long d2 = dx * dx + dy * dy;
				if (d2 < drzak.d2) {
					drzak.d2 = d2;
					drzak.tt = sheet;
				}
			}
		});
		return drzak.tt;

	}

	public List<Node0<T>> shallowList(final BoundingRect boundingRect) {
		final List<Node0<T>> list = new ArrayList<>(100);
		root.visit(boundingRect, new SloucenyVisitor<T>() {
			@Override
			protected void visitNod(final Node0<T> aNode) {
				list.add(aNode);
			}
		});
		return list;
	}

	public void visit(final BoundingRect boundingRect, final Visitor<T> visitor) {
		root.visit(boundingRect, visitor);
	}

	public void vloz(final int xx, final int yy, final T mapobj) {
		if (!checkRozsah(xx, yy)) {
			throw new RuntimeException("Hodnoty " + xx + " " + yy + " jsou mimo rozsah " + root);
		}

		Sheet<T> sheet = new Sheet<>(xx, yy, mapobj);
		DuplikHlidac duplikHlidac = new Ctverecnik.DuplikHlidac();
		root.vloz(sheet, duplikHlidac);
		while (duplikHlidac.duplicita) {
			// throw new RuntimeException("Duplicita");
			sheet = new Sheet<>(sheet.xx + 3, sheet.yy + 7, mapobj);
			duplikHlidac = new Ctverecnik.DuplikHlidac();
			root.vloz(sheet, duplikHlidac);
			// System.out.println("Duplicita resena " + mapobj);
		}
	}

	public void vypis() {
		root.vypis("root", 1);
	}

}
