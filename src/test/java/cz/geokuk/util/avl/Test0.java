package cz.geokuk.util.avl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.*;

import org.junit.Before;

public abstract class Test0 {

	protected Tree<Integer> tree;
	protected SortedSet<Integer> expect;

	@Before
	public void setUp() {
		tree = Tree.empty();
		expect = new TreeSet<>();
	}

	protected Tree<Integer> getAndReset() {
		final Tree<Integer> tree2 = tree;
		tree = Tree.empty();
		return tree2;
	}

	protected boolean kontrolaVyvazenosti(final Tree<?> tree) {
		final List<Tree<?>> list = new LinkedList<Tree<?>>();
		tree.kontrolaVyvazenosti(list);
		list.forEach(System.out::println);
		return list.isEmpty();
	}

	protected void add(final int x) {
		expect.add(x);
		tree = tree.insert(Mergers.onlyRight(), x);
		//assertTrue(kontrolaVyvazenosti(tree));
	}

	protected void delete(final int x) {
		expect.remove(x);
		tree = tree.delete(x);
		assertTrue(kontrolaVyvazenosti(tree));
	}

	protected void addNekontrolujVyvazebost(final int x) {
		expect.add(x);
		tree = tree.insert(Mergers.onlyRight(), x);
	}

	protected void add(final int ... x) {
		for (final int i : x) {
			add(i);
		}
	}

	protected void assertOk() {
		final List<Integer> skutecne = new LinkedList<>();
		tree.drainTo(skutecne);
		assertEquals(new LinkedList<>(expect), skutecne);
	}

	protected SortedSet<Integer> getAndResetExpect() {
		final SortedSet<Integer> tree2 = expect;
		expect = new TreeSet<Integer>();
		return tree2;
	}
}
