package cz.geokuk.util.file;

import java.io.*;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003 TurboConsult s.r.o.
 * </p>
 * <p>
 * Company:
 * </p>
 *
 * @author unascribed
 * @version 1.0
 */

public class TPumpReaderToWriter extends Thread {

	private final Reader	iRdr;
	private final Writer	iWrt;
	private boolean			iCloseWriter			= false;
	private boolean			iCloseReader			= true;
	private int				iBufferSize				= 1024;
	private Object			iNotificationReceiver	= this;
	private IOException		iException				= null;	// bude obsahovat výjimku, pokud nějaká bude
	private boolean			iIsFinished				= false;

	public TPumpReaderToWriter(final Reader aRdr, final Writer aWrt) {
		iRdr = aRdr;
		iWrt = aWrt;
	}

	@Override
	public void run() {
		try {
			final char[] buf = new char[iBufferSize];

			for (;;) {
				final int len = iRdr.read(buf);
				if (len <= 0) {
					break; // dočteno
				}
				// System.out.p rint(buf);
				iWrt.write(buf, 0, len);
			}
			iWrt.flush();
			if (iCloseReader) {
				iRdr.close();
			}
			if (iCloseWriter) {
				iWrt.close();
			}
			iIsFinished = true;
		} catch (final IOException ex) {
			synchronized (this) {
				iException = ex;
				iIsFinished = true;
			}
			ex.printStackTrace();
			throw new RuntimeException(ex);
		} finally {
			if (iNotificationReceiver != null) {
				synchronized (iNotificationReceiver) {
					iNotificationReceiver.notifyAll();
				}
			}
		}
	}

	public synchronized IOException getException() {
		return iException;
	}

	public synchronized boolean isFinished() {
		return iIsFinished;
	}

	public boolean isCloseWriter() {
		return iCloseWriter;
	}

	public void setCloseWriter(final boolean closeWriter) {
		this.iCloseWriter = closeWriter;
	}

	public void setCloseReader(final boolean closeReader) {
		this.iCloseReader = closeReader;
	}

	public boolean isCloseReader() {
		return iCloseReader;
	}

	public void setBufferSize(int bufferSize) {
		if (bufferSize < 1) {
			bufferSize = 1;
		}
		this.iBufferSize = bufferSize;
	}

	public int getBufferSize() {
		return iBufferSize;
	}

	public void setNotificationReceiver(final Object notificationReceiver) {
		this.iNotificationReceiver = notificationReceiver;
	}

	public Object getNotificationReceiver() {
		return iNotificationReceiver;
	}

}