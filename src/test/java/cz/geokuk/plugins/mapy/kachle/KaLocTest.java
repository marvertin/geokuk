package cz.geokuk.plugins.mapy.kachle;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.core.coordinates.Mou;

public class KaLocTest {

	@Test
	public void testJZroh() {

		Mou mou1 = new Mou(0xFEA00000, 0x05200000);
		KaLoc kaloc = KaLoc.ofJZ(mou1, 12);
		Mou mou2 = kaloc.getMouJZ();
		Assert.assertEquals(mou1, mou2);
	}

	@Test
	public void testSZroh() {

		Mou mou1 = new Mou(0x23800000, 0x28100000);
		KaLoc kaloc = KaLoc.ofSZ(mou1, 12);
		Mou mou2 = kaloc.getMouSZ();
		Assert.assertEquals(mou1, mou2);

	}

	@Test
	public void test0() {

		Mou mou1 = new Mou(0, 0);
		KaLoc kaloc = KaLoc.ofJZ(mou1, 4);
		Assert.assertEquals(0, kaloc.getSignedX());
		Assert.assertEquals(0, kaloc.getSignedY());
		Assert.assertEquals(8, kaloc.getFromSzUnsignedX());
		Assert.assertEquals(7, kaloc.getFromSzUnsignedY());
	}

}
