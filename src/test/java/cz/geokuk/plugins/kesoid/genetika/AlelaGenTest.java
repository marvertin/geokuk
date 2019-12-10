package cz.geokuk.plugins.kesoid.genetika;

import org.junit.*;

import cz.geokuk.plugins.kesoid.genetika.*;

public class AlelaGenTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {}

	private Genom genom;

	@Before
	public void setUp() throws Exception {
		genom = new Genom();
	}

	@Test
	public void test1() {
		genom.gen("genik").add(genom.alela("alik"));
		Assert.assertSame(genom.alela("alik").getGen(), genom.gen("genik"));
	}

	@Test
	public void test2() {
		final Alela alela = genom.gen("genik").alela("alik");
		genom.gen("genik").add(alela);
	}

	@Test(expected = RuntimeException.class)
	public void test2a() {
		final Alela alela = genom.gen("genik").alela("alik");
		genom.gen("genikaaa").add(alela);
	}

	@Test
	public void test3() {
		final Gen gen = genom.gen("genik");
		gen.alela("1");
		gen.alela("2");
		Assert.assertEquals(2, gen.getAlely().size());
	}
}
