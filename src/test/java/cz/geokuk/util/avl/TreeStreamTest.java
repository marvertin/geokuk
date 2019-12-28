package cz.geokuk.util.avl;

import static org.junit.Assert.*;

import java.util.*;

import org.junit.Test;

import cz.geokuk.util.avl.Tree.Splited;

public class TreeStreamTest extends Test0 {

	@Test
	public void testSplit0() {
		final Splited<Integer> splited = tree.split(500);
		assertSame(Avl.empty(), splited.left);
		assertSame(Avl.empty(), splited.right);
		assertSame(Optional.empty(), splited.value);
	}

	@Test
	public void testSplit1left() {
		add(5, 10, 15, 20);
		final Splited<Integer> splited = tree.split(2);
		assertSame(Avl.empty(), splited.left);
		assertSame(Optional.empty(), splited.value);
		tree = splited.right;
		assertOk();
	}

	@Test
	public void testSplit1right() {
		add(5, 10, 15, 20);
		final Splited<Integer> splited = tree.split(30);
		assertSame(Avl.empty(), splited.right);
		assertSame(Optional.empty(), splited.value);
		tree = splited.left;
		assertOk();
	}

	@Test
	public void testSplit1leftEdge() {
		add(5, 10, 15, 20);
		final Splited<Integer> splited = tree.split(5);
		assertEquals(Avl.empty(), splited.left);
		assertEquals(new Integer(5), splited.value.get());
		tree = splited.right;
		expect.remove(5);
		assertOk();
	}

	@Test
	public void testSplit1rightEdge() {
		add(5, 10, 15, 20);
		final Splited<Integer> splited = tree.split(20);
		assertSame(Avl.empty(), splited.right);
		assertEquals(new Integer(20), splited.value.get());
		tree = splited.left;
		expect.remove(20);
		assertOk();
	}

	@Test
	public void testSplit5000a() {
		final Random rnd = new Random(5469321l);
		for (int i=0; i< 5000; i++) {
			addNekontrolujVyvazebost(rnd.nextInt(1000));
		}
		addNekontrolujVyvazebost(500);
		assertOk();

		final Splited<Integer> splited = tree.split(500);
		assertEquals(new Integer(500), splited.value.get());


		assertSplited(splited, 500);
	}



	@Test
	public void testSplit5000b() {
		final Random rnd = new Random(5369321l);
		for (int i=0; i< 5000; i++) {
			addNekontrolujVyvazebost(rnd.nextInt(1000));
		}
		delete(500);
		assertOk();

		final Splited<Integer> splited = tree.split(500);
		assertFalse(splited.value.isPresent());

		assertSplited(splited, 500);
	}


	private void assertSplited(final Splited<Integer> splited, final int splitValue) {
		final TreeSet<Integer> left = new TreeSet<>();
		splited.left.drainTo(left);

		final TreeSet<Integer> right = new TreeSet<>();
		splited.right.drainTo(right);

		final SortedSet<Integer> expectLeft = expect.subSet(Integer.MIN_VALUE, splitValue);
		final SortedSet<Integer> expectRight = expect.subSet(splitValue+1, Integer.MAX_VALUE);

		assertEquals(expectLeft,  left);
		assertEquals(expectRight,  right);

		System.out.println(left);
		System.out.println(right);
	}




}
