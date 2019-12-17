package cz.geokuk.util.yndex2d;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.function.Consumer;

import com.google.common.math.LongMath;
import com.google.common.primitives.Ints;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class Sheet<T> extends NodeB<T> {


	final int xx;
	final int yy;

	final Lst<T> mapobj;
	/**
	 * @param aMapobj
	 */
	public Sheet(final int xx, final int yy,
			final int xx1, final int yy1, final int xx2, final int yy2, final Lst<T> mapobj) {
		super(xx1, yy1, xx2, yy2, mapobj == null ? 0 : mapobj.count);
		this.xx = xx;
		this.yy = yy;

		this.mapobj = mapobj;
		checkRozsah(xx,yy);
	}

	@Override
	boolean isSheet() {
		return true;
	}

	T get() {
		return mapobj.value;
	}

	@Override
	void vypis(final String aPrefix, final int aLevel) {
		final String mezery = String.format("%" + aLevel * 2 + "s", " ");
		log.debug("{}{}: [{},{}] {}", mezery, aPrefix, xx, yy, mapobj);
	}

	@Override
	public String toString() {
		return "{" + xx + " " + yy + " " + mapobj + "}";
	}




	@Override
	Ctverecnik<T> rozčtvrť() {
		final Empty<T> e = Empty.get();

		// Tady je dělení čtverců a je to jediné místo
		// teoreticky by to fungovalo i kdybychom nedělili na poloviny, třeba kvůli zaměření na českou republiku
		final int xMid = Ints.checkedCast(calculateMid(xx1, xx2));
		final int yMid = Ints.checkedCast(calculateMid(yy1, yy2));

		if (xx < xMid && yy < yMid) {
			return newCtverecnik(zahraň(xx1, yy1, xMid, yMid), e, e, e);
		} else if (xx >= xMid && yy < yMid) {
			return newCtverecnik(e, zahraň(xMid, yy1, xx2, yMid), e, e);
		} else if (xx < xMid && yy >= yMid) {
			return newCtverecnik(e, e, zahraň(xx1, yMid, xMid, yy2), e);
		} else if (xx >= xMid && yy >= yMid) {
			return newCtverecnik(e, e, e, zahraň(xMid, yMid, xx2, yy2));
		} else {
			throw new AssertionError("Ani jedna podminka nezabrala, podivne: " + this);
		}
	}

	/**
	 * Calculates the middle point between {@code a} and {@code b}. If the difference is odd, the lower bound is returned.
	 */
	private static long calculateMid(final long a, final long b) {
		return LongMath.checkedAdd(a, b) / 2;
	}

	/**
	 * Odvoď stejný sheet, ale s jinými hodnotami gran.
	 * @param xx1
	 * @param yy1
	 * @param xx2
	 * @param yy2
	 * @return
	 */
	private Sheet<T> zahraň(final int xx1, final int yy1, final int xx2, final int yy2) {
		return new Sheet<T>(xx, yy, xx1, yy1, xx2, yy2, mapobj);
	}

	private Sheet<T> with(final T mapobj) {
		return with(new Lst<>(mapobj));
	}

	private Sheet<T> with(final Lst<T> mapobj) {
		return new Sheet<T>(xx, yy, xx1, yy1, xx2, yy2, mapobj);
	}

	private void checkRozsah(final int xx, final int yy) {
		checkArgument(!(xx < xx1 || xx >= xx2 || yy < yy1 || yy >= yy2), "Hodnoty %s %s jsou mimo rozsah %s", xx, yy, this);
	}

	static class Lst<T> {
		final T value;
		final Lst<T> next;
		final int count;

		public Lst(final T value) {
			this(value, null);
		}

		public Lst(final T value, final Lst<T> next) {
			if (value == null) {
				throw new NullPointerException("Nesmi byt null v hodnotách ctvrecnickych");
			}
			this.value = value;
			this.next = next;
			count = next == null ? 1 : 1 + next.count;
		}


		static <T> Lst<T> join(final Lst<T> left, final Lst<T> right) {
			if (left == null) {
				return right;
			}
			if (right == null) {
				return left;
			}
			if (left.count > right.count) {
				System.out.println("vym");
				return join(right, left); // vždy kvůli rychlosti menší velvo
			}
			// ani jeden není null, musíme spojit
			return new Lst<>(left.value, join(left.next, right));

		}

		@Override
		public String toString() {
			if (count == 1) {
				return value.toString();
			} else {
				return value + " ... more " + count;
			}
		}


	}


	@Override
	boolean hasSameCoordinates(final Node<T> node) {
		if (node instanceof Sheet<?>) {
			final Sheet<T> sheet = (Sheet<T>) node;
			return xx == sheet.xx && yy == sheet.yy;
		} else {
			return false;
		}
	}

	@Override
	Node<T> joinWithSameCoordinates(final Node<T> node) {
		return with(Lst.join(((Sheet<T>)node).mapobj, mapobj));
	}

	/**
	 * Když je tam, redukuje se na to, když je venku tak prázdný.
	 */
	@Override
	Node<T> bound(final BoundingRect rect) {
		final boolean tenObjektJeUvnitr = xx >= rect.xx1 && xx < rect.xx2 && yy >= rect.yy1 && yy < rect.yy2;
		return tenObjektJeUvnitr ? this : Empty.get();
	}

	@Override
	boolean tryAdvance(final MySplitIterator<T> splititerator,  final Consumer<? super Sheet<T>> action) {
		if (mapobj == null) { // nic už nemáme
			return splititerator.tryAdvance(action);
		} else {
			action.accept(with(mapobj.value));
			splititerator.push(with(mapobj.next)); // a pushneme s jedním zlikvidovaným objektem
			return true; // a něco se zpracovalo
		}
	}

	@Override
	Splitenec<T> trySplit(final int pocetVedle) {
		log.debug("splituje sheet, nesplituje, dlouho by trvalo");
		return new Splitenec<T>(this, Empty.get());
	}
}