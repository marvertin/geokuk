package cz.geokuk.util.avl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.*;

import org.junit.Test;

public class TreeEqualsTest extends Test0 {

	@Test
	public void test00() {
		assertTrue(tree.equals(tree));
		assertEquals(tree.hashCode(), tree.hashCode());
	}

	@Test
	public void test0() {
		add(1, 2, 3, 4);
		assertTrue(tree.equals(tree));
		assertEquals(tree.hashCode(), tree.hashCode());
	}

	@Test
	public void test1() {
		add(1, 2, 3, 4);
		final Tree<Integer> tree2 = getAndReset();
		add(1, 2, 3, 4);
		assertTrue(tree.equals(tree2));
		assertTrue(tree2.equals(tree));
		assertEquals(tree.hashCode(), tree2.hashCode());
	}

	@Test
	public void test2() {
		add(2, 1, 3, 4);
		final Tree<Integer> tree2 = getAndReset();
		add(2, 3, 1, 4);
		assertTrue(tree.equals(tree2));
		assertTrue(tree2.equals(tree));
		assertEquals(tree.hashCode(), tree2.hashCode());
	}

	@Test
	public void test3() {
		add(1, 2, 3, 4);
		final Tree<Integer> tree2 = getAndReset();
		add(1, 2, 4, 3);
		assertTrue(tree.equals(tree2));
		assertTrue(tree2.equals(tree));
		assertEquals(tree.hashCode(), tree2.hashCode());
	}


	@Test
	public void test4() {
		add(1, 2, 3, 4);
		final Tree<Integer> tree2 = getAndReset();
		add(4, 3, 2, 1);
		assertTrue(tree.equals(tree2));
		assertTrue(tree2.equals(tree));
		assertEquals(tree.hashCode(), tree2.hashCode());
	}


	@Test
	public void testEqualsBig() {
		final Random rnd = new Random(5693881l);
		for (int i=0; i< 50000; i++) {
			addNekontrolujVyvazebost(rnd.nextInt(100));
		}
		final Tree<Integer> tree2 = getAndReset();
		final List<Integer> list = new ArrayList<>(getAndResetExpect());
		Collections.shuffle(list);
		for (final int i : list) {
			add(i);
		}
		assertTrue(tree.equals(tree2));
		assertTrue(tree2.equals(tree));
		assertEquals(tree.hashCode(), tree2.hashCode());

	}

}
