package cz.geokuk.util.avl;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;


public class MySplitIterator<T extends Comparable<T>> implements Spliterator<T> {

	private Tree<T> treex;
	private Stack<T> stack;
	private boolean advancnuto;

	public MySplitIterator(final Tree<T> tree) {
		this.treex = tree;
		stack = tree == null ? null : new Stack<>(tree, null);
	}

	@Override
	public boolean tryAdvance(final Consumer<? super T> action) {
		if (stack == null) {
			return false; // doiterováno jest
		}
		advancnuto = true;
		return pop().tryAdvance(this, action);
	}


	@Override
	public long estimateSize() {
		return treex.count;
	}

	@Override
	public int characteristics() {
		return IMMUTABLE | SIZED | SUBSIZED | NONNULL | SORTED | ORDERED;
	}


	/** Vytáhn z vrcholu stacku, spadne, pokdu tam nic není */
	private Tree<T> pop() {
		final Tree<T> tree = stack.tree;
		stack = stack.next;
		return tree;
	}

	/** Vloží na vrchol stacku */
	void push(final Tree<T> tree) {
		stack = new Stack<>(tree, stack);
	}

	/** Vloží na vrchol stacku */
	void push(final T val) {
		stack = new Stack<T>(new OnlyValue<T>(val), stack);
	}

	@RequiredArgsConstructor
	private static class Stack<T extends Comparable<T>> {
		private final Tree<T> tree;
		private final Stack<T> next;

	}

	@Override
	public Spliterator<T> trySplit() {
		if (advancnuto) {
			throw new IllegalStateException("Cannot split iterator which was advanced!");
		}
		if (treex.count <= 2) {
			return null; // když je jich málo, nebudeme splitovat
		}
		final Node<T> node = (Node<T>) treex; // We all can do it because the tree is not empty
		final Tree<T> tree1 = node.left; // left part of tree
		final Tree<T> tree2 = Avl.node(Avl.empty(), node.value, node.right); // right part of tree
		treex = tree2; // this iterator will iterate over right part
		stack =  new Stack<>(tree2, null);
		return new MySplitIterator<T>(tree1); // and this over left path
	}

	@RequiredArgsConstructor
	static class OnlyValue<T extends Comparable<T>> extends Empty<T> {

		final T value;

		@Override
		protected boolean tryAdvance(final MySplitIterator<T> mySplitIterator, final Consumer<? super T> action) {
			action.accept(value);
			return true;
		}

	}

	@Override
	public Comparator<? super T> getComparator() {
		return null; // pořadí je dáno přes compareto
	}
}
