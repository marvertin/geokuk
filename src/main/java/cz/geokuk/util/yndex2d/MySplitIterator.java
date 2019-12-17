package cz.geokuk.util.yndex2d;

import java.util.Spliterator;
import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MySplitIterator<T> implements Spliterator<Sheet<T>> {

	private Node<T> node;
	private Stack<T> stack;
	private boolean advancnuto;

	public MySplitIterator(final Node<T> node) {
		this.node = node;
		stack = node == null ? null : new Stack<>(node, null);
	}

	@Override
	public boolean tryAdvance(final Consumer<? super Sheet<T>> action) {
		if (stack == null) {
			return false; // doiterováno jest
		}
		advancnuto = true;
		return pop().tryAdvance(this, action);
	}

	@Override
	public Spliterator<Sheet<T>> trySplit() {
		if (advancnuto) {
			System.err.println("Nepodporujeme trySpli, když se advanclo");
			return null;
		}
		final Splitenec<T> splitenec = node.trySplit(0);
		if (splitenec.nahrazenec.count == 0 || splitenec.odriznuto.count == 0) {
			log.debug("Nedokazal splitnout: " + node.count);
			return null; // vlastně jsme nesplitli
		}
		if (log.isDebugEnabled()) {
			System.out.println("------------((-------------------");
			System.out.println("SPLITNUTO: " + splitenec.nahrazenec.count + "|" + splitenec.odriznuto.count);
			if (log.isTraceEnabled()) {
				node.vypis("node", 1);
				splitenec.odriznuto.vypis("odriznuto", 1);
				splitenec.nahrazenec.vypis("nahrazenec", 1);
			}
			System.out.println("------------))-------------------");
		}

		node = splitenec.nahrazenec;
		stack = new Stack<>(node, null);
		return new MySplitIterator<T>(splitenec.odriznuto);
	}

	@Override
	public long estimateSize() {
		return node.count;
	}

	@Override
	public int characteristics() {
		return IMMUTABLE | SIZED | SUBSIZED | NONNULL;
	}


	/** Vytáhn z vrcholu stacku, spadne, pokdu tam nic není */
	private Node<T> pop() {
		final Node<T> node = stack.node;
		stack = stack.next;
		return node;
	}

	/** Vloží na vrchol stacku */
	void push(final Node<T> node) {
		stack = new Stack<>(node, stack);
	}

	@RequiredArgsConstructor
	private static class Stack<T> {
		private final Node<T> node;
		private final Stack<T> next;

	}


}
