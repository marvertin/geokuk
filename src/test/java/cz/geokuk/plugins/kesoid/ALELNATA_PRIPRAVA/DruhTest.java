package cz.geokuk.plugins.kesoid.ALELNATA_PRIPRAVA;

import org.junit.*;

public class DruhTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {}

	private Genom genom;

	@Before
	public void setUp() throws Exception {
		genom = new Genom();
	}

	@Test
	public void test1() {
		Assert.assertEquals("ahoj", genom.druh("ahoj").getDisplayName());
	}

	@Test
	public void test2() {
		Assert.assertEquals("kukuč to je", genom.druh("ahoj").displayName("kukuč to je").getDisplayName());
	}

	@Test
	public void test3() {
		Assert.assertSame(genom.druh("ahoj"), genom.druh("ahoj"));
	}

	@Test
	public void test4() {
		Assert.assertNotSame(genom.druh("ahoj"), genom.druh("ahojx"));
	}

	@Test
	public void test7() {
		genom.druh("1");
		genom.druh("2");
		genom.druh("2");
		genom.druh("3");
		Assert.assertEquals(3, genom.getDruhy().size());
	}

}
