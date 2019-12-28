package cz.geokuk.util.avl;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
class Node<T extends Comparable<T>> extends Tree<T> {
	final Tree<T> left;
	final T value;
	final Tree<T> right;

	Node(final Tree<T> left, final T value, final Tree<T> right) {
		super(Math.max(left.height,  right.height) + 1, left.count + 1 + right.count);
		this.left = left;
		this.value = value;
		this.right = right;
	}

	@Override
	public Tree<T> insert(final BinaryOperator<T> valueMerger, final T val) {
		final int comp = val.compareTo(value);
		if (comp < 0) {
			return node(left.insert(valueMerger, val), value, right);
		} else if (comp > 0) {
			return node(left, value, right.insert(valueMerger, val));
		} else {
			return node(left, merge2values(value, valueMerger, val), right);
		}
	}

	@Override
	public Tree<T> delete(final T valueToDelete) {
		final int comp = valueToDelete.compareTo(value);
		if (comp < 0) {
			return node(left.delete(valueToDelete), value, right);
		} else if (comp > 0) {
			return node(left, value, right.delete(valueToDelete));
		} else { // ten náš uzel je ten deletovaný
			if (left == Tree.EMPTY) {
				return right;
			} else if (right == Tree.EMPTY) {
				return left;
			} else { // máme oba, bude to složitější
				final Vymazanec<T> vym = vymazNejvetsi((Node<T>) left);
				return node(vym.tree, vym.vymazan, right);
			}
		}
	}

	/**
	 * < 0 .... levý hlubší
	 * == 0 ... stejě hluboké
	 * > 0 .... pravý hlubší
	 */
	@Override
	int vyvazenost() {
		return right.height - left.height;
	}




	@Override
	void kontrolaVyvazenosti(final Collection<? super Tree<T>> nevyvazeneUzly) {
		left.kontrolaVyvazenosti(nevyvazeneUzly);
		final int vyv = vyvazenost();
		if (vyv < -1 || vyv > 1) {
			nevyvazeneUzly.add(this);
		}
		right.kontrolaVyvazenosti(nevyvazeneUzly);
	}


	@RequiredArgsConstructor
	private static class Vymazanec<T extends Comparable<T>> {
		final Tree<T> tree;
		final T vymazan;
	}


	private static <T extends Comparable<T>> Vymazanec<T> vymazNejvetsi(final Node<T> node) {
		if (node.right == EMPTY) {
			return new Vymazanec<>(node.left, node.value);
		} else {
			final Vymazanec<T> vym = vymazNejvetsi((Node<T>) node.right);
			return new Vymazanec<>(node(node.left, node.value, vym.tree), vym.vymazan);
		}
	}

	@Override
	public Tree<T> find(final T valueToFined) {
		final int comp = valueToFined.compareTo(value);
		if (comp < 0) {
			return left.find(valueToFined);
		} else if (comp > 0) {
			return right.find(valueToFined);
		} else { // když se to rovná, našli jsme
			return this;
		}
	}

	@Override
	public Optional<T> get() {
		return Optional.of(value);
	}

	@Override
	public void forEach(final Consumer<? super T> action) {
		left.forEach(action);
		action.accept(value);
		right.forEach(action);
	}

	@Override
	protected boolean tryAdvance(final MySplitIterator<T> mySplitIterator, final Consumer<? super T> action) {
		mySplitIterator.push(right);
		mySplitIterator.push(value);
		mySplitIterator.push(left);
		return mySplitIterator.tryAdvance(action);
	}

	@Override
	public Splited<T> split(final T val) {
		final int comp = val.compareTo(value);
		if (comp < 0) {
			final Splited<T> splited = left.split(val);
			return new Splited<>(splited.left, splited.value, node(splited.right, value, right));
		} else if (comp > 0) {
			final Splited<T> splited = right.split(val);
			return new Splited<>(node(left, value, splited.left), splited.value, splited.right);
		} else {
			return new Splited<>(left, Optional.of(value), right);
		}
	}

	@Override
	int hashCode(final int accum) {
		return right.hashCode(left.hashCode(accum) * 31 + value.hashCode());
	}

}
