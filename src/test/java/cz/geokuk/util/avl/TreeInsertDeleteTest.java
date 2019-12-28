package cz.geokuk.util.avl;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

public class TreeInsertDeleteTest extends Test0 {


	@Test
	public void testRand1() {
		final Random rnd = new Random(546321l);
		for (int i=0; i<100000; i++) {
			if (rnd.nextBoolean()) {
				add(rnd.nextInt(1000));
			} else {
				delete(rnd.nextInt(1000));
			}
		}
		assertOk();
		// počet odpovídá náhodné řadě, tolik toho tam prostě zůstane
		assertEquals(510, tree.count);
	}
}
