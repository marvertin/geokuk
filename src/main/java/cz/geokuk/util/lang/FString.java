package cz.geokuk.util.lang;

import java.lang.reflect.Array;
import java.text.StringCharacterIterator;
import java.util.Iterator;
import java.util.Vector;


/**
 * Třída opbsahuje užitečnou práci s řetězci.
 *
 * @author	Michal Polák
 * @version $Revision: 35 $
 * @see     "TW0139Util.vjp"
 * @see     "$Header: /Zakazky/TWare/Distribuce/TW0139/Util/cz/tconsult/tw/util/TwString.java 35    22.04.01 14:54 Roztocil $"
 */
public final class FString
{ 
  /** {@link TwString} je jen knihovna funkcí, nikoliv instanciovatelný objekt. */
  private FString() { /* INTENDED: zabránění externí instanciace. */ }

  /** Odstraní ze zadaného řetězce poslední znak. */
  public static final String removeNextChar(String s) {
    if (s == null || s.length() == 0) {
      return s;
    }
    return s.substring(0,s.length() - 1);
  }

  /** Pokud to lze, zařízne String na danou délku. */
  public static final String truncateRight(String s, int maxlen) {
    if (s.length() > maxlen) {
      return s.substring(0,maxlen);
    }
    return s;
  }

  /** Pokud to lze, zařízne String na danou délku. */
  public static final String truncateLeft(String s, int maxlen) {
    if (s.length() > maxlen) {
      return s.substring(s.length() - maxlen, s.length());
    }
    return s;
  }

  /** Vrací true, pokud je řětězec null nebo délku 0. */
  public static final boolean isVoid(String s) {
    return (s == null || s.length() == 0);
  }

  /** Vrací true, pokud je řetězec prázdný, to znamená null, nebo prázdný
   * nebo mezery.
   */
  public static final boolean isEmpty(String s) {
    return StringUtils.isBlank(s);
  }

  /** Pomocí oddělovače spojí prvky pole do Stringu.
   *
   * @param	aDelimiter	Oddělovač.
   * @param	aPole		Pole nějakých objektů, jejichž hodnoty .toString() budou spojeny do výsledku.
   */
  public static final String mergeArray(String aDelimiter, Object aPole) {
    if (aPole == null) {
      return null;
    }

    Class<?> c = aPole.getClass();
    if (!c.isArray())       throw new IllegalArgumentException("Argument aPole typu " + c + " není pole.");

    StringBuffer result = new StringBuffer(300);
    int len = Array.getLength(aPole);
    for (int i = 0; i < len; i++) {
      if (i > 0) {
        result.append(aDelimiter);
      }
      result.append(Array.get(aPole, i));
    }

    return result.toString();
  }

  /** Pomocí oddělovače spojí prvky kolekce do Stringu.
   *
   * @param	aDelimiter	Oddělovač.
   * @param	aIterator		Kolekce nějakých objektů, jejichž hodnoty .toString() budou spojeny do výsledku.
   * @author polakm
   */
  public static final String merge(String aDelimiter, Iterator<? extends Object> aIterator) {
    if (aIterator == null) {
      return null;
    }

    StringBuffer result = new StringBuffer(300);
    String delimiter = "";
    for (Iterator<? extends Object> i = aIterator; i.hasNext(); ) {

      result.append(delimiter);
      result.append("" + i.next());
      delimiter = aDelimiter;
    }

    return result.toString();
  }

  /** Spojí zadané parametry. Pokud je kterýkoliv z nich null, výsledkem je null.
   * @author roztocil
   * @since 2008-10-03
   */
  public static final String nullConcat(Object ... aItems) {
    StringBuffer sb = new StringBuffer();
    for (Object item : aItems) {
      if (item == null) {
        return null;
      }
      sb.append(item);
    }
    String result = sb.toString();
    return result;
  }

  /**Převede řetězec na pole, rozseká zadaným oddělovačem.
   * @param text text k rozdělení
   * @param delimiter oddělovač
   * @param multidelim pokud true, posloupnost oddělovačů je považována za jeden
   * @return pole s rozsekaným textem. Pokud je text prázdný řetězec, vrací prázdné pole.
   * Pokud je text null nebo delimiter je null, vrací null.
   * @author Jiří Polák
   * @deprecated náhradou je metoda split()
   */
  //TOTO: (JP) předělat z odsekávání textu na StringTokenizer - pro texty obsahující hodně oddělovačů je funkce velmi pomalá.
  @Deprecated
  public static String[] splitText(String text, String delimiter, boolean multidelim) {
    //nullové varianty
    if (text == null || delimiter == null) {
      return null;
    }
    //MV je nesmysl, aby v případě prázdného řetězce se vracelo null. Vrací se prázdné pole
    //nullové varianty
    if (text.length() == 0) {
      return new String[0];
    }

    int delimlength = delimiter.length();

    //neobsahuje delimiter
    if (delimlength == 0 || text.indexOf(delimiter) == -1) {
      return new String[] {text};
    }

    Vector<String> v = new Vector<String>();
    //jestliže máme považovat posloupnost oddělovačů za jeden, otrimuj oddělovače
    if (multidelim) {
      text = leftRemove(text, delimiter);
      text = rightRemove(text, delimiter);
    }
    //pozice levého oddělovače
    int p = text.indexOf(delimiter);

    //dokud existuje v textu oddělovač
    while (p > -1) {
      //přidej do vectoru text před oddělovačem
      v.addElement(text.substring(0, p));
      //odřízni od textu levou část i s oddělovačem
      text = text.substring(p + delimlength);
      //jestliže máme považovat posloupnost oddělovačů za jeden, otrimuj možné oddělovače
      if (multidelim) {
        text = leftRemove(text, delimiter);
      }
      //pozice levého oddělovače
      p = text.indexOf(delimiter);
    }
    if (text.length() > 0) {
      v.addElement(text);
    }

    //vrať výsledné pole
    String[] result = new String[v.size()];
    v.copyInto(result);

    return result;
  }

  /**Převede řetězec na pole, rozseká zadaným oddělovačem.
   * Posloupnost oddělovačů je považována za jeden.
   * @param text text k rozdělení
   * @param delimiter oddělovač
   * @return pole s rozsekaným textem
   * @author Jiří Polák
   * @deprecated náhradou je metoda split()
   */
  @Deprecated
  public static final String[] splitText(String text, String delimiter) {
    return splitText(text, delimiter, true);
  }


  /**Převede řetězec na pole, rozseká podle délky.
   * Vytvoří pole prvků s délkou specifikovanou parametrem (poslední prvek může být kratší.
   * @param text text k rozdělení
   * @param Size délka výsledných podřetězců. Pokud je Size <= 0, vrátí jednoprvkové pole s původním textem.
   * @return pole s rozsekaným textem
   * @author Jiří Polák
   */
  public static final String[] splitByLength(String text, int Size) {
    if (text == null){
      return null;
    }

    //Pokud je Size <= 0 nebo se jedná o prázdný text, vrať jednoprvkové pole s původním textem.
    if (Size <= 0 || text.equals("")){
      return new String[] {text};
    }

    int len = text.length();
    //urči velikost posledního prvku
    int sizelast = len % Size;
    //urči velikost pole
    int count = len / Size;
    if (sizelast > 0) {
      count ++;
    } else {
      //poslední prvek je stejně dlouhý jako ty předchozí
      sizelast = Size;
    }

    //nadeklaruj vracené pole
    String [] result = new String[count];
    //nacpi stringy bez posledního prvku
    for (int i = 0; i < count - 1; i++) {
      int index = i * Size;
      result[i] = text.substring(index, index + Size);
    }
    //nacpi poslední prvek
    result[count - 1] = text.substring(len - Size, len);

    return result;
  }

  /**Zřetězení po sobě jdoucích textů v poli stringů (vzestupně i sestupně).
   * Pokud některý z parametrů indexů je mimo rozsah pole, je nastaven na bližší hranici rozsahu.
   * @param StrArray pole stringů jehož prvky se budou řetězit.
   * @param FirstIndex index prvního zřetězovaného stringu
   * @param LastIndex index posledního zřetězovaného stringu
   * @return zřetězení prvků od First do Last, pokud je pole prázdné nebo null, vrací null
   * @author Jiří Polák
   */
  public static final String concatenate (String[] strArray, int firstIndex, int lastIndex)
  {
    if (strArray == null || strArray.length == 0) {
      return null;
    }

    //Jsou-li obě meze mimo pole, vracíme prázdný řetězec
    if (
        (firstIndex < 0 && lastIndex < 0)
        || (firstIndex >= strArray.length && lastIndex >= strArray.length)
    ) {
      return "";
    }

    //ošetříme meze
    if (firstIndex < 0) firstIndex = 0;
    if (lastIndex < 0) lastIndex = 0;
    if (firstIndex >= strArray.length) firstIndex = strArray.length - 1;
    if (lastIndex >= strArray.length) lastIndex = strArray.length - 1;

    //určíme pořadí zřetězovaných indexů
    int direction = 1;
    if (firstIndex > lastIndex){
      //musíme odečítat
      direction = -1;
    }

    StringBuffer result = new StringBuffer();
    int index = firstIndex;
    while (index != lastIndex){
      //přidej vše kromě posledního indexu
      result.append(strArray[index]);
      //změň index
      index = index + direction;
    }

    //a přidej poslední index
    result.append(strArray[lastIndex]);

    return result.toString();
  }

  /**
   * Smaže zleva zadané řetězce, pokud tam nějaké jsou.
   * @param text  Řetězec, ze kterého mazat.
   * @param pattern Mazaný řetězec.
   * @return Řetězec ne delší než zadaný s případně smazanými řetězci. Nikdy ne null.
   */
  public static final String leftRemove (String text, String pattern) {
    int length = pattern.length();
    while (text.startsWith(pattern)) {
      text = text.substring (length);
    }

    return text;
  }

  /**
   * Smaže zprava zadané řetězce, pokud tam nějaké jsou
   * @param text Řetězec, ze kterého se maže.
   * @param pattern Mazaný řetězec.
   * @return
   */
  /**Dokud jsou zprava zadané řetězce, maž je
   */
  public static final String rightRemove (String text, String pattern) {
    int length = pattern.length();
    while (text.endsWith(pattern)) {
      text = text.substring (0, text.length() - length);
    }

    return text;
  }

  /**
   * Doplň zprava znaky zadané v pattern tak, aby řetězec nebyl menší než
   * zadaný řetězec.
   * @param text   Řetězec, který bude doplňován.
   * @param length  Délka, na kteoru má být řetězec doplněn.
   * @param pattern Znak, kterým se řetězec doplní.
   * @return Řtězec odvozený z {@link text} doplněním znaků zprava {@link pattern} tak, aby nebyl kratší než {@link length}
   * @see {@link alignLeft}
   *
   */
  public static final String rightAdd (String text, int length, char pattern) {

    int len = text.length();
    if (len >= length) {
      return text;
    }

    len = length - len;
    char[] p = new char[len];
    for (int i = 0; i < len; i++) p[i] = pattern;

    return text + new String(p);
  }

  /**
   * Doplň zleva znaky zadané v pattern tak, aby řetězec nebyl menší než
   * zadaný řetězec.
   * @param text   Řetězec, který bude doplňován.
   * @param length  Délka, na kteoru má být řetězec doplněn.
   * @param pattern Znak, kterým se řetězec doplní.
   * @return Řtězec odvozený z {@link text} doplněním znaků {@link pattern} tak, aby nebyl kratší než {@link length}
   * @see {@link alignLeft}
   *
   */
  public static final String leftAdd (String text, int length, char pattern) {

    int len = text.length();
    if (len >= length) {
      return text;
    }

    len = length - len;
    char[] p = new char[len];
    for (int i = 0; i < len; i++) p[i] = pattern;

    return new String(p) + text;
  }

  /**Textové substituce.
   * Provádí se pouze náhrada původního textu zleva, nikoliv texty vzniklé jedním průchodem substituce
   * anebo překrývající se vzorky.<BR>
   * Příklad: <PRE>TwString.subst("abbbc", "bb", "X");</PRE> vrátí "aXbc".
   * @param text text pro náhrady.
   * @param searched hledaný nahrazovaný text
   * @param replaced text k náhradě
   * @return text s provedenou substitucí
   * @author Jiří Polák
   */
  public static final String subst(String text, String searched,  String replaced)
  {
    if (text == null || searched == null || "".equals(searched) || text.length() < searched.length()){
      return text;
    }

    StringBuffer result = new StringBuffer();

    int searchLength = searched.length();
    int lastPos = 0;
    int actPos = text.indexOf(searched, lastPos);

    while (actPos >= 0) {
      result.append(text.substring(lastPos, actPos));
      result.append(replaced);
      lastPos = actPos + searchLength;
      actPos = text.indexOf(searched, lastPos);
    }

    result.append(text.substring(lastPos, text.length()));

    return result.toString();
  }

  ////////////////////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////  Bohuslav Roztočil  ///////

  /**
   * Textové substituce.
   *
   * <br/>
   * Provádí se pouze náhrada původního textu zleva,
   *      nikoliv texty vzniklé jedním průchodem substituce anebo překrývající se vzorky.
   * Je možné zadat více nahrazovaných řetězců (a ke každému z nich jinou náhradu),
   *     vždy je nalezen ten nejlevější z nich a nahrazen.
   * Pokud se mezi vzory vyskytne honota <code class="java">null</code> nebo prázdný řetězec,
   *      bude tiše ignorována.
   * <br/>
   *
   * Příklad:
   * <pre class="java">
   * String zdroj    = "Ahoj %1, jak se %2%s";
   * String výsledek = TwString.subst(test,
   *                     new String[] { "%1",      "%2",       "%s" },
   *                     new String[] { "Bohušu",  "Vám daří", ", co?" }
   *                   );
   * System.out.println(zdroj);
   * System.out.println(výsledek);
   * </pre>
   *
   * @param   aText           Text pro náhrady.
   * @param   aPatterns       Hledané/nahrazované text.
   * @param   aReplacements   Texty k náhradě.
   * @return  aText s provedenou substitucí.
   * @author  Bohuslav Roztočil
   */
  public static final String subst(String aText, String[] aPatterns, String[] aReplacements)
  {
    if (aPatterns.length != aReplacements.length) {
      throw new IllegalArgumentException(
          "Pocet vzoru a nahrad musi byt shodny! (" + aPatterns.length + "/" + aReplacements.length + ")"
      );
    }

    StringBuffer    result = new StringBuffer(aText.length()*2);
    char[]          text = aText.toCharArray();
    int             position = 0;

    for (;;) {
      int posFound = Integer.MAX_VALUE;
      String replacement = null;
      String pattern     = null;

      for (int i = 0; i < aPatterns.length; i++) {
        if (aPatterns[i] == null)       continue;
        if ("".equals(aPatterns[i]))    continue;

        int p = aText.indexOf(aPatterns[i], position);

        if (p < 0)                      continue;
        if (p < posFound) {
          posFound    = p;
          pattern     = aPatterns[i];
          replacement = aReplacements[i];
        }
      }
      if (posFound == Integer.MAX_VALUE)  break;

      result.append(text, position, posFound - position);
      position = posFound;

      if (replacement != null) {
        result.append(replacement);
        position += pattern.length();
      }
    }
    result.append(text, position, text.length - position);

    return result.toString();
  }

  /**
   * Zajistí nahrazení prázdného ({@link isEmpty()}) řetězce za jiný.
   *
   * @param	aString			Původní řetězec.
   * @param	aReplacement	Řetězec vrácený, pokud platí {@link isEmpty() isEmpty(aString)}.
   * @return	Původní řetězec nebo jeho náhrada.
   */
  public static final String ifEmpty(String aString, String aReplacement)
  {
    if (isEmpty(aString))	return aReplacement;
    return aString;
  }

  /**
   * Zajistí nahrazení prázdného ({@link isVoid()}) řetězce za jiný.
   *
   * @param	aString			Původní řetězec.
   * @param	aReplacement	Řetězec vrácený, pokud platí {@link isVoid() isVoid(aString)}.
   * @return	Původní řetězec nebo jeho náhrada.
   */
  public static final String ifVoid(String aString, String aReplacement)
  {
    if (isVoid(aString))	return aReplacement;
    return aString;
  }

  /**
   * Zajistí nahrazení <b>null</b> řetězce za jiný.
   *
   * @param	aString			Původní řetězec.
   * @param	aReplacement	Řetězec vrácený, pokud platí <code>aString == null</code>.
   * @return	Původní řetězec nebo jeho náhrada.
   */
  public static final String ifNull(String aString, String aReplacement)
  {
    if (aString == null)	return aReplacement;
    return aString;
  }

  /**
   * Provede {@link java.lang.String#trim()} i pro parametry <b>null</b> bez vyhození vyjímky.
   *
   * @param	aString		Hodnota, kterou chceme oříznout.
   * @return				Hodnota {@link aString} zbavená okrajových mezer nebo "", byla-li původní hodnota <b>null</b>.
   */
  public static final String safeTrim(String aString)
  {
    if (aString == null)	return "";
    return aString.trim();
  }

  /**
   * Provede {@link java.lang.String#trim()} i pro parametry <b>null</b> bez vyhození vyjímky.
   * @param aString Hodnota, kterou chceme oříznout.
   * @return Hodnota {@link aString} zbavená okrajových mezer nebo <b>null</b>, byla-li původní hodnota <b>null</b>.
   */
  public static final String trimOrNull(String aString) {
    if (aString == null) {
      return null; 
    }
    return aString.trim();
  }

  /**
   * Zajistí, že řetězec není nikdy <b>null</b>.
   *
   * @param	aString		Hodnota, kterou chceme zabezpečit.
   * @return				Hadaná hodnota {@link aString} nebo "", byla-li původní hodnota <b>null</b>.
   */
  public static final String notNull(String aString)
  {
    if (aString == null)	return "";
    return aString;
  }

  /**
   * Zarovná řetězec na požadovanou délku.
   *
   * Pokud je řetězec příliš krátký,
   * @param	s		Zarovnávaný řetězec.
   * @param	len		Požadovaná délka řetězce po zarovnání.
   * @param	fill	Řetězec, kterým má být zarovnávaný řetězec doplněn na požadovanou délku.
   *                                      *					Je-li prázdný nebo null, použije se jedna mezera.
   * @return	Zadaný řetězec doplněný zprava na požadovanou délku určený vyplňovacím řetězcem.
   */
  public static final String alignRight(String s, int len, String fill)
  {
    if (s.length() == len) return s;
    if (s.length() < len) {
      if (isVoid(fill)) fill = " ";
      StringBuffer sb = new StringBuffer(len);
      sb.append(s);
      while (sb.length() < len) {
        sb.append(fill);
      }
      if (sb.length() == len) return sb.toString();
      return truncateRight(sb.toString(), len);
    }
    return truncateRight(s, len);
  }

  /**
   * Zarovná řetězec na požadovanou délku.
   *
   * Pokud je řetězec příliš krátký,
   * @param	s		Zarovnávaný řetězec.
   * @param	len		Požadovaná délka řetězce po zarovnání.
   * @param	fill	Znak, kterým má být zarovnávaný řetězec doplněn na požadovanou délku.
   * @return	Zadaný řetězec doplněný zprava na požadovanou délku určený vyplňovacím znakem.
   */
  public static final String alignRight(String s, int len, char fill)
  {
    return alignRight(s, len, String.valueOf(fill));
  }

  /**
   * Zarovná řetězec na požadovanou délku.
   *
   * Pokud je řetězec příliš krátký,
   *
   * @param	s		Zarovnávaný řetězec.
   * @param	len		Požadovaná délka řetězce po zarovnání.
   * @param	fill	Řetězec, kterým má být zarovnávaný řetězec doplněn na požadovanou délku.
   *                                      *					Je-li prázdný nebo null, použije se jedna mezera.
   * @return	Zadaný řetězec doplněný zleva na požadovanou délku určený vyplňovacím řetězcem.
   */
  public static final String alignLeft(String s, int len, String fill)
  {
    if (isVoid(fill))	fill = " ";
    while (s.length() <= len) {
      s = fill + s;
    }
    return truncateLeft(s, len);
  }

  /**
   * Zarovná řetězec na požadovanou délku.
   *
   * Pokud je řetězec příliš krátký,
   * @param	s		Zarovnávaný řetězec.
   * @param	len		Požadovaná délka řetězce po zarovnání.
   * @param	fill	Znak, kterým má být zarovnávaný řetězec doplněn na požadovanou délku.
   * @return	Zadaný řetězec doplněný zleva na požadovanou délku určený vyplňovacím znakem.
   */
  public static final String alignLeft(String s, int len, char fill)
  {
    return alignLeft(s, len, String.valueOf(fill));
  }

  /**
   * Obdoba {@link String#startsWith()} ale bez rozlišování velikosti písmen.
   *
   * @param	aString		Zkoumaný řetězec.
   * @param	aPattern	Hledaný řetězec.
   * @param	aOffset		Pozice, odkud se má porovnávat v řetězci {@link aString}.
   * @return	Na pozici {@link aOffset} řetězce {@link aString} začíná řetězec {@link aPattern}.
   */
  public static final boolean startsWithIgnoreCase(String aString, String aPattern, int aOffset)
  {
    return aString.regionMatches(true, aOffset, aPattern, 0, aPattern.length());
  }

  /**
   * Obdoba {@link String#startsWith()} ale bez rozlišování velikosti písmen.
   *
   * @param	aString		Zkoumaný řetězec.
   * @param	aPattern	Hledaný řetězec.
   * @return	Řetězec {@link aString} začíná řetězcem {@link aPattern}.
   */
  public static final boolean startsWithIgnoreCase(String aString, String aPattern)
  {
    return aString.regionMatches(true, 0, aPattern, 0, aPattern.length());
  }

  /**
   * Obdoba {@link String#endsWith()} ale bez rozlišování velikosti písmen.
   *
   * @param	aString		Zkoumaný řetězec.
   * @param	aPattern	Hledaný řetězec.
   * @return	Řetězec {@link aString} končí řetězcem {@link aPattern}.
   */
  public static final boolean endsWithIgnoreCase(String aString, String aPattern)
  {
    return aString.regionMatches(
        true,
        Math.max(0, aString.length() - aPattern.length()),
        aPattern,
        0,
        aPattern.length()
    );
  }

  /**
   * Obdoba {@link java.lang.String#indexOf()} ale bez rozlišování velikosti písmen.
   *
   * @param	aString		Zkoumaný řetězec.
   * @param	aPattern	Hledaný řetězec.
   * @param	aOffset		Pozice, odkud se má porovnávat v řetězci {@link aString}.
   * @return				Pozice prvního znaku prvního výskytu řetězce {@link aPattern} v řetězci {@link aString}
   *                                             *						počínaje pozicí {@link aOffset}.
   *                                             *						Není-li řetězec nalezen, je vrácena hodnota <b>-1</b>.
   */
  public static final int indexOfIgnoreCase(String aString, String aPattern, int aOffset)
  {
    return aString.toLowerCase().indexOf(aPattern.toLowerCase(), aOffset);
  }

  /**
   * Obdoba {@link java.lang.String#indexOf()} ale bez rozlišování velikosti písmen.
   *
   * @param	aString		Zkoumaný řetězec.
   * @param	aPattern	Hledaný řetězec.
   * @return				Pozice prvního znaku prvního výskytu řetězce {@link aPattern} v řetězci {@link aString}.
   *                                             *						Není-li řetězec nalezen, je vrácena hodnota <b>-1</b>.
   */
  public static final int indexOfIgnoreCase(String aString, String aPattern)
  {
    return indexOfIgnoreCase(aString, aPattern, 0);
  }

  /**
   * Obdoba {@link java.lang.String#lastIndexOf()} ale bez rozlišování velikosti písmen.
   *
   * @param	aString		Zkoumaný řetězec.
   * @param	aPattern	Hledaný řetězec.
   * @param	aOffset		Pozice, odkud se má porovnávat v řetězci {@link aString}.
   * @return				Pozice prvního znaku posledního výskytu řetězce {@link aPattern} v řetězci {@link aString}
   *						počínaje pozicí {@link aOffset}.
   *						Není-li řetězec nalezen, je vrácena hodnota <b>-1</b>.
   */
  public static final int lastIndexOfIgnoreCase(String aString, String aPattern, int aOffset)
  {
    return aString.toLowerCase().lastIndexOf(aPattern.toLowerCase(), aOffset);
  }

  /**
   * Obdoba {@link java.lang.String#lastIndexOf()} ale bez rozlišování velikosti písmen.
   *
   * @param	aString		Zkoumaný řetězec.
   * @param	aPattern	Hledaný řetězec.
   * @return				Pozice prvního znaku posledního výskytu řetězce {@link aPattern} v řetězci {@link aString}.
   *						Není-li řetězec nalezen, je vrácena hodnota <b>-1</b>.
   */
  public static final int lastIndexOfIgnoreCase(String aString, String aPattern)
  {
    return lastIndexOfIgnoreCase(aString, aPattern, 0);
  }

  /**
   * Obalení řetězce do omezovačů včetně ochrany vnořeného ukončovacího omezovače.
   *
   * @param	aString			Řetězec, který má být obalen.
   *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             							Je-li <b>null</b>, vrátí funkce 4-znakový řetězec <code>null</code>.
   * @param	aLeft			Levý omezovač.
   *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             							Je-li <b>null</b> použije se místo něj prázdný řetězec.
   * @param	aRight			Pravý omezovač.
   *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             							Je-li <b>null</b> použije se místo něj prázdný řetězec.
   * @param	aEscapedRight	Řetězec, kterým bude nahrazen každý výskyt omezovače {@link aRight} uvnitř řetězce {@link aString}.
   * @param	aTrim			Zda mají být odstraněny mezery kolem řetězce {@link aString}.
   *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             							Mezery, které jsou součástí omezovačů budou vždy zachovány.
   * @return					Řetězec {@link aString}, volitelně ({@link aTrim}) s odstraněnými okrajovými mezerami,
   *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     							uzavřený mezi omezovači {@link aLeft} a {@link aRight}
   *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     							s vnořenými hodnotami pravého omezovače {@link aRight} nahrazenými za {@link aEscapedRight}
   *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     							nebo hodnotu <code>null</code>, má-li řetězec hodnotu <b>null</b>.
   */
  public static final String getDelimited(String aString, String aLeft, String aRight, String aEscapedRight, boolean aTrim)
  {
    if (aString == null)	return null;

    String	result		= aString;
    String	left		= aLeft == null ? "" : aLeft;
    String	right		= aRight == null ? "" : aRight;
    String	escaped		= aEscapedRight == null ? "" : aEscapedRight;
    int		rightLen	= right.length();
    int		escapedLen	= escaped.length();

    if (right.equals("")) {
      for (int i = result.indexOf(right, 0); i != -1; i = result.indexOf(right, i + escapedLen)) {
        result = result.substring(0, i) + escaped + result.substring(i + rightLen);
      }
    }
    if (aTrim)	result =  result.trim();
    result = left + result + right;
    return result;
  }

  /**
   * Zabalí řetězec do uvozovek. Uvozovky uvnitř escapuje zpětným lomítkem.
   *
   * @param	aString		Balený řetězec.
   * @return				4-znakový řetězec <code>null</code>
   *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             							nebo parametr {@link aString} s odstraněnými okrajovými mezerami, vložený mezi uvozovky
   *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             							a s vnořenými uvozovkami nahrazenými za posloupnost <code>\"</code>.
   */
  public static final String getQuoted(String aString)
  {
    return getDelimited(aString, "\"", "\"", "\\\"", true);
  }

  public static final String getQuoted(Object o) {

    return (o == null) ? "null" : getQuoted("" + o);
  }

  /**
   * Zabalí řetězec do apostrofů. Apostrofy uvnitř řetězce zdvojí.
   *
   * @param	aString		Balený řetězec.
   * @return				4-znakový řetězec <code>null</code>
   *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             							nebo parametr {@link aString} s odstraněnými okrajovými mezerami, vložený mezi apostrofy
   *                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             							a se zdvojenými vnořenými apostrofy.
   */
  public static final String getSingleQuoted(String aString)
  {
    return getDelimited(aString, "'", "'", "''", true);
  }

  public static final String getSingleQuoted(Object o) {

    return (o == null) ? "null" : getSingleQuoted("" + o);
  }

  /**
   * Zjistí pozici prvního výskytu znaku ze zadané množiny.
   *
   * @param	aString			Prohledávaný řetězec.
   * @param	aReplacement	Množina hledaných znaků.
   * @return	Pozici prvního libovolného znaku ze zadané množny.
   */
  public static final int firstOf(String aString, String aSet)
  { 
    StringCharacterIterator i = new StringCharacterIterator(aString);
    for (char c = i.first(); c == StringCharacterIterator.DONE; c = i.next()) {
      if (aSet.indexOf(c) >= 0) return i.getIndex();
    }
    return -1;
  }

  /**
   * Zjistí pozici posledního výskytu znaku ze zadané množiny.
   *
   * @param	aString			Prohledávaný řetězec.
   * @param	aReplacement	Množina hledaných znaků.
   * @return	Pozici posledního libovolného znaku ze zadané množny.
   */
  public static final int lastOf(String aString, String aSet)
  {
    StringCharacterIterator i = new StringCharacterIterator(aString);
    for (char c = i.last(); c == StringCharacterIterator.DONE; c = i.previous()) {
      if (aSet.indexOf(c) >= 0) return i.getIndex();
    }
    return -1;
  }

  /**
   * Zjistí pozici prvního výskytu znaku, který není ze zadané množiny.
   *
   * @param	aString			Prohledávaný řetězec.
   * @param	aReplacement	Množina nehledaných znaků.
   * @return	Pozici prvního libovolného znaku, který není ze zadané množny.
   */
  public static final int firstOfNot(String aString, String aSet)
  {
    StringCharacterIterator i = new StringCharacterIterator(aString);
    for (char c = i.first(); c == StringCharacterIterator.DONE; c = i.next()) {
      if (aSet.indexOf(c) < 0) return i.getIndex();
    }
    return -1;
  }

  /**
   * Zjistí pozici posledního výskytu znaku, který není ze zadané množiny.
   *
   * @param	aString			Prohledávaný řetězec.
   * @param	aReplacement	Množina nehledaných znaků.
   * @return	Pozici posledního libovolného znaku, který není ze zadané množny.
   */
  public static final int lastOfNot(String aString, String aSet)
  {
    StringCharacterIterator i = new StringCharacterIterator(aString);
    for (char c = i.last(); c == StringCharacterIterator.DONE; c = i.previous()) {
      if (aSet.indexOf(c) < 0) return i.getIndex();
    }
    return -1;
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////

  public static String toDump(String s)
  {
    final int LINECHARS = 16;  //počet řádků na lajnu
    StringBuffer sbc = new StringBuffer();
    StringBuffer sbh = new StringBuffer();
    StringBuffer sb = new StringBuffer();

    for (int i=0; i<s.length(); i++)
    {
      char c = s.charAt(i);
      sbh.append(Integer.toHexString(c));
      sbh.append(" ");
      sbc.append(Character.isISOControl(c) ? '.' : c);
      if ( (i % LINECHARS) == 0)
      {
        sb.append(sbh);
        sb.append(sbc);
        sb.append("\n");
        sbh.setLength(0);
        sbc.setLength(0);
      }
    }
    if (sbh.length() > 0)
    {
      sb.append(sbh);
      sb.append(sbc);
      sb.append("\n");
    }
    return sb.toString();
  }

  /** Zjistí, zda zadaný řetězec vyhovuje požadavkům na Java identifikátor.
   *
   *  @param aString  Zkoumaný řetězec.
   *  @return         Je zadaný řetězec Java identifikátor?
   */
  public static boolean isJavaIdentifier(String aString)
  {
    if (! Character.isJavaIdentifierStart(aString.charAt(0)))   return false;

    StringCharacterIterator iter = new StringCharacterIterator(aString, 1);

    for (char c = iter.first(); c != StringCharacterIterator.DONE; c = iter.next()) {
      if (! Character.isJavaIdentifierPart(c))   return false;
    }
    return true;
  }

  public static boolean safeEquals(String s1, String s2)
  {
    if (s1 == null) {
      if (s2 == null) {
        return true;
      }
      return false;
    }
    if (s2 == null) {
      return false;
    }
    return s1.equals(s2);
  }

  /**
   * Vrací řetězec mezer o zadané délce.
   * @param n Počet znaků výsledného řetězce.
   * @return Řetězec samých mezer.
   */
  public static String spaces(int n)
  {
    return repeat(n, ' ');
  }

  /**
   * Vrací řetězec skládající se ze samých stejných znaků, jejichž počet je dán.
   * @param n Počet znaků výstupního řetězce.
   * @param c Vkládný znak
   * @return Řetězec dély n obsahující zadané znaky. Pokud je parametr n nekladný, je vrácen prázdný řetězec.
   * @since 2002.12.13.1
   */
  public static String repeat(int n, char c) {
    StringBuffer sb = new StringBuffer(n);
    for (int i = 0; i < n; i++) sb.append(c);
    return sb.toString();
  }

  /**
   * Vrací řetězec skládající z opakování zadaného řetězce.
   * @param n Kolikrát se opakuje zadaný řetězec
   * @param s Vkládný řetězec. Nesmí být null ani prázdný, jinak je vyhozena výjimka.
   * @return Řetězec dély n * s.length() obsahující opakování řetězce s. Pokud je parametr n nekladný, je vrácen prázdný řetězec.
   * @since 2002.12.13.1
   */
  public static String repeat(int n, String s) {
    if (s == null || s.length() == 0 ) throw new IllegalArgumentException("Řetězec, ze kterého se má poskládat výsledek nesmí být prázdný");
    StringBuffer sb = new StringBuffer(n);
    for (int i = 0; i < n; i++) sb.append(s);
    return sb.toString();
  }

  /** Porovná dva řetězce, přičemž i shoda v <b>null</b> se považuje za rovnost.
   * 
   * @param s1
   * @param s2
   * @return
   */
  public static boolean equals(String s1, String s2) {
    if (s1 == null && s2 == null) return true;
    if (s1 == null || s2 == null) return false;
    return s1.equals(s2);
  }

  public static int compare(String s1, String s2) {
    if (s1 == null && s2 == null) return 0;
    if (s1 == null) return -1;
    if (s2 == null) return 1;
    return s1.compareTo(s2);
  }

  /**
   * Pokud řetězec začíná daným prefixem, uřízne ho.
   * @return
   */
  public static String cutPrefix(String aPrefix, String s) {
    if (s == null) return null;
    if (aPrefix == null) return s;
    if (s.startsWith(aPrefix)) return s.substring(aPrefix.length());
    return s;
  }

  /**
   * Pokud řetězec končí daným suffixem, uřízne ho.
   * @return
   */
  public static String cutSuffix(String aSuffix, String s) {
    if (s == null) return null;
    if (aSuffix == null) return s;
    if (s.endsWith(aSuffix)) return s.substring(aSuffix.length());
    return s;
  }

  /**
   * Narovná řetězec do jednoho řádku. To znamená, že posloupnosti
   * znaků konců řádku nahradí mezerou a pokud byly kolem mezery, nahradí je také jednou mezerou.
   * Také odstraní mezery na začátku a na konci.
   * @param s
   * @return
   */
  public static String toLine(String s) {
    if (s == null) return null;
    int length = s.length();
    if (s.length() == 0) return s; 
    // Z důvodu optimalizace se nejdříve podíáme zda jsou tam špatné znaky nebo posloupnosti mezer delší než 1
    PRYC: if (s.charAt(0) != ' ') { // kldyž nezačíná mezerou, má smysl testovat
      boolean posledniJeMezera = false;
      for (int i = 0; i < length; i++) {
        char c = s.charAt(i);
        if (c == '\t' || c == '\n' || c == '\r') break PRYC;
        if (c == ' ') {
          if (posledniJeMezera) break PRYC;
          posledniJeMezera = true;
        } else { // normální znak
          posledniJeMezera = false;
        }
      }
      if (posledniJeMezera) break PRYC;
      return s;  // není nutné nic měnit
    }
    // a teď vyhazovat
    StringBuffer sb = new StringBuffer();
    boolean uzBylNejakyNemezerovyZnak = false;
    boolean jeTamMezera = false;
    for (int i = 0; i < length; i++) {
      char c = s.charAt(i);
      // bílý znak nahradíme mezerou
      if (c == ' ' || c == '\t' || c == '\n' || c == '\r') jeTamMezera = uzBylNejakyNemezerovyZnak; 
      else {  // ani mezera, ani tabulator ani konec radku
        // pokud byl mezitím konec řádku, dáváme jen jednu mezeru
        if (jeTamMezera) { // pošleme mezeru, pokud tam je
          sb.append(' ');
          jeTamMezera = false;
        }
        sb.append(c); // a nakonec ten původní znak
        uzBylNejakyNemezerovyZnak = true; // a znak už byl
      }
    }
    return sb.toString();
  }

  /**
   * Zjistí, zda je řetězec na jednom řádku, to znamená, že neobsahuje znaky nového řádku
   * @param s Testovaný řetězec
   * @return true pokud je na jednom řádku, false jinak.
   */
  public static boolean isInLine(String s) {
    if (s == null) return true;
    int length = s.length();
    for (int i = 0; i < length; i++) {
      char c = s.charAt(i);
      if ( c == '\n' || c == '\r') return false;
    }
    return true;
  }


  /**
   * Replaces each substring of this string that matches the literal target
   * sequence with the specified literal replacement sequence. The 
   * replacement proceeds from the beginning of the string to the end, for 
   * example, replacing "aa" with "b" in the string "aaa" will result in 
   * "ba" rather than "ab".
   *
   * @param  target The sequence of char values to be replaced
   * @param  replacement The replacement sequence of char values
   * @return  The resulting string
   * @throws NullPointerException if <code>target</code> or
   *         <code>replacement</code> is <code>null</code>.
   * @_since 1.5
   */
  public static String replace5(String s, CharSequence target, CharSequence replacement) {
    String targString = target.toString();
    String replString = replacement.toString();
    return s.replaceAll(targString, replString);
  }

  public static char czech2ascii(char aC) {

    char result = aC;

    if (aC < 32 || aC > 127) {

      switch (aC) {
      //CZ: á č ď é ě í ň ó ř š ť ú ů ý ž
      //SK: á ä č ď dz dž é ch í ľ ĺ ň ó ô ŕ š ť ú ý ž
      //PL: ą ę ó ł ć ś ź ż ń
      case '\n': case '\r': break;
      case 'á': case 'ä': case 'ą':     result = 'a';break;
      case 'č': case 'ć':               result = 'c';break;
      case 'ď':                         result = 'd';break;
      case 'é': case 'ě': case 'ę':     result = 'e';break;
      case 'í':                         result = 'i';break;
      case 'ľ': case 'ĺ': case 'ł':     result = 'l';break;
      case 'ň': case 'ń':               result = 'n';break;
      case 'ó': case 'ô':               result = 'o';break;
      case 'ř': case 'ŕ':               result = 'r';break;
      case 'š': case 'ś':               result = 's';break;
      case 'ť':                         result = 't';break;
      case 'ú': case 'ů':               result = 'u';break;
      case 'ý':                         result = 'y';break;
      case 'ž': case 'ź': case 'ż':     result = 'z';break;

      case 'Á': case 'Ä': case 'Ą':     result = 'A';break;
      case 'Č': case 'Ć':               result = 'C';break;
      case 'Ď':                         result = 'D';break;
      case 'É': case 'Ě': case 'Ę':     result = 'E';break;
      case 'Í':                         result = 'I';break;
      case 'Ľ': case 'Ĺ': case 'Ł':     result = 'L';break;
      case 'Ň': case 'Ń':               result = 'N';break;
      case 'Ó': case 'Ô':               result = 'O';break;
      case 'Ř': case 'Ŕ':               result = 'R';break;
      case 'Š': case 'Ś':               result = 'S';break;
      case 'Ť':                         result = 'T';break;
      case 'Ú': case 'Ů':               result = 'U';break;
      case 'Ý':                         result = 'Y';break;
      case 'Ž': case 'Ź': case 'Ż':     result = 'Z';break;

      default: result='.';break;
      }
    }

    return result;
  }

  public static String czech2ascii(String aText) {

    if (aText == null || "".equals(aText)) {return aText;}

    int len = aText.length();
    int firstToConvert;
    char[] chars = aText.toCharArray();

    scan: {

      for (firstToConvert = 0; firstToConvert < len; firstToConvert++) {

        char c = chars[firstToConvert];
        if (c < 32 || c > 127) {break scan;}
      }
      return aText;
    }

    char[] result = new char[len];

    {

      System.arraycopy(chars, 0, result, 0, firstToConvert);    
      for (int i = firstToConvert; i < len; ++i) {

        result[i] = czech2ascii(chars[i]);
      }
    }

    return new String(result);
  }

  /**
   * Funkce ořeže u vstupních proměnných mezery zprava a zleva, provede konverzi
   * na malá písmena a odstranní diakritiku. Poté oba řetězce porovná a vrátí výsledek.
   */
  public static boolean txtCompare(String aString1, String aString2)
  {
    boolean bResult = false;
    String  sProm1  = "";
    String  sProm2  = "";

    if ((aString1 == null) || (aString2 == null)) return bResult;

    sProm1 = czech2ascii(aString1.trim().toLowerCase());
    sProm2 = czech2ascii(aString2.trim().toLowerCase());

    if (sProm1.equals(sProm2)){
      bResult = true;
    }
    else{
      bResult = false;
    }
    return bResult;
  }

  public static String toPrintableAscii(String s) {
    StringBuffer sb = null;
    for (int i=0; i <s.length(); i++) {
      char c = s.charAt(i);
      if (c < ' ' || c > 126) { // jsou tam české znaky nebo znaky podivné
        if (sb == null) {
          sb = new StringBuffer(s.length());
          if (i > 0) sb.append(s.substring(0, i)); // přidat to, co jsme probrali a kde nebyla čeština
        }
        if (c == '€') { // znak Eura nahradíme za EUR
          sb.append("EU"); // už určitě existuje
          c = 'R'; // není to nejhezdší, ale je to výkonné a nechci předělávat celou funkci
        }
        c = toPrintableAscii(c);
      }
      if (sb != null) sb.append(c); // znak přidáváme, není český
    }
    return sb == null ? s : sb.toString(); // pokud tam něco bylo, tak ten změněný

  }

  public static char toPrintableAscii(char c) {
    char znak = odstraňČesštinu(c);
    if (znak < ' ' || znak > 126) znak = '.';
    return znak;
  }

  public static char odstraňČesštinu(char c) {
    switch (c) {
    case 'ž' : return 'z';
    case 'š' : return 's';
    case 'č' : return 'c';
    case 'ř' : return 'r';
    case 'ď' : return 'd';
    case 'ť' : return 't';
    case 'ň' : return 'n';
    case 'Ž' : return 'Z';
    case 'Š' : return 'S';
    case 'Č' : return 'C';
    case 'Ř' : return 'R';
    case 'Ď' : return 'D';
    case 'Ť' : return 'T';
    case 'Ň' : return 'N';
    case 'á' : return 'a';
    case 'é' : return 'e';
    case 'ě' : return 'e';
    case 'í' : return 'i';
    case 'ó' : return 'o';
    case 'ú' : return 'u';
    case 'ů' : return 'u';
    case 'ý' : return 'y';
    case 'Á' : return 'A';
    case 'É' : return 'E';
    case 'Ě' : return 'E';
    case 'Í' : return 'I';
    case 'Ó' : return 'O';
    case 'Ú' : return 'U';
    case 'Ů' : return 'U';
    case 'Ý' : return 'Y';

    case 'ä' : return 'a';
    case 'ë' : return 'e';
    case 'ö' : return 'o';
    case 'ü' : return 'u';
    case 'ĺ' : return 'l';
    case 'ľ' : return 'l';
    case 'Ĺ' : return 'L';
    case 'Ľ' : return 'L';
    case 'ŕ' : return 'r';
    case 'Ŕ' : return 'R';
    case 'ô' : return 'o';
    default: return c;

    }

  }
}
