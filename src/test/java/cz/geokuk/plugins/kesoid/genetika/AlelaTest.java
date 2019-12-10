package cz.geokuk.plugins.kesoid.genetika;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.*;

import cz.geokuk.plugins.kesoid.genetika.Alela;
import cz.geokuk.plugins.kesoid.genetika.Genom;

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
		Assert.assertEquals("ahoj", genom.alela("ahoj").getDisplayName());
	}

	@Test
	public void test2() {
		Assert.assertEquals("kukuč to je", genom.alela("ahoj").displayName("kukuč to je").getDisplayName());
	}

	@Test
	public void test3() {
		Assert.assertSame(genom.alela("ahoj"), genom.alela("ahoj"));
	}

	@Test
	public void test4() {
		Assert.assertNotSame(genom.alela("ahoj"), genom.alela("ahojx"));
	}

	@Test(expected = NullPointerException.class)
	public void test5() {
		Assert.assertNull(genom.alela("ahoj").getGen());
	}

	@Test
	public void test6() {
		Assert.assertFalse(genom.alela("ahoj").hasGen());
	}

	@Test
	public void test7() {
		genom.alela("1");
		genom.alela("2");
		genom.alela("2");
		genom.alela("3");
		Assert.assertEquals(alelyPred.size() + 3, genom.getAlely().size());
	}

}
