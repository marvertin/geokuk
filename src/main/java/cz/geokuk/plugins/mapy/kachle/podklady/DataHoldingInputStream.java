package cz.geokuk.plugins.mapy.kachle.podklady;

import java.io.*;

/**
 * Stream drží veškerá data, která se načetla a dokáže je pak vydat jako bytové pole.
 *
 * @author Martin Veverka
 *
 */
class DataHoldingInputStream extends FilterInputStream {

	private final ByteArrayOutputStream baos = new ByteArrayOutputStream(256 * 256);

	protected DataHoldingInputStream(final InputStream in) {
		super(in);
	}

	public byte[] getData() {
		return baos.toByteArray();
	}

	@Override
	public int read() throws IOException {
		final int c = super.read();
		baos.write(c);
		return c;
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		final int delka = super.read(b, off, len);
		if (delka > 0) {
			baos.write(b, off, delka);
		}
		return delka;
	}

}
