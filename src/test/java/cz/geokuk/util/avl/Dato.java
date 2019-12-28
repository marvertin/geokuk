package cz.geokuk.util.avl;

import java.util.function.BinaryOperator;

import lombok.Data;

/** Dato, u kterého se porovnává jen jen čísl oa druhé ne */
@Data
public class Dato implements Comparable<Dato> {

	static BinaryOperator<Dato> MERGER = (x, y) -> new Dato(x.getCis(), x.getText() + y.getText());


	public final int cis;
	public final String text;

	@Override
	public int compareTo(final Dato o) {

		return cis < o.cis ? -1 : cis > o.cis ? 1 : 0;
	}


	@Override
	public String toString() {
		return cis + text;
	}



}
