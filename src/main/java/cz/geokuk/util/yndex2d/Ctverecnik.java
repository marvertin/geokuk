package cz.geokuk.util.yndex2d;

import java.util.function.Consumer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class Ctverecnik<T> extends NodeB<T> {


	final Node<T> jz;
	final Node<T> jv;
	final Node<T> sz;
	final Node<T> sv;

	public Ctverecnik(final int xx1, final int yy1, final int xx2, final int yy2,
			final Node<T> jz, final Node<T> jv, final Node<T> sz, final Node<T> sv) {
		super(xx1, yy1, xx2, yy2, jz.count + jv.count + sz.count + sv.count);
		assert xx1 < xx2 && yy1 < yy2;
		this.jz = jz;
		this.jv = jv;
		this.sz = sz;
		this.sv = sv;
	}

	/**
	 * Stejé boundery, ale jiné vnitřky. Vrací empty, místo čtverečníku prázdného.
	 */
	public Node<T> with(final Node<T> jz, final Node<T> jv, final Node<T> sz, final Node<T> sv) {
		return jz.isEmpty() && jv.isEmpty() && sz.isEmpty() && sv.isEmpty()
				? Empty.get()
						: new Ctverecnik<T>(xx1, yy1, xx2, yy2, jz, jv, sz, sv);
	}


	@Override
	public String toString() {
		return "Ctverecnik [xx1=" + xx1 + ", yy1=" + yy1 + ", xx2=" + xx2 + ", yy2=" + yy2 + "]";
	}


	@Override
	boolean isSheet() {
		return false;
	}

	@Override
	void vypis(final String prefix, final int aLevel) {
		final String mezery = String.format("%" + aLevel * 2 + "s", " ");
		log.debug("{}{}: ({}) [{},{}] - [{},{}]", mezery, prefix, count, xx1, yy1, xx2, yy2);
		podvypis(jz, "jz", aLevel + 1);
		podvypis(jv, "jv", aLevel + 1);
		podvypis(sz, "sz", aLevel + 1);
		podvypis(sv, "sv", aLevel + 1);
	}

	private void podvypis(final Node<T> aNode, final String aPrefix, final int aLevel) {
		if (aNode == null) {
			final String mezery = String.format("%" + aLevel * 2 + "s", " ");
			log.debug("{}{}: null", mezery, aPrefix);
		} else {
			aNode.vypis(aPrefix, aLevel);
		}
	}

	/**
	 * Není co čtvrtit, už je to rozčtvrceno.
	 */
	@Override
	Ctverecnik<T> rozčtvrť() {
		return this;
	}

	@Override
	boolean hasSameCoordinates(final Node<T> node) {
		return false; // čtverečník má toho moc tak nemá stejné souřadnice s ničím
	}

	@Override
	Node<T> joinWithSameCoordinates(final Node<T> node) {
		throw new IllegalStateException("Vůbec se nemělo volat joinování zde");
	}

	@Override
	Node<T> bound(final BoundingRect rect) {
		final boolean jeToMimo = xx1 >= rect.xx2 || xx2 <= rect.xx1 || yy1 >= rect.yy2 || yy2 <= rect.yy1;
		if (jeToMimo) {
			return Empty.get();
		}

		final boolean jeToKompletUvnitr = xx1 >= rect.xx1 && xx2 <= rect.xx2 && yy1 >= rect.yy1 && yy2 <= rect.yy2;		// TODO Auto-generated method stub
		if (jeToKompletUvnitr) {
			return this;
		}
		return with(
				jz.bound(rect),
				jv.bound(rect),
				sz.bound(rect),
				sv.bound(rect)
				)
				;
	}

	@Override
	boolean tryAdvance(final MySplitIterator<T> splititerator,  final Consumer<? super Sheet<T>> action) {
		// zde hodnotu nemáme, ale dáme vše pod sebou na stack a zkusíme jít dál
		splititerator.push(jz);
		splititerator.push(jv);
		splititerator.push(sz);
		splititerator.push(sv);
		return splititerator.tryAdvance(action);
	}

	@Override
	Splitenec<T> trySplit(final int pocetVedle) {
		final Node<T> nej = nejvetsi();
		final int rozdilKdyzSplitnuZde = Math.abs(pocetVedle - count);
		final int pristiPocetvedle = count - nej.count + pocetVedle;
		final int rozdilKdyzPujduDolu =  Math.abs(pristiPocetvedle - nej.count);
		log.debug("Ctverecnik.trySplit: pocetVedle = {}   pocetTady = {},  nej = {}  rozdilKdyzPujduDolu = {}  rozdilKdyzSplitnuZde = {}", pocetVedle, count, nej.count,  rozdilKdyzPujduDolu, rozdilKdyzSplitnuZde);
		if (rozdilKdyzPujduDolu <= rozdilKdyzSplitnuZde) { // půjdu dolů i když je to shodné, to je  pro případ, že mám vše v jednom rohu
			final Splitenec<T> spl = nej.trySplit(pristiPocetvedle);
			final Node<T> __ = spl.nahrazenec;
			final Node<T> odriz = spl.odriznuto;
			if (nej == jz) {
				return new Splitenec<>(odriz, with(__, jv, sz, sv));
			}
			if (nej == jv) {
				return new Splitenec<>(odriz, with(jz, __, sz, sv));
			}
			if (nej == sz) {
				return new Splitenec<>(odriz, with(jz, jv, __, sv));
			}
			if (nej == sv) {
				return new Splitenec<>(odriz, with(jz, jv, sz, __));
			}
			throw new IllegalStateException("Nenastane");
		} else { // splitnu zde
			return new Splitenec<>(this, Empty.get());
		}
	}

	private Node<T> nejvetsi() {
		Node<T> max = jz;
		if (jv.count > max.count) {
			max = jv;
		}
		if (sz.count > max.count) {
			max = sz;
		}
		if (sv.count > max.count) {
			max = sv;
		}
		return max;
	}
}
