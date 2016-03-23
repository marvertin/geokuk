package cz.geokuk.core.program;

import java.io.File;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jcabi.manifests.Manifests;

public class FConst {

	private static final String	NOT_VERSION_I_AM_IN_DEVELOP			= "develop";

	private static final Logger	log									= LogManager.getLogger(FConst.class.getSimpleName());

	public static final boolean	ZAKAZAT_PRIPRAVOVANOU_FUNKCIONALITU	= false;

	public static final boolean	JAR_DIR_EXISTUJE;

	public static final String	VERSION;

	public static final boolean	I_AM_IN_DEVELOPMENT_ENVIRONMENT;

	public static final File	JAR_DIR;

	public static final File	PREFERENCES_FILE;

	public static final String	LATEST_RELEASE_URL					= "https://github.com/marvertin/geokuk/releases/latest";
	public static final String	POST_PROBLEM_URL					= "https://github.com/marvertin/geokuk/issues/new";

	public static final String	WEB_PAGE_URL						= "http://geokuk.cz/";

	public static final String	WEB_PAGE_WIKI						= "  http://wiki.geocaching.cz/wiki/Geokuk";

	public static final int		MAX_POC_WPT_NA_MAPE					= 30000;

	public static final File	HOME_DIR							= new File(System.getProperty("user.home"));

	private static final URL	zarazkatxt;

	static {
		final String ZARAZKA_TXT = "zarazka.txt";
		zarazkatxt = FConst.class.getClassLoader().getResource(ZARAZKA_TXT);
		final String s = zarazkatxt.toExternalForm();
		final Pattern pat1 = Pattern.compile("jar:file:/(.*)!/geokuk/" + ZARAZKA_TXT);
		final Pattern pat2 = Pattern.compile("file:/(.*)/geokuk/" + ZARAZKA_TXT);
		final Matcher mat1 = pat1.matcher(s);
		final Matcher mat2 = pat2.matcher(s);
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
		// versionproperties.
		String version;
		boolean iamindevelopmentenvi;
		try {
			version = Manifests.read("Geokuk-Version");
			iamindevelopmentenvi = false;
		} catch (final IllegalArgumentException e) {
			version = NOT_VERSION_I_AM_IN_DEVELOP;
			iamindevelopmentenvi = true;
		}
		VERSION = version;
		I_AM_IN_DEVELOPMENT_ENVIRONMENT = iamindevelopmentenvi;
		// preferenčník
		PREFERENCES_FILE = new File(JAR_DIR, "geokuk-preferences.xml");

	}

	public static void logInit() {
		log.info("GEOKUK " + VERSION);
		log.info("JAR_DIR = " + JAR_DIR);
		log.info("JAR_DIR_EXISTUJE = " + JAR_DIR_EXISTUJE);
		log.info("HOME_DIR = " + HOME_DIR);
		log.info("WEB_PAGE_URL = " + WEB_PAGE_URL);
	}

	public static final String NL = System.getProperty("line.separator");

}
