package cz.geokuk.util.exception;

import java.io.IOException;
import java.net.URL;

/**
 * @author veverka
 */
public interface ExceptionDumperRepositorySpi {

	public static final String EXC_PREFIX = "exc";

	/**
	 * Zjištění, zda repozitoř umožňuje číst do ní zapsané výjimky.
	 * @return Vrátí true, pokud repozitoř umožňuje čtení do nich zapsaných vyjímek.
	 * V tom případě však musí metoda getUrl vrátit URL, ze kterého lze výjimku opravdu přečíst.
	 * @since 15.9.2006 7:30:29
	 */
	public boolean isReadable();

	/**
	 * Zapíše text výjimky někam na základě zadaného kódu.
	 * @param aCode
	 * @param aExceptionData
	 * @throws IOException
	 */
	public abstract void write(AExcId aCode, String aExceptionData) throws IOException;

	/**
	 * Vrátí rozlišující číslo běhu aplikace. Výsledek z metody se zúčastní tvorby jednoznačné identifikace výjimky.
	 * V rámci jednoho ClassLoaderu mají všechny výjimky jednoznačně přiřazeno číslo. Aby se však zajistila
	 * jednoznačnost čísla výjimky v rámci celé repozitoře, je k dispozici tato metoda.
	 *
	 * @return Nějaké jednoznačné číslo, které je stejné pro danou JVM (classloader), ale musí se vzájemně
	 * a to i historicky lišit pro různé JVM.
	 */
	public abstract int getRunNumber();

	/**
	 * Vrátí URL, pomocí něhož je možné přistoupit k výpisu výjimky.
	 * URL musí být funkční minimálně v té JVM, v níž je tato funkce vyvolána.
	 * Pokud ve vyjímečném případě je repozitoř postavena tak, že výpis výjimky nelze získat,
	 * vrací se null. To je však dovoleno je  v případě triviálních repozitoří, jako standardní výstup.
	 * @return Pokud {@link #isReadable()} vrací false, tak tato metoda vrací null. Jinak vrací URL.
	 * Null vrací i v případě, kdy pro zadaný kód není výjimka nalezena, to znamená, kdy kód je špatně.
	 */
	public URL getUrl(AExcId aCode);
}