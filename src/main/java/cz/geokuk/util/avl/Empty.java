package cz.geokuk.util.avl;

import java.util.Collection;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;

class Empty<T extends Comparable<T>> extends Tree<T> {

	Empty() {
		super(0, 0);
	}

	@Override
	public Tree<T> insert(final BinaryOperator<T> valueMerger, final T val) {
		return node(empty(), val, empty());
	}

	@Override
	public Tree<T> delete(final T val) {
		// Z prázdného stromu není co mazat
		return this;
	}

	@Override
	int vyvazenost() {
		return 0;
	}

	@Override
	void kontrolaVyvazenosti(final Collection<? super Tree<T>> nevyvazeneUzly) {
	}

	@Override
	public String toString() {
		return "_E_";
	}

	@Override
	public Tree<T> find(final T val) {
		return empty();
	}

	@Override
	public Optional<T> get() {
		return Optional.empty();
	}

	@Override
	public void forEach(final Consumer<? super T> action) {
	}

	@Override
	protected boolean tryAdvance(final MySplitIterator<T> mySplitIterator, final Consumer<? super T> action) {
		// Jednoduše provoláme, není potřeba nic dělat.
		return mySplitIterator.tryAdvance(action);
	}

	@Override
	public Splited<T> split(final T value) {
		return new Splited<T>(empty(), Optional.empty(), empty());
	}

	@Override
	int hashCode(final int accum) {
		return accum; // prázdné uzly nemají vliv na hashcode
	}



}
