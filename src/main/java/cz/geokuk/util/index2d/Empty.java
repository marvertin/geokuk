package cz.geokuk.util.index2d;

import java.util.function.Consumer;

/**
 * Prázdný ucel nic neobsahující, děláme proto, abychom nemuseli používat null.
 * @author veverka
 *
 * @param <T>
 */
class Empty<T> extends Node<T> {

	@SuppressWarnings("rawtypes")
	private static final Empty EMPTY = new Empty<>();

	// Nelze zkonstruovat
	private Empty() { super(0); }

	@Override
	boolean isSheet() {
		return false;
	}



	@Override
	void vypis(final String aPrefix, final int aLevel) {
	}

	@SuppressWarnings("unchecked")
	public static <T> Empty<T> get() {
		return EMPTY;
	}

	@Override
	Ctverecnik<T> rozčtvrť() {
		// TODO Auto-generated method stub
		throw new IllegalStateException("NIC nelze čtvrtit");
	}

	@Override
	boolean hasSameCoordinates(final Node<T> node) {
		return false; // opravdu nic nemá stejé souřadnice s ničím
	}

	@Override
	Node<T> joinWithSameCoordinates(final Node<T> node) {
		return node; // není co joinovat
	}

	@Override
	Node<T> bound(final BoundingRect br) {
		// Co je prázdné, nemůže být nikdy vyprázdneno.
		return this;
	}

	@Override
	boolean tryAdvance(final MySplitIterator<T> splititerator,  final Consumer<? super Sheet<T>> action) {
		// Jednoduše provoláme, není potřeba nic dělat.
		return splititerator.tryAdvance(action);
	}

	@Override
	public String toString() {
		return "EMPTY";
	}

	@Override
	Splitenec<T> trySplit(final int pocetVedle) {
		return new Splitenec<>(Empty.get(), Empty.get());
	}
}
