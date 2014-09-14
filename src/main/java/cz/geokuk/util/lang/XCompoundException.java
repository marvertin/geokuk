package cz.geokuk.util.lang;

import java.util.ArrayList;
import java.util.List;

/** Sběrač (technických) vyjímek.
 * <p>
 * Použijte v situaci,
 * kdy může nastat více technických vyjímek (typicky: fráze 'finally')
 * a žádnou z nich nechcete ztratit.
 * <p>
 * Z věcného pohledu jsou všechny zaregistrované vyjímky "příčinami".
 * Realizace vyjímek ve standardní knihovně však umožňuje definovat jen jednu příčinu
 * a tou se stane vyjímka, kterou předáte kontruktoru sběrače vyjímek
 * (a současně bude zařazena jako první do seznamu registrovaných vyjímek).
 * Pokud vám toto chování nevyhovuje
 * - chcete považovat všechny "příčiny" za rovnocenné -
 * použijte konstruktor bez vyjímkového parametru.
 * IMO však vždy existuje jedna prvotní příčina, ostatní jsou pak jen doprovodné.
 * <p>
 * Viz. typické použití:
 * <pre>
 * XCompoundException xce;
 * try { 
 *   ...
 * } catch (AnExcepion e) {
 *   xce = new XCompoundException(e);
 * } finally {
 *   if (xce == null) xce = new XCompoundException();
 *   try {
 *     if (input != null) input.close();
 *   } catch (IOException e) {
 *     xce.add(e);
 *   }
 *   try {
 *     if (output != null) output.close();
 *   } catch (IOException e) {
 *     xce.add(e);
 *   }
 *   ...
 *   if (!xce.isEmpty()) throw xce.flatten().normalize();
 * }
 * </pre>
 * 
 * @author roztocil
 */
public class XCompoundException extends RuntimeException {
  private static final long serialVersionUID = 1L;
  
  private final List<Throwable> causes = new ArrayList<>();
  
  /** Nový sběrač vyjímek, bez zprávy a prvotní příčiny. */
  public XCompoundException() {
    super();
  }
  
  /** Nový sběrač vyjímek, se zprávou, ale bez prvotní příčiny. */
  public XCompoundException(String aMessages) {
    super(aMessages);
  }
  
  /** Nový sběrač vyjímek, se zprávou a prvotní příčinou. */
  public XCompoundException(String aMessages, Throwable aCause) {
    super(aMessages, aCause);
    causes.add(aCause);
  }
  
  /** Nový sběrač vyjímek, bez zprávy, ale s prvotní příčinou. */
  public XCompoundException(Throwable aCause) {
    super(aCause);
    causes.add(aCause);
  }
  
  /** Zjednodušší sběrač/složenou vyjímku.
   * @return <ol>
   *         <li>Pokud je sběrač prázdný, vrátí {@code null}.
   *         <li>Obsahuje-li jedinou vyjímku a ta je {@link RuntimeException}, vrátí ji.
   *         <li>Obsahuje-li jinou jedinou vyjímku, obalí ji do RuntimeException a tu vrátí.
   *         <li>Jinak vrátí sebe sama.
   *         </li>
   */
  public RuntimeException normalize() {
    switch (causes.size()) {
    case 0:
      return null;

    case 1:
      Throwable exception = causes.get(0);
      if (exception instanceof RuntimeException) {
        return (RuntimeException)exception;
      }
      return new RuntimeException(getMessage(), exception);
    
    default:
      return this;
    }
  }
  
  /** Nastaví "příčinu". {@inheritDoc} */
  @Override
  public synchronized Throwable initCause(Throwable arg0) {
    super.initCause(arg0);
    causes.add(0, arg0);
    return this;
  }
  
  /** Obsahuje složená vyjímka nějaké nasbírané vyjímky? */
  public boolean isEmpty() {
    boolean result = causes.isEmpty();
    return result;
  }
  
  /** Přidá další vyjímku do seznamu.
   * 
   * @param aCause  Vyjímka, která má být zaregistrována.
   * @return  Tuto vyjímku,  tím umožňuje řetězené použití např. {@code xce.add(e).add(e1).add(e2)}.
   */
  public XCompoundException add(Throwable aCause) {
    causes.add(aCause);
    return this;
  }
  
  /** Sploštění složených vyjímek.
   * <p>
   * Každou registrovanou složenou vyjímku (tj. {@link XCompoundException})
   * nahradí seznamem vyjímek v ní registrovaných.
   * Výsledkem je, že aktuální vyjímka neobsahuje ve svém seznamu žádnou složenou vyjímku.
   * <p>
   * POZOR!
   * Pokud některá z registrovaných vyjímek definovala vlastní text (zprávu), přijdete o něj.
   *  
   * @return Tuto vyjímku, tím umožňuje řetězené použití např. {@code xce.flatten().normalize()}.
   */
  public XCompoundException flatten() {
    for (int i = 0; i < causes.size(); i++) {
      Throwable item = causes.get(i);
      if (item instanceof XCompoundException == false) continue;
      List<Throwable> nextLevel = ((XCompoundException)item).causes;
      causes.remove(i);
      causes.addAll(i, nextLevel);
    }
    return this;
  }
  
  /** Registrované vyjímky.
   * @return Seznam registrovaných vyjímek jako pole.
   *         Tím je, dle Martina Veverky zajištěno, že budou všechny vyjímky vypsány standardními TC nástroji.
   */
  public Throwable[] getCauses() {
    Throwable[] result = new Throwable[causes.size()];
    causes.toArray(result );
    return result;
  }
}
