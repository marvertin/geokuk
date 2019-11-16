package cz.geokuk.util.exception;

import java.io.*;
import java.net.URL;

import cz.geokuk.util.file.TPumpReaderToWriter;
import cz.geokuk.util.lang.FThrowable;
import cz.geokuk.util.lang.StringUtils;

/**
 * Pomocná třída pro dumpování výjimek. Volání metod na této třídě je ekvivalentní s voláním metod na ExcpetionDumper.getInstance(). Jsou zde jen nejčastěji používané a to jen dumpovací metody. Pro všechny ostatní metody si získejte isntanci exception dumperu.
 *
 * @author Martin Veverka
 * @since 15.9.2006 8:18:58
 */
public final class FExceptionDumper {
	public static final File EXCEPTION_DIR = new File(new File(System.getProperty("java.io.tmpdir"), "geokuk"), "excrep");

	// private static final String EXCREP = "excrep";
	private static final String TC_EXCREP_DIR_PROPERTY = "tc.excrep.dir";

	private static ExceptionDumperRepositorySpi sDefaultRepository;

	private static ThreadLocal<ExceptionDumper> tExceptinDumper = new ThreadLocal<ExceptionDumper>() {
		@Override
		protected ExceptionDumper initialValue() {
			return new ExceptionDumper(); // pokud žádný dumper není nastaven, tak se vrací jakýsi implicitní
		}
	};

	/**
	 * Vydampuje výjimku do repozitoře a vrátí její identifikaci. Dumpuje do implicitní repozitoře.
	 *
	 * @param aThrowable
	 * @param aExceptionSeverity
	 * @param aCircumstance
	 * @return Identifikace vydumpované výjimky
	 */
	public static AExcId dump(final Throwable aThrowable, final EExceptionSeverity aExceptionSeverity, final String aCircumstance) {
		final AExcId excid = getExceptionDumper().dump(aThrowable, aExceptionSeverity, aCircumstance, getDefaultRepository());
		if (aExceptionSeverity == EExceptionSeverity.DISPLAY || aExceptionSeverity == EExceptionSeverity.WORKARROUND) {
			FError.report(aThrowable.getMessage(), excid);
		}
		return excid;
	}

	/**
	 * Vydampuje výjimky do repozitře pod jeden soubor a vrátí identifikaci prvního z nich. Dumpuje do implicitní repozitoře.
	 *
	 * @param aThrowables
	 * @param aExceptionSeverity
	 * @param aCircumstances
	 * @return
	 * @since 4.8.2006 10:46:11
	 */
	public static AExcId dump(final Throwable[] aThrowables, final EExceptionSeverity aExceptionSeverity, final String[] aCircumstances) {
		return getExceptionDumper().dump(aThrowables, aExceptionSeverity, aCircumstances, getDefaultRepository());
	}

	/**
	 * Vrátí implicíitní repositry. Adresář s repository je určen systémovou property {@value #TC_EXCREP_DIR_PROPERTY}. Pokud property není nastavena, je repozitoř v adresáři $HOME/excrep.
	 *
	 * @return
	 */
	public synchronized static ExceptionDumperRepositorySpi getDefaultRepository() {
		if (sDefaultRepository == null) {
			final File dir = getExcrepFolder();
			try {
				sDefaultRepository = new FileBasedExceptionDumperRepository(dir);
			} catch (final RuntimeException e) {
				// Vypíšeme výjimku při inicializaci repozitoře a dále musíme fungovat
				FThrowable.printStackTrace(e, System.err, "inicializingExceptionRepository");
				sDefaultRepository = new StdErrExceptionDumperRepository();
			}
		}
		return sDefaultRepository;
	}

	/**
	 * Vrátí plný texty výjimku, jejíž kód je zadán. Text lze považovat za HTML.
	 *
	 * @param aCode
	 *            Kód výjimky. Je to přesně ten kód, který byl vrácen metodou {@link #dump(Throwable, EExceptionSeverity, String)}
	 * @return Vrací null, pokud není pod daným kódem registrována žádná výjimka.
	 */
	public static String getDumpedException(final AExcId aCode) {
		final ExceptionDumperRepositorySpi repository = getDefaultRepository();
		if (!repository.isReadable()) {
			return "Exception " + aCode + " is in the unreadeable repository: " + repository.getClass().getName();
		}
		try {
			// očekáváme defaultní kódování
			final URL url = repository.getUrl(aCode);
			if (url == null) {
				return null;
			}
			final InputStreamReader reader = new InputStreamReader(url.openConnection().getInputStream());
			final StringWriter writer = new StringWriter();
			final TPumpReaderToWriter pumpa = new TPumpReaderToWriter(reader, writer);
			pumpa.run(); // spustit synchronně
			return writer.toString();
		} catch (final IOException e) {
			FThrowable.printStackTrace(e, System.err, "errorEceptionDumping");
			throw new RuntimeException(e);
		}
	}

	/**
	 * Vrátí dumpera pro dané vlákno nebo pro danou aplikaci.
	 *
	 * @return
	 */
	public static ExceptionDumper getExceptionDumper() {
		return tExceptinDumper.get(); // Vrátit exception dumper pro příslušné vlákno.
	}

	public static URL getExceptionUrl(final AExcId aCode) {
		final ExceptionDumperRepositorySpi repository = getDefaultRepository();
		// očekáváme defaultní kódování
		final URL url = repository.getUrl(aCode);
		return url;
	}

	public static void main(final String[] args) throws IOException {
		System.out.println(
		        dump(new RuntimeException("prvnička", new RuntimeException("druhačka", new NullPointerException("třeťačka"))), EExceptionSeverity.DISPLAY, "Tak tady neco napíšu\na dva řádky"));

		System.out.println(dump(new Throwable[] { null, new RuntimeException("prvnička", new RuntimeException("druhačka", new NullPointerException("třeťačka")

				)), new RuntimeException("nějak v druhé řadě"), null, new RuntimeException("nějak ve třetí řadě")

		}, EExceptionSeverity.DISPLAY, new String[] { "Tak tady neco napíšu\nna dva řádky", " A toto je někaký text pro druhý výpis" }));

		final AExcId excid = dump(new RuntimeException("bbb"), EExceptionSeverity.DISPLAY, "Tak tady neco napíšu\na dva řádky");
		System.out.println("Dumpovano: " + excid);
		System.out.println(dump(new RuntimeException("ccc"), EExceptionSeverity.DISPLAY, "Tak tady neco napíšu\na dva řádky"));
		// for (int i=0; i < 120; i++) {
		// dump(new RuntimeException("Opakovana"), EExceptionSeverity.DISPLAY, "Tak tady neco napíšu\na dva řádky");
		// }

		// System.out.p rintln(getDefaultRepository().toString());

		System.out.println(dump(null, null, ""));

		System.out.println("-----------------------------");
		final String textVyjimky = getDumpedException(excid);
		System.out.println(textVyjimky);
		System.out.println();
		System.out.println(getDumpedException(excid));
		final String textVyjimkyNeni = getDumpedException(AExcId.from(excid.toString() + "AA"));
		System.out.println("Tato vyjimka neni: " + textVyjimkyNeni);
	}

	/**
	 * Přeruší řetěz výjimek. Aktuální výjimku vydumpuje a míso ní vyhodí výjimku jinou, která se nezřetězuje s původní výjimkou, ale obsahuje pouze její identifikaci. Toto je vhodné pro případ, kdy výjimka prochází serializovaně přes vzálené volání do úplně jiného prostředí, ve kterým by třídy
	 * výjimek nemusely být k dispozoci. Navíc toto řešení zajištťuje bezpečnost v tom smyslu, že se na vzdálenou stranu nedostanou nechtěná data.
	 *
	 * @param aThrowable
	 * @param aExceptionSeverity
	 * @param aCircumstance
	 * @return
	 */
	public static XChainRupture ruptureChain(final Throwable aThrowable, final EExceptionSeverity aExceptionSeverity, final String aCircumstance) {
		final AExcId excid = getExceptionDumper().dump(aThrowable, aExceptionSeverity, aCircumstance, getDefaultRepository());
		return new XChainRupture(excid, aCircumstance);
	}

	/**
	 * Vrátí dumpera pro dané vlákno nebo pro danou aplikaci.
	 *
	 * @return
	 */
	public static void setExceptionDumper(final ExceptionDumper aExceptionDumper) {
		tExceptinDumper.set(aExceptionDumper == null ? new ExceptionDumper() : aExceptionDumper);
	}

	/**
	 * Zjistí umístění složky z několikati zdrojů.
	 *
	 * @since [2009-04-30 12:58, roztocil]
	 */
	private static File getExcrepFolder() {
		File result;
		// Nejprve zkusíme Systenm.properties - dá se změnit zvnějšku, nejlepší kandidát na ad hoc změnu.
		final String path = getExcrepPathViaSystemProperty();
		if (StringUtils.isBlank(path)) {
			// Poslední, co zkusíme, je umístění v home adresáři. Bude fungovat vždycky.
			result = EXCEPTION_DIR;
		} else {
			result = new File(path);
		}
		return result;
	}

	private static String getExcrepPathViaSystemProperty() {
		final String result = System.getProperty(TC_EXCREP_DIR_PROPERTY);
		return result;
	}

	private FExceptionDumper() {
		/* No instancies. */}
}
