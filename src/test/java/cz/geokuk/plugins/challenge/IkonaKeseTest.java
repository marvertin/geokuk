package cz.geokuk.plugins.challenge;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.util.index2d.BoundingRect;
import cz.geokuk.util.index2d.Indexator;

/** Okraj ikony keše pro štítek kandidáta; bez displeje. */
public class IkonaKeseTest {

	@Test
	public void bezIkonyJePolovinaNeznameIkonyAMeritkoJiZvetsuje() {
		final int[] o = new int[2];
		IkonaKese.okraje(null, new Mou(1000, 1000), 1, o);
		Assert.assertEquals(8, o[0]);
		Assert.assertEquals(0, o[1]);
		IkonaKese.okraje(null, new Mou(1000, 1000), 2, o);
		Assert.assertEquals("zvětšení ikon se projeví", 16, o[0]);
		IkonaKese.okraje(new Indexator<>(new BoundingRect(0, 0, 10_000, 10_000)), new Mou(1000, 1000), 1.5, o);
		Assert.assertEquals("keš v indexu není, platí odhad", 12, o[0]);
	}
}
