package cz.geokuk.plugins.challenge;

import org.junit.Assert;
import org.junit.Test;

import cz.geokuk.plugins.challenge.Challenge.Typ;

public class StitkyPruhuTest {

	@Test
	public void nazvyTypu() {
		Assert.assertEquals("360°", StitkyPruhu.typ(Typ.SEKTORY_360));
		Assert.assertEquals("D/T", StitkyPruhu.typ(Typ.DT_MATICE));
		Assert.assertEquals("Kalendář", StitkyPruhu.typ(Typ.KALENDAR));
	}

	@Test
	public void stitekSektoru() {
		Assert.assertEquals("Sektor 213° · 5 kandidátů · 2,8 km", StitkyPruhu.sektor(213, 5, 2_840));
		Assert.assertEquals("Sektor 0° · 1 kandidát · 850 m", StitkyPruhu.sektor(0, 1, 850));
		Assert.assertEquals("Sektor 7° · 3 kandidáti · 12,0 km", StitkyPruhu.sektor(7, 3, 12_000));
		Assert.assertEquals("Sektor 359° · bez kandidátů", StitkyPruhu.sektor(359, 0, 5_000));
	}

	@Test
	public void tvaryKandidatu() {
		Assert.assertEquals("0 kandidátů", StitkyPruhu.kandidatu(0));
		Assert.assertEquals("1 kandidát", StitkyPruhu.kandidatu(1));
		Assert.assertEquals("2 kandidáti", StitkyPruhu.kandidatu(2));
		Assert.assertEquals("4 kandidáti", StitkyPruhu.kandidatu(4));
		Assert.assertEquals("5 kandidátů", StitkyPruhu.kandidatu(5));
		Assert.assertEquals("21 kandidátů", StitkyPruhu.kandidatu(21));
	}

	@Test
	public void vzdalenostSeZaokrouhlujeBezSkokuMeziJednotkami() {
		Assert.assertEquals("0 m", StitkyPruhu.vzdalenost(0));
		Assert.assertEquals("990 m", StitkyPruhu.vzdalenost(994));
		Assert.assertEquals("1,0 km", StitkyPruhu.vzdalenost(995));
		Assert.assertEquals("2,8 km", StitkyPruhu.vzdalenost(2_840));
		Assert.assertEquals("99,9 km", StitkyPruhu.vzdalenost(99_940));
		Assert.assertEquals("100 km", StitkyPruhu.vzdalenost(99_950));
		Assert.assertEquals("135 km", StitkyPruhu.vzdalenost(135_400));
		Assert.assertEquals("", StitkyPruhu.vzdalenost(Double.NaN));
		Assert.assertEquals("", StitkyPruhu.vzdalenost(-1));
	}
}
