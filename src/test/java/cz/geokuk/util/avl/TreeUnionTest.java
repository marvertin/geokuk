package cz.geokuk.util.avl;

import java.util.SortedSet;

import org.junit.Test;

public class TreeUnionTest extends TreeCombineTest0 {




	@Test
	public void test0() {
		assertOk();
	}


	@Test
	public void test01() {
		addA(58);
		assertOk();
	}



	@Test
	public void test02() {
		addA(58);
		assertOk();
	}


	@Test
	public void testRuzne() {
		addA(13);
		addB(17);
		assertOk();
	}


	@Test
	public void testRuzne2() {
		addA(10, 25, 60);
		addB(17, 8, 14, 9);
		assertOk();
	}


	@Test
	public void testRuzne3() {
		addA(4);
		addB(17);
		assertOk();
	}



	@Test
	public void testStejne() {
		addA(13);
		addB(13);
		assertOk();
	}


	@Test
	public void testPodmA() {
		addA(13, 30);
		addB(5, 13, 30);
		assertOk();
	}


	@Test
	public void testPodmB() {
		addA(4, 13, 30, 200);
		addB(5, 13, 30);
		assertOk();
	}


	@Test
	public void testBoth() {
		addA(1,2,3,4,5,6,7,8);
		addB(25,2,3,5,17,49,86);
		assertOk();
	}


	@Override
	protected void prikombinujExpected(final SortedSet<Integer> kam, final SortedSet<Integer> co) {
		kam.addAll(co);
	}


	@Override
	protected Tree<Integer> zkobinuj(final Tree<Integer> treeAA, final Tree<Integer> treeBB) {
		return Tree.union(treeAA, ValueMergers.onlyLeft(), treeBB);
	}

}
