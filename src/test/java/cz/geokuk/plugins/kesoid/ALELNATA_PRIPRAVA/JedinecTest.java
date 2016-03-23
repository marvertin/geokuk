package cz.geokuk.plugins.kesoid.ALELNATA_PRIPRAVA;

import org.junit.*;

public class JedinecTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {}

	private Genom genom;
	private Druh druh;

	@Before
	public void setUp() throws Exception {
		genom = new Genom();
		druh = genom.druh("gugu");
	}

	@Test
	public void test1() {
		final Alela alela = genom.alela("alik");
		final Jedinec j = druh.zrozeni();
		Assert.assertFalse(j.has(alela));
	}

	@Test
	public void test2a() {
		final Alela alela = genom.alela("alik");
		genom.gen("genik").add(alela);
		final Jedinec j = druh.zrozeni();
		Assert.assertFalse(j.has(alela));
	}

	@Test
	public void test2b() {
		final Alela alela = genom.alela("alik");
		genom.gen("genik").add(alela);
		final Jedinec j = druh.zrozeni();
		j.add(alela);
		Assert.assertTrue(j.has(alela));
	}

	@Test
	public void test3() {
		final Alela alela = genom.alela("alik");
		final Gen gen = genom.gen("genik");
		gen.add(alela);
		gen.alela("44");
		final Jedinec j = druh.zrozeni();
		j.add(alela);
		Assert.assertSame(genom.alela("alik"), j.getAlela(gen));
	}

	@Test
	public void test4() {
		genom.gen("alik").alela("11");
		final Alela a12 = genom.gen("alik").alela("12");
		final Alela a13 = genom.gen("belik").alela("13");
		genom.gen("belik").alela("14");
		final Jedinec j = druh.zrozeni();
		j.add(a12);
		j.add(a13);
		Assert.assertSame(a12, j.getAlela(genom.gen("alik")));
		Assert.assertSame(a13, j.getAlela(genom.gen("belik")));
	}

	@Test
	public void test4a() {
		final Alela a11 = genom.gen("alik").alela("11");
		final Alela a12 = genom.gen("alik").alela("12");
		final Alela a13 = genom.gen("belik").alela("13");
		final Alela a14 = genom.gen("belik").alela("14");
		final Jedinec j = druh.zrozeni();
		j.add(a11);
		j.add(a14);
		j.add(a12);
		j.add(a13);
		Assert.assertSame(a12, j.getAlela(genom.gen("alik")));
		Assert.assertSame(a13, j.getAlela(genom.gen("belik")));
	}

	@Test
	public void test4b() {
		genom.gen("alik").alela("11");
		final Alela a12 = genom.gen("alik").alela("12");
		final Alela a13 = genom.gen("belik").alela("13");
		final Alela a14 = genom.gen("belik").alela("14");
		final Jedinec j = druh.zrozeni();
		j.add(a12);
		druh.zrozeni().add(a14);
		Assert.assertSame(a12, j.getAlela(genom.gen("alik")));
		Assert.assertSame(a13, j.getAlela(genom.gen("belik")));
	}

	@Test
	public void test4c() {
		final Alela a11 = genom.gen("alik").alela("11");
		genom.gen("alik").alela("12");
		final Alela a13 = genom.gen("belik").alela("13");
		genom.gen("belik").alela("14");
		final Jedinec j = druh.zrozeni();
		j.add(a13);
		druh.addGen(genom.gen("alik"));
		Assert.assertSame(a11, j.getAlela(genom.gen("alik")));
		Assert.assertSame(a13, j.getAlela(genom.gen("belik")));
	}

	@Test
	public void test5b() {
		final Gen belik = genom.gen("belik");
		druh.addGen(belik);
		final Alela a13 = belik.alela("13");
		belik.alela("14");
		genom.gen("alik").alela("11");
		final Alela a12 = genom.gen("alik").alela("12");
		final Jedinec j = druh.zrozeni();
		j.add(a12);
		Assert.assertSame(a12, j.getAlela(genom.gen("alik")));
		Assert.assertSame(a13, j.getAlela(belik));
	}

	@Test
	public void test5c() {
		final Alela a13 = genom.gen("belik").alela("13");
		genom.gen("belik").alela("14");
		genom.gen("alik").alela("11");
		final Alela a12 = genom.gen("alik").alela("12");
		final Jedinec j = druh.zrozeni();
		j.add(a13);
		j.add(a12);
		Assert.assertSame(a12, j.getAlela(genom.gen("alik")));
		Assert.assertSame(a13, j.getAlela(genom.gen("belik")));
	}
}
