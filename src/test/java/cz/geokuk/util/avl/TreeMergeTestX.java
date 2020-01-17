package cz.geokuk.util.avl;

import static org.junit.Assert.assertEquals;

import java.util.*;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

public class TreeMergeTestX  {


	private Tree<Dato> da;
	private Tree<Dato> db;

	private SortedMap<Dato, Dato> eda;
	private SortedMap<Dato, Dato> edb;

	@Before
	public void setUp() {
		da = Tree.empty();
		db = Tree.empty();

		eda = new TreeMap<Dato, Dato>();
		edb = new TreeMap<Dato, Dato>();

	}

	void assertMergeOk() {
		// Zatím je to průnik s konkatenací
		final TreeSet<Dato> expectSet = eda.keySet().stream()
				.filter(edb::containsKey)
				.map(a -> new Dato(a.cis, a.text + edb.get(a).text))
				.collect(Collectors.toCollection(TreeSet::new));

		expectSet.addAll(eda.keySet());
		expectSet.addAll(edb.keySet());
		final List<Dato> expect = new LinkedList<>(expectSet);


		final Tree<Dato> tree = Tree.union(da, Dato.MERGER, db);
		final List<Dato> skutecne = new LinkedList<>();
		tree.drainTo(skutecne);
		System.out.println();
//		System.out.println("expect:   " + expect);
//		System.out.println("skutecne: " + skutecne);
		assertEquals(expect,  skutecne);

	}



	@Test
	public void testMergeX0() {
		assertMergeOk();
	}


	@Test
	public void testMergeX0a() {
		addA(1);
		assertMergeOk();
	}

	@Test
	public void testMergeX0b() {
		addB(100);
		assertMergeOk();
	}

	@Test
	public void testMergeX1() {
		addA(1);
		addB(2);
		assertMergeOk();
	}

	@Test
	public void testMergeX2() {
		addA(42);
		addB(42);
		assertMergeOk();
	}

	@Test
	public void testMergeX3() {
		addA(1,5,8,9,12);
		addB(2,3,6,8,9,16);
		assertMergeOk();
	}

	@Test
	public void testMergeX4() {
		addA(1,5,8,9,12,16);
		addB(1, 2,3,6,8,9,16);
		assertMergeOk();
	}


	@Test
	public void testBig() {
		final Random rnd = new Random(564646);
		for (int i=0; i < 10000; i++) {
			final int cis = rnd.nextInt();
			if (rnd.nextBoolean()) {
				addA(cis);
			}
			if (rnd.nextBoolean()) {
				addB(cis);
			}
		}
		assertMergeOk();
	}

	private Dato da(final int cis) {
		return new Dato(cis, "a");
	}

	private Dato db(final int cis) {
		return new Dato(cis, "b");
	}

	protected void addA(final int x) {
		eda.put(da(x), da(x));
		da = da.insert(ValueMergers.onlyRight(), da(x));
	}


	protected void addA(final int ... x) {
		for (final int i : x) {
			addA(i);
		}
	}

	protected void addB(final int x) {
		edb.put(db(x), db(x));
		db = db.insert(ValueMergers.onlyRight(),db(x));
	}


	protected void addB(final int ... x) {
		for (final int i : x) {
			addB(i);
		}
	}


}
