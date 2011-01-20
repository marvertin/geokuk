package cz.geokuk.util.exception;

import java.io.IOException;
import java.net.URL;

import cz.geokuk.util.lang.StringUtils;


/**
 * Dumpuje výjimky na standardní chybový výstup.
 * Použije se, pokud se nepodaří inicializovat skutečnou repozitoř.
 * @author veverka
 */
public class StdErrExceptionDumperRepository implements ExceptionDumperRepositorySpi {


  /* (non-Javadoc)
   * @see cz.tconsult.tw.util.exception.ExceptionDumperRepositorySpi#write(java.lang.String, java.lang.String)
   */
  public void write(AExcId aCode, String aExceptionData) throws IOException {
    System.err.println(aCode + StringUtils.LINE_SEPARATOR + aExceptionData);
  }

  /* (non-Javadoc)
   * @see cz.tconsult.tw.util.exception.ExceptionDumperRepositorySpi#getRunNumber()
   */
  public int getRunNumber() {
    return 0;
  }

  /**
   * Nelze konvertovat na URL
   * @param aCode
   * @return
   * @since 15.9.2006 7:24:56
   */
  public URL getUrl(AExcId aCode) {
    return null;
  }

  /**
   * @return false
   * @see cz.geokuk.util.exception.ExceptionDumperRepositorySpi#isReadable()
   */
  public boolean isReadable() {
    return false;
  }
  
}
