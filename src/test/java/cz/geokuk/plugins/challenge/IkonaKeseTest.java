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

	@Test
	public void tretiPrvekPoleJeHorniOkrajIkony() {
		final int[] o = new int[4];
		IkonaKese.okraje(null, new Mou(1000, 1000), 1, o);
		Assert.assertEquals(-8, o[2]);
		IkonaKese.okraje(null, new Mou(1000, 1000), 2, o);
		Assert.assertEquals(-16, o[2]);
	}

	@Test
	public void okrajeZeSkutecneIkonyJsouVlevoVpravoNahoreASvislyStred() {
		final cz.geokuk.plugins.kesoid.Wpt wpt = new cz.geokuk.plugins.kesoid.Wpt();
		final cz.geokuk.plugins.kesoid.mapicon.Sklivec sklivec = new cz.geokuk.plugins.kesoid.mapicon.Sklivec();
		// 10 × 6 px, střed obrázku v bodě keše, posunutý o 2 doprava a 1 dolů: x od -3 do 7, y od -2 do 4
		sklivec.imaganti.add(new cz.geokuk.api.mapicon.Imagant(new java.awt.image.BufferedImage(10, 6, java.awt.image.BufferedImage.TYPE_INT_ARGB), 2, 1));
		wpt.setSklivec(sklivec);
		final int[] o = new int[4];
		IkonaKese.okraje(wpt, 1, o);
		Assert.assertEquals("pravý okraj", 7, o[0]);
		Assert.assertEquals("svislý střed", 1, o[1]);
		Assert.assertEquals("horní okraj", -2, o[2]);
		Assert.assertEquals("levý okraj", -3, o[3]);
	}
}
