package cz.geokuk.plugins.kesoid.ALELNATA_PRIPRAVA;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.*;

public class GenTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {}

	private Genom genom;
	private Collection<Gen> genyPred;

	@Before
	public void setUp() throws Exception {
		genom = new Genom();
		genyPred = new ArrayList<>(genom.getGeny());
	}

	@Test
	public void test1() {
		Assert.assertEquals("ahoj", genom.gen("ahoj").getDisplayName());
	}

	@Test
	public void test2() {
		Assert.assertEquals("kukuč to je", genom.gen("ahoj").displayName("kukuč to je").getDisplayName());
	}

	@Test
	public void test3() {
		Assert.assertSame(genom.gen("ahoj"), genom.gen("ahoj"));
	}

	@Test
	public void test4() {
		Assert.assertNotSame(genom.gen("ahoj"), genom.gen("ahojx"));
	}

	@Test
	public void test7() {
		genom.gen("1");
		genom.gen("2");
		genom.gen("2");
		genom.gen("3");
		Assert.assertEquals(genyPred.size() + 3, genom.getGeny().size());
	}

}
