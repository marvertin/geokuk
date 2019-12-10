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
	public void test1() {
		genom.gen("genik").alela("alik");
		Assert.assertSame(genom.seekAlela("alik").getGen(), genom.gen("genik"));
	}

	@Test
	public void test3() {
		final Gen gen = genom.gen("genik");
		gen.alela("1");
		gen.alela("2");
		Assert.assertEquals(2, gen.getAlely().size());
	}
}
