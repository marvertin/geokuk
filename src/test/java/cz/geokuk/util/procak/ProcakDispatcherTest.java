package cz.geokuk.util.procak;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

public class ProcakDispatcherTest {

	private TestProcakAccum accum;
	private ProcakDispatcher<String> pd;

	@Before
	public void setUp() {
		accum = new TestProcakAccum();
		pd = new ProcakDispatcher<>(
				Arrays.asList(new TestProcak1(accum), new TestProcak2(accum), new TestProcak3(accum)));

	}


	@Test
	public void test1() {
		pd.dispatch("1blb");
		pd.done();
		accum.assertx("x1blb");
	}

	@Test
	public void test2() {
		pd.dispatch("2blb");
		pd.done();
		accum.assertx("y2blb");
	}

	@Test
	public void test3() {
		pd.dispatch("*blb");
		pd.done();
		accum.assertx("x*blb");
	}

	@Test
	public void test6() {
		pd.dispatch("1a");
		pd.dispatch("2b");
		pd.dispatch("1c");
		pd.dispatch("*d");
		pd.dispatch("2e");
		pd.dispatch("*f");
		pd.done();
		accum.assertx("x1a y2b x1c x*d y2e x*f");
	}


	@Test
	public void test7() {
		pd.dispatch("Q3");
		pd.done();
		accum.assertx("zQ3");
	}

	@Test
	public void test8() {
		pd.dispatch("Q8");
		pd.done();
		accum.assertx("<1> <2> <3> qQ8");
	}

	@Test
	public void test9() {
		pd.dispatch("1a");
		pd.dispatch("2b");
		pd.dispatch("Qano");
		pd.dispatch("3u");
		pd.dispatch("Q3");
		pd.dispatch("1c");
		pd.dispatch("*d");
		pd.dispatch("2e");
		pd.dispatch("Qne");
		pd.dispatch("*f");
		pd.done();
		accum.assertx("x1a y2b z3u zQ3 x1c x*d y2e x*f <1> <2> <3> qQano qQne");
	}

	@Test
	public void test10() {
		pd.dispatch("1a");
		pd.dispatch("QQo");
		pd.dispatch("2b");
		pd.dispatch("Qano");
		pd.dispatch("3u");
		pd.dispatch("Q3");
		pd.dispatch("1c");
		pd.dispatch("*d");
		pd.dispatch("2e");
		pd.dispatch("Qne");
		pd.dispatch("*f");
		pd.done();
		accum.assertx("x1a x(QQo) y2b z3u zQ3 x1c x*d y2e x*f <1> <2> <3> / qQano qQne <1> <2> <3> qQQo");
	}

	@Test
	public void test14() {
		pd.dispatch("3a");
		pd.dispatch("3b");
		pd.dispatch("3c");
		pd.done();
		accum.assertx("z3a z3b z3c");
	}

	@Test
	public void test15() {
		pd.dispatch("3Hr");
		pd.done();
		accum.assertx("y(3Hr)a <1> <2> y(3Hr)b z3Hr <3>");
	}

	@Test
	public void test16() {
		pd.dispatch("3H+");
		pd.done();
		accum.assertx("y(3H+)a <1> <2> y(3H+)b y3H+ <3>");
	}

	@Test
	public void test17() {
		pd.dispatch("3Hr");
		pd.dispatch("3H+");
		pd.done();
		accum.assertx("y(3Hr)a y(3H+)a <1> <2> y(3Hr)b z3Hr y(3H+)b y3H+ <3>");
	}

	@Test
	public void test18() {
		pd.dispatch("3Hr");
		pd.dispatch("3ji");
		pd.dispatch("3H+");
		pd.dispatch("2ka");
		pd.done();
		accum.assertx("y(3Hr)a z3ji y(3H+)a y2ka <1> <2> y(3Hr)b z3Hr y(3H+)b y3H+ <3>");
	}

}
