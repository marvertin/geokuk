package cz.geokuk.util.exception;

import java.io.IOException;
import java.net.URL;

import cz.geokuk.util.lang.StringUtils;

/**
 * Dumpuje výjimky na standardní chybový výstup. Použije se, pokud se nepodaří inicializovat skutečnou repozitoř.
 *
 * @author veverka
 */
public class StdErrExceptionDumperRepository implements ExceptionDumperRepositorySpi {

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.tconsult.tw.util.exception.ExceptionDumperRepositorySpi#getRunNumber()
	 */
	@Override
	public int getRunNumber() {
		return 0;
	}

	/**
	 * Nelze konvertovat na URL
	 *
	 * @param aCode
	 * @return
	 * @since 15.9.2006 7:24:56
	 */
	@Override
	public URL getUrl(final AExcId aCode) {
		return null;
	}

	/**
	 * @return false
	 * @see cz.geokuk.util.exception.ExceptionDumperRepositorySpi#isReadable()
	 */
	@Override
	public boolean isReadable() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.tconsult.tw.util.exception.ExceptionDumperRepositorySpi#write(java.lang.String, java.lang.String)
	 */
	@Override
	public void write(final AExcId aCode, final String aExceptionData) throws IOException {
		System.err.println(aCode + StringUtils.LINE_SEPARATOR + aExceptionData);
	}

}
