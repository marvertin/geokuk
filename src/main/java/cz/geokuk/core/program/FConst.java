package cz.geokuk.core.program;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FConst {

    private static final Logger log =
            LogManager.getLogger(FConst.class.getSimpleName());

  public static final boolean ZAKAZAT_PRIPRAVOVANOU_FUNKCIONALITU = false;

  public static final boolean JAR_DIR_EXISTUJE;

  public static final String VERSION;

  public static final File JAR_DIR;

  public static final File PREFERENCES_FILE;

  public static final String LATEST_RELEASE_URL = "https://github.com/marvertin/geokuk/releases/latest";
  public static final String POST_PROBLEM_URL = "https://github.com/marvertin/geokuk/issues/new";

  public static final String WEB_PAGE_URL = "http://geokuk.cz/";

  public static final String WEB_PAGE_WIKI = "  http://wiki.geocaching.cz/wiki/Geokuk";

  public static final int MAX_POC_WPT_NA_MAPE = 30000;

  public static final File HOME_DIR = new File(System.getProperty("user.home"));

  private static final URL versionproperties;


  static {
    try {
      Properties prop = new Properties();
      String VERSION_PROPERTIES = "version.properties";
      versionproperties = FConst.class.getClassLoader().getResource(VERSION_PROPERTIES);
      String s = versionproperties.toExternalForm();
      Pattern pat1 = Pattern.compile("jar:file:/(.*)!/geokuk/version.properties");
      Pattern pat2 = Pattern.compile("file:/(.*)/geokuk/version.properties");
      Matcher mat1 = pat1.matcher(s);
      Matcher mat2 = pat2.matcher(s);
      if (mat1.matches()) { // je to z jaru
        JAR_DIR = new File(mat1.group(1)).getParentFile();
        JAR_DIR_EXISTUJE = true;
      } else if (mat2.matches()) {
        JAR_DIR = new File(mat2.group(1));
        JAR_DIR_EXISTUJE = true;
      } else {
        JAR_DIR = new File("").getAbsoluteFile();
        JAR_DIR_EXISTUJE = false;
      }
      log.debug(JAR_DIR);
      //versionproperties.
      InputStream istm = versionproperties.openStream();
      prop.load(istm);
      VERSION = prop.getProperty("version");

      // preferenčník
      PREFERENCES_FILE = new File(JAR_DIR, "geokuk-preferences.xml");


    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static void logInit() {
    log.info("GEOKUK " + VERSION);
      log.info("VERSION PROEPRTIES: " + versionproperties);
      log.info("JAR_DIR = " + JAR_DIR);
      log.info("JAR_DIR_EXISTUJE = " + JAR_DIR_EXISTUJE);
      log.info("HOME_DIR = " + HOME_DIR);
      log.info("WEB_PAGE_URL = " + WEB_PAGE_URL);
  }


  public static final String NL = System.getProperty("line.separator");

}
