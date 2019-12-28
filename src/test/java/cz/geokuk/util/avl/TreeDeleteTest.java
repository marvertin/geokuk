package cz.geokuk.util.avl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class TreeDeleteTest extends Test0 {

	@Test
	public void testDelVetsi1() {
		for (int i = 1; i <= 100; i++) {
			add(i);
		}
		for (int i = 0; i < 100; i++) {
			delete(i);
			assertOk();
		}
	}

	@Test
	public void testDelVetsi2() {
		for (int i = 1; i <= 100; i++) {
			add(i);
		}
		for (int i = 101; i >= 0; i--) {
			delete(i);
			assertOk();
		}
	}

	@Test
	public void testDelVetsi3() {
		for (int i = 0; i < 100; i++) {
			add(i);
		}
		for (int i = 1; i < 1800; i += 17) {
			delete(i % 100);
			assertOk();
		}
		assertSame(Tree.EMPTY, tree);
	}

	@Test public void testDel_2134_1() {
		add(2, 1, 3, 4);
		delete(1);
		assertOk();
	}

	@Test public void testDel_2134_2() {
		add(2, 1, 3, 4);
		delete(2);
		assertOk();
	}


	@Test public void testDel_2134_3() {
		add(2, 1, 3, 4);
		delete(3);
		assertOk();
	}


	@Test public void testDel_2134_4() {
		add(2, 1, 3, 4);
		delete(4);
		assertOk();
	}


	@Test public void testDel_3421_1() {
		add(3, 4, 2, 1);
		delete(1);
		assertOk();
	}

	@Test public void testDel_3421_2() {
		add(3, 4, 2, 1);
		delete(2);
		assertOk();
	}


	@Test public void testDel_3421_3() {
		add(3, 4, 2, 1);
		delete(3);
		assertOk();
	}


	@Test public void testDel_3421_4() {
		add(3, 4, 2, 1);
		delete(4);
		assertOk();
	}


	@Test
	public void testDel0() {
		tree = tree.delete(5);
		assertSame(Tree.EMPTY, tree);
		assertOk();
	}

	@Test
	public void testDel0a() {
		add(5);
		assertOk();
		delete(5);
		assertOk();
		assertSame(Tree.EMPTY, tree);
		assertEquals(0, tree.count);
	}


	@Test
	public void testDel0b() {
		add(5);
		assertOk();
		delete(7);
		assertOk();
		assertEquals(1, tree.count);
	}

	@Test
	public void testDel0c() {
		add(5);
		assertOk();
		delete(3);
		assertOk();
		assertEquals(1, tree.count);
	}


	///// 4 2 6

	@Test
	public void testDel_426_2() {
		add(4, 2, 6);
		delete(2);
		assertOk();
	}

	@Test
	public void testDel_426_4() {
		add(4, 2, 6);
		delete(4);
		assertOk();
	}

	@Test
	public void testDel_426_6() {
		add(4, 2, 6);
		delete(4);
		assertOk();
	}

	@Test public void testDel_426_1() { add(4, 2, 6); delete(1); assertOk(); }
	@Test public void testDel_426_3() { add(4, 2, 6); delete(3); assertOk(); }
	@Test public void testDel_426_5() { add(4, 2, 6); delete(5); assertOk(); }
	@Test public void testDel_426_7() { add(4, 2, 6); delete(7); assertOk(); }

	///// 4 6 2

	@Test
	public void testDel_462_4() {
		add(4, 6, 2);
		delete(4);
		assertOk();
	}

	@Test
	public void testDel_462_6() {
		add(4, 6, 2);
		delete(6);
		assertOk();
	}

	@Test
	public void testDel_462_2() {
		add(4, 6, 2);
		delete(2);
		assertOk();
	}

	@Test public void testDel_462_1() { add(4, 6, 2); delete(1); assertOk();	}
	@Test public void testDel_462_3() { add(4, 6, 2); delete(3); assertOk();	}
	@Test public void testDel_462_5() { add(4, 6, 2); delete(5); assertOk();	}
	@Test public void testDel_462_7() { add(4, 6, 2); delete(7); assertOk();	}


	///// 2 4 6

	@Test
	public void testDel_246_2() {
		add(2, 4, 6);
		delete(2);
		assertOk();
	}

	@Test
	public void testDel_246_4() {
		add(2, 4, 6);
		delete(4);
		assertOk();
	}

	@Test
	public void testDel_246_6() {
		add(2, 4, 6);
		delete(6);
		assertOk();
	}

	@Test public void testDel_246_1() { add(2, 4, 6); delete(1); assertOk(); }
	@Test public void testDel_246_3() { add(2, 4, 6); delete(3); assertOk(); }
	@Test public void testDel_246_5() { add(2, 4, 6); delete(5); assertOk(); }
	@Test public void testDel_246_7() { add(2, 4, 6); delete(7); assertOk(); }

	///// 2 6 4

	@Test
	public void testDel_264_2() {
		add(2, 6, 4);
		delete(2);
		assertOk();
	}

	@Test
	public void testDel_264_4() {
		add(2, 6, 4);
		delete(4);
		assertOk();
	}

	@Test
	public void testDel_264_6() {
		add(2, 6, 4);
		delete(6);
		assertOk();
	}

	@Test public void testDel_264_1() { add(2, 6, 4);	delete(1); assertOk();}
	@Test public void testDel_264_3() { add(2, 6, 4);	delete(3); assertOk();}
	@Test public void testDel_264_5() { add(2, 6, 4);	delete(5); assertOk();}
	@Test public void testDel_264_7() { add(2, 6, 4);	delete(7); assertOk();}

	///// 6 2 4

	@Test
	public void testDel_624_2() {
		add(2, 6, 4);
		delete(2);
		assertOk();
	}

	@Test
	public void testDel_624_4() {
		add(2, 6, 4);
		delete(4);
		assertOk();
	}

	@Test
	public void testDel_624_6() {
		add(2, 6, 4);
		delete(6);
		assertOk();
	}

	@Test public void testDel_624_1() { add(2, 6, 4); delete(1); assertOk(); }
	@Test public void testDel_624_3() { add(2, 6, 4); delete(3); assertOk(); }
	@Test public void testDel_624_5() { add(2, 6, 4); delete(5); assertOk(); }
	@Test public void testDel_624_7() { add(2, 6, 4); delete(7); assertOk(); }


	///// 6 4 2

	@Test
	public void testDel_642_2() {
		add(2, 6, 4);
		delete(2);
		assertOk();
	}

	@Test
	public void testDel_642_4() {
		add(2, 6, 4);
		delete(4);
		assertOk();
	}

	@Test
	public void testDel_642_6() {
		add(2, 6, 4);
		delete(6);
		assertOk();
	}

	@Test public void testDel_642_1() {	add(2, 6, 4); delete(1); assertOk(); }
	@Test public void testDel_642_3() {	add(2, 6, 4); delete(3); assertOk(); }
	@Test public void testDel_642_5() {	add(2, 6, 4); delete(5); assertOk(); }
	@Test public void testDel_642_7() {	add(2, 6, 4); delete(7); assertOk(); }


}
