package cz.geokuk.util.avl;

import java.util.function.BinaryOperator;

/**
 * define common mergers
 * @author veverka
 *
 */
public class Mergers {

	public static <T extends Comparable<T>> BinaryOperator<T> throwing() {
		return (x, y) -> {
			throw new IllegalStateException("Expected distincvalues in trees: " + x + " | " + y);
		};
	}

	public static  <T extends Comparable<T>> BinaryOperator<T> onlyLeft() {
		return (x, y) -> x;
	}

	public static  <T extends Comparable<T>> BinaryOperator<T> onlyRight() {
		return (x, y) -> y;
	}

}
