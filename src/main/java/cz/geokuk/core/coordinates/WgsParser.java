package cz.geokuk.core.coordinates;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsruje souřadnice ze stringu zadané v libovolném formátu
 * @author tatinek
 *
 */
public class WgsParser {

  private static final List<PovolenaVariacePismen> povoleneVariace = new ArrayList<PovolenaVariacePismen>();

  private static final Pattern pat =
      Pattern.compile("([0-9]+[.,]?[0-9]*) *°? *(?:([0-9]+[.,]?[0-9]*) *'? *(?:([0-9]+[.,]?[0-9]*) *\"? *)?)?");


  /** Rozprasruje souřasdnice, pokud se to nepodaří, vrátí null */
  public Wgs parsruj(String s) {
    Vzorek vzorek = najdiNejvhodnejsi(s);
    if (vzorek == null) return null;
    return vzorek.toWgs();
  }

  //  private void test1(String s) {
  //    System.out.println("----------------------------------------------------------");
  //    Matcher m = pat.matcher(s);
  //    if (m.find()) {
  //      System.out.println(s.substring(0, m.start()));
  //      System.out.println(s.substring(m.start(), m.end()));
  //      int poz = m.end();
  //      if (m.find()) {
  //        System.out.println(s.substring(poz, m.start()));
  //        System.out.println(s.substring(m.start(), m.end()));
  //        System.out.println(s.substring(m.end()));
  //
  //      }
  //    }
  //
  //  }

  private Souradky najdi(String s, Matcher m, int start, int end) {
    m.reset();
    m.region(start, end);
    if (m.find()) {
      Souradky sou = new Souradky();
      sou.prefix = s.substring(m.regionStart(), m.start()).toUpperCase();
      sou.stupne = new Cislo(m.group(1));
      sou.minuty = new Cislo(m.group(2));
      sou.vteriny = new Cislo(m.group(3));
      sou.suffix = s.substring(m.end(), m.regionEnd()).toUpperCase();

      if (obsahujeNepovolenyZnak(sou.prefix)) return null;
      if (obsahujeNepovolenyZnak(sou.suffix)) return null;
      Character pismenoSvetovychStran = sou.pismenoSvetovychStran();
      if (pismenoSvetovychStran == null) return null; // odfilrovana pismena
      // a filtrujeme desetinná čísla, která nejsou na konci
      if (! sou.stupne.isVyplneno()) return null; // to by nemelo nastat, ale pro jsitotu
      if (sou.minuty.isVyplneno() && sou.stupne.isDesetinne()) return null; // když desetinné stupně, tak žádné minuty nesmí být
      if (sou.vteriny.isVyplneno() && sou.minuty.isDesetinne()) return null; // když desetinné minuty, tak žádné vteřiny nesmí být
      return sou;
    } else {
        return null;
    }
  }


  private boolean obsahujeNepovolenyZnak(String s) {
    for (int i=0; i<s.length(); i++) {
      if (Character.isDigit(s.charAt(i))) return true;
      switch (s.charAt(i)) {
      case '°':
      case '\'':
      case '"':
        return true;
      }
    }
    return false;
  }


  private Vzorek rozeber(String s, Matcher m, int pozice) {
    if (Character.isDigit(s.charAt(pozice)) && Character.isDigit(s.charAt(pozice-1)))
      return null;  // hlavně neřezat mezi číslicemi
    Vzorek v = new Vzorek();
    v.sou1 = najdi(s, m, 0, pozice);
    if (v.sou1 == null) return null;
    v.sou2 = najdi(s, m, pozice, s.length());
    if (v.sou2 == null) return null;

    // odfiltrujeme nejasne kombinace pismen, jako W a S
    Character pismeno1 = v.sou1.pismenoSvetovychStran();
    Character pismeno2 = v.sou2.pismenoSvetovychStran();
    if (pismeno1 == null || pismeno2 == null) return null;

    for (PovolenaVariacePismen povapi : povoleneVariace) {
      if (povapi.p1 == pismeno1 && povapi.p2 == pismeno2)
        return v; // je to OK
      if (povapi.p1 == pismeno2 && povapi.p2 == pismeno1) {
        Souradky pom = v.sou1;
        v.sou1 = v.sou2;
        v.sou2 = pom;
        return v; // je to OK, ale msueli jsme vyměnit strany
      }
    }
    return null; // neprošlo sítem písmen
  }


  private Vzorek najdiNejvhodnejsi(String s) {
    int symetrieNejlepsihoVzorku = Integer.MAX_VALUE;
    Vzorek nejlepsiVzorek = null;
    Matcher m = pat.matcher(s);
    for (int i=2; i < s.length()-2; i++) {
      Vzorek vzorek = rozeber(s, m, i);
      if (vzorek != null) {
        int symetrie = vzorek.symetrie();
        if (symetrie < symetrieNejlepsihoVzorku) {
          nejlepsiVzorek = vzorek;
          symetrieNejlepsihoVzorku = symetrie;
          //          System.out.println(vzorek);
        } else {
          //          System.err.println(vzorek);
        }
      }
    }
    return nejlepsiVzorek;
  }

  private void zkousej(String s) {
    System.out.println("-----------" + s +
        "-----------------------------------------------");
    //    Matcher m = pat.matcher(s);
    //    for (int i=2; i < s.length()-2; i++) {
    //      Vzorek vzorek = rozeber(s, m, i);
    //      if (vzorek != null) {
    //        System.out.println(vzorek);
    //      }
    //    }

    Vzorek vzorek = najdiNejvhodnejsi(s);
    System.out.println(vzorek);

  }

  private class Vzorek {
    Souradky sou1;
    Souradky sou2;
    @Override
    public String toString() {
      return String.format("%s * %s", sou1, sou2);
    }

    /**
     * 0 znamená zcela symetrické, 1 liší se o jedno, 2 liší se o dvě.
     * @return
     */
    int symetrie() {
      return Math.abs(sou1.pocetSlozek() - sou2.pocetSlozek());
    }

    Wgs toWgs() {
      return new Wgs(sou1.toDouble(), sou2.toDouble());
    }
  }

  private static class Cislo {

    private final String s;

    Cislo(String s) {
      this.s = s == null ? null : s.replace(',', '.');
      if (s == null) return;
    }

    @Override
    public String toString() {
      return s;
    }

    boolean isVyplneno() {
      return s != null;
    }

    boolean isDesetinne() {
      return s.contains(".");
    }

    private double toDouble() {
      if (s == null || s.length() == 0) return 0;
      double result = Double.parseDouble(s);
      return result;
    }

  }

  private static class Souradky {
    String prefix;
    Cislo stupne;
    Cislo minuty;
    Cislo vteriny;
    String suffix;

    @Override
    public String toString() {
      return String.format("[%s-<%s|%s|%s>-%s]", prefix, stupne, minuty, vteriny, suffix);
    }

    /*
     * Vrací null, pokud je to špatně, co se týče písmen, ' ' pokud písmeno není uvedeno
     * nebo N, E, S, W pro světové strany případně X, když se neví zda sever nebo jih.
     */
    public Character pismenoSvetovychStran() {
      Character pismeno1 = extrahujJedinePovolenePismeno(prefix);
      Character pismeno2 = extrahujJedinePovolenePismeno(suffix);
      Character pismeno = urciToSpravnePismeno(pismeno1, pismeno2);
      return pismeno;

    }

    private Character extrahujJedinePovolenePismeno(String s) {
      Character pismeno = ' '; // to znamená žádné písmeno tam nebylo
      for (int i=0; i<s.length(); i++) {
        if (Character.isLetter(s.charAt(i))) {
          if (pismeno != ' ' && pismeno != s.charAt(i)) return null; // je tam  druhé písmeno
          pismeno = s.charAt(i);
        }
      }
      switch (pismeno) {
      case ' ':
      case 'N':
      case 'E':
      case 'S':
      case 'W':
      case 'V':
      case 'J':
      case 'Z':
        return pismeno; // to tam smí být
      }
      return null; // je tam špatné písmeno
    }

    private Character urciToSpravnePismeno(Character pismeno1, Character pismeno2) {
      //System.out.println("soluad? " + pismeno1 + pismeno2);
      if (pismeno1 == null || pismeno2 == null) return null;
      if (pismeno1 == pismeno2) return pismeno1; // i neuvedeni pismene se sem vleze
      if (pismeno1 == ' ') return pismeno2;
      if (pismeno2 == ' ') return pismeno1;
      return null;
    }

    int pocetSlozek() {
      if (vteriny.isVyplneno()) return 3;
      if (minuty.isVyplneno()) return 2;
      return 1;
    }

    double toDouble() {
      double x = stupne.toDouble() + minuty.toDouble()/60 + vteriny.toDouble()/3600;
      return x;
    }
  }

  private static class PovolenaVariacePismen {
    final char p1;
    final char p2;

    public PovolenaVariacePismen(char p1, char p2) {
      this.p1 = p1;
      this.p2 = p2;
    }
  }

  private static void povolVariaci(char p1, char p2) {
    PovolenaVariacePismen variace = new PovolenaVariacePismen(p1, p2);
    povoleneVariace.add(variace);

  }


  static {
    povolVariaci(' ', ' ');
    // mezinárodní
    povolVariaci('N', 'E');
    povolVariaci('N', 'W');
    povolVariaci('S', 'E');
    povolVariaci('S', 'W');

    // české
    povolVariaci('S', 'V');
    povolVariaci('S', 'Z');
    povolVariaci('J', 'V');
    povolVariaci('J', 'Z');

    // jednostranné
    povolVariaci('N', ' ');
    povolVariaci(' ', 'E');
    povolVariaci(' ', 'W');
    povolVariaci('J', ' ');
    povolVariaci(' ', 'Z');
    povolVariaci(' ', 'V');
  }

  public static void main(String[] args) {

    WgsParser parser = new WgsParser();

    parser.zkousej(";;111 222 333 444 555 666\";");
    parser.zkousej(";;111 222 333E;N444 555 666++");
    parser.zkousej("49 16 17 21");
    parser.zkousej("N49.156, 16.788E");
    parser.zkousej("n49°16'21\";16°18.425");
    parser.zkousej("N49°45\" 16 18 425");
    parser.zkousej("49 16,7 17 21 23.45");
    parser.zkousej("49,12345°16,12345");
    parser.zkousej("aaa49bbb16ccc");
    parser.zkousej("49°16°21 45");
    parser.zkousej("17°W49°S");
    parser.zkousej("33°Z49°E");
    parser.zkousej("11,22,33,44");
    parser.zkousej("11,22,33");
    System.out.println("----------------------------------------------------------");
  }
}
