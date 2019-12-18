package cz.geokuk.util.index2d;

/**
 * Předek "B" tedy bounded hodnot, což jsou čtverečníky i sheety.
 * @author veverka
 *
 * @param <T>
 */
abstract class NodeB<T> extends Node<T> {

	final int xx1;
	final int yy1;
	final int xx2;
	final int yy2;

	NodeB(final int aXx1, final int aYy1, final int aXx2, final int aYy2, final int count) {
		super(count);
		xx1 = aXx1;
		yy1 = aYy1;
		xx2 = aXx2;
		yy2 = aYy2;
	}


	Ctverecnik<T> newCtverecnik (final Node<T> jz, final Node<T> jv, final Node<T> sz, final Node<T> sv) {
		return new Ctverecnik<T>(xx1, yy1, xx2, yy2, jz, jv, sz, sv);
	}

}
