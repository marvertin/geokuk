package cz.geokuk.util.avl;

import java.util.Optional;

import cz.geokuk.util.avl.Tree.Splited;
import lombok.RequiredArgsConstructor;

/**
 *  Internal class. Combines thow trees with different semantics.
 * @author veverka
 *
 */
@RequiredArgsConstructor
class TreeCombiner<T extends Comparable<T>> {

	private final EmptyResolver<Tree<T>> leftEr;
	private final ValueCombiner<T> valueCombiner;
	private final EmptyResolver<Tree<T>> rightEr;

	public Tree<T> combine(final Tree<T> tree1, final Tree<T> tree2) {
		if (tree1 == Tree.EMPTY) {
			return leftEr.resolve(tree2); // intersection with nothing is nothing
		}
		if (tree2 == Tree.EMPTY) {
			return rightEr.resolve(tree1);
		}
		final Node<T> node1 = (Node<T>) tree1;
		final Node<T> node2 = (Node<T>) tree2;
		if  (node1.count < node2.count) { // optimalization, better is split lesser, it is more balanced
			final T central = node2.value;
			System.out.println("1central: " + central);
			final Splited<T> spl1 = node1.split(central);
			return join(
					combine(spl1.left, node2.left),
					valueCombiner.combine(spl1.value, Optional.of(central)),
					combine(spl1.right, node2.right));
		} else {
			final T central = node1.value;
			System.out.println("2central: " + central);
			final Splited<T> spl2 = node2.split(central);
			return join(
					combine(node1.left, spl2.left),
					valueCombiner.combine(Optional.of(central), spl2.value),
					combine(node1.right, spl2.right));
		}
	}

	public static <T extends Comparable<T>> Tree<T> join(final Tree<T> left, final Optional<T> value, final Tree<T> right) {
		System.out.println(left + "<----" + value + "---->" + right);
		assert value.isPresent();
		return value.isPresent()
				? Avl.node(left, value.get(), right)
						: Tree.union(left, ValueMergers.throwing(), right);
	}

	interface ValueCombiner<T> {
		Optional<T> combine (Optional<T> left, Optional<T> right);
	}

	interface EmptyResolver<S> {
		S resolve (S opositTree);
	}

	static <T extends Comparable<T>> EmptyResolver<Tree<T>> opositTree() {
		return x -> x;
	}

	static <T extends Comparable<T>> EmptyResolver<Tree<T>> emptyTree() {
		return x -> Avl.empty();
	}

}
