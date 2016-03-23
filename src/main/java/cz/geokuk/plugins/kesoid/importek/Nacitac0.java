package cz.geokuk.plugins.kesoid.importek;

import java.io.*;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import cz.geokuk.framework.ProgressModel;
import cz.geokuk.framework.ProgressorInputStream;
import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;
import cz.geokuk.util.lang.FString;

/**
 * @author veverka
 *
 */
public abstract class Nacitac0 {

	protected static final String	PREFIX_USERDEFINOANYCH_GENU	= "geokuk_";
	static Pattern					osetriCislo					= Pattern.compile("[^0-9]");

	protected abstract void nacti(File file, IImportBuilder builder, Future<?> future, ProgressModel aProgressModel) throws IOException;

	protected abstract void nacti(ZipFile zipFile, ZipEntry zipEntry, IImportBuilder builder, Future<?> future, ProgressModel aProgressModel) throws IOException;

	abstract boolean umiNacist(ZipEntry zipEntry);

	abstract boolean umiNacist(File file);

	protected final void nactiBezVyjimky(final File file, final IImportBuilder builder, final Future<?> future, final ProgressModel aProgressModel) {
		try {
			try {
				nacti(file, builder, future, aProgressModel);
			} catch (final IOException e) {
				throw new RuntimeException("Problem reading \"" + file + "\"", e);
			}
		} catch (final Exception e) {
			FExceptionDumper.dump(e, EExceptionSeverity.DISPLAY, "Problem při načítání kešek, ale jedeme dál");
		}
	}

	protected final void nactiBezVyjimky(final ZipFile zipFile, final ZipEntry zipEntry, final IImportBuilder builder, final Future<?> future, final ProgressModel aProgressModel) {
		try {
			try {
				nacti(zipFile, zipEntry, builder, future, aProgressModel);
			} catch (final IOException e) {
				throw new RuntimeException("Problem reading \"" + zipEntry + "\"", e);
			}
		} catch (final Exception e) {
			FExceptionDumper.dump(e, EExceptionSeverity.DISPLAY, "Problem při načítání kešek, ale jedeme dál");
		}
	}

	/**
	 * @param aString
	 * @return
	 */
	protected int parseCislo(String s) {
		s = osetriCislo.matcher(s).replaceAll("").trim();
		if (s.length() == 0) {
			return 0;
		}
		try {
			return Integer.parseInt(s);
		} catch (final NumberFormatException e) {
			FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Problem s parsrovanim cisla \"" + s + "\" pri cteni hodnoceni nebo bestofu");
			return 0; // je to španě, vrátíme nuli
		}
	}

	protected InputStream wrapByProgressor(final InputStream istm, final String sourceName, final ProgressModel aProgressModel) {
		return new BufferedInputStream(new ProgressorInputStream(aProgressModel, "Loading: " + sourceName, istm));
	}

	protected String intern(final String aString) {
		return FString.intern(aString);
	}
}
