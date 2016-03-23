/*
 * Created on 29.3.2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cz.geokuk.util.file;

import java.io.*;

import cz.geokuk.util.lang.FThrowable;

/**
 *
 * Obalí řádek zadaným prefixem či suffixem.
 */
public class LineWrappingDecorationWriter extends LineDecorationWriter {

	private String	iPrefix;
	private String	iSuffix;

	/**
	 * @param out
	 */
	public LineWrappingDecorationWriter(final Writer out) {
		super(out);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cz.tconsult.tw.util.LineDecorationWriter#onLineBeg()
	 */
	@Override
	protected void onLineBeg() throws IOException {
		if (iPrefix != null) {
			super.out.write(iPrefix);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cz.tconsult.tw.util.LineDecorationWriter#onLineEnd()
	 */
	@Override
	protected void onLineEnd() throws IOException {
		if (iSuffix != null) {
			super.out.write(iSuffix);
		}
	}

	/**
	 * @return
	 */
	public String getPrefix() {
		return iPrefix;
	}

	/**
	 * @return
	 */
	public String getSuffix() {
		return iSuffix;
	}

	/**
	 * @param aString
	 */
	public void setPrefix(final String aString) {
		iPrefix = aString;
	}

	/**
	 * @param aString
	 */
	public void setSuffix(final String aString) {
		iSuffix = aString;
	}

	private static void vyja(final int pocet) {
		if (pocet <= 0) {
			throw new RuntimeException("yyyyyyyyyyyyyyyyy");
		}
		vyja(pocet - 1);
	}

	public static void main(final String[] args) {
		System.out.println("JEDU");
		final LineWrappingDecorationWriter wrt = new LineWrappingDecorationWriter(new OutputStreamWriter(System.out));
		wrt.setPrefix("[[[[[[[[");
		wrt.setSuffix("]]]]]]]");

		try (PrintWriter pwrt = new PrintWriter(wrt, true)) {
			pwrt.print("aaaaaaaa");
			pwrt.print("bbbbbbb");
			pwrt.print("cccccccc\r");
			pwrt.flush();
			pwrt.print("\nNOVY");
			pwrt.println("dddddd");
			pwrt.println();
			pwrt.println();
			pwrt.println("aaaaaaaaaaaa\r\rbbbbbbbbbbbbbbbb\r\nccccccccccccc\n\n");
			try {
				vyja(10);
			} catch (final Exception e) {
				FThrowable.printStackTrace(e, System.err, "POKUSNA");
			}

		}
	}

}
