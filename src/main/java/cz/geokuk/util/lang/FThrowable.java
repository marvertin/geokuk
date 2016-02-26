package cz.geokuk.util.lang;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import cz.geokuk.util.file.LineWrappingDecorationWriter;


/**
 * Třída je pomocnou třídou pro {@link XRuntime}, {@link XObject0} a jejich následníky.
 * Obsahuje pouze statické metody, neinstanciuje se. Takle blbě je to uděláno proto,
 * že java neobsahuje vícenásobnou dědičnost a také proto, že v předkovi Throwable
 * není vše, co by tam mělo být. unkcionalita je podobná jako u com.ms.wfc.core.WFCException,
 * ale zahrnuje také errory a nemá tolik kosntruktorů, aby to nesvádělo ke zjednodušování
 * práce na úkor diagnistikovatelnosti.
 */
public final class FThrowable {
  private FThrowable() { /* Nikdo z venku nesmí tvořit instance. */ }

  private static int sČítačVypisovačů = 0;
  private static final Map<Throwable, Integer> sVýjimkaNaČíslo = new WeakHashMap<>();

  public static int getExceptionNumber(Throwable aTh) {
    if (aTh == null) return 0;
    // získat řetěz výjimek
    ThrowableAndSourceMethod[] throwableChain = getThrowableChain(aTh);
    assert throwableChain.length > 0; // protože nějakou výjimku máme, tak musí být i včejnu
    // a vzít číslo té poslední, protože to je jádro pudla, protože to je jádro pudla, to je ta pravá
    // příčina všeho a dle ní se výjimky musejí číslovat
    Throwable th = throwableChain[throwableChain.length - 1].getThrowable();

    Integer číslo = sVýjimkaNaČíslo.get(th);
    if (číslo == null) {
      číslo = ++sČítačVypisovačů;
      sVýjimkaNaČíslo.put(th, číslo);
    }
    return číslo;
  }

  /**
   * Vrací řetězec výjimek.
   *
   * @param thr Výjimka, od nějž se má řetězec odvíjet.
   * @return Pole obsahující řetězec výjimek a zdojových metod. Pod indexem nula
   * je nejvyšší výjimka, tedy výjimka předaná jako parameter a metoda
   * je null.
   */
  public static ThrowableAndSourceMethod[] getThrowableChain(Throwable thr) {
    if (thr == null) return new ThrowableAndSourceMethod[0]; // prázdné pole, pokud žádná výjimka není.
    ThrowableAndSourceMethod[] vazy = ThrowableChainPicker.from(thr).poskládejŘetězVýjimek();
    return vazy;
  }

  /** Vrátí výjimku přehozenou přes zadanou výjimku,
   * typ výjimky vrátí co možná nejbližší nahoru k přehazované výjimce.
   * Pokud žádný rozumný typ nenalezne, přehazuje XRuntime.
   * @param s Technicky orientovaná zpráva. Řetězec se stane technickou zprávou nově vytvořené výjimky.
   * @parem exc Přehazovaná výjimka
   * @return Přehozená výjimka, přinejhorším XRuntime.
   */

  /**
   * Konstruuje řetězec přidáním třídy.
   */
  static String _constructClassString(Object c, String s) {
    if (c == null) {
      return s;
    } else if (c instanceof Class<?>) {
      return "[" + ((Class<?>) c).getName() + "] " + s;
    } else {
      String ss;
      // při problémech s toString() vyhodit bez toho
      try {
        ss = c.toString();
      } catch (RuntimeException e) {
        ss = "problém s toString()";
      }
      return "[" + c.getClass().getName() + "] " + ss + " = " + s;
    }
  }

  /**
   * @deprecated Použij findThrowableType;
   */
  @Deprecated
  public static Exception findExceptionType(Throwable thr, Class<? extends Throwable> aExcType) {
    return (Exception) findThrowableType(thr, aExcType);
  }

  /**
   * Vyhledání konkrétní výjimky v seznamu výjimek. Hledá první výjimku,
   * která je zadaná nebo její následník. Vrací null, pokud se taková výjimka nenajde.
   * Může vrátit i kořen výjimek, pokud je to již ona.
   */
  public static Throwable findThrowableType(Throwable thr, Class<? extends Throwable> aExcType) {
    if (!Throwable.class.isAssignableFrom(aExcType))
      throw new XRuntime("findExceptionType: třída " + aExcType + " není výjimka");
    ThrowableAndSourceMethod[] vazy = getThrowableChain(thr);
    for (ThrowableAndSourceMethod vaz : vazy) {
      if (aExcType.isAssignableFrom(vaz.iThrowable.getClass()))
        return vaz.iThrowable; // aby se zabránilo cyklů, pokudb by nějaká vjimka odkazovala na sebe
    }
    return null; // nic se nenašlo
  }

  /**
   * @param aStream
   */
  public static String printStackTrace(Throwable th, PrintStream aStream, String aPrefix) {
    PrintWriter pwrt = new PrintWriter(aStream, false);
    String s = printStackTrace(th, pwrt, aPrefix);
    pwrt.flush();
    return s;
  }

  public static String printStackTrace(Throwable th, PrintWriter aStream, String aPrefix) {
    return new ExceptionPrinter().printStackTrace(th, aStream, aPrefix);
  }

  public static String printStackTraceHtml(Throwable th, PrintWriter aStream, String aPrefix) {
    return new ExceptionPrinterHtml().printStackTrace(th, aStream, aPrefix);
  }

  /////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////

  private static class ExceptionPrinterHtml extends ExceptionPrinter {
    @Override
    protected void printlnExceptionMessage(PrintWriter wrt, String message) {
      wrt.print("<span style='color: red'>");
      wrt.print(message);
      wrt.println("</span>");
    }

    @Override
    protected void printExceptionNestingNumber(PrintWriter wrt, String nestingNumber) {
      wrt.print("<span style='color: green'>" + nestingNumber + "</span>");
    }

    @Override
    protected void printZdrojMetoda(PrintWriter wrt, Method zdroj) {
      wrt.print("<span style='color: darkmagenta'>" + zdroj.getName() + "()" + "</span>");
    }

    @Override
    protected void printExceptionClassName(PrintWriter wrt, String aClassName) {
      wrt.print("<span style='color: blue'>" + aClassName + "</span>");
    }

    @Override
    protected void printIdentityHashCode(PrintWriter wrt, int aIdentityHashCode) {
      wrt.print("<small>" + aIdentityHashCode + "</small>");
    }
  }

  private static class ExceptionPrinter {
    private void _printStackTrace(Throwable thr, PrintWriter wrt) {
      //System.err.p rintln("======================ZZZZZZZZZZZZZZZZ=================");
      //thr.printStackTrace();
      //System.err.p rintln("======================KKKKKKKKKKKKK=================");
      boolean vypisujJmenoMetody = true;
      synchronized (wrt) {
        ThrowableAndSourceMethod[] vazy = ThrowableChainPicker.from(thr).poskládejŘetězVýjimek();
        Set<String> vypsanci = new HashSet<>(); // poznání co se vypsalo, aby se dalo označit hvězdičkou
        for (int i = 0; i < vazy.length; i++) {
          ThrowableAndSourceMethod vaz = vazy[i];
          if (!(vaz.iThrowable instanceof XTopRenderingException)) { // Není to ta renderovací pseudovýjimka
            printExceptionNestingNumber(wrt, i + "/" + (vazy.length - 1) + " ");
            if (vypisujJmenoMetody && vaz.iSourceMethod != null) {
              printZdrojMetoda(wrt, vaz.iSourceMethod);
              wrt.print(": ");
            }
            vypisujJmenoMetody = true;
            printExceptionClassName(wrt, vaz.iThrowable.getClass().getName() + " ");
            printIdentityHashCode(wrt, System.identityHashCode(vaz.iThrowable));
            printlnExceptionMessage(wrt, buildThrowableMessageWithoutExceptionName(vaz.iThrowable));
          } else {  // je to rendrovací, tak musím zabýt výpis jména metody zjišťující výjimku dále ve steku
            vypisujJmenoMetody = false;
          }

          StackTraceElement[] trace = vaz.iThrowable.getStackTrace();
          int shodnych;
          if (i + 1 < vazy.length) {  // pokud není tento stek stekem posledním
            StackTraceElement[] nextTrace = vazy[i + 1].iThrowable.getStackTrace();
            shodnych = compareStackTraces(trace, nextTrace);
          } else {
            shodnych = 0;
          }
          printOnlyStackTrace(wrt, trace, shodnych, i, vypsanci);
        }
      }
    }


    protected void printlnExceptionMessage(PrintWriter wrt, String message) {
      wrt.println(message);
    }


    protected void printExceptionNestingNumber(PrintWriter wrt, String nestingNumber) {
      wrt.print(nestingNumber);
    }


    protected void printZdrojMetoda(PrintWriter wrt, Method zdroj) {
      wrt.print(zdroj == null ? "<unknown method>" : (zdroj.getName() + "()"));
    }

    protected void printExceptionClassName(PrintWriter wrt, String aClassName) {
      wrt.print(aClassName);
    }

    protected void printIdentityHashCode(PrintWriter wrt, int aIdentityHashCode) {
      wrt.print('#');
      wrt.print(aIdentityHashCode);
    }


    public String printStackTrace(Throwable th, PrintWriter s, String aPrefix) {
      try {
        LineWrappingDecorationWriter wrt = new LineWrappingDecorationWriter(s);
        PrintWriter pwrt = new PrintWriter(wrt);
        String pfx = "EXC-" + getExceptionNumber(th) + "-" + (aPrefix == null ? "" : aPrefix);
        wrt.setPrefix(pfx + ": ");
        pwrt.println("Exception printed at " + ATimestamp.now().toIsoStringLocal());
        //StackTraceElement trace = inferCaller(FThrowable.class);
        //if (trace != null) {
        //  pwrt.println("Exception printed at " + trace);
        //}
        _printStackTrace(new XTopRenderingException(th), pwrt);
        pwrt.flush();
        return pfx;
      } catch (Throwable e) {
        // Tyhle výpisy vypadají jak vypadají a jsou už dost dobře zušlechtěny.
        // Reagují na situaci, kdy z nějakého neznámého důvodu selže poměrně dost komplikované
        // sestavení výjimky, pak se co nejjednoduššími prostředky vypisuje jak výjimka selhání, tak
        // vlastní výjimka, která se měla sestavit. Pokud je kód na sestavení výpisu dobře
        // nebude se zde nic vypisovat.
        System.err.println("!!!!!!!! EXCEPTION WHILE PRINTING EXCEPTION - START !!!!!!!!!!!!!!");
        e.printStackTrace();
        System.err.println("!!!!!!!! ORIGINAL EXCEPTION IS !!!!!!!!!!!!!!");
        th.printStackTrace();
        System.err.println("!!!!!!!! EXCEPTION WHILE PRINTING EXCEPTION - END !!!!!!!!!!!!!!");
        if (e instanceof ThreadDeath) throw (ThreadDeath) e;
        return "?!?"; // opravdu nevím co vrátit, ale to je asi jedno
      }
    }

    /**
     * Vypíše čistě kus zásobníku.
     *
     * @param pwrt             Kam vypisovat
     * @param trace            Zásobník na pypsání
     * @param aKolikNezobrazit Kolik řádků nemá být zobrazeno, pokud je nula, zobrazí se vše
     * @param aPoradoveCislo   Pořadové číslo řetězené výjimky, které se také vypíše.
     */
    private void printOnlyStackTrace(PrintWriter pwrt, StackTraceElement[] trace, int aKolikNezobrazit, int aPoradoveCislo, Set<String> aVypsanci) {
      if (aKolikNezobrazit < 0)
        aKolikNezobrazit = 0;
      if (aKolikNezobrazit >= trace.length && trace.length > 0) aKolikNezobrazit = trace.length - 1;
      if (trace == null || trace.length - aKolikNezobrazit <= 0)
        return;
      for (int i = 0; i < trace.length - aKolikNezobrazit; i++) {
        String vypsanec = trace[i] == null ? "neznamy" : (trace[i].getClassName() + "." + trace[i].getMethodName() + "-" + (trace.length - i));
        pwrt.println("    "
            + (aPoradoveCislo == 0 ? "render" : Integer.toString(aPoradoveCislo))
            + "." + (trace.length - i)
            + (aVypsanci.contains(vypsanec) ? " * " : " - ")
            + (trace[i] + "").replace('\t', ' '));
        aVypsanci.add(vypsanec);
      }
      if (aKolikNezobrazit > 0) {
        pwrt.println("       ... + " + aKolikNezobrazit + " hereafter");
      }
    }

    /**
     * Porovná dva zásobníky a vrátí počet shodných položek.
     *
     * @param aTrace1
     * @param aTrace2
     * @return
     */
    private int compareStackTraces(StackTraceElement[] aTrace1, StackTraceElement[] aTrace2) {
      if (aTrace1 == null || aTrace2 == null)
        return 0; // nemají společnou část, pokud jsou null
      int m = aTrace1.length - 1, n = aTrace2.length - 1;
      while (m >= 0 && n >= 0 && aTrace1[m].equals(aTrace2[n])) {
        m--;
        n--;
      }
      int framesInCommon = aTrace1.length - 1 - m;
      return framesInCommon;
    }

    private static String buildThrowableMessageWithoutExceptionName(Throwable th) {
      String s;
      if (th instanceof SQLException) {
        SQLException e = (SQLException) th;
        s = e + "<b> SQLSTATE=" + e.getSQLState() + " ERRORCODE=" + e.getErrorCode() + "</b>";
      } else {
        s = th + "";
      }
      if (s.startsWith(th.getClass().getName())) { // toString nebyl přepsán a normálně přidává jméno výjimky
        int poz = th.getClass().getName().length();
        s = s.substring(poz);
      }
      return s;
    }
  }

  /**
   * Třída, která umí poskládat řetěz výjimek a to dost dobře, jede reflektem,
   * takže nevynechá žádnou metodu, umí se tedy přizpůsobit i starším třídám.
   *
   * @author veverka
   */
  private static class ThrowableChainPicker {
    private final Set<Throwable> iJizZarazeneVyjimkyx = new HashSet<>();
    private final Throwable iThr;
    

    /*
     * Sestrojí picker nad určitou výjimkou.
     */

    private ThrowableChainPicker(Throwable aThr) {
      iThr = aThr;
    }

    public static ThrowableChainPicker from(Throwable aThr) {
      return aThr == null ? null : new ThrowableChainPicker(aThr);
    }

    public ThrowableAndSourceMethod[] poskládejŘetězVýjimek() {
      iJizZarazeneVyjimkyx.clear();
      List<ThrowableAndSourceMethod> list = new ArrayList<>();
      poskládejŘetězVýjimek(list, iThr, null);
      return list.toArray(new ThrowableAndSourceMethod[list.size()]);
    }

    /**
     * Poslkládá řetěz výjimek rekurzivním přidáváním do předaného seznamu,
     *
     * @param aList Seznam, v němž skládáme řetěz.
     * @param th    Výjimka pro zařazení do řetězu.
     */
    private void poskládejŘetězVýjimek(List<ThrowableAndSourceMethod> aList, Throwable th, Method aZdrojMetoda) {
      if (iJizZarazeneVyjimkyx.contains(th))
        return; // tato výjimky již v řetězu je
      iJizZarazeneVyjimkyx.add(th);
      ThrowableAndSourceMethod vaz1x = new ThrowableAndSourceMethod();
      vaz1x.iThrowable = th;
      vaz1x.iSourceMethod = aZdrojMetoda;
      aList.add(vaz1x);
      ThrowableAndSourceMethod[] vazy = pickoutNestedThrowable(th);
      for (ThrowableAndSourceMethod vaz : vazy) {
        poskládejŘetězVýjimek(aList, vaz.iThrowable, vaz.iSourceMethod);
      }
    }

    /**
     * Zjistí všechny vnořené výjimky, jenž jsou vnořeny v této výjimce přímo,
     * nejde do hlouby. Může klidně vracet v poli vícekrát stejnou výjimku, pokud je výjika vracena více způsoby.
     *
     * @param thr
     * @return
     * @author veverka
     * @since 31.10.2006 11:04:27
     */
    private static ThrowableAndSourceMethod[] pickoutNestedThrowable(Throwable thr) {
      List<ThrowableAndSourceMethod> list = new ArrayList<>(3);
      // Standardní getCause() je nutno ověřit přednostně, aby se výjimky správně řadily, pokud někdo doplní getMostSpecificCause,
      // jak to s voblibó činí spring.
      if (thr.getCause() != null) {
        ThrowableAndSourceMethod vaz = new ThrowableAndSourceMethod();
        vaz.iThrowable = thr.getCause();
        try {
          vaz.iSourceMethod = Throwable.class.getMethod("getCause");
        } catch (NoSuchMethodException e) {
          // INTENTIONAL: budeme ticho, nehodí se vyhazovat výjimku, pokud nelze zjistit metodu, prostě metoda nebude
        }
        list.add(vaz);
      }
      Method[] mets = thr.getClass().getMethods();
      for (Method method : mets) {
        if (method.getParameterTypes().length != 0)
          continue; // metoda není bezparametrická
        if (Modifier.isStatic(method.getModifiers()))
          continue;
        if ("fillInStackTrace".equals(method.getName()))
          continue; // nesmíme volat tuto metodu
        if (!Throwable.class.isAssignableFrom(method.getReturnType())
            && !Throwable[].class.isAssignableFrom(method.getReturnType()))
          continue; // metoda nevrací throwable ani potomka
        // tak teď víme, že metoda vrací buď přímo výjimku nebo pole výjimek
        try {
          //System.out.p rintln("Volam metodu " + method.getName() + " na " + thr);
          Object vysledek = method.invoke(thr);
          if (vysledek instanceof Throwable) {
            ThrowableAndSourceMethod vaz = new ThrowableAndSourceMethod();
            vaz.iThrowable = (Throwable) vysledek;
            vaz.iSourceMethod = method;
            list.add(vaz);
          } else if (vysledek instanceof Throwable[]) {
            Throwable[] vyjimky = (Throwable[]) vysledek;
            for (Throwable aVyjimky : vyjimky) {
              ThrowableAndSourceMethod vaz = new ThrowableAndSourceMethod();
              vaz.iThrowable = aVyjimky;
              vaz.iSourceMethod = method;
              list.add(vaz);
            }
          }
          // a když to vrátí null, tak nedělám samozřejmě nic
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
          // INTENTIONAL: budeme ticho, nehodí se vyhazovat výjimku, pokud nelze zjistit příčinu.
        }
      }
      return list.toArray(new ThrowableAndSourceMethod[list.size()]);
    }

  }

  public static class ThrowableAndSourceMethod {
    // Vnořená výjimka
    Throwable iThrowable;
    // Jméno metody, která nás pouští k vnořené výjimkce
    Method iSourceMethod;

    /**
     * @return Returns the sourceMethod. metoda, pomocí které se z výjimky získala tato podvýjimka.,
     */
    public Method getSourceMethod() {
      return iSourceMethod;
    }

    /**
     * @return Returns the throwable. Získaná podvíjimka.
     */
    public Throwable getThrowable() {
      return iThrowable;
    }
  }

  private static class XTopRenderingException extends Exception {
    static final long serialVersionUID = 4670301709951704844L;

    public XTopRenderingException(Throwable aCause) {
      super("JO, to je vono", aCause);
    }
  }

  ////////////////////////////////////////////////////////ú
/// Testování výpisu výjimek
  private static class VyjimkovySimulovac {

    void metoda1() {
      prijmiZespoduAPrehod();
      //Object o = null;
      //o = o.getClass();  // vyhodí NullPointerException
    }

    void metoda2() throws InvocationTargetException {
      try {
        metoda1();
      } catch (RuntimeException e) {
        throw new InvocationTargetException(e, "Prvni prehozeni");
      }
    }


    void metoda3a() {
      try {
        metoda2();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    void metoda3b() {
      metoda3a();
    }

    void metoda3c() {
      metoda3b();
    }


    void metoda4() {
      try {
        metoda3c();
      } catch (RuntimeException e) {
        throw new RuntimeException("Treti prehozeni", e);
      }
    }

    void dolu1a(RuntimeException e) {
      throw new RuntimeException("Dole poprve", e);
    }

    void dolu1b(RuntimeException e) {
      try {
        dolu1a(e);
      } catch (RuntimeException ee) {
        throw new RuntimeException("Zespodu B", ee);
      }
    }

    void dolu1c(RuntimeException e) {
      try {
        dolu1b(e);
      } catch (RuntimeException ee) {
        throw new RuntimeException("Zespodu C", ee);
      }
    }


    void dolu2(RuntimeException e) {
      dolu1c(e);
    }

    void dolu3(RuntimeException e) {
      dolu2(e);
    }

    void dolu4(RuntimeException e) {
      dolu3(e);
    }

    void vyhodVyjimkuChytAPosliDolu() {
      try {
        metoda4();
      } catch (RuntimeException e) {
        dolu4(e);
      }
    }

    void uzKonecneVypisVyjimku(Throwable e) {
      FThrowable.printStackTrace(e, System.err, "ctxnav");

    }

    void vyhodJenzBudeChycena1() throws ParseException {
      throw new ParseException("Bude chcena a putovat nahoru", 555);

    }

    void vyhodJenzBudeChycena2() throws ParseException {
      vyhodJenzBudeChycena1();
    }

    Exception vyhodNedeDoleAPosliNahoru1() {
      try {
        vyhodJenzBudeChycena2();
        return null;
      } catch (ParseException e) {
        return e;
      }
    }

    Exception vyhodNedeDoleAPosliNahoru2() {
      return vyhodNedeDoleAPosliNahoru1();
    }

    Exception vyhodNedeDoleAPosliNahoru3() {
      return vyhodNedeDoleAPosliNahoru2();
    }

    void prijmiZespoduAPrehod() {
      throw new RuntimeException("Prijata ze spodu v navratovce",
          vyhodNedeDoleAPosliNahoru3());
    }

    void vyhodVyjimkuChytAVypis1() {
      try {
        vyhodVyjimkuChytAPosliDolu();
      } catch (RuntimeException e) {
        uzKonecneVypisVyjimku(e);
      }
    }

    void vyhodVyjimkuChytAVypis2() {
      vyhodVyjimkuChytAVypis1();
    }

    void vyhodVyjimkuChytAVypis3() {
      vyhodVyjimkuChytAVypis2();
    }

    @SuppressWarnings("unused")
    void vyhodVyjimkuChytAVypis4() {
      vyhodVyjimkuChytAVypis3();
    }

    void vyhodVyjimkuChytAVypis5() {
      vyhodVyjimkuChytAVypis3();
    }

  }

  public static void main(String[] args) {
    new VyjimkovySimulovac().vyhodVyjimkuChytAVypis5();
  }

/*  
  EXC-1-xx: Exception printed at 2006-08-28T09:52:03.658+0200
  EXC-1-xx:     render.9 - cz.tconsult.tw.lang.FThrowable$ExceptionPrinter.printStackTrace(FThrowable.java:386)
  EXC-1-xx:     render.8 - cz.tconsult.tw.lang.FThrowable.printStackTrace(FThrowable.java:260)
  EXC-1-xx:     render.7 - cz.tconsult.tw.lang.FThrowable.printStackTrace(FThrowable.java:254)
  EXC-1-xx:     render.6 - cz.tconsult.tw.lang.FThrowable$VyjimkovySimulovac.uzKonecneVypisVyjimku(FThrowable.java:613)
  EXC-1-xx:     render.5 - cz.tconsult.tw.lang.FThrowable$VyjimkovySimulovac.vyhodVyjimkuChytAVypis1(FThrowable.java:621)
  EXC-1-xx:     render.4 - cz.tconsult.tw.lang.FThrowable$VyjimkovySimulovac.vyhodVyjimkuChytAVypis2(FThrowable.java:626)
  EXC-1-xx:     render.3 - cz.tconsult.tw.lang.FThrowable$VyjimkovySimulovac.vyhodVyjimkuChytAVypis3(FThrowable.java:630)
  EXC-1-xx:     render.2 - cz.tconsult.tw.lang.FThrowable$VyjimkovySimulovac.vyhodVyjimkuChytAVypis5(FThrowable.java:638)
  EXC-1-xx:     render.1 - cz.tconsult.tw.lang.FThrowable.main(FThrowable.java:644)
  EXC-1-xx: 1/4 java.lang.IllegalStateException : Dole poprve
  EXC-1-xx:     1.10 - cz.tconsult.tw.lang.FThrowable$VyjimkovySimulovac.dolu1(FThrowable.java:589)
  EXC-1-xx:     1.9 - cz.tconsult.tw.lang.FThrowable$VyjimkovySimulovac.dolu2(FThrowable.java:593)
  EXC-1-xx:     1.8 - cz.tconsult.tw.lang.FThrowable$VyjimkovySimulovac.dolu3(FThrowable.java:597)
  EXC-1-xx:     1.7 - cz.tconsult.tw.lang.FThrowable$VyjimkovySimulovac.dolu4(FThrowable.java:601)
  EXC-1-xx:     1.6 - cz.tconsult.tw.lang.FThrowable$VyjimkovySimulovac.vyhodVyjimkuChytAPosliDolu(FThrowable.java:608)
  EXC-1-xx:     1.5 - cz.tconsult.tw.lang.FThrowable$VyjimkovySimulovac.vyhodVyjimkuChytAVypis1(FThrowable.java:619)
  EXC-1-xx:     1.4 - cz.tconsult.tw.lang.FThrowable$VyjimkovySimulovac.vyhodVyjimkuChytAVypis2(FThrowable.java:626)
  EXC-1-xx:        ... 3 more
  EXC-1-xx: 2/4 getCause(): java.lang.RuntimeException : Prvni prehozeni
  EXC-1-xx:     2.7 - cz.tconsult.tw.lang.FThrowable$VyjimkovySimulovac.metoda3(FThrowable.java:584)
  EXC-1-xx:     2.6 - cz.tconsult.tw.lang.FThrowable$VyjimkovySimulovac.vyhodVyjimkuChytAPosliDolu(FThrowable.java:606)
  EXC-1-xx:     2.5 - cz.tconsult.tw.lang.FThrowable$VyjimkovySimulovac.vyhodVyjimkuChytAVypis1(FThrowable.java:619)
  EXC-1-xx:        ... 4 more
  EXC-1-xx: 3/4 getCause(): java.lang.RuntimeException : Prvni prehozeni
  EXC-1-xx:     3.8 - cz.tconsult.tw.lang.FThrowable$VyjimkovySimulovac.metoda2(FThrowable.java:576)
  EXC-1-xx:     3.7 - cz.tconsult.tw.lang.FThrowable$VyjimkovySimulovac.metoda3(FThrowable.java:582)
  EXC-1-xx:     3.6 - cz.tconsult.tw.lang.FThrowable$VyjimkovySimulovac.vyhodVyjimkuChytAPosliDolu(FThrowable.java:606)
  EXC-1-xx:        ... 5 more
  EXC-1-xx: 4/4 getCause(): java.lang.NullPointerException 
  EXC-1-xx:     4.9 - cz.tconsult.tw.lang.FThrowable$VyjimkovySimulovac.metoda1(FThrowable.java:569)
  EXC-1-xx:     4.8 - cz.tconsult.tw.lang.FThrowable$VyjimkovySimulovac.metoda2(FThrowable.java:574)
  EXC-1-xx:     4.7 - cz.tconsult.tw.lang.FThrowable$VyjimkovySimulovac.metoda3(FThrowable.java:582)
  EXC-1-xx:        ... 6 more
  */
}
