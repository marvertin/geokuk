package cz.geokuk.util.avl;

/**
 * Sestavovač imutabilního AVL stromu. Strom je už během sestavování vyvážen.
 * @author Martin
 *
 */
public class Avl {

	static int citac;

	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>> Empty<T> empty() {
		return (Empty<T>) Tree.EMPTY;
	}

	public static <T extends Comparable<T>> Node<T> node(final Tree<T> left , final T val, final Tree<T> right) {
		citac++;
		final int v = right.height - left.height;
		if (v <= -2) { // levý strom je hlubší, rotujeme doprava
			if (left.vyvazenost() <= 0) {
				return rotateRight(left, val, right);
			} else {
				return doubleRotateLeftRight(left, val, right);
			}
		} else if (v >= 2) { // pravý podstrom je hlubší, rotujeme doleva
			if (right.vyvazenost() >= 0) {
				return rotateLeft(left, val, right);
			} else {
				return doubleRotateRightLeft(left, val, right);
			}
		} else {
			return new Node<T>(left, val, right); // vyvážení není potřeba, je vyváženo, takže můžeme zřídit
		}
	}


	/**
	 * Quickly return balanced tree from sorted array.
	 * @param <T>
	 * @param pole Must contains only distinct values an must be sorted.
	 * @param start Start index inclusive
	 * @param end End index exclusive.
	 * @return
	 */
	public static <T extends Comparable<T>> Tree<T> fromSorted(final T[] pole, final int start, final int end) {
		if (start >= end) {
			return empty();
		} else if (end - start == 1) {
			return new Node<>(empty(), pole[start], empty());
		} else {
			final int centr = (start + end) / 2;
			return new Node<>( fromSorted(pole, start, centr), pole[centr], fromSorted(pole, centr + 1, end));
		}
	}


	private static <T extends Comparable<T>> Node<T> rotateRight(final Tree<T> xLeft, final T xVal, final Tree<T> xRight) {
		// Označení je voleno dloe obrázku v http://ksp.mff.cuni.cz/kucharky/vyhledavaci-stromy/
		final Node<T> y = (Node<T>) xLeft;
		final Tree<T> a = y.left;
		final Tree<T> b = y.right;
		final Tree<T> c = xRight;

		return node(a, y.value, node(b, xVal, c));
	}

	private static <T extends Comparable<T>> Node<T> rotateLeft(final Tree<T> xLeft, final T xVal, final Tree<T> xRight) {
		// Označení je voleno zrcadlově k obrázku v http://ksp.mff.cuni.cz/kucharky/vyhledavaci-stromy/
		final Node<T> y = (Node<T>) xRight;
		final Tree<T> a = y.right;
		final Tree<T> b = y.left;
		final Tree<T> c = xLeft;

		return node(node(c, xVal, b), y.value, a);
	}



	private static <T extends Comparable<T>> Node<T> doubleRotateLeftRight(final Tree<T> xLeft, final T xVal, final Tree<T> xRight) {
		// Označení je voleno dloe obrázku v http://ksp.mff.cuni.cz/kucharky/vyhledavaci-stromy/
		final Node<T> y = (Node<T>) xLeft;
		final Node<T> z = (Node<T>) y.right;
		final Tree<T> a = y.left;
		final Tree<T> b = z.left;
		final Tree<T> c = z.right;
		final Tree<T> d = xRight;

		return node(node(a, y.value, b), z.value, node(c, xVal, d));
	}

	private static <T extends Comparable<T>> Node<T> doubleRotateRightLeft(final Tree<T> xLeft, final T xVal, final Tree<T> xRight) {
		// Označení je voleno zrcadlověk obrázku v http://ksp.mff.cuni.cz/kucharky/vyhledavaci-stromy/
		final Node<T> y = (Node<T>) xRight;
		final Node<T> z = (Node<T>) y.left;
		final Tree<T> a = y.right;
		final Tree<T> b = z.right;
		final Tree<T> c = z.left;
		final Tree<T> d = xLeft;

		return node(node(d, xVal, c), z.value, node(b, y.value, a));
	}

}
