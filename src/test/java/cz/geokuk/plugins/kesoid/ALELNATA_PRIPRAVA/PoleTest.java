package cz.geokuk.plugins.kesoid.ALELNATA_PRIPRAVA;

import org.junit.Assert;
import org.junit.Test;

public class PoleTest {

	@Test
	public void test1() {
		final Pole<Long> pole = new Pole<>();
		Assert.assertNull(pole.get(0));
		Assert.assertNull(pole.get(5));
		Assert.assertNull(pole.get(176));
	}

	@Test
	public void test2() {
		final Pole<Long> pole = new Pole<>();
		pole.put(0, 42l);
		pole.put(16, 17l);
		Assert.assertEquals(new Long(42l), pole.get(0));
		Assert.assertNull(pole.get(5));
		Assert.assertEquals(new Long(17l), pole.get(16));
		Assert.assertNull(pole.get(176));
		pole.put(16, 317l);
		Assert.assertEquals(new Long(317l), pole.get(16));
		pole.put(16, null);
		Assert.assertNull(pole.get(16));

	}

}
