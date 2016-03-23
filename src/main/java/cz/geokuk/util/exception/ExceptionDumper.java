package cz.geokuk.util.exception;

import java.io.*;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.util.file.RefinedWhiteWriter;
import cz.geokuk.util.lang.ATimestamp;
import cz.geokuk.util.lang.FThrowable;
import cz.geokuk.util.lang.FThrowable.ThrowableAndSourceMethod;

/**
 * Vypisovač výjimek. Velmi lehký objekt, který naformátuje výjimku a pošle ji do repozitoře.
 */
public class ExceptionDumper {

	private static final Logger			log		= LogManager.getLogger(ExceptionDumper.class.getSimpleName());
	/** Signleton proměnná pri implicitní repozitoř */

	private List<AditionalInfoEntry>	iStackx	= new ArrayList<>();
	private int							iStackSize;

	/**
	 * Vydumpuje předanou výjimku do lokality k tomu určené. Poté vrátí identifikátor, pod kterým může být vydumpovaná podoba výjimky nalezena za účelem zobrazení.
	 *
	 * Výjimka je dumpována typicky do jednoho souboru souborového systému a je dumpována v HTML za účelem snadnější orientace (obarvení jednotlivých částí výjimky). HTML je úplné, nevyžaduje žádné CSS, obrázky a podobně, takže daný HTML soubor s výjimkou lze předat a zobrazit v prohlížeči. Výjimky
	 * se číslují. Výjimka není celá vypsána do logu. Do logu se vypíší pouze zkráceně texty výjimky, jí přidělená identifikace a pokud je to možné, tak plná cesta k informaci s výjimkou, například plné jméno souboru a to nejlépe ve formátu URL.
	 *
	 * @param aThrowable
	 *            Dumpovaná výjimka.
	 *
	 * @param aExceptionSeverity
	 *            Závažnost výjimky z pohledu uživatele. Závažnost bude uvedena ve výpisu výjimky a určí složku v níž bude výjimka umístěna.
	 *
	 * @param aCircumstance
	 *            Okolnosti, za jichž je výjimka dumpována. Libovolný i víceřádkový text. Nezáleží na oddělovačích řádků. Oddělovače budou upraveny dle hostitelského systému.
	 *
	 * @return Jednoznačnou identifikaci výjimky. Kód je volen tak, aby se mohl stát součástí jména souboru nebo součástí URL. Tento kód bude zobrazen uživateli, je tedy volen také tak, aby uživatel dokázal tento kód opsat na papír, případně nadiktovat někomu do telefonu. Číslo má tento formát:
	 * 
	 *         <pre>
	 * ssznnnn
	 *
	 *   bsl-12a345
	 * ss    Pořadové číslo spuštění aplikace.
	 * z     Závažnost výjimky, jedno z (a,b,c,x) {@link EExceptionSeverity}
	 * nnnn  Pořadové číslo vypisované výjimky v rámci spuštění aplikace.
	 *       Pokud bude vypisována tatáž výjimka znovu, dostává stejné pořadové číslo a to i tehdy, pokud je
	 *       později vypsána s jinou závažností.
	 *
	 *         </pre>
	 *
	 */
	public synchronized AExcId dump(Throwable aThrowable, EExceptionSeverity aExceptionSeverity, String aCircumstance, ExceptionDumperRepositorySpi aRepository) {
		return dump(new Throwable[] { aThrowable }, aExceptionSeverity, new String[] { aCircumstance }, aRepository);
	}

	/**
	 * Do jednoho souboru vypustí více výpisů výjimek. Celé to však očísluje podle první výjimky, jenž je vypisována.
	 * 
	 * @param aThrowablea
	 * @param aExceptionSeverity
	 * @param aCircumstancea
	 *            Pro každou jednotlivou výjimku nějaké kecy.
	 * @return
	 * @since 4.8.2006 10:43:04
	 */
	public synchronized AExcId dump(Throwable[] aThrowables, EExceptionSeverity aExceptionSeverity, String[] aCircumstances, ExceptionDumperRepositorySpi aRepository) {
		if (aThrowables == null)
			aThrowables = new Throwable[0];
		if (aCircumstances == null)
			aCircumstances = new String[0];
		// Odstranění null
		// Nejdříve zjistíme, kolik je jich v oli nenulových
		int pocetNeNull = 0;
		for (Throwable aThrowable : aThrowables) {
			if (aThrowable != null)
				pocetNeNull++;
		}
		// Teď vše zkopírujeme do menšího pole
		Throwable[] throwables = new Throwable[pocetNeNull];
		String[] circumstances = new String[pocetNeNull];
		int k = 0;
		for (int i = 0; i < aThrowables.length; i++) {
			if (aThrowables[i] == null)
				continue;
			throwables[k] = aThrowables[i];
			if (i < aCircumstances.length)
				circumstances[k] = aCircumstances[i];
			k++;
		}
		assert k == throwables.length;

		if (throwables.length == 0) {
			throwables = new Throwable[] { new NullPointerException("No exception was passed to dump method!") };
			circumstances = new String[] { "This exception was added by exception dumper, becouse no exception was passed to dump" };
		}
		try {
			if (aExceptionSeverity == null)
				aExceptionSeverity = EExceptionSeverity.DISPLAY;
			int exceptionNumber = FThrowable.getExceptionNumber(throwables[0]);
			AExcId id = AExcId.from(ExceptionDumperRepositorySpi.EXC_PREFIX + aRepository.getRunNumber() + aExceptionSeverity.getCode() + exceptionNumber);

			StringWriter swrt = new StringWriter();
			PrintWriter pwrt = new PrintWriter(new RefinedWhiteWriter(swrt));
			pwrt.print("<span id='tcTechException'>");
			pwrt.println("<h1>EXCEPTION " + id + "</h1>");
			if (throwables.length > 1) {
				pwrt.println("<p>The exception id is derived form the number of the first exception</p>");

			}

			pwrt.println("Severity: <b>" + aExceptionSeverity + "</b><br/>");
			pwrt.println("Dumped at: <b>" + ATimestamp.now().toIsoStringLocal() + "</b></p>");
			pwrt.println();
			if (iStackx.size() > 0) {
				pwrt.println("<p>There are " + iStackx.size() + " aditional infos et the end of this page</p>");

			}

			pwrt.println("<hr/>");
			pwrt.println("<h2>Brief exception info for exceptions (" + throwables.length + ")</h2>");
			for (int j = 0; j < throwables.length; j++) {
				Throwable throwable = throwables[j];
				String circumstance = circumstances[j];
				// Okolnosti, za jakých výjimka nastala
				if (circumstance != null) {
					pwrt.println("<p>");
					pwrt.println(circumstance);
					pwrt.println("</p>");
					pwrt.println();
				}
				// Zkácený výpis pouze hlášek
				pwrt.println("<pre>");
				printShortExceptionListInHtml(pwrt, throwable);
				pwrt.println("</pre>");
			}
			pwrt.println("<hr/>");
			pwrt.println("<h2>Stack trace for exceptions (" + throwables.length + ")</h2>");
			for (Throwable throwable : throwables) {
				// vlastní výjimku
				pwrt.println("<pre>");
				FThrowable.printStackTraceHtml(throwable, pwrt, "**");
				pwrt.println("</pre>");
				pwrt.println();
				pwrt.println();
				// if (DeveloperSettingBase.cfg.isPrintDumpedExceptionMessageToStdErr()) {
				log.error(Arrays.asList(aCircumstances));
				FThrowable.printStackTrace(throwable, System.err, "dump-" + id);
				log.error("ERR");
				log.error("OUT");
				printShortExceptionList(System.err, throwable);
				// }
			}

			// Systémové property
			pwrt.println("<hr/>");
			pwrt.println("<h2>System properties</h2>");
			pwrt.println();
			pwrt.println("<pre>");
			printSystemProperties(pwrt);
			pwrt.println("</pre>");
			pwrt.println();
			pwrt.println("<hr/>");

			dumpAdditionaEntries(pwrt, throwables);

			pwrt.print("</span>");
			pwrt.close();
			aRepository.write(id, swrt.toString());
			logZeVyjimkaBylaVypsana(aExceptionSeverity, id, aRepository);
			return id;
		} catch (ThreadDeath e) {
			throw e;
		} catch (Throwable e) { // to je průšvih, došlo k chybě při dumpování chyby
			// tak jednoduše vypsat na standardní chybový výstup a kočit, jako by se nic nestalo
			log.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			log.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			log.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			log.error("Exception while processing exception!");
			FThrowable.printStackTrace(e, System.err, "exceptionOnEception");
			log.error("-----------------------------");
			for (int j = 0; j < throwables.length; j++) {
				Throwable throwable = throwables[j];
				FThrowable.printStackTrace(throwable, System.err, "originalException" + j);
			}
			log.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			log.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			log.error("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			return AExcId.from("excIsInSystemErr");
		}

	}

	/**
	 * @param aExceptionSeverity
	 * @param id
	 * @since 15.9.2006 8:33:05
	 */
	private void logZeVyjimkaBylaVypsana(EExceptionSeverity aExceptionSeverity, AExcId id, ExceptionDumperRepositorySpi aRepository) {
		String logMsg = "!!! DUMPED EXCEPTION '" + id + "' into \"" + aRepository.getUrl(id) + "\" !!!";
		log.error(logMsg); // nechci, aby se dalo zabránit tomuto výpisu, tak přímo na standardní výstup
	}

	/**
	 * @param pwrt
	 * @param aThrowable
	 * @since 4.8.2006 10:29:15
	 */
	private void printShortExceptionListInHtml(PrintWriter pwrt, Throwable aThrowable) {
		ThrowableAndSourceMethod[] throwableChain = FThrowable.getThrowableChain(aThrowable);
		for (int i = 0; i < throwableChain.length; i++) {
			FThrowable.ThrowableAndSourceMethod method = throwableChain[i];
			String prefix = "EXC-" + FThrowable.getExceptionNumber(aThrowable) + ": ";
			pwrt.println("    " + prefix + "<span style='color: green'>" + (i + 1) + "/" + throwableChain.length + "</span> "
					+ (method.getSourceMethod() == null ? "" : "<span style='color: darkmagenta'>" + method.getSourceMethod().getName() + "()" + "</span>: ") + "<span style='color: blue'>"
					+ method.getThrowable().getClass().getName() + "</span> : <span style='color: red'>" + method.getThrowable().getMessage() + "</span>");
		}
	}

	private void printShortExceptionList(PrintStream pwrt, Throwable aThrowable) {
		ThrowableAndSourceMethod[] throwableChain = FThrowable.getThrowableChain(aThrowable);
		for (int i = 0; i < throwableChain.length; i++) {
			FThrowable.ThrowableAndSourceMethod method = throwableChain[i];
			String prefix = "EXC-" + FThrowable.getExceptionNumber(aThrowable) + ": ";
			pwrt.println("!!!!! " + prefix + (i + 1) + "/" + throwableChain.length + " " + method.getThrowable().getClass().getName() + ": " + method.getThrowable().getMessage());
		}
	}

	/**
	 * @param pwrt
	 */
	private void printSystemProperties(PrintWriter pwrt) {
		SortedMap<String, String> sm = new TreeMap<>();
		for (Object oklic : System.getProperties().keySet()) {
			String sklic = oklic + ""; // pomalost zde nevadí
			sm.put(sklic, System.getProperty(sklic));
		}

		String pathSeparator = sm.get("path.separator");
		for (Map.Entry<String, String> entry : sm.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			String[] strings = value.split(pathSeparator);
			boolean jeToCesta = strings.length > 1 && (key.endsWith(".path") || key.endsWith(".dirs"));
			if (jeToCesta) {
				pwrt.println(key + " = ");
				for (String string : strings) {
					pwrt.println("        " + string);
				}
			} else {
				pwrt.println(key + " = " + value);
			}
		}
	}

	private synchronized void dumpAdditionaEntries(PrintWriter pwrt, Throwable[] aThrowables) {
		// Prevence proti java.util.ConcurrentModificationException
		// Zřejmě občas docházelo k tomu, že v průběhu tady tohoto výpisu
		// někdo (nějaký zaregistrovaný AditionalInfoProvider) vrtnul do iStackx
		List<AditionalInfoEntry> copiedStackx = new ArrayList<>(iStackx);
		for (AditionalInfoEntry entry : copiedStackx) {
			entry.dump(pwrt);
		}
	}

	public synchronized void pushAditionalInfoProvider(AditionalInfoProvider aAditionalInfoProvider, String aDescription, Class<?> aPushingClass) {
		AditionalInfoEntry entry = new AditionalInfoEntry();
		entry.iProvider = aAditionalInfoProvider;
		entry.iDescription = aDescription;
		entry.iPushingClass = aPushingClass;
		entry.iNumber = iStackSize + 1; // aby se číslovalo od jedné
		if (iStackSize < iStackx.size()) {
			iStackx.subList(iStackSize, iStackx.size()).clear();
		}
		iStackx.add(entry);
		iStackSize = iStackx.size();
	}

	public synchronized void popAditionalInfoProvider() {
		if (iStackSize > 0) {
			iStackSize--;
		}
	}

	private class AditionalInfoEntry {
		private AditionalInfoProvider	iProvider;
		private String					iDescription;
		private Class<?>				iPushingClass;
		private int						iNumber;

		private void dump(PrintWriter pwrt) {
			pwrt.println("<hr><h2>Additional info (" + iNumber + "/" + iStackx.size() + ") - " + iDescription + "</h2>");
			pwrt.println("AditionalIfnfo provider class <tt>" + (iProvider == null ? "NULL" : iProvider.getClass().getName()) + "</tt> was pushed by <tt>" + iPushingClass.getName() + "</tt>");
			pwrt.println("<br/>");
			pwrt.println("<br/>");
			// pwrt.println("<pre>");
			callPrintAditionalInfo(pwrt);
			// pwrt.println("</pre>");
			pwrt.println("<br/>");

		}

		/**
		 * @param pwrt
		 */
		private void callPrintAditionalInfo(PrintWriter pwrt) {
			try {
				if (iProvider == null) {
					pwrt.println("Aditional infor provider is NULL");
				} else {
					iProvider.printAditionalInfo(pwrt);
				}
			} catch (ThreadDeath td) {
				throw td;
			} catch (Throwable t) {
				pwrt.println("Exception while addint aditional context by addAditionalContext of " + getClass());
				pwrt.println();
				FThrowable.printStackTrace(t, pwrt, "printAditionalInfo");
				pwrt.println();
			}
		}
	}

}
