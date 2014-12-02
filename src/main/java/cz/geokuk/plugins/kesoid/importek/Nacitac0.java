package cz.geokuk.plugins.kesoid.importek;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import cz.geokuk.plugins.kesoid.Kes;
import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;

/**
 * @author veverka
 *
 */
public abstract class Nacitac0 {

  static Pattern osetriCislo = Pattern.compile("[^0-9]");

  protected abstract void nactiKdyzUmis(InputStream istm, String jmeno, IImportBuilder builder,  Future<?> future) throws IOException;

  protected final void nactiKdyzUmisBezVyjimky(InputStream istm, String jmeno, IImportBuilder builder, Future<?> future) {
    try {
      try {
        nactiKdyzUmis(istm, jmeno, builder, future);
      } catch (IOException e) {
        throw new RuntimeException("Preoblem reading \"" + jmeno + "\"", e);
      }
    } catch (Exception e) {
      FExceptionDumper.dump(e, EExceptionSeverity.DISPLAY, "Problem při načítání kešek, ale jedeme dál");
    }
  }

  /**
   * @param aString
   * @return
   */
  protected int parseCislo(String s) {
    s = osetriCislo.matcher(s).replaceAll("").trim();
    if (s.length() == 0) return 0;
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Problem s parsrovanim cisla \"" + s + "\" pri cteni hodnoceni nebo bestofu");
      return 0; // je to španě, vrátíme nuli
    }
  }

  /**
   * @param aString
   * @return
   */
  protected int parseHodnoceni(String s) {
    s = osetriCislo.matcher(s).replaceAll("").trim();
    if (s.length() == 0) return 0;
    try {
      return Integer.parseInt(s);
    } catch (NumberFormatException e) {
      //      FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Problem s parsrovanim cisla \"" + s + "\" pri cteni hodnoceni nebo bestofu");
      return Kes.NENI_HODNOCENI; // je to španě, tak asi není hodnocení
    }
  }
}
