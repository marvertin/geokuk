package cz.geokuk.util.avl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class AvlFromSortedTest extends Test0 {

	@Test
	public void test0() {
		assertSame(Avl.empty(), Avl.fromSorted(new Integer[0], 0, 0));
	}

	@Test
	public void tes1() {

		final Integer[] pole = new Integer[1000000];
		for (int i=0; i < pole.length; i++) {
			pole[i] = i;
			addNekontrolujVyvazebost(i);
		}

		final Tree<Integer> tree2 = Avl.fromSorted(pole, 0, pole.length);
		kontrolaVyvazenosti(tree2);
		assertEquals(tree, tree2);

	}

}
