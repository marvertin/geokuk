package cz.geokuk.plugins.kesoid.genetika;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.*;

public class AlelaTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {}

	private Genom genom;
	private Collection<Alela> alelyPred;

	@Before
	public void setUp() throws Exception {
		genom = new Genom();
		alelyPred = new ArrayList<>(genom.getAlely());
	}

	@Test
	public void test1() {
		Assert.assertEquals("ahoj", genom.gen("ha").alela("ahoj").getDisplayName());
	}

	@Test
	public void test2() {
		Assert.assertEquals("kukuč to je", genom.gen("ha").alela("ahoj").displayName("kukuč to je").getDisplayName());
	}

	@Test
	public void test3() {
		Assert.assertSame(genom.gen("ha").alela("ahoj"), genom.gen("ha").alela("ahoj"));
	}

	@Test
	public void test4() {
		final Alela alela = genom.gen("ha").alela("ahoj");
		Assert.assertNotSame(alela, genom.gen("ha").alela("ahojx"));
	}

	@Test
	public void test7() {
		genom.gen("x").alela("1");
		genom.gen("x").alela("2");
		genom.gen("x").alela("2");
		genom.gen("x").alela("3");
		Assert.assertEquals(alelyPred.size() + 3, genom.getAlely().size());
	}

}
