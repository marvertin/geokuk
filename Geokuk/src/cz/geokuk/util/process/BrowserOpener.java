package cz.geokuk.util.process;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;


/**
 * 
 * Komplet zkopírované z webu
 * 
 * A simple, static class to display a URL in the system browser.
 *
 * Under Unix, the system browser is hard-coded to be 'netscape'.
 * Netscape must be in your PATH for this to work.  This has been
 * tested with the following platforms: AIX, HP-UX and Solaris.
 *
 * Under Windows, this will bring up the default browser under windows,
 * usually either Netscape or Microsoft IE.  The default browser is
 * determined by the OS.  This has been tested under Windows 95/98/NT.
 *
 * Examples:
 * * BrowserControl.displayURL("http://www.javaworld.com")
 *
 * BrowserControl.displayURL("file://c:\\docs\\index.html")
 *
 * BrowserContorl.displayURL("file:///user/joe/index.html");
 * 
 * Note - you must include the url type -- either "http://" or
 * "file://".
 */
public class BrowserOpener {


  /**
   * Display a file in the system browser.  If you want to display a
   * file, you must include the absolute path name.
   *
   * @param url the file's url (the url must start with either "http://" or
   * "file://").
   */
  public static void displayURL(URL url) {
    boolean windows = isWindowsPlatform();
    String cmd1 = null;
    String cmd2 = null;
    try {
      if (windows) {
        // cmd = 'rundll32 url.dll,FileProtocolHandler http://...'
        cmd1 = WIN_PATH + " " + WIN_FLAG + " " + url;
        Runtime.getRuntime().exec(cmd1);
      } else {
        // Under Unix, Netscape has to be running for the "-remote"
        // command to work.  So, we try sending the command and
        // check for an exit value.  If the exit command is 0,
        // it worked, otherwise we need to start the browser.
        // cmd = 'netscape -remote openURL(http://www.javaworld.com)'
        cmd1 = UNIX_PATH + " " + UNIX_FLAG + "(" + url + ")";
        Process p = Runtime.getRuntime().exec(cmd1);
        try {
          // wait for exit code -- if it's 0, command worked,
          // otherwise we need to start the browser up.
          int exitCode = p.waitFor();
          if (exitCode != 0) {
            // Command failed, start up the browser
            // cmd = 'netscape http://www.javaworld.com'
            cmd2 = UNIX_PATH + " " + url;
            p = Runtime.getRuntime().exec(cmd2);
          }
        } catch (InterruptedException x) {
          FExceptionDumper.dump(x, EExceptionSeverity.WORKARROUND, "Přerušení metodou interrupt(), taková výjimka asi nikdy nenastane.");
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(String.format("Nedari se otevrit browser na %s pro \"%s\" cmd1=\"%s\" cmd2= \"%s\"",
          System.getProperty("os.name"), url, cmd1, cmd2), e);
    }
  }

  /**
   * Try to determine whether this application is running under Windows
   * or some other platform by examing the "os.name" property.
   *
   * @return true if this application is running under a Windows OS
   */
  public static boolean isWindowsPlatform() {
    String os = System.getProperty("os.name");
    if (os != null && os.startsWith(WIN_ID))
      return true;
    else
      return false;

  }

  /**
   * Simple example.
   * @throws MalformedURLException
   */
  public static void main(String[] args) throws MalformedURLException {
    displayURL(new URL("http://www.javaworld.com"));
  }

  // Used to identify the windows platform.
  private static final String WIN_ID = "Windows";

  // The default system browser under windows.
  private static final String WIN_PATH = "rundll32";

  // The flag to display a url.
  private static final String WIN_FLAG = "url.dll,FileProtocolHandler";

  // The default browser under unix.
  private static final String UNIX_PATH = "netscape";

  // The flag to display a url.
  private static final String UNIX_FLAG = "-remote openURL";
}
