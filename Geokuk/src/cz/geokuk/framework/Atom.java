package cz.geokuk.framework;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public abstract class Atom implements Comparable<Atom> {

  @SuppressWarnings("rawtypes")
  private static Map<Class<? extends Atom>, TypAtomu> repo 
  = new HashMap<Class<? extends Atom>, TypAtomu>();

  private String name;
  private int ordinal;

  public static <E extends Atom> Set<E> of(E... types) {
    return new HashSet<E>(Arrays.asList(types));
  }

  public static <E extends Atom> E valueOf(Class<E> clazz, String jmeno) {
    clazz.getFields();
    try {
      Class.forName(clazz.getName(), true, clazz.getClassLoader());
    } catch (ClassNotFoundException e) { // to urcite uspeje
    }
    //AWptType fINALLOCATION = AWptType.FINAL_LOCATION;
    TypAtomu<E> ta = dejTyp(clazz);
    E atom = ta.mapa.get(jmeno);
    if (atom == null) {
      atom = vytvorInstanci(clazz);
      atom.name = jmeno;
      atom.ordinal = ta.mapa.size();
      ta.mapa.put(jmeno, atom);
    }
    return atom;
  }


  public String name() {
    return name;
  }

  public int ordinal() {
    return ordinal;
  }

  public static <E> Set<E> noneOf(Class<E> e) {
    return new HashSet<E>();
  }

  private static <E extends Atom> E vytvorInstanci(Class<E> clazz) {
    try {
      return clazz.newInstance();
    } catch (InstantiationException e) {
      throw new RuntimeException(e);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  private static <E extends Atom> TypAtomu<E> dejTyp(Class<? extends Atom> typ) {
    @SuppressWarnings("unchecked")
    TypAtomu<E> typAtomu = repo.get(typ);
    if (typAtomu == null) {
      typAtomu = new TypAtomu<E>();
      repo.put(typ, typAtomu);
    }
    return typAtomu;
  }


  private static class TypAtomu<E extends Atom> {
    Map<String, E> mapa = new LinkedHashMap<String, E>();
  }

  @Override
  public String toString() {
    return name;
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(Atom o) {
    return name.compareTo(o.name);
  }
}
