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
		Assert.assertEquals(alelyPred.size() + 3+1, genom.getAlely().size());
	}

	@Test
	public void testName1() {
		final Alela xqq_189 = genom.gen("xqq").alela("189");
		Assert.assertEquals("189", xqq_189.simpleName());
	}

	@Test
	public void testName2() {
		final Alela xqq_189 = genom.gen("xqq").alela("189");
		Assert.assertEquals("189:xqq", xqq_189.qualName());
		Assert.assertNotEquals("x189:xqq", xqq_189.qualName());
	}

	public void locate0() {
		Assert.assertEquals(":", genom.ODDELOVAC_KVALIFOVANY);
	}

	@Test
	public void locate1() {
		final Alela alela = genom.gen("xx").alela("11");
		Assert.assertSame(alela, genom.locateQualAlela("11:xx").get());
	}

	@Test
	public void locate2() {
		genom.gen("xx").alela("11");
		Assert.assertFalse(genom.locateQualAlela("11:xy").isPresent());
	}

	@Test
	public void locate3() {
		genom.gen("xx").alela("11");
		Assert.assertFalse(genom.locateQualAlela("12:xx").isPresent());
	}

	@Test
	public void locate4() {
		genom.gen("xx").alela("11");
		Assert.assertFalse(genom.locateQualAlela("12xx").isPresent());
	}

}
