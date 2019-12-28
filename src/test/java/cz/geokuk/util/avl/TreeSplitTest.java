package cz.geokuk.util.avl;

import static org.junit.Assert.assertEquals;

import java.util.*;
import java.util.stream.Collectors;

import org.junit.Test;

public class TreeSplitTest extends Test0 {


	@Test
	public void testRand1() {
		final Random rnd = new Random(546321l);
		for (int i=0; i< 5000; i++) {
			addNekontrolujVyvazebost(rnd.nextInt(1000));
		}
		assertOk();

		final List<Integer> vysl = tree.stream(false).collect(Collectors.toList());
		final List<Integer> expected = new LinkedList<Integer>(expect);

		assertEquals(expected, vysl);
	}


	@Test
	public void testStreamSeq() {
		for (int i=0; i< 5000; i++) {
			addNekontrolujVyvazebost(i);
		}
		assertOk();

		final List<Integer> vysl = tree.stream(false).collect(Collectors.toList());
		final List<Integer> expected = new LinkedList<Integer>(expect);

		assertEquals(expected, vysl);
	}

	@Test
	public void testStreamPara() {
		for (int i=0; i< 5000; i++) {
			addNekontrolujVyvazebost(i);
		}
		assertOk();

		final List<Integer> vysl = tree.stream(true).collect(Collectors.toList());
		final List<Integer> expected = new LinkedList<Integer>(expect);

		assertEquals(expected, vysl);
	}

}
