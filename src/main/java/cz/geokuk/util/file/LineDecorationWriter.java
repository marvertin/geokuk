/*
 * Created on 29.3.2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package cz.geokuk.util.file;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * @author veverka
 *
 * Writer umožňující nějak dekorovat řádky. Vždy na začátku a na konci
 * řádku vyvolá metodu, která může něco udělat.
 */
public abstract class LineDecorationWriter extends FilterWriter {

	private boolean iIgnoreLf;
	private boolean iProcessingLine;
	private boolean iEmptyLine;

	/**
	 * @param out
	 */
	public LineDecorationWriter(Writer out) {
		super(out);
	}


	/* (non-Javadoc)
	 * @see java.io.Writer#write(char[], int, int)
	 */
	public void write(char[] cbuf, int off, int len) throws IOException {
		int lastWrited = off;
		for (int i = off; i < off + len; i++) { // pro každý znak
			char c = cbuf[i];
			if (iIgnoreLf && c == '\n') continue; // ignorujeme lf, takže normálně zpracováváme
			iIgnoreLf = c == '\r'; // příští LF budeme ignorovat
			boolean endOfLine = c == '\r' || c == '\n'; // právě přišel znak ukončující řádek
			if (!iProcessingLine) { // právě přišel nějaký znak dalšího řádku, může to být i znak ukončovací
				iEmptyLine = endOfLine; // pokud jsme na začátku řádku, ale máme již konec, jedná se o prázdný řádek
				out.write(cbuf, lastWrited, i - lastWrited); // zapsat, až podsud (ne aktuální znak)
				lastWrited = i;
				onLineBeg(); // pokud sice právě skončil řádek, ale ještě nezačal, musíme označit začátek
				iProcessingLine = true;
			}
			if (endOfLine) {
				out.write(cbuf, lastWrited, i - lastWrited); // zapsat, až podsud (ne aktuální znak)
				lastWrited = i;
				onLineEnd(); // označit konec řádku
				iProcessingLine = false;
			}
		}
		out.write(cbuf, lastWrited, off + len - lastWrited); // zapsat nezapsaný zbytek
	}


	protected abstract void onLineBeg()  throws IOException;

	protected abstract void onLineEnd()  throws IOException;

	/**
	 * @return True, pokud právě zpracovávaný řádek v metodě {@link #onLineBeg()}
	 * nebo {@link #onLineEnd()} je prázdný, to znamená, že neobsahuje vůbec žádný znak,
	 * a to ani mezeru ani nic jiného.
	 */
	public boolean isEmptyLine() {
		return iEmptyLine;
	}

	/* (non-Javadoc)
	 * @see java.io.Writer#write(java.lang.String, int, int)
	 */
	public void write(String str, int off, int len) throws IOException {
		write(str.toCharArray(), off, len);
	}

}
