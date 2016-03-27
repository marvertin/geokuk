package cz.geokuk.util.index2d;

import com.google.common.math.LongMath;
import com.google.common.primitives.Ints;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkArgument;

public class Ctverecnik<T> extends Node0<T> {

	private static final Logger log = LogManager.getLogger(Ctverecnik.class.getSimpleName());
	private Node0<T> jz;
	private Node0<T> jv;
	private Node0<T> sz;

	private Node0<T> sv;
	private final int xx1;
	private final int yy1;
	private final int xx2;
	private final int yy2;

	private final int xMid;
	private final int yMid;

	public Ctverecnik(final int aXx1, final int aYy1, final int aXx2, final int aYy2) {
		xx1 = aXx1;
		yy1 = aYy1;
		xx2 = aXx2;
		yy2 = aYy2;
		xMid = Ints.checkedCast(calculateMid(xx1, xx2));
		yMid = Ints.checkedCast(calculateMid(yy1, yy2));
	}

	@Override
	public String toString() {
		return "Ctverecnik [xx1=" + xx1 + ", yy1=" + yy1 + ", xx2=" + xx2 + ", yy2=" + yy2 + "]";
	}

	@Override
	public void visit(final BoundingRect rect, final Visitor<T> visitor) {
		if (rect != null) {
			final boolean jeToMimo = xx1 >= rect.xx2 || xx2 <= rect.xx1 || yy1 >= rect.yy2 || yy2 <= rect.yy1;
			if (jeToMimo) {
				return;
			}

			final boolean jeToKompletUvnitr = xx1 >= rect.xx1 && xx2 <= rect.xx2 && yy1 >= rect.yy1 && yy2 <= rect.yy2;

			if (jeToKompletUvnitr) {
				visitor.visit(this);
			} else {
				visitPodrizene(rect, visitor);
			}
		} else { // nemáme zadaný obdélník, takže je nekonečný,
			// takže to nemůže být mimo ani kompletně uvnitř
			visitPodrizene(rect, visitor);
		}
	}

	public void vloz(final Sheet<T> sheet, final AtomicBoolean duplicityGuard) {
		final int xx = sheet.getXx();
		final int yy = sheet.getYy();
		checkRozsah(xx, yy);

		if (xx < xMid && yy < yMid) {
			jz = vlozDoPodctverce(jz, sheet, xx1, yy1, xMid, yMid, duplicityGuard);
		} else if (xx >= xMid && yy < yMid) {
			jv = vlozDoPodctverce(jv, sheet, xMid, yy1, xx2, yMid, duplicityGuard);
		} else if (xx < xMid && yy >= yMid) {
			sz = vlozDoPodctverce(sz, sheet, xx1, yMid, xMid, yy2, duplicityGuard);
		} else if (xx >= xMid && yy >= yMid) {
			sv = vlozDoPodctverce(sv, sheet, xMid, yMid, xx2, yy2, duplicityGuard);
		} else {
			throw new AssertionError("Ani jedna podminka nezabrala, podivne: " + this);
		}
		if (!duplicityGuard.get()) {
			count++; // pokud jsem opravdu vložil
		}
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

	private void podvypis(final Node0<T> aNode, final String aPrefix, final int aLevel) {
		if (aNode == null) {
			final String mezery = String.format("%" + aLevel * 2 + "s", " ");
			log.debug("{}{}: null", mezery, aPrefix);
		} else {
			aNode.vypis(aPrefix, aLevel);
		}
	}

	private void visitPodrizene(final BoundingRect rect, final Visitor<T> visitor) {
		if (jz != null) {
			jz.visit(rect, visitor);
		}
		if (jv != null) {
			jv.visit(rect, visitor);
		}
		if (sz != null) {
			sz.visit(rect, visitor);
		}
		if (sv != null) {
			sv.visit(rect, visitor);
		}
	}

	private static <T> Node0<T> vlozDoPodctverce(final Node0<T> node, final Sheet<T> aSheet, final int xx1, final int yy1, final int xx2, final int yy2, final AtomicBoolean duplicityGuard) {
		if (node == null) { // vlozit se tam
			return aSheet;
		} else if (node instanceof Ctverecnik) {
			final Ctverecnik<T> ctver = (Ctverecnik<T>) node;
			ctver.vloz(aSheet, duplicityGuard);
			return node;
		} else if (node instanceof Sheet) {
			final Sheet<T> sheet = (Sheet<T>) node;
			if (sheet.getXx() == aSheet.getXx() && sheet.getYy() == aSheet.getYy()) {
				duplicityGuard.set(true); // takovy uz tam mame
				return node; // beze zmeny
			}
			final Ctverecnik<T> ctver = new Ctverecnik<>(xx1, yy1, xx2, yy2);
			ctver.vloz(sheet, duplicityGuard);
			ctver.vloz(aSheet, duplicityGuard);
			return ctver;
		} else { // item je objektakem
			throw new RuntimeException("Podivny node: " + node.getClass().getName());
		}
	}

	/**
	 * Calculates the middle point between {@code a} and {@code b}. If the difference is odd, the lower bound is returned.
	 */
	private static long calculateMid(long a, long b) {
		return LongMath.checkedAdd(a, b) / 2;
	}

	private void checkRozsah(final int xx, final int yy) {
		checkArgument(!(xx < xx1 || xx >= xx2 || yy < yy1 || yy >= yy2), "Hodnoty %s %s jsou mimo rozsah %s", xx, yy, this);
	}
}
