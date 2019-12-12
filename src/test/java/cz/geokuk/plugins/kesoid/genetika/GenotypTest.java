package cz.geokuk.plugins.kesoid.genetika;

import org.junit.*;

public class GenotypTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	private Genom genom;
	private Druh druh;

	private Gen A;
	private Alela a;
	private Alela b;
	private Alela c;

	private Gen P;
	private Alela p;
	private Alela q;
	private Alela r;

	private Gen X;
	private Alela x;
	private Alela y;
	private Alela z;

	private Genotyp apx0;

	@Before
	public void setUp() throws Exception {
		genom = new Genom();
		A = genom.gen("AA");
		a = A.getVychoziAlela();
		b = A.alela("bb");
		c = A.alela("cc");

		P = genom.gen("PP");
		p = P.getVychoziAlela();
		q = P.alela("qq");
		r = P.alela("rr");

		X = genom.gen("XX");
		x = X.getVychoziAlela();
		y = X.alela("yy");
		z = X.alela("zz");

		druh = genom.druh("gugu");
		druh.addGen(A);
		druh.addGen(P);
		druh.addGen(X);

		apx0 = druh.genotypVychozi();
	}

	@Test
	public void test1() {
		final Genotyp g1 = druh.genotypVychozi();
		final Genotyp g2 = druh.genotypVychozi();
		Assert.assertSame(g1, g2);
	}

	@Test
	public void test2() {
		final Genotyp cpx = apx0.with(c);
		final Genotyp crx1 = cpx.with(r);

		final Genotyp arx = apx0.with(r);
		final Genotyp crx2 = arx.with(c);

		final Genotyp crx3 = apx0.with(c, r);
		final Genotyp crx4 = apx0.with(r, c);

		Assert.assertNotSame(cpx, arx);
		Assert.assertSame(crx1, crx2);
		Assert.assertSame(crx1, crx3);
		Assert.assertSame(crx1, crx4);

		final Genotyp apx11 = crx1.without(c).without(r);
		final Genotyp apx12 = crx1.without(c, r);
		final Genotyp apx13 = crx1.without(r).without(c);
		final Genotyp apx14 = crx1.without(r, c);

		final Genotyp apx15 = crx1.without(c).without(r);
		final Genotyp apx16 = crx1.without(c, r);
		final Genotyp apx17 = crx1.without(r).without(c);
		final Genotyp apx18 = crx1.without(r, c);

		Assert.assertSame(apx0, apx11);
		Assert.assertSame(apx0, apx12);
		Assert.assertSame(apx0, apx13);
		Assert.assertSame(apx0, apx14);
		Assert.assertSame(apx0, apx15);
		Assert.assertSame(apx0, apx16);
		Assert.assertSame(apx0, apx17);
		Assert.assertSame(apx0, apx18);

	}

	@Test
	public void test3() {
		final Genotyp bqy = apx0.with(b, q, y);
		final Genotyp bpy = apx0.with(b, p, y);
		Assert.assertNotSame(bpy, bqy);

		final Genotyp bpy1 = bqy.without(q);
		final Genotyp bpy2 = bqy.with(p);
		Assert.assertSame(bpy, bpy1);
		Assert.assertSame(bpy, bpy2);

		final Genotyp cyr1 = bqy.with(c).with(y).with(r);
		final Genotyp cyr2 = bqy.with(a, b, c, x, z, y, r);
		Assert.assertSame(cyr1, cyr2);

	}

	@Test
	public void test4() {
		final Genotyp bpy1 = apx0.with(b, p, y);
		final Genotyp bpy2 = apx0.with(b, y);
		Assert.assertSame(bpy1, bpy2);
	}

	@Test
	public void test5() {
		final Genotyp bqy = apx0.with(b, q, y);
		final Genotyp cyr1 = bqy.with(c).with(y).with(r);
		final Genotyp cyr2 = bqy.with(a, b, c, x, z, y, r);
		Assert.assertSame(cyr1, cyr2);

	}

	@Test
	public void test6() {
		final Genotyp bqx0 = apx0.with(b, q);
		final Genotyp bqy = apx0.with(b, q, y);
		final Genotyp bqx = bqy.without(y);
		final Genotyp bqy1 = bqy.without(z);

		Assert.assertSame(bqx, bqx0);
		Assert.assertNotSame(bqx, bqy1);
		Assert.assertNotSame(bqy, bqx);
		Assert.assertSame(bqy, bqy1);

	}

	@Test
	public void test7a() {
		Assert.assertSame(apx0, apx0.without(p));
	}

	@Test
	public void test7b() {
		Assert.assertSame(apx0, apx0.without(x));
	}

	@Test
	public void test7c() {
		Assert.assertSame(apx0, apx0.without(q));
	}

}
