package cz.geokuk.util.index2d;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Drží celý index všech objektů na mapě, tedy se dají přes něj dostat i ty objekty.
 *
 * @author Martin Veverka
 *
 */
public class Indexator<T> {

	private final Ctverecnik<T> root;

	public Indexator(final BoundingRect br) {
		root = new Ctverecnik<>(br.xx1, br.yy1, br.xx2, br.yy2);
	}

	public int count(final BoundingRect boundingRect) {
		final AtomicInteger counter = new AtomicInteger();
		root.visit(boundingRect, (SloucenyVisitor<T>) aNode -> counter.addAndGet(aNode.count));
		return counter.get();
	}

	public Sheet<T> locateAnyOne(final BoundingRect br) {
		try {
			visit(br, (FlatVisitor<T>) sheet -> {
				throw new XNalezeno(sheet);
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
		visit(br, (FlatVisitor<T>) sheet -> {
			final long dx = sheet.getXx() - xx;
			final long dy = sheet.getYy() - yy;
			final long d2 = dx * dx + dy * dy;
			if (d2 < drzak.d2) {
				drzak.d2 = d2;
				drzak.tt = sheet;
			}
		});
		return drzak.tt;
	}

	public void visit(final BoundingRect boundingRect, final Visitor<T> visitor) {
		root.visit(boundingRect, visitor);
	}

	public void vloz(final int xx, final int yy, final T mapobj) {
		final Sheet<T> sheet = new Sheet<>(xx, yy, mapobj);
		root.vloz(sheet);
	}

	public void vypis() {
		root.vypis("root", 1);
	}
}
