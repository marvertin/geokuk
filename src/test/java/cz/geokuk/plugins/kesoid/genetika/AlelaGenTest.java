package cz.geokuk.plugins.kesoid.genetika;

import org.junit.*;

public class AlelaGenTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {}

	private Genom genom;

	@Before
	public void setUp() throws Exception {
		genom = new Genom();
	}


	@Test
	public void test3() {
		final Gen gen = genom.gen("genik");
		gen.alela("1");
		gen.alela("2");
		Assert.assertEquals(2+1, gen.getAlely().size());
	}

	@Test
	public void test4() {
		final Gen gen = genom.gen("genik");
		Assert.assertEquals(1, gen.getAlely().size());
	}


	@Test
	public void test5() {
		final Gen gen = genom.gen("genik");
		Assert.assertTrue(gen.alela("~~").isVychozi());
	}
}
