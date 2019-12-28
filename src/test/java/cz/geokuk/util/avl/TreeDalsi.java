package cz.geokuk.util.avl;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TreeDalsi extends Test0 {

	@Test
	public void testDelVetsi1() {
	}

	@Test
	public void testDelVetsi2() {
		System.out.println("citac: " + Avl.citac);

		Avl.citac = 0;
		for (int i = 1; i <= 1_000_000; i++) {
			add(i);
		}

		System.out.println("citac: " + Avl.citac);
		Avl.citac = 0;

		final Node<Integer> node = (Node<Integer>) tree;
		System.out.println(node.value);
		final Node<Integer> node2 = Tree.node(Tree.empty(), node.value, node.right);
		System.out.println(node2.value);
		System.out.println("citac: " + Avl.citac);
		Avl.citac = 0;
		assertTrue(kontrolaVyvazenosti(node2));

	}

}
