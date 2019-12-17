package cz.geokuk.util.yndex2d;

import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
abstract class Node<T> {

	final int count;

	abstract boolean isSheet();

	/**
	 * @param aPrefix
	 * @param aLevel
	 */
	abstract void vypis(String aPrefix, int aLevel);

	abstract Ctverecnik<T> rozčtvrť();

	/** Zda má stejné souřasnice jak ozadaný, to může mít jen sheet, nikdo jiný */
	abstract boolean hasSameCoordinates(Node<T> node);

	/** přijoinuje node o kterém víme, že má stejné souřadnice, už to nekontroluje */
	abstract Node<T> joinWithSameCoordinates(Node<T> node);

	/**
	 * Empty je jen jeden a pokud je to on, tak je empty.
	 * @return
	 */
	public boolean isEmpty() {
		return this == Empty.get();
	}

	/**
	 * Zredukuje nód tak, aby obsahoval jen objekty v daném rectaglu.
	 * @param br
	 * @return
	 */
	abstract Node<T> bound(BoundingRect br);


	abstract boolean tryAdvance(MySplitIterator<T> splititerator, final Consumer<? super Sheet<T>> action);

	abstract Splitenec<T> trySplit(int pocetVedle);
}
