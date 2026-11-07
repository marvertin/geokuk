package cz.geokuk.plugins.challenge;

import org.junit.Assert;
import org.junit.Test;

public class NejblizsiPrvkyTest {

	@Test
	public void vybereNejmensiHodnotyVzestupne() {
		final double[] h = { 9, 3, 7, 1, 5, 8, 2 };
		Assert.assertArrayEquals(new int[] { 3, 6, 1 }, NejblizsiPrvky.vyber(h, h.length, 3));
	}

	@Test
	public void zvladneRovnostAVsechnyPrvky() {
		final double[] h = { 2, 2, 2, 1 };
		final int[] v = NejblizsiPrvky.vyber(h, h.length, 4);
		Assert.assertEquals(3, v[0]);
		Assert.assertEquals(4, v.length);
		Assert.assertEquals(0, NejblizsiPrvky.vyber(h, h.length, 0).length);
	}

	@Test
	public void berePrvniNHodnotZPole() {
		final double[] h = { 5, 4, 3, 0, 0 };
		Assert.assertArrayEquals(new int[] { 2, 1 }, NejblizsiPrvky.vyber(h, 3, 2));
	}
}
