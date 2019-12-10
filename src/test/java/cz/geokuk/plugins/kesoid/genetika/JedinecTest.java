package cz.geokuk.plugins.kesoid.genetika;

import java.util.*;

import org.junit.*;

import cz.geokuk.plugins.kesoid.genetika.*;

public class JedinecTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

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
		j.put(alela);
		Assert.assertTrue(j.has(alela));
	}

	@Test
	public void test3() {
		final Alela alela = genom.alela("alik");
		final Gen gen = genom.gen("genik");
		gen.add(alela);
		gen.alela("44");
		final Jedinec j = druh.zrozeni();
		j.put(alela);
		Assert.assertSame(genom.alela("alik"), j.getAlela(gen));
	}

	@Test
	public void test4() {
		genom.gen("alik").alela("11");
		final Alela a12 = genom.gen("alik").alela("12");
		final Alela a13 = genom.gen("belik").alela("13");
		genom.gen("belik").alela("14");
		final Jedinec j = druh.zrozeni();
		j.put(a12);
		j.put(a13);
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
		j.put(a11);
		j.put(a14);
		j.put(a12);
		j.put(a13);
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
		j.put(a12);
		druh.zrozeni().put(a14);
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
		j.put(a13);
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
		j.put(a12);
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
		j.put(a13);
		j.put(a12);
		Assert.assertSame(a12, j.getAlela(genom.gen("alik")));
		Assert.assertSame(a13, j.getAlela(genom.gen("belik")));
	}

	@Test
	public void testGetAlely1() {
		final Gen belik = genom.gen("belik");
		druh.addGen(belik);
		final Alela a13 = belik.alela("13");
		belik.alela("14");
		Assert.assertEquals(Collections.singleton(a13), druh.zrozeni().getAlely());
	}

	@Test
	public void testGetAlely2() {
		final Gen belik = genom.gen("belik");
		druh.addGen(belik);
		belik.alela("13");
		final Alela a14 = belik.alela("14");
		final Jedinec jed = druh.zrozeni();
		jed.put(a14);
		Assert.assertEquals(Collections.singleton(a14), jed.getAlely());
	}

	@Test
	public void testGetAlely3a() {
		final Gen belik = genom.gen("belik");
		belik.alela("13");
		final Alela a14 = belik.alela("14");
		final Jedinec jed = druh.zrozeni();
		jed.put(a14);
		Assert.assertEquals(Collections.singleton(a14), jed.getAlely());
	}

	@Test
	public void testGetAlely4() {
		final Alela a11 = genom.gen("alik").alela("a11");
		final Alela a12 = genom.gen("alik").alela("a12");
		final Alela a13 = genom.gen("alik").alela("a13");

		final Alela b11 = genom.gen("belik").alela("b11");
		final Alela b12 = genom.gen("belik").alela("b12");
		final Alela b13 = genom.gen("belik").alela("b13");

		final Jedinec jed = druh.zrozeni();
		jed.put(a11);
		jed.put(b13);
		Assert.assertEquals(ales(a11, b13), jed.getAlely());
	}

	@Test
	public void testGetAlely4a() {
		final Alela a11 = genom.gen("alik").alela("a11");
		final Alela a12 = genom.gen("alik").alela("a12");
		final Alela a13 = genom.gen("alik").alela("a13");

		final Alela b11 = genom.gen("belik").alela("b11");
		final Alela b12 = genom.gen("belik").alela("b12");
		final Alela b13 = genom.gen("belik").alela("b13");

		final Jedinec jed = druh.zrozeni();
		jed.put(b13);
		Assert.assertEquals(ales(b13), jed.getAlely());
	}

	@Test
	public void testGetAlely4b() {
		final Jedinec jed = testGetAlely();
	}

	private Jedinec testGetAlely() {
		final Alela a11 = genom.gen("alik").alela("a11");
		final Alela a12 = genom.gen("alik").alela("a12");
		final Alela a13 = genom.gen("alik").alela("a13");

		final Alela b11 = genom.gen("belik").alela("b11");
		final Alela b12 = genom.gen("belik").alela("b12");
		final Alela b13 = genom.gen("belik").alela("b13");

		druh.addGen(genom.gen("alik"));
		final Jedinec jed = druh.zrozeni();
		jed.put(b13);
		Assert.assertEquals(ales(a11, b13), jed.getAlely());
		return jed;
	}

	@Test
	public void testGetAlely4c() {
		final Jedinec jed = testGetAlely();
		final Alela c11 = genom.gen("culik").alela("c11");
		final Alela c12 = genom.gen("culik").alela("c12");

		final Alela a11 = genom.gen("alik").alela("a11");
		final Alela b13 = genom.gen("belik").alela("b13");

		druh.addGen(genom.gen("culik"));
		Assert.assertEquals(ales(a11, b13, c11), jed.getAlely());

	}

	@Test
	public void testGetAlely5() {
		final Alela a11 = genom.gen("alik").alela("a11");
		final Alela a12 = genom.gen("alik").alela("a12");
		final Alela a13 = genom.gen("alik").alela("a13");

		final Alela b11 = genom.gen("belik").alela("b11");
		final Alela b12 = genom.gen("belik").alela("b12");
		final Alela b13 = genom.gen("belik").alela("b13");

		druh.addGen(genom.gen("alik"));
		druh.addGen(genom.gen("belik"));

		final Jedinec jed = druh.zrozeni();
		Assert.assertEquals(ales(a11, b11), jed.getAlely());
	}

	@Test
	public void testGetAlely50() {
		final Alela a11 = genom.gen("alik").alela("a11");
		final Alela a12 = genom.gen("alik").alela("a12");
		final Alela a13 = genom.gen("alik").alela("a13");

		final Alela b11 = genom.gen("belik").alela("b11");
		final Alela b12 = genom.gen("belik").alela("b12");
		final Alela b13 = genom.gen("belik").alela("b13");

		final Jedinec jed = druh.zrozeni();
		Assert.assertEquals(ales(), jed.getAlely());
	}

	private Set<Alela> ales(final Alela... aly) {
		return new HashSet<>(Arrays.asList(aly));
	}

	@Test
	public void testRemove1() {
		final Alela a11 = genom.gen("alik").alela("a11");
		final Alela a12 = genom.gen("alik").alela("a12");
		final Alela a13 = genom.gen("alik").alela("a13");

		final Alela b11 = genom.gen("belik").alela("b11");
		final Alela b12 = genom.gen("belik").alela("b12");
		final Alela b13 = genom.gen("belik").alela("b13");

		druh.addGen(genom.gen("alik"));
		druh.addGen(genom.gen("belik"));

		final Jedinec jed = druh.zrozeni();
		Assert.assertEquals(ales(a11, b11), jed.getAlely());

		jed.put(a12);
		Assert.assertEquals(ales(a12, b11), jed.getAlely());

		jed.remove(a12);
		Assert.assertEquals(ales(a11, b11), jed.getAlely());

	}

	@Test
	public void testRemove2() {
		final Alela a11 = genom.gen("alik").alela("a11");
		final Alela a12 = genom.gen("alik").alela("a12");
		final Alela a13 = genom.gen("alik").alela("a13");

		final Alela b11 = genom.gen("belik").alela("b11");
		final Alela b12 = genom.gen("belik").alela("b12");
		final Alela b13 = genom.gen("belik").alela("b13");

		druh.addGen(genom.gen("alik"));
		druh.addGen(genom.gen("belik"));

		final Jedinec jed = druh.zrozeni();
		Assert.assertEquals(ales(a11, b11), jed.getAlely());

		jed.put(a12);
		Assert.assertEquals(ales(a12, b11), jed.getAlely());

		jed.remove(a13);
		Assert.assertEquals(ales(a12, b11), jed.getAlely());

	}

}
