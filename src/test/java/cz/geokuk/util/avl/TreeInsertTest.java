package cz.geokuk.util.avl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

public class TreeInsertTest extends Test0 {

	@Test
	public void test789() {
		add(71, 82, 93);
		assertOk();
	}

	@Test
	public void test798() {
		add(71, 92, 83);
		assertOk();
	}

	@Test
	public void test879() {
		add(81, 72, 93);
		assertOk();
	}

	@Test
	public void test897() {
		add(81, 92, 73);
		assertOk();
	}

	@Test
	public void test978() {
		add(91, 72, 83);
		assertOk();
	}

	@Test
	public void test987() {
		add(91, 82, 73);
		assertOk();
	}

	@Test
	public void test() {
//		final long seed = System.currentTimeMillis();
//		System.out.println("Seed: " + seed);
		final Random rnd = new Random(1577491207871l);
		Tree<Double> xx = Tree.empty();
		for (int i=0; i<100; i++) {
			final double d = rnd.nextDouble();
			xx = xx.insert(Mergers.onlyRight(), d);
			assertTrue(kontrolaVyvazenosti(xx));
		}
	}

	@Test
	public void testBig() {
		final Random rnd = new Random(1577491207871l);
		for (int i=0; i < 100000; i++) {
			addNekontrolujVyvazebost(rnd.nextInt());
		}
		assertTrue(kontrolaVyvazenosti(tree));
		assertOk();
		System.out.println(tree.height);
		System.out.println(tree.count);
	}

	@Test
	public void test1() {
		add(5, 7, 2, 1, 3, 6);
		assertOk();
	}

	@Test
	public void test3() {
		add(4, 4, 4);
		assertOk();
		assertEquals(1, tree.count);
	}



}
