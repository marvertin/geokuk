package cz.geokuk.util.avl;

import static org.junit.Assert.assertSame;

import java.util.Random;

import org.junit.Test;

public class TreeMergeTest extends Test0 {

	@Test
	public void testMerge0() {

		assertSame(Avl.empty(), Tree.merge(tree, Mergers.throwing(), Avl.empty()));
	}

	@Test
	public void testMerge0left() {
		add(4, 56, 8, 99, 88);
		tree =Tree.merge(tree, Mergers.throwing(), Avl.empty());
		assertOk();
	}


	@Test
	public void testMerge0right() {
		add(4, 56, 8, 99, 88);
		tree =Tree.merge(Avl.empty(), Mergers.throwing(), tree);
		assertOk();
	}


	@Test
	public void testMergeBothDistinct1() {
		add(1, 2, 3, 4, 5, 6);
		final Tree<Integer> tree1 = tree; tree =Avl.empty();
		add(100, 200, 300, 400, 500, 600);
		tree = Tree.merge(tree1, Mergers.throwing(), tree);
		assertOk();
	}

	@Test
	public void testMergeBothDistinct2() {
		add(1, 3, 5, 7, 9);
		final Tree<Integer> tree1 = tree; tree =Avl.empty();
		add(2, 4, 6, 8, 10);
		tree = Tree.merge(tree1, Mergers.throwing(), tree);
		assertOk();
	}


	@Test
	public void testMergeBoth() {
		add(1, 3, 5, 7, 9);
		final Tree<Integer> tree1 = tree; tree =Avl.empty();
		add(2, 4, 5, 8, 10);
		tree = Tree.merge(tree1, Mergers.onlyLeft(), tree);
		assertOk();
	}

	@Test
	public void testMergeBig() {
		final Random rnd = new Random(5469321l);
		for (int i=0; i< 5000; i++) {
			addNekontrolujVyvazebost(rnd.nextInt(100));
		}
		final Tree<Integer> tree1 = tree; tree = Avl.empty();
		for (int i=0; i< 5000; i++) {
			addNekontrolujVyvazebost(rnd.nextInt(100) + 50);
		}
		tree = Tree.merge(tree1, Mergers.onlyLeft(), tree);
		assertOk();
	}

}
