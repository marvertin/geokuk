package cz.geokuk.util.exception;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cz.geokuk.util.file.FileManager;

/**
 * Repositoř výjime realizovaná na filesystému.
 *
 * @author veverka
 */
public class FileBasedExceptionDumperRepository implements ExceptionDumperRepositorySpi {

	private static Pattern		pat	= Pattern.compile(ExceptionDumperRepositorySpi.EXC_PREFIX + "([0-9]+)([a-z]+)([0-9]+)");

	/**
	 * APrověřený adresář
	 */
	private final File			iDir;

	/**
	 * Číslo spuštění
	 */
	private int					iRunNumber;

	private final FileManager	fm	= FileManager.getInstance(4096);

	/**
	 *
	 */
	public FileBasedExceptionDumperRepository(final File aDir) {
		try {
			if (aDir == null) {
				throw new IllegalArgumentException("The parameter aDir is null");
			}
			final File dir = aDir.getCanonicalFile();
			initDir(dir);
			iDir = dir;
		} catch (final Exception e) {
			throw new RuntimeException("Cannot use directory \"" + aDir + "\" as exception repository directory becouse: ", e);
		}
	}

	/**
	 * Zapíše text výjimky někam na základě zadaného kódu. Vyhodí výjimku, pokud se zápis z libovolného důvodu nepodaří.
	 *
	 * @param aCode
	 * @param aExceptionData
	 * @throws IOException
	 */
	@Override
	public void write(final AExcId aCode, final String aExceptionData) throws IOException {
		final File file = toFile(aCode);
		file.getParentFile().mkdirs();
		fm.writeStringToFile(file, aExceptionData);
	}

	/**
	 * Vrátí číslo spuštění výjimky.
	 *
	 * @return
	 */
	@Override
	public int getRunNumber() {
		return iRunNumber;
	}

	/**
	 * @return Returns the dir.
	 */
	public File getDir() {
		return iDir;
	}

	private void initDir(final File aDir) throws IOException {
		if (aDir.exists()) {
			if (!aDir.isDirectory()) {
				throw new RuntimeException("The file \"" + aDir + "\" exists, it is file, not folder!");
			}
		} else { // adresář neexistuje, jdeme ho vytvořit
			aDir.mkdirs(); // vytvoříme všechny adresáře na cestě
		}
		// takže víme, že máme existující adresář
		// musíme zkontrolovat, zda do něj lze zapisovat.
		if (!aDir.canRead()) {
			throw new RuntimeException("Cannot read from the directory \"" + aDir + "\"");
		}
		if (!aDir.canWrite()) {
			throw new RuntimeException("Cannot write to the directory \"" + aDir + "\"");
		}
		// nyní víme, že z adresáře můžeme číst a můžeme tam i zapisovat
		// nyní inkrementneme číslo spuštění
		final File spusteniFile = new File(aDir, "runNumber.txt");

		// Natáhne číslo spuštění, bude to vždy nové číslo
		iRunNumber = loadAndIncrementNumberInFile(spusteniFile);

		// A zkusíme, zda lze v adresáři opravdu pracovat
		zkusitPraciVAdresari(aDir);
	}

	/**
	 * @param aDir
	 * @throws IOException
	 */
	private void zkusitPraciVAdresari(final File aDir) throws IOException {
		// A zkusíme, že umíme vytvořid adresář i soubor (k randomům přičítáme číslo, aby nevylezlo moc malé číslo, jenž bude mít exponenciální vyjádření
		final File pokusdir = new File(aDir, "dir" + (Math.random() + 20));
		pokusdir.mkdir();
		final File pokusfile = new File(pokusdir, "filex" + (Math.random() + 30));
		fm.writeStringToFile(pokusfile, "Pokusný řetězec zapisovaný do souboru");
		pokusfile.delete();
		pokusdir.delete();
	}

	/**
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static int loadAndIncrementNumberInFile(final File aFile) throws IOException {
		aFile.getCanonicalFile().getParentFile().mkdirs();
		int runNumber = 0;
		try (RandomAccessFile raf = new RandomAccessFile(aFile, "rw")) {
			try (FileChannel channel = raf.getChannel()) {

				// Use the file channel to create a lock on the file.
				// This method blocks until it can retrieve the lock.
				final FileLock lock = channel.lock();
				try {
					int c;
					while ((c = raf.read()) >= 0) {
						if (c >= '0' && c <= '9') { // jen číslice nás zajímají
							runNumber = runNumber * 10 + c - '0';
						}
					}
					runNumber++; // inkrementujeme načtené číslo, pokud jsme nenačetli minule nic, inkrementujeme jedničku
					raf.seek(0);
					raf.write(Integer.toString(runNumber).getBytes());
					raf.setLength(raf.getFilePointer());
				} finally {
					// Release the lock
					lock.release();
				}
			}
		}
		return runNumber;
	}

	/**
	 * Převede kód přímo na soubor.
	 *
	 * @param aCode
	 * @return
	 */
	private File toFile(final AExcId aCode) {
		return new File(iDir, toRelativePath(aCode));
	}

	/**
	 * Převede kód výjimky do relativní cesty, kde bude uložena
	 *
	 * @param aCode
	 * @return
	 */
	private String toRelativePath(final AExcId aCode) {
		// pokud začíná kódem lokace, tah ho odstraníme
		final String code = aCode.toString().trim().toLowerCase();
		final StringBuilder sb = new StringBuilder();
		final Matcher matcher = pat.matcher(code);
		if (matcher.matches()) {
			sb.append(matcher.group(1)); // pořadové číslo
			sb.append('/'); // oddělovač adresářů
			sb.append(matcher.group(2)); // závažnost
			sb.append('/'); // oddělovač adresářů
			String cislo = matcher.group(3);
			while (cislo.length() < 4) {
				cislo = '0' + cislo;
			}
			final int rozdel = cislo.length() - 2; // aby byla stovka na složku
			sb.append(cislo.substring(0, rozdel));
		} else {
			sb.append("podivini");
		}
		sb.append('/'); // oddělovač adresářů
		sb.append(aCode);
		sb.append(".html");

		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return iDir.getAbsolutePath();
	}

	// public static void main(String[] args) throws IOException {
	// //ExceptionDumperRepositorySpi dr = new FileBasedExceptionDumperRepository(new File("c:/a/b/c/edure"));
	// ExceptionDumperRepositorySpi dr = FExceptionDumper.getDefaultRepository();
	// System.out.p rintln("Run number: " + dr.getRunNumber());
	// dr.write("123xx13", "Vyjima AA");
	// dr.write("123xx148", "Vyjima BB");
	// dr.write("123xx513", "Vyjima CC");
	// dr.write("123xx613", "Vyjima CC");
	// dr.write("123xx613a", "Vyjima CC");
	// //System.out.p rintln(dr.read("123xx14"));
	//
	// }

	@Override
	public URL getUrl(final AExcId aCode) {
		try {
			final File toFile = toFile(aCode);
			if (!toFile.canRead()) {
				return null; // nemohu číst
			}
			// Přes URI se jde do URL proto, aby se rozumně zakódovaly mezery
			return toFile.toURI().toURL();
		} catch (final MalformedURLException e) {
			throw new RuntimeException("Číslo chybové hlášky " + aCode + " nelze konvertovat na URL", e);
		}
	}

	/**
	 * @return true
	 * @see cz.geokuk.util.exception.ExceptionDumperRepositorySpi#isReadable()
	 */
	@Override
	public boolean isReadable() {
		return true;
	}

}
