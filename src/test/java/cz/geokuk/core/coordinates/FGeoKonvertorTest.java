package cz.geokuk.core.coordinates;

import org.junit.Assert;
import org.junit.Test;

public class FGeoKonvertorTest {

	private static final double PRESNOST_WGS = 1e-5;
	private static final double PRESNOST_MERKATOR = 0.01;

	private static double RZ = 6378137;
	private static double OZ = 2 * Math.PI * RZ;



	private void assertEquals(Wgs wgs1, Wgs wgs2) {
		Assert.assertEquals(wgs1.lat, wgs2.lat, PRESNOST_WGS);
		Assert.assertEquals(wgs1.lon, wgs2.lon, PRESNOST_WGS);
	}

	private void assertEquals(Mercator mer1, Mercator mer2) {
		Assert.assertEquals(mer1.mx, mer2.mx, PRESNOST_MERKATOR);
		Assert.assertEquals(mer1.my, mer2.my, PRESNOST_MERKATOR);
	}

	private void testBijekce(Wgs w) {
		assertEquals(w, w.toMercator().toWgs());
	}

	private void testMouWgs(double lat, double lon, int xx, int yy) {
		Wgs w = new Wgs(lat, lon);
		Mou m = new Mou(xx, yy);
		assertEquals(w, m.toWgs());
		// assertEquals(m, w.toMou());
	}

	private void testWgs2Mou(double lat, double lon, int xx, int yy) {
		Wgs w = new Wgs(lat, lon);
		Mou m1 = new Mou(xx, yy);
		Mou m2 = w.toMou();
		System.out.println(w + " = " + m2 + " ... (" + m1 + ")");
		Assert.assertEquals((double)m1.xx, (double)m2.xx, 1);
		Assert.assertEquals((double)m1.yy, (double)m2.yy, 1);
		assertEquals(w, m2.toWgs());
	}

	private void testWgs2Mercator(double lat, double lon, double mx, double my) {
		Wgs w = new Wgs(lat, lon);
		Mercator m = new Mercator(mx, my);
		System.out.println(w + " = " + w.toMercator() + " ... (" + m + ")");
		assertEquals(m, w.toMercator());
	}

	@Test
	public void t0a() {
		assertEquals(new Wgs(0, 0), new Mercator(0, 0).toWgs());
	}

	@Test
	public void t0b() {
		assertEquals(new Mercator(0, 0), new Wgs(0, 0).toMercator());
	}

	@Test
	public void tbijekce1a() {
		testBijekce(new Wgs(0, 40.32));
	}

	@Test
	public void tbijekce2a() {
		testBijekce(new Wgs(40.32, 0));
	}

	@Test
	public void tbijekce3a() {
		testBijekce(new Wgs(60.32, 170));
	}

	@Test
	public void tbijekce1am() {
		testBijekce(new Wgs(0, -40.32));
	}

	@Test
	public void tbijekce2am() {
		testBijekce(new Wgs(-40.32, 0));
	}

	@Test
	public void tbijekce3am() {
		testBijekce(new Wgs(-60.32, -170));
	}

	@Test
	public void tbijekce3b() {
		testBijekce(new Wgs(-60.32, 170));
	}

	@Test
	public void tbijekce3c() {
		testBijekce(new Wgs(60.32, -168));
	}

	@Test
	public void tbijekce3d() {
		testBijekce(new Wgs(60.32, 190));
	}

	@Test
	public void tbijekce3e() {
		testBijekce(new Wgs(60.32, 180));
	}

	@Test
	public void test0a() {
		testMouWgs(0, 0, 0, 0);
	}

	@Test
	public void test1rovnik() {
		testWgs2Mou(0, 1, 11930465, 0);
	}

	@Test
	public void test90rovnik() {
		testWgs2Mou(0, 90, 0x4000_0000, 0);
	}

	@Test
	public void test180rovnik() {
		testWgs2Mou(0, 180, 0x8000_0000, 0);
	}

	@Test
	public void test270rovnik() {
		testWgs2Mou(0, 270, 0xc000_0000, 0);
	}

	@Test
	public void testWgs2Mou5() {
		testWgs2Mou(80, 80, 954437177, 1665333205);
	}

	@Test
	public void test0am() {
		testWgs2Mercator(0, 0, 0, 0);
	}

	@Test
	public void test1rovnikm() {
		testWgs2Mercator(0, 1, OZ / 360, 0);
	}

	@Test
	public void test15ecelych6rovnikm() {
		testWgs2Mercator(0, 15.6, OZ / 360 * 15.6, 0);
	}


	@Test
	public void test90rovnikm() {
		testWgs2Mercator(0, 90, OZ / 4, 0);
	}

	@Test
	public void test180rovnikm() {
		testWgs2Mercator(0, 180, -OZ / 2, 0);
	}

	@Test
	public void testm180rovnikm() {
		testWgs2Mercator(0, -180, -OZ / 2, 0);
	}

	@Test
	public void test270rovnikm() {
		testWgs2Mercator(0, 270, -OZ / 4, 0);
	}

	@Test
	public void testMinus90rovnikm() {
		testWgs2Mercator(0, -90, -OZ / 4, 0);
	}

	@Test
	public void testWgsToMerk5() {
		testWgs2Mercator(4.5, 0, 0, 501453.51);
	}

	@Test
	public void testWgsToMerk6() {
		testWgs2Mercator(50, 0, 0, 6446275.841);
	}

	@Test
	public void testWgsToMerk7() {
		testWgs2Mercator(45, 0, 0, 5621521.486);
	}

	@Test
	public void testWgsToMerk8() {
		testWgs2Mercator(80, 0, 0, 15538711.1);
	}

	@Test
	public void testWgsToMerk8a() {
		testWgs2Mercator(80, 15.6, OZ / 360 * 15.6, 15538711.1);
	}


	@Test
	public void testWgsToMerk5m() {
		testWgs2Mercator(-4.5, 0, 0, -501453.51);
	}

	@Test
	public void testWgsToMerk6m() {
		testWgs2Mercator(-50, 0, 0, -6446275.841);
	}

	@Test
	public void testWgsToMerk7m() {
		testWgs2Mercator(-45, 0, 0, -5621521.486);
	}

	@Test
	public void testMercatorToMou1() {
		Mou mou = FGeoKonvertor.toMou(new Mercator(OZ/4, 0));
		System.out.println(OZ/4 + " " + mou);
		Assert.assertEquals(0x4000_0000, mou.xx);
	}

	@Test
	public void testMercatorToMou2() {
		Mou mou = FGeoKonvertor.toMou(new Mercator(OZ/4, OZ/8));
		System.out.println(OZ/4 + " " + mou);
		Assert.assertEquals(0x4000_0000, mou.xx);
		Assert.assertEquals(0x2000_0000, mou.yy);
	}

}
