package cz.geokuk.util.yndex2d;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Optional;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import cz.geokuk.util.yndex2d.Sheet.Lst;

/**
 * Drží celý index všech objektů na mapě, tedy se dají přes něj dostat i ty objekty.
 *
 * @author Martin Veverka
 *
 */
public class Indexator<T> {

	private final Node<T> root;
	private final BoundingRect br;

	public Indexator(final BoundingRect br) {
		this.br = br;
		root = Empty.get();
	}

	private Indexator(final BoundingRect br, final Node<T> root) {
		this.br = br;
		this.root = root;
	}

	private Indexator<T> with(final Node<T> root) {
		return new Indexator<T>(br, root);
	}

	public Optional<T> locateAnyOne() {
		return stream().findFirst();
	}

	public Stream<T> stream() {
		return streamSheet(false).map(Sheet::get);
	}

	public Stream<T> parallelStream() {
		return streamSheet(true).map(Sheet::get);
	}

	public Optional<T> locateNearestOne(final int xx, final int yy) {
		return streamSheet(true)
				.min( (s1, s2) -> {
					final long dx1 = s1.xx - xx;
					final long dy1 = s1.yy - yy;
					final long d1 = dx1 * dx1 + dy1 * dy1;
					final long dx2 = s2.xx - xx;
					final long dy2 = s2.yy - yy;
					final long d2 = dx2 * dx2 + dy2 * dy2;
					return (int) (d1 - d2);
				})
				.map(Sheet::get);

	}

	/** Celkový počet objektů uvnitř */
	public int getCount() {
		return root.count;
	}

	public Indexator<T> add(final int xx, final int yy, final T mapobj) {
		return with(merge(root, newSheet(xx, yy, mapobj)));
	}

	public Indexator<T> merge(final Indexator<T> indexator) {
		checkArgument(checkSameBounding(indexator.root, root), "Bounding %s %s není stejný.", indexator.root, root);
		return with(merge(indexator.root, root));
	}

	/**
	 * Boundne do daného obdélníku a vrátí indexator s objekty omezenými jen na ten obdélník.
	 * @param rect
	 * @return
	 */
	public Indexator<T> bound(final BoundingRect rect) {
		return with(root.bound(rect));
	}


	private Stream<Sheet<T>> streamSheet(final boolean paralel) {
		return StreamSupport.stream(spliterator(), paralel);
	}

	private Spliterator<Sheet<T>> spliterator() {
		return new MySplitIterator<T>(root);
	}

	private Sheet<T> newSheet(final int xx, final int yy, final T mapobj) {
		return new Sheet<T>(xx, yy, br.xx1, br.yy1, br.xx2, br.yy2, new Lst<T>(mapobj));
	}
	public void vypis() {
		root.vypis("root", 1);
	}


	private static <T> Node<T> merge(final Node<T> node1, final Node<T> node2) {
		if (node1.isEmpty()) {
			return node2;
		}
		if (node2.isEmpty()) {
			return node1;
		}
		assert checkSameBounding(node1, node2);
		if (node1.hasSameCoordinates(node2)) {
			return node1.joinWithSameCoordinates(node2);
		} else {
			final Ctverecnik<T> ctv1 = node1.rozčtvrť();
			final Ctverecnik<T> ctv2 = node2.rozčtvrť();

			return ctv1.newCtverecnik(
					merge(ctv1.jz, ctv2.jz),
					merge(ctv1.jv, ctv2.jv),
					merge(ctv1.sz, ctv2.sz),
					merge(ctv1.sv, ctv2.sv));
		}
	}

	private static boolean checkSameBounding(final Node<?> nodea, final Node<?> nodeb) {
		if (nodea.isEmpty() || nodeb.isEmpty()) {
			return true;
		}
		final NodeB<?> na = (NodeB<?>) nodea;
		final NodeB<?> nb = (NodeB<?>) nodeb;
		return na.xx1 == nb.xx1 && na.xx2 == nb.xx2 && na.yy1 == nb.yy1 && na.yy2 == nb.yy2;
	}


}
