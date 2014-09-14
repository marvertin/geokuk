package cz.geokuk.util.lang;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




/** Časová známka
 * 
 * @author Veverka Martin
 * @version 0.00.00
 */
public final class ATimestamp extends Object0 implements IElement, IElementLong, Comparable<ATimestamp>, Ordinable<ATimestamp> {

  private static final long serialVersionUID = -4333541321205520147L;

  private java.util.Date iJavaDate;

  /* Patern regulárního výrazu, dosazovaný při prvním použití */
  private static Pattern sPattern;

  /**
   * Počet milisekund v jednom dni
   */
  public static long getMillisecondsPerDay() {
    return 86400000L; // = 24*60*60*1000;
  }

  /**
   * Koonstruuje datum z double cisla tohoto formátu: před desetinou čárkou:
   * počet dnů od 1.1.1900 za desetinnou čárkou: část dne od půlnoci public
   * ATimestamp(double cislo){ this((long)(cislo*getMillisecondsPerDay()));
   * checkRange(); }
   */

  /**
   * Konstruuje z počtu milisekund po 1.1.1970
   */
  private ATimestamp(long cislo) {
    //super(cislo - getBulgarian1970Constant());
    iJavaDate = new Date(cislo);
  }

  /**
   * Vrací řetězec v tomto formátu:
   * 
   * @return yyyy-mm-dd hh:mm:ss.ttt
   */
  public String asString() {
    return "" + asLong();
  }

  /**
   * Převod na standardní typ {@link Date}.
   * 
   * Hlavním smyslem metody je, abychom se nemuseli trápit, zda hodnota
   * {@link #asLong() ATimestamp.asLong()}má stejný význam jako
   * {@link Date#getTime() java.util.Date.getTime()}.
   * 
   * @return {@link Date}se stejným významem jako ATimestamp.
   */
  public Date asJavaDate() {
    if (iJavaDate == null)
      throw new IllegalStateException("INTERNAL ERROR - iJavaDate cannot be null.");
    Date date = new Date(this.asLong());
    return date;
  }

  /**
   * Převod {@link ATimestamp}na standardní {@link Date}.
   * 
   * @param ts
   *          Časová známka, kterou potřebujeme převést na standardní Java typ.
   * @return java.util.Date se stejným významem jako <tt>ts</tt> nebo <b>null
   *         </b>, pokud je <tt>ts</tt> <b>null </b>.
   */
  public static Date asJavaDate(ATimestamp ts) {
    if (ts == null)
      return null;
    return ts.asJavaDate();
  }

  /**
   * Vrací datum jako double: před desetinou čárkou: počet dnů od 1.1.1900 za
   * desetinnou čárkou: část dne od půlnoci
   * 
   * @return double Datum public double asDouble() { //Neodmazávat vnější
   *         závorky, jinak zaokrouhlení v nevhodném pořadí. return
   *         ((double)asLong() / getMillisecondsPerDay()); }
   */

  /**
   * Vrací počet milisekund po 1.1.1970
   * 
   * @return long Datum
   */
  public long asLong() {
    //return this.getTime() + _getBulgarian1970Constant();
    return iJavaDate.getTime();
  }

  /**
   * Vrací čas jako typ vhodný pro SQL
   */
  public java.sql.Timestamp asSqlTimestamp() {
    return new java.sql.Timestamp(asLong());
  }

  /**
   * Převádí long na double z báze 1.1.1970 na 1.1.1900
   * 
   * @param int
   *          dateTime
   * @return double private static double getDouble (long dateTime) {
   * 
   * return (dateTime + _getBulgarian1970Constant()) / getMillisecondsPerDay();
   * 
   * 
   * //if (vdablu == 0) // dodablu(); //return vdablu; }
   */

  /**
   * Nejdříve zkusí získat číslo (inverzní fce k asString()), pak ze
   * standardního stringu získat datum a čas, pokud se to nepovede, zkouší náš
   * fromFormatedString {@link #fromFormatedString(String)}.
   * 
   * @param String
   *          datum
   * @return Date
   */
  private static long fromString(String datum) {

    try {
      return Long.valueOf(datum);
    } catch (Exception e) {
      return fromFormatedStringByRegexp(datum, null); // pi použití této metody
                                                      // musí být zadána zóna
    }

  }

  /**
   * Vrátí v pro člověka čitelném formátu. Obsahuje úplné informace, které jsou
   * k dispozici. Takto vracené hodnoty slouží k zobrazení za účelem logování,
   * testování a podobně, v žádné, případě by se přijatá data neměla parsrovat a
   * něco z nich odvozovat. Pokud potřebujete konkrétní formát využijte metody
   * info.., získejte odpovídající poliožky a využijte formátovače.
   */
  public String toString() {
    return infoUtc().toString();
  }

  /**
   * Return an ISO 8601 compliant timestamp representation.
   */
  public String toIsoString() {
    return infoUtc().toIsoString();
  }

  /**
   * Return an ISO 8601 compliant timestamp representation vyjádřenou v zadané
   * zóně.
   */
  public String toIsoString(TimeZone aZone) {
    return info(aZone).toIsoString();
  }

  /**
   * Return an ISO 8601 compliant timestamp representation vyjádřenou v lokální
   * zóně
   */
  public String toIsoStringLocal() {
    return info(TimeZone.getDefault()).toIsoString();
  }

  /**
   * Vrací současný datum a čas
   * 
   * @return double
   */
  public static ATimestamp now() {

    Date d = new Date();
    //return new ATimestamp(d.getTime() + getBulgarian1970Constant());
    return new ATimestamp(d.getTime());
  }

  /**
   * Vrací datum odpovídající zadanému časovému pásmu.
   * 
   * @return Datum, které je v zadaný čas v odpovídajícím časovém, =ásmu.
   */
  public ADate getDate(TimeZone aZone) {
    GregorianCalendar cal = new GregorianCalendar(aZone);
    cal.setTime(iJavaDate); //nastavím sám sebe do kalenfářč
    return ADate.from(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
  }

  public ATimestamp add(long aDiff) {
    return new ATimestamp(asLong() + aDiff);
  }

  /**
   * Rozdíl v milisekundách. O kolik milisekund je objekt mladší než argument.
   */
  public long diff(ATimestamp aTime) {

    return asLong() - aTime.asLong();
  }

  /**
   * Vrací zónový ofset v miliseknudách.
   * 
   * @deprecated Metoda nemusí vracet, co chceš. Použij některou z metod info a
   *             na ní zjisti potřebné informace.
   */
  public long getZoneOffset() {
    Calendar cal = new GregorianCalendar();
    cal.setTime(iJavaDate);
    return cal.get(Calendar.ZONE_OFFSET);
  }

  /**
   * Vrací ofset letního času.
   * 
   * @deprecated Metoda nemusí vracet, co chceš. Použij některou z metod info a
   *             na ní zjisti potřebné informace.
   */
  public long getDstOffset() {
    Calendar cal = new GregorianCalendar();
    cal.setTime(iJavaDate);
    return cal.get(Calendar.DST_OFFSET);
  }

  /**
   * Záíská objekt poskytující informace o datumu a čase vztažené k zadané zóně.
   * 
   * @return Objekt poskytující svými metodami infomrace o času.
   */
  public Info info(TimeZone aZone) {
    return new Info(aZone);
  }

  /**
   * Získá objekt poskytující informace o čase vztažené k UTC.
   * 
   * @return
   */
  public Info infoUtc() {
    return new Info(TimeZone.getTimeZone("UTC"));
  }

  private static int _rezezNaCislo(String aRetez, int aDefault) {
    return aRetez == null ? aDefault : Integer.parseInt(aRetez);
  }

  private static long fromItems(int aRok, int aMesic, int aDen, int aHodina, int aMinuta, int aSekunda, int aMilisekund, TimeZone aTimeZone) {
    Calendar cal = Calendar.getInstance(aTimeZone);
    cal.set(aRok, aMesic - 1, aDen, aHodina, aMinuta, aSekunda);
    cal.set(Calendar.MILLISECOND, aMilisekund);
    return cal.getTimeInMillis(); // vytvořit hodnotu
  }

  
private static long fromFormatedStringByRegexp(String aDatStr, TimeZone aDefaultTimeZone)
        {
          if (sPattern == null)
             sPattern = Pattern.compile("\\s*(\\d\\d\\d\\d)[-./](\\d\\d?)(?:[-./](\\d\\d?)(?:[ tT]+(\\d\\d?)(?::(\\d\\d)(?::(\\d\\d)(?:\\.(\\d+))?)?)?)?)? *(UTC|Z|(?:GMT)?[+-]\\d\\d?(?::)?\\d\\d|(?:GMT)?[+-]\\d{1,3})?\\s*");
          Matcher mat = sPattern.matcher(aDatStr);
//          System.out.p rint("*** >" + aDatStr + "< ");
          if (mat.matches())
          {
            /*
             * for (int i = 1; i <= mat.groupCount(); i++) {
             * System.out.p rint(mat.group(i) + "|"); } System.out.p rintln();
             */

            TimeZone zona;
            String zonastr = mat.group(8);
            if (zonastr != null) { // je naplněna časová zóna
               char prvni = zonastr.charAt(0);
               if (prvni == '+' || prvni == '-') zonastr = "GMT" + zonastr;
               zona = TimeZone.getTimeZone(zonastr);
            } else {
                zona = aDefaultTimeZone;
            }
            if (zona == null)
               throw new XCreateElement("Pokus o vytvoreni ATimestamp z '" + aDatStr + "', v retezci neni uvedena casova zona a ve volani neni casova zona specifikovana");
            // zpracovat milisekundy
            String smilis = mat.group(7); // milisekundy
            int milis = 0;
            if (smilis != null)
            {
              while (smilis.length() < 3) {
                  smilis += "0"; // nevýkonné, ale nebude se dělat často
              }
              if (smilis.length() > 3) smilis = smilis.substring(0,3);
              milis = Integer.parseInt(smilis);
            }

            return fromItems(
              _rezezNaCislo(mat.group(1),0),
              _rezezNaCislo(mat.group(2),1),
              _rezezNaCislo(mat.group(3),1),
              _rezezNaCislo(mat.group(4),0),
              _rezezNaCislo(mat.group(5),0),
              _rezezNaCislo(mat.group(6),0),
              milis, zona
            );
          } else {
              throw new XCreateElement("Pokus o vytvoreni ATimestamp z '" + aDatStr + "', retezec ma spatnou syntaxi");
          }
        }  

 private static void testZona(String aStr) {
 }

 private static void testFormat(String aStr) {

    ATimestamp cas = ATimestamp.from(aStr);
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    cal.setTimeInMillis(cas.asLong());
  }

  public static void main(String[] args) {
    testFormat("2015-11-28 17:33:53.12 GMT+05:00");
    testFormat("2015-11-28 19:44:17.789 GMT+3");
    testFormat("  2015-11-28 17:33:53.224 GMT+05");
    testFormat("2015-3-7   6:33:53   GMT+05   ");
    testFormat("2015-13-17 16:33:53 GMT-6");
    testFormat("       2015-3-7       6:33:53 UTC   ");
    testFormat("2015-3-7   6:33:09 GMT-6");
    testFormat("2015-08-17 12:55   GMT+13");
    testFormat("2015-3-7   6 UTC");
    testFormat("2015-3-7  GMT-6");
    testFormat("2015-3 GMT+6");
    testFormat("1970-1 GMT+0");
    testFormat("1970-1-1 3:30:05.555 GMT+03:30");
    testFormat("1969-12-31 23:59 GMT+000");
    testFormat("1969-12-31T23:59 GMT+000");
    testFormat("1969-12-31T23:59GMT+000");
    testFormat("1970-1-1T0:10:01.333UTC");
    testFormat("1970-1-1T0:10:01.333Z");
    testFormat("1970-1-1T0:10:01.333GMT+01");
    testFormat("1970-1-1T0:10:01.333GMT-01");
    testFormat("1970-1-1T0:10:01.333+01");
    testFormat("1970-1-1T0:10:01.333-01");
    testFormat("1970-1-1T0:10:01.333+01:02");
    testFormat("1970-1-1T0:10:01.333-01:02");
    testFormat("1970-1-1T05:16:00.444-03");
    testFormat("1970-1-1T05:16:00.444-03:04");
    testFormat("1970-1-1T05:16:00.444-0304");
    testFormat("1970-1-1T0:10:01.333+0102");
    testFormat("1970-1-1T0:10:01.333-0102");
    testZona("UTC");
    testZona("GMT");
    testZona("GMT+01");
    testZona("GMT+1");
    testZona("GMT-1");
    testZona("GMT+03:15");
    testZona("GMT+0315");
    testZona("GMT-03:15");
    testZona("GMT-0315");
    testZona("GMT-03156");
    testZona("HH-0315");
    testZona("Z");

    /*
     * System.out.p rintln(new ATimestamp("1996-10-27
     * 00:00:00").toGmtIsoString()); System.out.p rintln(new
     * ATimestamp("1996-10-27 00:30:00").toGmtIsoString());
     * System.out.p rintln(new ATimestamp("1996-10-27
     * 01:00:00").toGmtIsoString()); System.out.p rintln(new
     * ATimestamp("1996-10-27 01:30:00").toGmtIsoString());
     * System.out.p rintln(new ATimestamp("1996-10-27
     * 02:00:00").toGmtIsoString()); System.out.p rintln(new
     * ATimestamp("1996-10-27 02:30:00").toGmtIsoString());
     * System.out.p rintln(new ATimestamp("1996-10-27
     * 03:00:00").toGmtIsoString()); System.out.p rintln(new
     * ATimestamp("1996-10-27 03:30:00").toGmtIsoString());
     * System.out.p rintln(new ATimestamp("1996-10-27
     * 04:00:00").toGmtIsoString()); System.out.p rintln(new
     * ATimestamp("1996-02-14 16:16:16").toGmtIsoString());
     * System.out.p rintln(new ATimestamp("1966-08-14
     * 16:16:16").toGmtIsoString()); System.out.p rintln(new
     * ATimestamp("1966-02-14 16:16:16").toGmtIsoString());
     * System.out.p rintln("------------------------------------------------------");
     * ATimestamp ts = now(); // aktualni čas System.out.p rintln("Vypis
     * verejnymi metodami"); System.out.p rintln(" asLong: " + ts.asLong());
     * System.out.p rintln(" asString: " + ts.asString()); System.out.p rintln("
     * toString: " + ts.toString()); System.out.p rintln("Vypis interni hodnoty
     * typu java.util.Date"); System.out.p rintln(" by DateFormat: " +
     * DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.LONG,Locale.getDefault()).format(ts.iJavaDate));
     * System.out.p rintln(" toString: " + ts.iJavaDate.toString());
     * System.out.p rintln(" DefaulTimeZone: " +
     * TimeZone.getDefault().toString()); //System.out.p rintln(" toGMTString: " +
     * ts.iJavaDate.toGMTString()); //System.out.p rintln(" toLocaleString: " +
     * ts.iJavaDate.toLocaleString());
     * 
     * System.out.p rintln("------------------------------------------------------");
     * long l = 846356400000L; for (int i = 0; i <15; i++) { l+= 1812345;
     * ATimestamp t = new ATimestamp(l); System.out.p rintln(t.asString() + " - " +
     * t.toString() + " ||| " + " - " + t.infoDefault() + " - " + t.infoUtc() + " - " +
     * t.info(TimeZone.getTimeZone("GMT+5")) + " - " +
     * t.info(TimeZone.getTimeZone("GMT-7")) ); }
     */

  }

  /**
   * Zjistí, zda je ostře menší než b.
   * 
   * @param Object
   *          b
   * @return boolean Výsledek
   */
  public boolean isLess(Object b) {

    ATimestamp obj = (ATimestamp) checkCompare(b);
    return obj != null && asLong() < obj.asLong();
  }

  public boolean isLess(ATimestamp b) {
    return compareTo(b) < 0;
  }

  public boolean isLessOrEqual(ATimestamp b) {
    return compareTo(b) <= 0;
  }

  public boolean isGreater(ATimestamp b) {
    return compareTo(b) > 0;
  }

  public boolean isGreaterOrEqual(ATimestamp b) {
    return compareTo(b) >= 0;
  }

  public boolean isEqual(ATimestamp b) {
    return compareTo(b) == 0;
  }

  public boolean isNotEqual(ATimestamp b) {
    return compareTo(b) != 0;
  }

  /**
   * Zjistí, zda je rovno datumu.
   * 
   * @param Object
   *          aDatum
   * @return boolean Výsledek
   */
  public boolean equals(Object aObject) {
    if (!(aObject instanceof ATimestamp))
      return false;
    ATimestamp obj = (ATimestamp) aObject;
    return asLong() == obj.asLong();
  }

  public int compareTo(ATimestamp aObj) {
    long a = this.asLong();
    long b = ((ATimestamp) checkCompare(aObj)).asLong(); // vyhodí výjimku,
                                                         // pokud bude null
    return a == b ? 0 : a < b ? -1 : 1;
  }

  /**
   * Heš kód pro datum.
   * 
   * @return Heškód agregovaného java.util.date.
   */
  public int hashCode() {
    return iJavaDate.hashCode();
  }

  /**
   * Dekóduje časový parametr zadaný jako řetězec. Musí obsahovat i časovou
   * zónu, jinak bude vyhozena výjimka. Pokud máš datumy bez časové zóny, použij
   * další parametr.
   * 
   * @param s
   *          Řetězec obsahující datum a čas. Nebo zde může být v řetězci číslo
   *          vyhozené metodou asString. Řetězec musí vyhovovat regulárnímu
   *          výrazu: \s*\d\d\d\d[-./]\d\d?([-./]\d\d?(
   *          +\d\d?(:\d\d(:\d\d(\.\d+)?)?)?)?)?
   *          *(UTC|GMT[+-]\d\d?:\d\d|GMT[+-]\d{1,3})\s|\d+*
   * @param aTimeZone
   *          Zóna, která se použije v případě, že v etězci není zóna explicitně
   *          uvedena. Pokud uvedena je, tak se tento údaj ignoruje.
   * @return Vytvořený ATimestamp.
   */
  public static ATimestamp from(String s) {
    return StringUtils.isBlank(s) ? null : new ATimestamp(fromString(s));
  }

  /**
   * Vytvoří ATimestamp analýzou řetězce. Pokud v řetězci není zadána časová
   * zóna, předpokládá se, že čas je uveden v zadané časové zóně.
   * 
   * @param s
   *          Řetězec obsahující datum a čas. Řetězec musí vyhovovat regulárnímu
   *          výrazu: \s*\d\d\d\d[-./]\d\d?([-./]\d\d?(
   *          +\d\d?(:\d\d(:\d\d(\.\d+)?)?)?)?)?
   *          *(UTC|GMT[+-]\d\d?:\d\d|GMT[+-]\d{1,3})?\s*
   * @param aTimeZone
   *          Zóna, která se použije v případě, že v etězci není zóna explicitně
   *          uvedena. Pokud uvedena je, tak se tento údaj ignoruje.
   * @return Vytvořený ATimestamp.
   */
  public static ATimestamp from(String s, TimeZone aTimeZone) {
    return StringUtils.isBlank(s) ? null : new ATimestamp(fromFormatedStringByRegexp(s, aTimeZone));
  }

  /**
   * Vytvoří ATimestamp analýzou řetězce. Pokud v řetězci není zadána časová
   * zóna, předpokládá se, že čas je v UTC.
   * 
   * @param s
   *          Řetězec obsahující datum a čas. Řetězec musí vyhovovat regulárnímu
   *          výrazu: \s*\d\d\d\d[-./]\d\d?([-./]\d\d?(
   *          +\d\d?(:\d\d(:\d\d(\.\d+)?)?)?)?)?
   *          *(UTC|GMT[+-]\d\d?:\d\d|GMT[+-]\d{1,3})?\s*
   * @return Vytvořený ATimestamp.
   */
  public static ATimestamp fromUtc(String s) {
    return StringUtils.isBlank(s) ? null : new ATimestamp(fromFormatedStringByRegexp(s, TimeZone.getTimeZone("UTC")));
  }

  /**
   * Vytvoří ATimestamp ze zadaných údajů, které odpovídají zadané časové zóně.
   * 
   * @param aRok
   *          Rok čtyřmístný.
   * @param aMesic
   *          Měsíc 1..12
   * @param aDen
   *          1..31
   * @param aHodina
   *          0..23
   * @param aMinuta
   *          0..59
   * @param aSekunda
   *          0..59
   * @param aMilisekund
   *          0..999
   * @param aTimeZone
   *          Časová zóna.
   * @return
   */
  public static ATimestamp from(int aRok, int aMesic, int aDen, int aHodina, int aMinuta, int aSekunda, int aMilisekund, TimeZone aTimeZone) {
    if (aTimeZone == null) {
        throw new IllegalArgumentException("Casova zona musi byt uvedena");
    }

    return new ATimestamp(fromItems(aRok, aMesic, aDen, aHodina, aMinuta, aSekunda, aMilisekund, aTimeZone));
  }

  /**
   * Vytvoří ATimestamp ze zadaných údajů, které odpovídají zadané časové zóně.
   * 
   * @param aRok
   *          Rok čtyřmístný.
   * @param aMesic
   *          Měsíc 1..12
   * @param aDen
   *          1..31
   * @param aHodina
   *          0..23
   * @param aMinuta
   *          0..59
   * @param aSekunda
   *          0..59
   * @param aMilisekund
   *          0..999
   * @param aTimeZone
   *          Časová zóna.
   * @return
   */
  public static ATimestamp fromUtc(int aRok, int aMesic, int aDen, int aHodina, int aMinuta, int aSekunda, int aMilisekund) {
    return new ATimestamp(fromItems(aRok, aMesic, aDen, aHodina, aMinuta, aSekunda, aMilisekund, TimeZone.getTimeZone("UTC")));
  }

  /**
   * @deprecated Newní důvod pro existenci takové metody. Použij stejnojmennou
   *             metodu přijímající long a ze svého timestampu, který máš si
   *             vytáhni long, aspon budeš vědět, co děláš. Ujisiti, se, že
   *             předávaný argument je opravdu správný a ne posunutý
   *             nepochopením významu timestampu.
   */
  public static ATimestamp from(java.sql.Timestamp ts) {
    return ts == null ? null : new ATimestamp(ts.getTime());
  }

  public static ATimestamp from(long aCas) {
    return new ATimestamp(aCas);
  }

  public static boolean canFrom(String s) {
    try {
      from(s);
      return true;
    } catch (XCreateElement e) {
      return false;
    }
  }

  /**
   * @deprecated Newní důvod pro existenci takové metody. Použij stejnojmennou
   *             metodu přijímající long a ze svého timestampu, který máš si
   *             vytáhni long, aspon budeš vědět, co děláš. Ujisiti, se, že
   *             předávaný argument je opravdu správný a ne posunutý
   *             nepochopením významu timestampu.
   */
  public static boolean canFrom(java.sql.Timestamp ts) {
    return true;
  }

  public static boolean canFrom(long aCas) {
    try {
      from(aCas);
      return true;
    } catch (XCreateElement e) {
      return false;
    }
  }

  ////////////////////////////////////////////////////////////////////////////////
  /// Informace

  public final class Info {
    private final Calendar iCal;

    Info(TimeZone aTimeZone) {
      iCal = Calendar.getInstance(aTimeZone); // instance kalendáře pro určitou
                                              // zónu
      iCal.setTimeInMillis(iJavaDate.getTime()); // čas v milisekundách
    }

    /**
     * Vrací číslo roku odpovídající časové rzóně, pro kterou bylo Info
     * vytvořeno
     * 
     * @return čtyřmístné číslo roku
     */
    public int getYearNumber() {
      return iCal.get(Calendar.YEAR);
    }

    /**
     * Vrací číslo měsíce odpovídající časové zóně, pro kterou bylo Info
     * vytvořeno
     * 
     * @return 1 až 12
     */
    public int getMonthNumber() {
      return iCal.get(Calendar.MONTH) + 1;
    }

    /**
     * Vrací číslo dne odpovídající časové zóně, pro kterou bylo Info vytvořeno
     * 
     * @return 1 až 31
     */
    public int getDayNumber() {
      return iCal.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Vrací počet hodin odpovídající časové zóně, pro kterou bylo Info
     * vytvořeno
     * 
     * @return 0 až 23
     */
    public int getHour() {
      return iCal.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * Vrací počet minut odpovídající časové zóně, pro kterou bylo Info
     * vytvořeno
     * 
     * @return 0 až 59
     */
    public int getMinute() {
      return iCal.get(Calendar.MINUTE);
    }

    /**
     * Vrací počet sekund odpovídající časové zóně, pro kterou bylo Info
     * vytvořeno
     * 
     * @return 0 až 59
     */
    public int getSecond() {
      return iCal.get(Calendar.SECOND);
    }

    /**
     * Vrací počet milisekund odpovídající časové zóně, pro kterou bylo Info
     * vytvořeno.
     * 
     * @return 0 ažž 999
     */
    public int getMilisecond() {
      return iCal.get(Calendar.MILLISECOND);
    }

    /**
     * Vrací datum, které je v časové zóně, se kterou bylo info vytvořeno.
     * 
     * @return
     */
    public ADate getDate() {
      return ADate.from(getYearNumber(), getMonthNumber(), getDayNumber());
    }

    /**
     * Vrací časovou zónu, se kterou byl tento objekt vytvořen.
     * 
     * @return
     */
    public TimeZone getTimeZone() {
      return iCal.getTimeZone();
    }

    /**
     * Vrací zónový ofset v miliseknudách.
     */
    public long getZoneOffset() {
      return iCal.get(Calendar.ZONE_OFFSET);
    }

    /**
     * Vrací ofset letního času v milisekundách.
     */
    public long getDstOffset() {
      return iCal.get(Calendar.DST_OFFSET);
    }

    /**
     * Vrací offset v milisekundách oproti UTC. Výsledek je součtem zónového a
     * DST offsetu.
     * 
     * @return
     */
    public long getOffset() {
      return getDstOffset() + getZoneOffset();
    }

    /**
     * Vrátí v pro člověka čitelném formátu. Obsahuje úplné informace, které
     * jsou k dispozici. Takto vracené hodnoty slouží k zobrazení za účelem
     * logování, testování a podobně, v žádné, případě by se přijatá data neměla
     * parsrovat a něco z nich odvozovat. Pokud potřebujete konkrétní formát
     * využijte metody info.., získejte odpovídající poliožky a využijte
     * formátovače.
     */
    public String toString() {
      long offset = getOffset();
      String vysl = MessageFormat.format(
          "{0,number,0000}-{1,number,00}-{2,number,00} {3,number,00}:{4,number,00}:{5,number,00}.{6,number,000} GMT{7,number,+00;-00}",
              getYearNumber(), getMonthNumber(), getDayNumber(), getHour(), getMinute(),
              getSecond(), getMilisecond(), offset / 3600000);
      if (offset % 3600000 != 0) {
        vysl += ":" + offset / 60000;
      }
      return vysl;
    }

    /**
     * Return an ISO 8601 compliant timestamp representation.
     */
    public String toIsoString() {
      String result = MessageFormat.format("{0,number,0000}-{1,number,00}-{2,number,00}T{3,number,00}:{4,number,00}:{5,number,00}.{6,number,000}",
              getYearNumber(), getMonthNumber(), getDayNumber(), getHour(),getMinute(), getSecond(),
              getMilisecond());
      return result + offsetStr();
    }

    private String offsetStr() {
      long offset = getOffset();
      if (offset == 0)
        return "Z";
      char sign = offset < 0 ? '-' : '+';
      offset = Math.abs(offset);
      offset = offset / (1000 * 60); // a je to v minutách
      return sign + MessageFormat.format("{0,number,00}{1,number,00}", new Object[] {offset / 60, offset % 60, });
    }
  }

  /**
   * Totéžž co {@link #add}
   * 
   * @param aNthObject
   *          Počet milisekund o kolik posunout.
   * @return Instance ATimestamp s posunutým měsícem.
   */
  public ATimestamp getAnother(long aNthObject) {
    return this.add(aNthObject);
  }

  /**
   * Totéž co diff.
   * 
   * @param aObject
   *          Objekt, který se odečítá.
   * @return Počet milisekund rozdílu.
   */
  public long getDistance(ATimestamp aObject) {
    if (aObject == null) {
        throw new IllegalArgumentException();
    }
    return diff(aObject);
  }

}
