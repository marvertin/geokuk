package cz.geokuk.util.procak;

import static org.junit.Assert.assertEquals;

/*
 * Akumuluje výsledky testu
 */
public class TestProcakAccum {

	StringBuilder sb = new StringBuilder();

	public void add(final String s) {
		if (sb.length() > 0) {
			sb.append(" ");
		}
		sb.append(s);
	}

	public void assertx(final String expected) {
		assertEquals(expected + " <1> <2> <3> <~> .1 .2 .3 .~", sb.toString());
	}

	public void asserty(final String expected) {
		assertEquals(expected, sb.toString());
	}


}
