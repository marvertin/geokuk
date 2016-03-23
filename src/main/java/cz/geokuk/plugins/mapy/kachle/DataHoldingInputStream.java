package cz.geokuk.plugins.mapy.kachle;

import java.io.*;

/**
 * Stream drží veškerá data, která se načetla a dokáže je pak vydat jako bytové pole.
 * 
 * @author tatinek
 *
 */
class DataHoldingInputStream extends FilterInputStream {

	private ByteArrayOutputStream baos = new ByteArrayOutputStream(256 * 256);

	protected DataHoldingInputStream(InputStream in) {
		super(in);
	}

	@Override
	public int read() throws IOException {
		int c = super.read();
		baos.write(c);
		return c;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int delka = super.read(b, off, len);
		if (delka > 0) {
			baos.write(b, off, delka);
		}
		return delka;
	}

	public byte[] getData() {
		return baos.toByteArray();
	}

}
