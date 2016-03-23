package cz.geokuk.util.lang;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

/** Třída pro prácis datumy a časem. Od svého předka Date se liší zejména:
 * množstvím funkcí které je schopna provádět a větším počtem konstruktorů
 *
 * Je nabvžena tak, aby byla použitelná při čtení/zápisu z/do databází SQL,
 * zejména pro MS SQL server. Z toho důvodu umí zpracovávat i datum a čas získaný
 * v doublu, tak jak to specifikuje ADO.
 * Do databáze se ukládá datum a čas zásadně v UTC i s ohledem na letní čas.
 * Je to standardní vlastnost Javovských tříd Date, Calendar, DateFormat a neshledal
 * jsem za vhodné toto chování měnit.
 *
 * Prezentace času probíhá stále v aktuálním čase časového pásma dle nastavení Windows.
 * Je využíván defaultní locale.
 *
 * V praxi to znamená, že v databázi je o hodinu nebo dvě méně (léto/zima), než kolik jsme zadali
 * a kolik se nám zobrazí při převodu na řetězec. Ničemu by to nemělo vadit. Problém může
 * pouze nastat, pokud bychom k databázi přistupovali jinými prostředky (mimo javovský Date)
 * například vytažením datumu přímo do Stringu. Před tímto postupem však důrazně varuji.
 * Také není vhodné v uložených procedurách přímo uvádět konstanty nebo datové a časové kosntanty
 * strkat do databáze mimo tento aparát.
 *
 */

/**
 * Reprezentuje datum jako takové, to znamená den měsíc rok bez vazby na konkrétní okamžik. Datum je reprezentováno interně jako počet dnů od 1.1.1900.
 */
public final class ADate extends AObject0 implements Serializable, Comparable<ADate>, IElement, Ordinable<ADate>, ADateComparable {

	private static final long	serialVersionUID	= -5930101122857422133L;

	// LATER [veverka?] vnitřní držbu udělej pomocí den,měsíc,ruk a metody stav nad kalendářem
	/**
	 * Drží datum. Je reprezentováno půlnocí v GMT čase.
	 */
	// final jsem musel odstranit kvůli _clone()
	private java.util.Date		iJavaDatum;

	/**
	 * Vytvoří datum z SQL datumu
	 */
	private ADate(java.sql.Date aSqlDatum) {
		iJavaDatum = new java.util.Date(aSqlDatum.getTime());
	}

	/**
	 * Konstruuje z již existujícího java.util.Date. Pouze pro interní potřebu.
	 */
	private ADate(java.util.Date aJavaDatum) {
		iJavaDatum = aJavaDatum;
	}

	/**
	 * Konstruuje datum z roku, měsíce a dne.
	 *
	 * @param int
	 *            rok, int mesic, int den
	 */
	private ADate(int rok, int mesic, int den) {
		iJavaDatum = fromRokMesicDen(rok, mesic, den);
	}

	/**
	 * Koonstruuje datum z řetězce. Možné formáty řetězců: yyyy-mm-dd (yyyy-m-d) mm-dd-yyyy (m-d-yyyy) yyyy/mm/dd (yyyy/m/d) mm/dd/yyyy (m/d/yyyy) dd.mm.yyyy (d.m.yyyy)
	 *
	 * @param String
	 *            Datum
	 */
	private ADate(String Datum) {
		iJavaDatum = fromString(Datum);
	}

	/**
	 * Pořadí dne v měsíci jako int(rozsah 1 až 31) 1...první den v měsící 31..zpravidla poslední den v měsíci (zálěží na měsíci)
	 *
	 * @return Den v měsíci
	 */
	public int getDayNumber() {
		return actcal().get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * Pořadí měsíce v roce jako int(1 až 12) 1...první měsíc (leden) 12..poslední měsíc (prosinec)
	 *
	 * @return Měsíc v roce
	 */
	public int getMonthNumber() {
		// Pozor, Calendar ma mesice od nuly !!
		return actcal().get(Calendar.MONTH) + 1;
	}

	/**
	 * Pořadí roku v tisíciletí.
	 *
	 * @return Rok
	 */
	public int getYearNumber() {
		return actcal().get(Calendar.YEAR);
	}

	/**
	 * Vrací řetězec v tomto formátu:
	 *
	 * @return yyyy-mm-dd
	 */
	public String toString() {
		return asString();
	}

	/**
	 * @deprecated Tato metoda je jen pro ladící účely, normálně nepoužívat!
	 */
	public long asLong() {
		return iJavaDatum.getTime();
	}

	/**
	 * Vrací datum jako SQL datum
	 *
	 * @deprecated Pouzij FPersistence.encodeDateToPersistence(ADate date). FPersistence vraci Date tak jak je potřeba pro zperzistnentěnéí. Nebo použij {@link #getSqlDateInDefaultTimezone()} nebo {@link #getSqlDateInUtc()}, podle toho co doopravdy potřebuješ.
	 *             <p/>
	 *             Uvědomme si, že java.sqlDate má uvnitř čas s přessností na milisekndy a ten jak víme je: the difference, measured in milliseconds, between the current time and midnight, January 1, 1970 UTC.
	 *             <p/>
	 *             Skutečnost, že je typ java.sql.Data v dané časové zńě znamená, že obsahuje čas odpovídající přesně půlnoci, ktrou začíná zobrazované den v patřičné časové zóně.
	 *             <p/>
	 *             Skutečnost, že vrací Date v nějakém locale znemená, že vrací čst
	 */
	public java.sql.Date asSqlDate() {
		return new java.sql.Date(iJavaDatum.getTime());
	}

	/**
	 * Konstruuje datum z roku, mesice a dne.
	 *
	 * @param int
	 *            rok, int mesic, int den
	 * @return java.util.Date Datum
	 */
	private static java.util.Date fromRokMesicDen(int rok, int mesic, int den) {
		try {
			Calendar c = newcal();
			// Pozor, Calendar ma mesice od nuly !!
			c.set(rok, mesic - 1, den, 0, 0, 0);

			if (c.get(Calendar.YEAR) != rok || c.get(Calendar.MONTH) != mesic - 1 || c.get(Calendar.DAY_OF_MONTH) != den)
				throw new IllegalArgumentException(den + "." + mesic + "." + rok);

			c.set(Calendar.MILLISECOND, 0);
			return c.getTime();
		} catch (Exception e) {
			throw new XCreateElement("Z cisel'" + rok + " " + mesic + " " + den + "' nelze udelat datum", e);
		}
	}

	/**
	 * Datum z řetězce. Možné formáty řetězců: yyyy-mm-dd (yyyy-m-d) mm-dd-yyyy (m-d-yyyy) yyyy/mm/dd (yyyy/m/d) mm/dd/yyyy (m/d/yyyy) dd.mm.yyyy (d.m.yyyy)
	 *
	 * @param String
	 *            Datum
	 * @return java.util.Date Datum
	 */
	private static java.util.Date fromString(String Datum) {
		try {
			String DatePart;

			DatePart = Datum;
			String odd;
			odd = "";
			if (DatePart.indexOf('.') > 0)
				odd = ".";
			if (DatePart.indexOf(',') > 0)
				odd = ",";
			if (DatePart.indexOf('-') > 0)
				odd = "-";
			if (DatePart.indexOf('/') > 0)
				odd = "/";
			DatePart = DatePart + odd;
			int poradi = 0;
			String Parts[] = new String[3];
			for (int i = 0; i < 3; i++) {
				Parts[i] = DatePart.substring(0, DatePart.indexOf(odd));
				DatePart = DatePart.substring(DatePart.indexOf(odd) + 1);
				if (Parts[i].length() > 2)
					poradi = i;
			}

			int iDateParts[] = new int[3];
			if (poradi == 0) {
				iDateParts[0] = Integer.valueOf(Parts[0]);
				iDateParts[1] = Integer.valueOf(Parts[1]);
				iDateParts[2] = Integer.valueOf(Parts[2]);
			} else {
				iDateParts[0] = Integer.valueOf(Parts[2]);
				iDateParts[1] = Integer.valueOf(Parts[1]);
				iDateParts[2] = Integer.valueOf(Parts[0]);
			}

			return fromRokMesicDen(iDateParts[0], iDateParts[1], iDateParts[2]);
		} catch (Exception e) {
			throw new XCreateElement("Z reztezce '" + Datum + "' nelze udelat datum", e);
		}
	}

	/**
	 * Vrací formátovaný řetězec z datumu
	 * 
	 * @return d.m.yyyy private String toDmyString() {
	 * 
	 *         return MessageFormat.format("{0,number,0}.{1,number,0}.{2,number,0000}", new Object[] {new Integer(getDayNumber()), new Integer(getMonthNumber()), new Integer(getYearNumber())}); }
	 */

	/**
	 * Vrací formátovaný řetězec z datumu
	 *
	 * @return yyyy-mm-dd
	 */
	public String asString() {
		return MessageFormat.format("{0,number,0000}-{1,number,00}-{2,number,00}", getYearNumber(), getMonthNumber(), getDayNumber());
	}

	// ================================================
	// práce s localy

	/**
	 * Dodá gregoriánský kalendář pro nulový offset (0, UTC).
	 *
	 * @return Calendar Kalendar
	 */
	private static Calendar newcal() {
		// kalendář je natvrdo s nulovým ofsetem, protože čas nás nezajímá
		return new GregorianCalendar(new SimpleTimeZone(0, "UTC"));
	}

	private Calendar actcal() {
		Calendar c = newcal();
		c.setTime(iJavaDatum);
		return c;
	}

	/**
	 * Je po zadaném datumu, ne však toto datum. Pokud je zadané datum null, vždy je po něm.
	 *
	 * @return boolean Zda je po
	 * @deprecated Použij isGreater
	 */
	public boolean after(ADate adatum) {
		return isGreater(adatum);
	}

	/**
	 * Je po zadaném datumu nebo rovno tomuto datumu. Pokud je zadané datum null, vždy je po něm.
	 *
	 * @return boolean Zda je po nebo dnes
	 * @deprecated Použij isGreaterOrEqual
	 */
	public boolean afterOrThis(ADate adatum) {
		return isGreaterOrEqual(adatum);
	}

	/**
	 * Je před zadaným datumem, ne však toto datum. Pokud je zadané datum null, vždy je před ním.
	 *
	 * @return boolean Zda je před
	 * @deprecated Použij isLess
	 */
	public boolean before(ADate adatum) {
		return isLess(adatum);
	}

	/**
	 * Je před zadaným datumem nebo rovno tomuto datumu. Pokud je zadané datum null, vždy je před ním.
	 *
	 * @return boolean Zda je před nebo dnes
	 * @deprecated Použij isLessOrEqual
	 */
	public boolean beforeOrThis(ADate adatum) {
		return isLessOrEqual(adatum);
	}

	// ===========================Datumová aritmetika===================

	/**
	 * Pořadí dne v týdnu jako int (rozsah 1 až 7) 1...neděle 7...sobota
	 *
	 * @return den v týdnu
	 * @deprecated JP měla by se použít funkce s naším výčtovým typem, see getWeekDay
	 */
	public int getDayInWeekNumber() {
		return actcal().get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * Den v týdnu
	 *
	 * @return den v týdnu
	 */
	public EDayOfWeek getWeekDay() {

		return EDayOfWeek.fromCalendarValue(actcal().get(Calendar.DAY_OF_WEEK));
	}

	/**
	 * Přičte k datumu určený počet dní (int i)a vrátí nové datum
	 *
	 * @param Počet
	 *            přičítaných dní
	 * @return ADate Datum
	 */
	public ADate addDays(int i) {
		Calendar c = actcal();
		c.add(Calendar.DATE, i);
		return new ADate(c.getTime());
	}

	/**
	 * Přičte k datumu určený počet týdnů (int i) a vrátí nové datum
	 *
	 * @param Počet
	 *            přičítaných týdnů
	 * @return ADate Datum
	 */
	public ADate addWeeks(int i) {
		Calendar c = actcal();
		c.add(Calendar.DATE, i * 7);
		return new ADate(c.getTime());
	}

	/**
	 * Rozdíl v počtu dnů mezi dvěma datumy
	 *
	 * @param ADate
	 *            Druhé datum
	 * @return Počet dnů mezi datumy
	 */
	public int daysBetw(ADate Datum) {
		return (int) ((iJavaDatum.getTime() - Datum.iJavaDatum.getTime()) / ATimestamp.getMillisecondsPerDay());
	}

	/**
	 * Datum posledního dne v měsíci
	 *
	 * @return ADate Datum
	 * @deprecated Použij metodu getAMonth, nextMonth, prevMonth a třídu AMonth
	 */
	public ADate getLastDayInMonth() {
		return getMonth().lastDate();
	}

	/**
	 * Datum prvního dne v měsíci
	 *
	 * @return ADate Datum
	 * @deprecated Použij metodu getAMonth, nextMonth, prevMonth a třídu AMonth
	 */
	public ADate getFirstDayInMonth() {
		return getMonth().firstDate();
	}

	/**
	 * Datum prvního dne v roce
	 *
	 * @return ADate Datum
	 */
	public ADate getFirstDayInYear() {
		return new ADate(getYearNumber(), 1, 1);
	}

	/**
	 * Datum posledního dne v roce
	 *
	 * @return ADate Datum
	 */
	public ADate getLastDayInYear() {
		return new ADate(getYearNumber(), 12, 31);
	}

	/**
	 * Následující den (podle názvu dne).
	 *
	 * @return ADate Datum vrátí první následující datum pro WeekDay (tedy ne dnešek.
	 */
	public ADate getNextWeekDay(EDayOfWeek DayOfWeek) {
		ADate Result = addDays(1);
		while (!DayOfWeek.equals(Result.getWeekDay())) {
			Result = Result.addDays(1);
		}
		return Result;
	}

	/**
	 * Datum posledního dne v týdnu
	 *
	 * @return ADate Datum
	 * @deprecated (JP) vrací sobotu, měla by být naimplementována až v národních potomcích, např v @see ADateRelativeCz.
	 */
	public ADate getLastDayInWeek() {
		return addWeeks(1).addDays(-actcal().get(Calendar.DAY_OF_WEEK));
	}

	/**
	 * Vrací datum odpovídající zadanému časovému pásmu.
	 *
	 * @return Datum, které je v zadaný čas v odpovídajícím časovém, =ásmu.
	 */
	public ATimestamp getNoonTimestamp(TimeZone aZone) {
		Calendar cal = new GregorianCalendar(aZone);
		cal.set(this.getYearNumber(), this.getMonthNumber() - 1, this.getDayNumber(), 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return ATimestamp.from(cal.getTime().getTime() + 12 * 60 * 60 * 1000);
	}

	/**
	 * Vrátí půlnoc, kterou začíná právě tento den
	 *
	 * @param aZone
	 * @return
	 * @author veverka
	 * @since 16.9.2006 17:20:06
	 */
	public ATimestamp getMidnightStarting(TimeZone aZone) {
		return getNoonTimestamp(aZone).add(-12 * 60 * 60 * 1000);
	}

	/**
	 * Vrátí půlnoc, kterou končí právě tento den
	 *
	 * @param aZone
	 * @return
	 * @author veverka
	 * @since 16.9.2006 17:20:19
	 */
	public ATimestamp getMidnightEnding(TimeZone aZone) {
		return getNoonTimestamp(aZone).add(+12 * 60 * 60 * 1000);
	}

	/**
	 * Vrátí java.sql.Date obsahující časový okamžik, který je o půlnoci, kdy začíná den v defaultní časové zuóně..
	 *
	 * @return
	 * @author veverka
	 * @since 16.9.2006 17:22:22
	 */
	public java.sql.Date getSqlDateInDefaultTimezone() {
		return new java.sql.Date(getMidnightStarting(TimeZone.getDefault()).asLong());
	}

	/**
	 * Vrátí java.sql.Date obsahující časový okamžik, který je o půlnoci, kdy začíná den na nultém poledníku.
	 *
	 * @return
	 * @author veverka
	 * @since 16.9.2006 17:22:22
	 */
	public java.sql.Date getSqlDateInUtc() {
		return new java.sql.Date(iJavaDatum.getTime());
	}

	public AMonth getMonth() {
		return AMonth.from(getYearNumber(), getMonthNumber());
	}

	public AMonth nextMonth() {
		return getMonth().next();
	}

	public AMonth prevMonth() {
		return getMonth().prev();
	}

	/**
	 * Zjistí, zda je rovno datumu.
	 *
	 * @param Object
	 *            aDatum
	 * @return boolean Výsledek. Pokud byl argument null, je vráceno false.
	 */
	public boolean equals(Object aDatum) {
		if (!(aDatum instanceof ADate))
			return false;
		ADate obj = (ADate) aDatum;
		return iJavaDatum.equals(obj.iJavaDatum);
	}

	public boolean isLess(ADate b) {
		return compareTo(b) < 0;
	}

	public boolean isLessOrEqual(ADate b) {
		return compareTo(b) <= 0;
	}

	public boolean isGreater(ADate b) {
		return compareTo(b) > 0;
	}

	public boolean isGreaterOrEqual(ADate b) {
		return compareTo(b) >= 0;
	}

	public boolean isEqual(ADate b) {
		return compareTo(b) == 0;
	}

	public boolean isNotEqual(ADate b) {
		return compareTo(b) != 0;
	}

	public int compareTo(ADate aObj) {
		if (iJavaDatum.equals(aObj.iJavaDatum))
			return 0;
		if (aObj.iJavaDatum.after(iJavaDatum))
			return -1;
		if (iJavaDatum.after(aObj.iJavaDatum))
			return 1;
		throw new RuntimeException("Interni chyba porovnavani ADate");
	}

	/**
	 * Heš kód pro datum.
	 *
	 * @return Heškód agregovaného java.util.date.
	 */
	public int hashCode() {
		return iJavaDatum.hashCode();
	}

	public static ADate from(String aDatum) {
		return StringUtils.isBlank(aDatum) ? null : new ADate(aDatum);
	}

	/**
	 * Vrátí datum, které je v defaultní časové zóně, v zadaném čase. Zdá se, že tuto metodu lze použít pto získání správného datumu z datumu získaného z informixí proměnné typu DATE. Takto to srpávně funguje v JDK 1.4 a JAVA 5. Ve verzích JDK 1.3, 1.2 a 1.1 to fungovalo pokaždé jinak, stále do toho
	 * Sunové vrtali. Pravděpodobně musí být splněna podmínka, že klientská Java aplikaci běží ve stejné nóně jako Informixí server, jist si ale nejsem.
	 *
	 * @param date
	 * @return
	 * @author veverka
	 */
	public static ADate fromSqlDateInDefaultTimezone(java.sql.Date date) {
		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance(); // kalendar se standardni casovou zonou

		cal.setTime(date); // nastavim kalendar na cas z prijateho data
		int rok = cal.get(Calendar.YEAR);
		int mesic = cal.get(Calendar.MONTH) + 1; // JANUARY = 0
		int den = cal.get(Calendar.DAY_OF_MONTH);

		// return ADateConvertor.toADate(date);
		return new ADate(rok, mesic, den);
	}

	/**
	 * Vrátí datum, které je na nultém poledníku, v zadaném čase. Zdá se, že tuto metodu lze použít pto získání správného datumu z datumu získaného z informixí proměnné typu DATE. Takto to srpávně funguje v JDK 1.4 a JAVA 5. Ve verzích JDK 1.3, 1.2 a 1.1 to fungovalo pokaždé jinak, stále do toho
	 * Sunové vrtali. Pravděpodobně musí být splněna podmínka, že klientská Java aplikaci běží ve stejné nóně jako Informixí server, jist si ale nejsem.
	 *
	 * @param date
	 * @return
	 * @author veverka
	 */
	public static ADate fromSqlDateInUtc(java.sql.Date date) {
		if (date == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance(new SimpleTimeZone(0, "UTC")); // kalendar se standardni casovou zonou

		cal.setTime(date); // nastavim kalendar na cas z prijateho data
		int rok = cal.get(Calendar.YEAR);
		int mesic = cal.get(Calendar.MONTH) + 1; // JANUARY = 0
		int den = cal.get(Calendar.DAY_OF_MONTH);

		// return ADateConvertor.toADate(date);
		return new ADate(rok, mesic, den);
	}

	public static ADate from(int aRok, int aMesic, int aDen) {
		return (aRok == 0 && aMesic == 0 && aDen == 0) ? null : new ADate(aRok, aMesic, aDen);
	}

	public static boolean canFrom(String aDatum) {
		try {
			from(aDatum);
			return true;
		} catch (XCreateElement e) {
			return false;
		}
	}

	/**
	 * @deprecated Metoda nedělá to, co chceš. Může mosunout datum o jedničku. Doporučuji použít kalendář, vytáhnout SQL date jednotlivé složky a zkonstruovat datum. Pozor na časovou zuónu.
	 */

	public static boolean canFrom(java.sql.Date aSqlDatum) {
		return true;
	}

	public static boolean canFrom(int aRok, int aMesic, int aDen) {
		try {
			from(aRok, aMesic, aDen);
			return true;
		} catch (XCreateElement e) {
			return false;
		}
	}

	/**
	 * Totéžž co {@link #add}
	 *
	 * @param aNthObject
	 *            Počet dnů o kolik posunout.
	 * @return Instance ADate s posunutým měsícem.
	 */
	public ADate getAnother(long aNthObject) {
		if (Math.abs(aNthObject) > Integer.MAX_VALUE - 2)
			throw new IllegalArgumentException("Počtu dnů o něž posouvat je " + aNthObject + " a to je na mě v absolutní hodnotě moc!");
		return addDays((int) aNthObject);
	}

	/**
	 * Totéž co diff.
	 *
	 * @param aObject
	 *            Objekt, který se odečítá.
	 * @return Počet dnů rozdílu.
	 */
	public long getDistance(ADate aObject) {
		if (aObject == null) {
			throw new IllegalArgumentException("aObject is null!");
		}
		return daysBetw(aObject);
	}

	/**
	 * @return první den předešlého čtvrtletí.
	 */
	public ADate getFirstDayOfPreviousQuarter() {
		int mesic = getMonthNumber();
		int rok = getYearNumber();

		if (mesic >= 1 && mesic <= 3) {
			mesic = 10;
			rok--;
		} else if (mesic >= 4 && mesic <= 6) {
			mesic = 1;
		} else if (mesic >= 7 && mesic <= 9) {
			mesic = 4;
		} else if (mesic >= 10 && mesic <= 12) {
			mesic = 7;
		} else {
			throw new IllegalStateException("number of month not in range 1-12");
		}

		return ADate.from(rok, mesic, 1);
	}

	/*
	 * public static void main(String[] aArgs) {
	 * 
	 * ADate aDate = ADate.from(2005, 3, 30); int dayNumber = aDate.getDayNumber(); AMonth m = aDate.prevMonth(); ADate lastMonthDate = m.lastDate(); int i = lastMonthDate.getDayNumber(); ADate d = (i < dayNumber) ? lastMonthDate : m.dateOf(dayNumber); System.err.p rintln(d); }
	 */
}
