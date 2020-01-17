package cz.geokuk.util.avl;

import static org.junit.Assert.assertEquals;

import java.util.*;

import org.junit.Before;
import org.junit.Test;

public abstract class TreeCombineTest0 {


	protected Tree<Integer> treeA;
	protected SortedSet<Integer> expectA;
	protected Tree<Integer> treeB;
	protected SortedSet<Integer> expectB;

	@Before
	public void setUp() {
		treeA = Tree.empty();
		treeB = Tree.empty();
		expectA = new TreeSet<>();
		expectB = new TreeSet<>();
	}

	protected void addA(final int x) {

		expectA.add(x);
		treeA = treeA.insert(ValueMergers.onlyRight(), x);
	}

	protected void addB(final int x) {

		expectB.add(x);
		treeB = treeB.insert(ValueMergers.onlyRight(), x);
	}

	protected void addA(final int ... x) {
		for (final int i : x) {
			addA(i);
		}
	}

	protected void addB(final int ... x) {
		for (final int i : x) {
			addB(i);
		}
	}


	protected void assertOk() {
		final List<Integer> skutecne = new LinkedList<>();
		final Tree<Integer> treeAA = treeA;
		final Tree<Integer> treeBB = treeB;
		zkobinuj(treeAA, treeBB).drainTo(skutecne);

		final SortedSet<Integer> expect = new TreeSet<Integer>(expectA);
		prikombinujExpected(expect, expectB);

		assertEquals(new LinkedList<>(expect), skutecne);
	}


	protected abstract Tree<Integer> zkobinuj(Tree<Integer> treeAA, Tree<Integer> treeBB);

	protected abstract void prikombinujExpected(SortedSet<Integer> kam, SortedSet<Integer> co);


	@Test
	public void testBig() {
		final Random rnd = new Random(5469321l);
		for (int i=0; i< 1000; i++) {
			if (rnd.nextBoolean()) {
				addA(rnd.nextInt(2000));
			}
			if (rnd.nextBoolean()) {
				addB(rnd.nextInt(2000));
			}
		}
		assertOk();
	}

}
