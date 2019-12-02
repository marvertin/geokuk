package cz.geokuk.plugins.kesoid.importek;

import java.io.*;
import java.util.*;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.plugins.kesoid.KesBag;
import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.mvc.KesoidModel;
import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;
import cz.geokuk.util.file.*;

/**
 * @author Martin Veverka
 */
public class MultiNacitac {
	private static final Logger log = LogManager.getLogger(MultiNacitac.class.getSimpleName());

	// TODO: Doporučuji přejmenovat na GEOKUK_ROOTDIR_DEF resp. GEOGET_ROOTDIR_DEF. Už dávno nejde jen o jméno souboru/složky. [2016-04-09, Bohusz]
	private static final Root.Def FILE_NAME_REGEX_GEOKUK_DIR = new Root.Def(Integer.MAX_VALUE, Pattern.compile("(?i).*\\.(geokuk|gpx|zip|jpg|raw|tif)"), null);
	private static final Root.Def FILE_NAME_REGEX_GEOGET_DIR = new Root.Def(1, Pattern.compile("(?i).*\\.db3"), Pattern.compile("(?i).*\\.[0-9]{8}\\.db3"));
	private static final Root.Def GSAK_ROOTDIR_DEF = new Root.Def(2, Pattern.compile("sqlite.db3"), null);

	private final DirScanner ds;

	private final List<Nacitac0> nacitace = new ArrayList<>();
	private final KesoidModel kesoidModel;

	// private static final String CACHE_SUFFIX = ".cache.serialized";

	/**
	 * Checks whether the given file is a ZIP file. Copied from http://www.java2s.com/Code/Java/File-Input-Output/DeterminewhetherafileisaZIPFile.htm
	 */
	private static boolean isZipFile(final File fileToTest) {
		if (fileToTest.isDirectory()) {
			return false;
		}
		if (fileToTest.length() < 4) {
			return false;
		}
		try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(fileToTest)))) {
			final int test = in.readInt();
			return test == 0x504b0304;
		} catch (final IOException e) {
			throw new IllegalArgumentException("The file " + fileToTest + " cannot be checked!", e);
		}
	}

	public MultiNacitac(final KesoidModel kesoidModel) {
		this.kesoidModel = kesoidModel;
		ds = new DirScanner();
		nacitace.add(new NacitacGeokuk());
		nacitace.add(new NacitacGpx());
		nacitace.add(new NacitacImageMetadata());
		nacitace.add(new GeogetLoader());
		nacitace.add(new GsakDbLoader(kesoidModel::getGsakParametryNacitani));
	}

	public List<KeFile> gsakSoubory(final Filex aDataDir) {
		return ds.scan(new Root(aDataDir.getFile(), GSAK_ROOTDIR_DEF));
	}

	public KesBag nacti(final Future<?> future, final Genom genom) throws IOException {
		final List<KeFile> list = ds.coMamNacist();
		if (list == null) {
			return null;
		}
		final KesoidImportBuilder builder = new KesoidImportBuilder(kesoidModel.getGccomNick(), kesoidModel.getProgressModel());
		builder.init();
		for (final KeFile file : list) {
			log.debug("Nacitam: " + file);
			try {
				zpracujJedenFile(file, builder, future);
			} catch (final Exception e) {
				FExceptionDumper.dump(e, EExceptionSeverity.DISPLAY, "Problem pri cteni souboru " + file);
				ds.nulujLastScaned(); // ať se načte znovu
			}
		}

		builder.done(genom);

		return builder.getKesBag();
	}

	// TODO Proč jsou tu ty File parametry, když máme k dispozici kesoidModel, odkud se jejich hodnoty vždy berou? [2016-04-09, Bohusz]
	public void setRootDirs(final boolean prenacti, final File kesDir, final File geogetDir, final File gsakDir) {
		final List<Root> roots = new ArrayList<>();
		if (kesDir != null) {
			roots.add(new Root(kesDir, FILE_NAME_REGEX_GEOKUK_DIR));
		}
		if (geogetDir != null) {
			roots.add(new Root(geogetDir, FILE_NAME_REGEX_GEOGET_DIR));
		}
		if (gsakDir != null) {
			roots.add(new Root(gsakDir, GSAK_ROOTDIR_DEF));
		}
		ds.seRootDirs(prenacti, roots.toArray(new Root[roots.size()]));
	}

	/**
	 * @param kefile
	 * @param builder
	 * @param future
	 * @throws IOException
	 */
	private void zpracujJedenFile(final KeFile kefile, final KesoidImportBuilder builder, final Future<?> future) throws IOException {
		final File file = kefile.getFile();
		if (isZipFile(file)) {
			try (ZipFile zipFile = new ZipFile(file)) {
				final boolean nacitat = kesoidModel.maSeNacist(kefile);
				for (final Enumeration<? extends ZipEntry> en = zipFile.entries(); en.hasMoreElements();) {
					final ZipEntry entry = en.nextElement();
					for (final Nacitac0 nacitac : nacitace) {
						builder.setCurrentlyLoading(kefile, nacitat);
						if (nacitat && nacitac.umiNacist(entry)) {
							nacitac.nactiBezVyjimky(zipFile, entry, builder, future, kesoidModel.getProgressModel());
						}
					}
				}
			}
		} else {
			for (final Nacitac0 nacitac : nacitace) {
				final boolean nacitat = kesoidModel.maSeNacist(kefile);
				builder.setCurrentlyLoading(kefile, nacitat);
				if (nacitat && nacitac.umiNacist(file)) {
					nacitac.nactiBezVyjimky(file, builder, future, kesoidModel.getProgressModel());
				}
			}
		}
	}
}
