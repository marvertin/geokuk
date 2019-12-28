package cz.geokuk.util.avl;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.*;

@ToString
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class Tree<T extends Comparable<T>> {

	public static Empty<?> EMPTY = new Empty<>();

	public final int height;
	public final int count;

	public abstract Tree<T> insert(BinaryOperator<T> valueMerger, T val);

	public abstract Tree<T> delete(T val);

	public abstract Tree<T> find(T val);

	public abstract Optional<T> get();

	public abstract void forEach (Consumer<? super T> action);

	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>> Empty<T> empty() {
		return (Empty<T>) EMPTY;
	}

	///// Interní

	abstract int vyvazenost();

	final void drainTo(final Collection<? super T> collection) {
		forEach(collection::add);
	}

	abstract void kontrolaVyvazenosti(final Collection<? super Tree<T>> nevyvazeneUzly);

	static final <T extends Comparable<T>> Node<T> node(final Tree<T> left , final T val, final Tree<T> right) {
		return Avl.node(left,  val,  right);
	}

	protected abstract boolean tryAdvance(MySplitIterator<T> mySplitIterator, Consumer<? super T> action);


	Stream<T> stream(final boolean paralel) {
		return StreamSupport.stream(spliterator(), paralel);
	}

	private Spliterator<T> spliterator() {
		return new MySplitIterator<T>(this);
	}

	public abstract Splited<T> split(T t);

	public static <T extends Comparable<T>> Tree<T> merge(final Tree<T> tree1, final BinaryOperator<T> valueMerger, final Tree<T> tree2) {
		if (tree1 == EMPTY) {
			return tree2;
		}
		if (tree2 == EMPTY) {
			return tree1;
		}
		final Node<T> node1 = (Node<T>) tree1;
		final Node<T> node2 = (Node<T>) tree2;

		if  (node1.count < node2.count) { // optimalization, better is split lesser, it is more balanced
			final T central = node2.value;
			final Splited<T> spl1 = node1.split(central);
			final T mergedValue = spl1.value.map(val1 -> merge2values(val1, valueMerger, central)).orElse(central);
			//			assert mergedValue.compareTo(central) == 0 : "Value merger must return same comparable value: " + mergedValue + " != merge(" + spl1.v + ", " + val + ")";

			return node(merge(spl1.left, valueMerger, node2.left), mergedValue, merge(spl1.right, valueMerger, node2.right));
		} else {
			final T central = node1.value;
			final Splited<T> spl2 = node2.split(central);
			final T mergedValue = spl2.value.map(val2 -> merge2values(central, valueMerger, val2)).orElse(central);
			return node(merge(node1.left, valueMerger, spl2.left), mergedValue, merge(node1.right, valueMerger, spl2.right));
		}
	}

	public static <T extends Comparable<T>> boolean equals(final Tree<T> tree1, final Tree<T> tree2) {
		if (tree1 == EMPTY) {
			if (tree2 == EMPTY) {
				return true;
			} else {
				return false;
			}
		} else {
			if (tree2 == EMPTY) {
				return false;
			} else {
				final Node<T> node1 = (Node<T>) tree1;
				final Node<T> node2 = (Node<T>) tree2;
				if  (tree1.count < tree2.count) { // optimalization, better is split lesser, it is more balanced
					final Splited<T> spl1 = node1.split(node2.value);
					return spl1.value.isPresent() && equals(spl1.left, node2.left) && equals(spl1.right, node2.right);
				} else {
					final Splited<T> spl2 = node2.split(node1.value);
					return spl2.value.isPresent() && equals(node1.left, spl2.left) && equals(node1.right, spl2.right);
				}
			}

		}
	}

	abstract int hashCode(int accum);

	@Override
	public int hashCode() {
		return hashCode(0);
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("unchecked")
		final Tree<T> other = (Tree<T>) obj;
		if (count != other.count) {
			return false;
		}
		if (height != other.height) {
			return false;
		}
		return equals(this, other);
	}



	static <T extends Comparable<T>> T merge2values(final T val1, final BinaryOperator<T> valueMerger, final T val2) {
		final T result = valueMerger.apply(val1, val2);
		assert result.compareTo(val1) == 0 && result.compareTo(val2) == 0 : "Value merger must return same comparable value: " + result + " != merge(" + val1 + ", " + val2 + ")";
		return result;
	}

	@RequiredArgsConstructor
	@ToString
	public static class Splited<T extends Comparable<T>> {
		public final Tree<T> left;
		public final Optional<T> value;
		public final Tree<T> right;
	}



}
