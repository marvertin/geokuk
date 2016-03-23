package cz.geokuk.util.lang;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reprezentace konkrétního měsíce, například května 2001.
 *
 * @author <a href="mailto:martin.veverka@turboconsult.cz">????? ?????</a>
 * @version $Revision: 3 $
 * @see "$Header: /Zakazky/TWare/Distribuce/TW0139/Util/cz/tconsult/tw/data/AMonth.java 3     30.03.01 11:52 Veverka $"
 */
public final class AMonth extends AObject0 implements IElementInt, Comparable<AMonth>, Ordinable<AMonth> {
	private static final long	serialVersionUID	= 6216726658054355928L;

	private static final int	MINY				= 12;						// Months IN Year
	private static final int	MINVALUE			= 1000 * MINY;
	private static final int	MAXVALUE			= 9999 * MINY + MINY - 1;

	private static Pattern		sPattern;

	/**
	 * Hodnota
	 */
	private final int			iRokMes;

	/**
	 * Vrací se jako long.
	 *
	 * @return Hodnota udávající počet měsíců, které uplynuly od začátku našeho letopočtu. Přitom Leden hypotetického roku 0 má hodnotu 0, čili leden roku 1 má hodnotu 12.
	 */
	public int asInt() {
		return iRokMes;
	}

	/**
	 * Vrací se jako string.
	 *
	 * @return Měsíc jako řetězec vždy na 7 znaků ve tvaru RRRR-MM.
	 */
	public String asString() {
		return MessageFormat.format("{0,number,0000}-{1,number,00}", iRokMes / MINY, iRokMes % MINY + 1);
	}

	/**
	 * Převede se do stringu v nějakém čitelném formátu.
	 *
	 * @return Rok a měsíc v čitelném formátu.
	 */
	public String toString() {
		return asString();
	}

	/**
	 * Vrací měsíc
	 *
	 * @return Číslo měsíce z intervalu 1..12
	 */
	public int getMonthNumber() {
		return iRokMes % MINY + 1;
	}

	/**
	 * Vrací rok.
	 *
	 * @return Číslo roku.
	 */
	public int getYearNumber() {
		return iRokMes / MINY;
	}

	/**
	 * Přičte požadovaný počet měsíců.
	 *
	 * @param aKolik
	 *            Kolik měsíců se má přičíst. Záporné číslo odčítá.
	 * @return Objekt měsíce zvýšený o patřičný počet měsíců.
	 */
	public AMonth add(final int aKolik) {
		return new AMonth(iRokMes + aKolik);
	}

	/**
	 * Přičte požadovaný počet roků.
	 *
	 * @param aKolik
	 *            Kolik roků se má přičíst. Záporné číslo odčítá.
	 * @return Objekt měsíce snížený o příslušný počet roků.
	 */
	public AMonth addYears(final int aKolik) {
		return new AMonth(iRokMes + aKolik * MINY);
	}

	/**
	 * Vrátí následující měsíc.
	 *
	 * @return Následující měsíc po aktuálním měsíci.
	 */
	public AMonth next() {
		return add(1);
	}

	/**
	 * Vrátí předcházející měsíc.
	 *
	 * @return Předcházející měsíc před aktuíálním měsícem.
	 */
	public AMonth prev() {
		return add(-1);
	}

	/**
	 * Vrátí datum v příslušném roce a měsíci v zadaném dni.
	 *
	 * @param aDay
	 *            Číslo dne v intervalu 1..31, pro některé měsíce musí být menší.
	 * @return Datum odpovídající číslu měsíce.
	 */
	public ADate dateOf(final int aDay) {
		return ADate.from(getYearNumber(), getMonthNumber(), aDay);
	}

	/**
	 * @param aDay
	 *            je dceprekovaný, nemá význbam.
	 * @return tOTÉŽ CO FUNKCE BEZ PARAMETRŮ.
	 * @deprecated Použij metodu bez parametru, stejně parametr nemá na nic vliv, je to chyba.
	 */
	public ADate firstDate(final int aDay) {
		return dateOf(1);
	}

	/**
	 * Vrátí první den v měsíci
	 *
	 * @return první den v měsíci.
	 */
	public ADate firstDate() {
		return dateOf(1);
	}

	/**
	 * Vrátí poslední den v měsíci.
	 *
	 * @return Poslední den v měsíci.
	 */
	public ADate lastDate() {
		return dateOf(daysIn());
	}

	/**
	 * Vrátí počet dnů v měsíci.
	 *
	 * @return Počet dnů v měsíci, číslo z intervalu 28..31
	 */
	public int daysIn() {
		final Calendar cal = new GregorianCalendar(new SimpleTimeZone(0, "UTC"));
		cal.set(getYearNumber() - 1, getMonthNumber(), 1); // proč je tam to - není vůbec jasné
		cal.add(Calendar.MONTH, 12);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		return cal.get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Rozdíl zadaných měsíců. Příklad:
	 *
	 * <pre>
	 *   5, AMonth.from(2003, 8).diff(AMonth.from(2003,3)) == 5
	 *  11, AMonth.from(2003,12).diff(AMonth.from(2003,1)) == 11
	 *  78, AMonth.from(2008, 8).diff(AMonth.from(2002,2)) == 78
	 * -23, AMonth.from(1992, 2).diff(AMonth.from(1994,1)) == -23
	 *   0, AMonth.from(2003, 8).diff(AMonth.from(2003,8)) == 0
	 * </pre>
	 *
	 * @param aMonth
	 *            Měsíc, který má být odečten.
	 * @return Počet měsíců mezi zadanými měsíci, může být i záportný.
	 */
	public int diff(final AMonth aMonth) {
		return iRokMes - aMonth.iRokMes;
	}

	public boolean isLess(final AMonth b) {
		return compareTo(b) < 0;
	}

	public boolean isLessOrEqual(final AMonth b) {
		return compareTo(b) <= 0;
	}

	public boolean isGreater(final AMonth b) {
		return compareTo(b) > 0;
	}

	public boolean isGreaterOrEqual(final AMonth b) {
		return compareTo(b) >= 0;
	}

	public boolean isEqual(final AMonth b) {
		return compareTo(b) == 0;
	}

	public boolean isNotEqual(final AMonth b) {
		return compareTo(b) != 0;
	}

	/**
	 * Porovná dva objekty typu AMonth. Vyhodí výjimku, pokud se porovnává s null nebo s jiným typem než AMonth.
	 *
	 * @param aObj
	 *            Objekt se, kterým porovnávat.
	 * @return -1, pokud je menší než argument, 0, při rovnosti, 1, pokud je větší než argument
	 */
	public int compareTo(final AMonth aObj) {
		return iRokMes == aObj.iRokMes ? 0 : iRokMes < aObj.iRokMes ? -1 : 1;
	}

	/**
	 * Porovnání dvou měsíců.
	 *
	 * @param aObject
	 *            Objekt, se kterým se porovnává.
	 * @return true, pokud se rovnají. False pokud se nerovnají, argument je null nebo jiný typ než AMonth.
	 */
	public boolean equals(final Object aObject) {
		if (!(aObject instanceof AMonth))
			return false;
		final AMonth obj = (AMonth) aObject;
		return iRokMes == obj.iRokMes;
	}

	/**
	 * Heš kód
	 *
	 * @return heškód odvozený z roku a měsíce.
	 */
	public int hashCode() {
		return Integer.valueOf(iRokMes).hashCode();
	}

	/**
	 * Zkosntruuje objekt ze ztadaného roku a měsíce.
	 *
	 * @param aRokMes
	 *            Pořadové číslo měsíce. Blíže viz {@link #asInt())
	 * @return Zkosntruovaný objekt AMonth
	 */
	public static AMonth from(final int aRokMes) {
		return new AMonth(aRokMes);
	}

	/**
	 * Vytvoří hodnotu ze zadaného řetězce
	 *
	 * @param aRokMes
	 *            Rok a měsíc ve tvaru RRRR-MM.
	 * @return odpovídající hodnota
	 */
	public static AMonth from(final String aRokMes) {
		return aRokMes == null ? null : new AMonth(parseMonth(aRokMes));
	}

	/**
	 * Vytvoří AMonth ze zadaných hodnot
	 *
	 * @param aRok
	 *            Čtyřmístný rok
	 * @param aMes
	 *            Měsíc 1..12
	 * @return Zkosntruovaný objekt AMonth
	 */
	public static AMonth from(final int aRok, final int aMes) {
		return new AMonth(fromItems(aRok, aMes));
	}

	/**
	 * Otestuje, zda je možné vytvořit instanci z roku a měsíce.
	 *
	 * @param aRokMes
	 * @return true, pokud odpovídající volání from by vyhodilo výjimku, jinak false.
	 */
	public static boolean canFrom(final int aRokMes) {
		try {
			from(aRokMes);
			return true;
		} catch (final XCreateElement e) {
			return false;
		}
	}

	/**
	 * Otestuje, zda je možné vytvořit instanci ze řetězece.
	 *
	 * @param aRokMes
	 *            V řetězcovém tvaru rok a měsíc.
	 * @return true, pokud odpovídající volání from by vyhodilo výjimku, jinak false.
	 */
	public static boolean canFrom(final String aRokMes) {
		try {
			from(aRokMes);
			return true;
		} catch (final XCreateElement e) {
			return false;
		}
	}

	/**
	 * Otestuje, zda je možné vytvořit instanci ze zadaného roku a měsíce.
	 *
	 * @param aRok
	 *            Hodntoa roku..
	 * @param aMes
	 *            Měsíc v intervalu 1..12
	 * @return true, pokud odpovídající volání from by vyhodilo výjimku, jinak false.
	 */
	public static boolean canFrom(final int aRok, final int aMes) {
		try {
			from(aRok, aMes);
			return true;
		} catch (final XCreateElement e) {
			return false;
		}
	}

	/**
	 * Totéž co {@link #add}
	 *
	 * @param aNthObject
	 *            Počet měsíců o kolik posunout.
	 * @return Instance AMonth s posunutým měsícem.
	 */
	public AMonth getAnother(final long aNthObject) {
		if (Math.abs(aNthObject) > Integer.MAX_VALUE - 2)
			throw new IllegalArgumentException("Počtu měsíců o něž posouvat je " + aNthObject + " a to je na mě v absolutní hodnotě moc!");
		return add((int) aNthObject);
	}

	/**
	 * Totéž co diff.
	 *
	 * @param aObject
	 *            Objekt, který se odečítá.
	 * @return Počet měsíců rozdílu.
	 */
	public long getDistance(final AMonth aObject) {
		if (aObject == null) {
			throw new IllegalArgumentException("aObject is null!");
		}
		return diff(aObject);
	}

	////////////////////////////////////////////
	private static int parseMonth(final String aString) {
		if (sPattern == null)
			sPattern = Pattern.compile("\\s*(\\d{4})[ -./]?(\\d\\d?)\\s*");
		final Matcher mat = sPattern.matcher(aString);
		if (mat.matches()) {
			final int rok = Integer.parseInt(mat.group(1));
			final int mes = Integer.parseInt(mat.group(2));
			return fromItems(rok, mes);
		} else {
			throw new XCreateElement("Syntakticka chyba v roku a mesici '" + aString + "'");
		}

	}

	private static int fromItems(final int aRok, final int aMes) {
		if (aRok < 1000 || aRok > 9999)
			throw new XCreateElement("Rok " + aRok + " neni ctyrmistny");
		if (aMes < 1 || aMes > 12)
			throw new XCreateElement("Mesic " + aMes + " neni v intervalu 1..12");
		return aRok * MINY + aMes - 1; // aby uvnitř měsíce byly od nuly
	}

	// Kontrola rozsahu.
	private AMonth(final int aRokMes) {
		iRokMes = aRokMes;
		checkRange();
	}

	/**
	 * Kontrola rozsahu. Metoda se volá z konstruktoru a kontroluje rozsah.
	 */
	protected final void checkRange() {
		if (!(MINVALUE <= iRokMes && iRokMes <= MAXVALUE))
			throw new XCreateElement("Hodnota " + iRokMes + " není v požadovaném intervalu <" + MINVALUE + "," + MAXVALUE + ">");
	}

	/**
	 * Returns minimal acceptable value of <code>AMonth</code>. You can't create an instance of <code>AMonth</code> with any less value.
	 *
	 * @author kalendov
	 */
	public static AMonth getMinValue() {
		return AMonth.from(MINVALUE);
	}

	/**
	 * Returns maximal acceptable value of <code>AMonth</code>. You can't create an instance of <code>AMonth</code> with any greater value.
	 *
	 * @author kalendov
	 */
	public static AMonth getMaxValue() {
		return AMonth.from(MAXVALUE);
	}

}
