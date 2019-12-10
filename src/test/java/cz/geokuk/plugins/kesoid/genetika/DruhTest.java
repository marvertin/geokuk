package cz.geokuk.plugins.kesoid.genetika;

import org.junit.*;

import cz.geokuk.plugins.kesoid.genetika.*;

public class DruhTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

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
		genom.druh("2");
		genom.druh("2");
		genom.druh("2");
		genom.druh("3");
		genom.druh("3");
		genom.druh("3");
		// je zde i univerzální druh
		Assert.assertEquals(3 + 1, genom.getDruhy().size());
	}

	@Test
	public void test8() {
		// už teď tam bude univerzální druh
		Assert.assertEquals(1, genom.getDruhy().size());
	}

	@Test
	public void test9() {
		// už teď tam bude univerzální druh
		final Druh druh = genom.druh("blbec");
		final Gen gen = genom.gen("mujgen");
		Assert.assertFalse(druh.hasGen(gen));
		druh.addGen(gen);
		Assert.assertTrue(druh.hasGen(gen));
	}
}
