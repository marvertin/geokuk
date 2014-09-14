/**
 * 
 */
package cz.geokuk.framework;


import static java.util.Locale.ENGLISH;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;
import cz.geokuk.util.file.Filex;



/**
 * @author veverka
 *
 */
public class MyPreferences extends Preferences {

  private static final Map<Class<?>, Duo> sMetody = new HashMap<>();

  private static final String NULL = "<{<[NULL]>}>";
  private final Preferences pref;

  /////////////// Přidané typy //////////////


  public void putFile(String key, File file) {
    put(key, file.getPath());
  }

  public File getFile(String key, File defalt) {
    File result;
    try {
      String fileStr = get(key, defalt == null ? null : defalt.getPath());
      result = fileStr == null ? null : new File(fileStr);
    } catch (RuntimeException e) {
      throw new RuntimeException("key=" + key  + ", defalt=" + defalt, e);
    }
    return result;
  }

  public void putFilex(String key, Filex filex) {
    put(key, filex.getFile().getPath());
    putBoolean(key + "_relativeToProgram", filex.isRelativeToProgram());
    putBoolean(key + "_active", filex.isActive());
  }

  public Filex getFilex(String key, Filex defalt) {

    if (defalt == null) {
      defalt = new Filex(null, false, false);
    }
    Filex result = new Filex(
        getFile(key, defalt.getFile()),
        getBoolean(key + "_relativeToProgram", defalt.isRelativeToProgram()),
        getBoolean(key + "_active", defalt.isActive()));
    return result.getFile() == null ? null : result;
  }

  // Mou
  public void putMou(String key, Mou bod) {
    put(key, zabal(bod));
  }

  private String zabal(Mou bod) {
    return bod == null ? null : bod.xx + "," + bod.yy;
  }

  public Mou getMou(String key, Mou def) {
    String s = get(key, zabal(def));
    if (s == null) return null;
    String[] ss = s.split(",");
    try {
      return new Mou(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
    } catch (NumberFormatException e) {
      FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Nesmysl v preferencich: " + key + "=" + s);
      remove(key); // když je tam blbost, raději mažeme
      return def;
    }
  }

  // Point
  public void putPoint(String key, Point bod) {
    put(key, zabal(bod));
  }

  private String zabal(Point bod) {
    return bod == null ? null : bod.x + "," + bod.y;
  }

  public Point getPoint(String key, Point def) {
    String s = get(key, zabal(def));
    if (s == null) return null;
    String[] ss = s.split(",");
    try {
      return new Point(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
    } catch (NumberFormatException e) {
      FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Nesmysl v preferencich: " + key + "=" + s);
      remove(key); // když je tam blbost, raději mažeme
      return def;
    }
  }

  //Dimension
  public void putDimension(String key, Dimension bod) {
    put(key, zabal(bod));
  }

  private String zabal(Dimension bod) {
    return bod == null ? null : bod.width + "," + bod.height;
  }

  public Dimension getDimension(String key, Dimension def) {
    String s = get(key, zabal(def));
    if (s == null) return null;
    String[] ss = s.split(",");
    try {
      return new Dimension(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
    } catch (NumberFormatException e) {
      FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Nesmysl v preferencich: " + key + "=" + s);
      remove(key); // když je tam blbost, raději mažeme
      return def;
    }
  }

  //Wgs
  public void putWgs(String key, Wgs bod) {
    put(key, zabal(bod));
  }

  private String zabal(Wgs bod) {
    return bod == null ? null : bod.lat + "," + bod.lon;
  }

  public Wgs getWgs(String key, Wgs def) {
    String s = get(key, zabal(def));
    if (s == null) return null;
    String[] ss = s.split(",");
    try {
      return new Wgs(Double.parseDouble(ss[0]), Double.parseDouble(ss[1]));
    } catch (NumberFormatException e) {
      FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Nesmysl v preferencich: " + key + "=" + s);
      remove(key); // když je tam blbost, raději mažeme
      return def;
    }
  }


  public <T extends Enum<T>> T getEnum(String key, T def, Class<T> cls) {
    String s = get(key, def == null ? null : def.name());
    if (s == null) return null;
    T result;
    try {
      result = Enum.valueOf(cls, s);
    } catch (Exception e) { // když je to špatná hodnota, jako by nebyla žádná
      FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Nesmysl v preferencich: " + key + "=" + s);
      remove(key);
      return def;
    }
    return result;
  }


  // nedokázal jsem to jinak, abych to mohl mít v getAnyElement
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Enum getEnumxx(String key, Enum<?> def, Class<Enum> cls) {
    String s = get(key, def == null ? null : def.name());
    if (s == null) return null;
    Enum result;
    try {
      result = Enum.valueOf(cls, s);
    } catch (Exception e) { // když je to špatná hodnota, jako by nebyla žádná
      FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Nesmysl v preferencich: " + key + "=" + s);
      remove(key);
      return def;
    }
    return result;
  }

  public void putEnum(String key, Enum<?> e) {
    put(key, e == null ? null : e.name());
  }

  public <E extends Enum<E>> void putEnumSet(String key, EnumSet<E> val) {
    put(key, zabal(val));
  }

  public <E extends Atom> void putAtom(String key, E val) {
    put(key, val == null ? null : val.name());
  }

  public <E extends Atom> void putAtomSet(String key, Set<E> val) {
    put(key, zabal(val));
  }

  public void putStringList(String key, List<String> val) {
    put(key, zabal(val));
  }

  public void putStringSet(String key, Set<String> val) {
    put(key, zabal(new ArrayList<>(val)));
  }

  public List<String> getStringList(String key, List<String> defval) {
    String ss = get(key, zabal(defval));
    return rozbal(ss);
  }

  public Set<String> getStringSet(String key, Set<String> defval) {
    return new HashSet<>(getStringList(key, new ArrayList<>(defval)));
  }

  public <E extends Atom> E getAtom(String key, E def, Class<E> cls) {
    String val = get(key, def == null ? null : def.name());
    return Atom.valueOf(cls, val);
  }


  public <E extends Atom> Set<E> getAtomSet(String key, Set<E> def, Class<E> cls) {
    String ss =  get(key, zabal(def));
    if (ss == null) return null;
    Set<E> set;
    try {
      set = Atom.noneOf(cls);
      for (String s : ss.split(",")) {
        String sss = s.trim();
        if (sss.length() == 0) {
          continue;
        }
        //System.out.println("KUKU " + sss);
        E e = Atom.valueOf(cls, sss);
        set.add(e);
      }
    } catch (Exception e) { // pokud je tam něco blbě, bere se default
      FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Nesmysl v preferencich: " + key + "=" + ss);
      remove(key);
      return def;
    }
    return set;
  }

  public <E extends Enum<E>> EnumSet<E> getEnumSet(String key, EnumSet<E> def, Class<E> cls) {
    String ss =  get(key, zabal(def));
    if (ss == null) return null;
    EnumSet<E> set;
    try {
      set = EnumSet.noneOf(cls);
      for (String s : ss.split(",")) {
        String sss = s.trim();
        if (sss.length() == 0) {
          continue;
        }
        //System.out.println("KUKU " + sss);
        E e = Enum.valueOf(cls, sss);
        set.add(e);
      }
    } catch (Exception e) { // pokud je tam něco blbě, bere se default
      FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Nesmysl v preferencich: " + key + "=" + ss);
      remove(key);
      return def;
    }
    return set;
  }

  private String zabal(List<String> val) {
    if (val == null) return null;
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (String s : val) {
      if (!first) {
        sb.append(',');
      }
      for (char c : s.toCharArray()) {
        if (c == ',' || c == '\\') {
          sb.append('\\');
        }
        sb.append(c);
      }
      first = false;
    }
    return sb.toString();
  }

  private List<String> rozbal(String val) {
    if (val == null) return null;
    List<String> list = new ArrayList<>();
    StringBuilder sb = new StringBuilder();
    boolean skipnext = false;
    for (char c : val.toCharArray()) {
      if (c == '\\' && ! skipnext) {
        skipnext = true;
      } else {
        if (! skipnext && c == ',') {
          list.add(sb.toString());
          sb.setLength(0);
        } else {
          sb.append(c);
        }
      }
    }
    list.add(sb.toString());
    return list;
  }


  private <E extends Atom> String zabal(Set<E> val) {
    if (val == null) return null;
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (E e : val) {
      if (!first) {
        sb.append(',');
      }
      sb.append(e.name());
      first = false;
    }
    return sb.toString();
  }

  private <E extends Enum<E>> String zabal(EnumSet<E> val) {
    if (val == null) return null;
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (E e : val) {
      if (!first) {
        sb.append(',');
      }
      sb.append(e.name());
      first = false;
    }
    return sb.toString();
  }

  //////////////////////////////////////////////
  private MyPreferences(Preferences aPreferences) {
    pref = aPreferences;
  }

  public static MyPreferences userRoot() {
    return new MyPreferences(Preferences.userRoot());
  }

  /**
   * @return
   * @see java.util.prefs.Preferences#absolutePath()
   */
  @Override
  public String absolutePath() {
    return pref.absolutePath();
  }

  /**
   * @param key
   * @see java.util.prefs.Preferences#addNodeChangeListener(java.util.prefs.NodeChangeListener)
   */
  @Override
  public void addNodeChangeListener(NodeChangeListener aArg0) {
    pref.addNodeChangeListener(aArg0);
  }

  /**
   * @param aArg0
   * @see java.util.prefs.Preferences#addPreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
   */
  @Override
  public void addPreferenceChangeListener(PreferenceChangeListener aArg0) {
    pref.addPreferenceChangeListener(aArg0);
  }

  /**
   * @throws BackingStoreException
   * @see java.util.prefs.Preferences#clear()
   */
  @Override
  public void clear() throws BackingStoreException {
    pref.clear();
  }

  /**
   * @param aObj
   * @return
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object aObj) {
    return pref.equals(aObj);
  }

  /**
   * @param aArg0
   * @throws IOException
   * @throws BackingStoreException
   * @see java.util.prefs.Preferences#exportNode(java.io.OutputStream)
   */
  @Override
  public void exportNode(OutputStream aArg0) throws IOException, BackingStoreException {
    pref.exportNode(aArg0);
  }

  /**
   * @param aArg0
   * @throws IOException
   * @throws BackingStoreException
   * @see java.util.prefs.Preferences#exportSubtree(java.io.OutputStream)
   */
  @Override
  public void exportSubtree(OutputStream aArg0) throws IOException, BackingStoreException {
    pref.exportSubtree(aArg0);
  }

  /**
   * @throws BackingStoreException
   * @see java.util.prefs.Preferences#flush()
   */
  @Override
  public void flush() throws BackingStoreException {
    pref.flush();
  }

  /**
   * @param aArg0
   * @param value
   * @return
   * @see java.util.prefs.Preferences#get(java.lang.String, java.lang.String)
   */
  @Override
  public String get(String key, String defvalue) {
    String s = pref.get(key, defvalue);
    if (NULL.equals(s)) return null;
    return s;
  }

  /**
   * @param key
   * @param value
   * @return
   * @see java.util.prefs.Preferences#getBoolean(java.lang.String, boolean)
   */
  @Override
  public boolean getBoolean(String key, boolean value) {
    return pref.getBoolean(key, value);
  }

  /**
   * @param key
   * @param value
   * @return
   * @see java.util.prefs.Preferences#getByteArray(java.lang.String, byte[])
   */
  @Override
  public byte[] getByteArray(String key, byte[] value) {
    return pref.getByteArray(key, value);
  }

  /**
   * @param key
   * @param value
   * @return
   * @see java.util.prefs.Preferences#getDouble(java.lang.String, double)
   */
  @Override
  public double getDouble(String key, double value) {
    return pref.getDouble(key, value);
  }

  /**
   * @param key
   * @param value
   * @return
   * @see java.util.prefs.Preferences#getFloat(java.lang.String, float)
   */
  @Override
  public float getFloat(String key, float value) {
    return pref.getFloat(key, value);
  }

  /**
   * @param key
   * @param value
   * @return
   * @see java.util.prefs.Preferences#getInt(java.lang.String, int)
   */
  @Override
  public int getInt(String key, int value) {
    return pref.getInt(key, value);
  }

  /**
   * @param key
   * @param value
   * @return
   * @see java.util.prefs.Preferences#getLong(java.lang.String, long)
   */
  @Override
  public long getLong(String key, long value) {
    return pref.getLong(key, value);
  }

  /**
   * @return
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return pref.hashCode();
  }

  /**
   * @return
   * @throws BackingStoreException
   * @see java.util.prefs.Preferences#childrenNames()
   */
  @Override
  public String[] childrenNames() throws BackingStoreException {
    return pref.childrenNames();
  }

  /**
   * @return
   * @see java.util.prefs.Preferences#isUserNode()
   */
  @Override
  public boolean isUserNode() {
    return pref.isUserNode();
  }

  /**
   * @return
   * @throws BackingStoreException
   * @see java.util.prefs.Preferences#keys()
   */
  @Override
  public String[] keys() throws BackingStoreException {
    return pref.keys();
  }

  /**
   * @return
   * @see java.util.prefs.Preferences#name()
   */
  @Override
  public String name() {
    return pref.name();
  }

  /**
   * @param key
   * @return
   * @see java.util.prefs.Preferences#node(java.lang.String)
   */
  @Override
  public MyPreferences node(String aArg0) {
    return new MyPreferences(pref.node(aArg0));
  }

  /**
   * @param aArg0
   * @return
   * @throws BackingStoreException
   * @see java.util.prefs.Preferences#nodeExists(java.lang.String)
   */
  @Override
  public boolean nodeExists(String aArg0) throws BackingStoreException {
    return pref.nodeExists(aArg0);
  }

  /**
   * @return
   * @see java.util.prefs.Preferences#parent()
   */
  @Override
  public Preferences parent() {
    return pref.parent();
  }

  /**
   * @param aArg0
   * @param value
   * @see java.util.prefs.Preferences#put(java.lang.String, java.lang.String)
   */
  @Override
  public void put(String key, String value) {
    if (value == null) {
      put(key, NULL);
    } else {
      pref.put(key, value);
    }
  }

  /**
   * @param aArg0
   * @param value
   * @see java.util.prefs.Preferences#putBoolean(java.lang.String, boolean)
   */
  @Override
  public void putBoolean(String key, boolean value) {
    pref.putBoolean(key, value);
  }

  /**
   * @param aArg0
   * @param value
   * @see java.util.prefs.Preferences#putByteArray(java.lang.String, byte[])
   */
  @Override
  public void putByteArray(String key, byte[] value) {
    pref.putByteArray(key, value);
  }

  /**
   * @param key
   * @param value
   * @see java.util.prefs.Preferences#putDouble(java.lang.String, double)
   */
  @Override
  public void putDouble(String key, double value) {
    pref.putDouble(key, value);
  }

  /**
   * @param key
   * @param value
   * @see java.util.prefs.Preferences#putFloat(java.lang.String, float)
   */
  @Override
  public void putFloat(String key, float value) {
    pref.putFloat(key, value);
  }

  /**
   * @param key
   * @param value
   * @see java.util.prefs.Preferences#putInt(java.lang.String, int)
   */
  @Override
  public void putInt(String key, int value) {
    pref.putInt(key, value);
  }

  /**
   * @param key
   * @param value
   * @see java.util.prefs.Preferences#putLong(java.lang.String, long)
   */
  @Override
  public void putLong(String key, long value) {
    pref.putLong(key, value);
  }

  /**
   * @param key
   * @see java.util.prefs.Preferences#remove(java.lang.String)
   */
  @Override
  public void remove(String key) {
    pref.remove(key);
  }

  /**
   * @throws BackingStoreException
   * @see java.util.prefs.Preferences#removeNode()
   */
  @Override
  public void removeNode() throws BackingStoreException {
    pref.removeNode();
  }

  /**
   * @param aArg0
   * @see java.util.prefs.Preferences#removeNodeChangeListener(java.util.prefs.NodeChangeListener)
   */
  @Override
  public void removeNodeChangeListener(NodeChangeListener aArg0) {
    pref.removeNodeChangeListener(aArg0);
  }

  /**
   * @param aArg0
   * @see java.util.prefs.Preferences#removePreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
   */
  @Override
  public void removePreferenceChangeListener(PreferenceChangeListener aArg0) {
    pref.removePreferenceChangeListener(aArg0);
  }

  /**
   * @throws BackingStoreException
   * @see java.util.prefs.Preferences#sync()
   */
  @Override
  public void sync() throws BackingStoreException {
    pref.sync();
  }

  /**
   * @return
   * @see java.util.prefs.Preferences#toString()
   */
  @Override
  public String toString() {
    return pref.toString();
  }

  public static MyPreferences root() {
    return MyPreferences.userRoot().node("geokuk");
  }

  public static MyPreferences current() {
    String root = System.getProperty("prefroot", "current");
    return root().node(root);
  }

  public static MyPreferences home() {
    return root().node("home");
  }

  public Color getColor(String name, Color defaultBarva) {
    return new Color(getInt(name, defaultBarva.getRGB()), true);
  }

  public void putColor(String nameg, Color color) {
    putInt(nameg, color.getRGB());
  }

  public Font getFont(String name, Font fontdef) {
    String fontstr = get(name, fontToString(fontdef));
    Font font = Font.decode(fontstr);
    return font;
  }

  public void putFont(String name, Font font) {
    put(name, fontToString(font));
  }

  private String fontToString(Font font) {
    return (font.getFamily() + "-" + fontStyleToString(font.getStyle())
        + "-" + font.getSize());

  }

  private String fontStyleToString(int style) {
    switch(style) {
    case Font.PLAIN: return "PLAIN";
    case Font.ITALIC: return "ITALIC";
    case Font.BOLD: return "BOLD";
    case Font.BOLD + Font.ITALIC: return "BOLDITALIC";
    }
    return "";
  }

  private static final class Duo {
    Method get;
    Method put;
    Class<?> cls;
  }

  private static Duo duo(Class<?> cls) {
    Duo duo = sMetody.get(cls);
    if (duo == null) {
      duo = new Duo();
      duo.cls = cls;
      sMetody.put(cls, duo);
    }
    return duo;
  }

  @SuppressWarnings("unchecked")
  public <T> T getAnyElement(String key, T defx, Class<T> cls) {
    if (cls.isEnum()) {
      // Nedokázal jsem to jinak
      @SuppressWarnings("rawtypes")
      Enum<?> enumxx = getEnumxx(key, (Enum<?>) defx, (Class<Enum>) cls);
      T result = (T) enumxx;
      return result;
    } else {
      if (cls.isAnnotationPresent(Preferenceble.class)) {
        try {
          T result = getStructure(key, cls.newInstance());
          return result;
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      } else {
        Duo duo = sMetody.get(cls);
        if (duo == null) throw new RuntimeException("Neexistuje pristupova metoda pro element typu: " + cls + ", mozna je to struktura a postrada anotaci");
        try {
          T result = (T) duo.get.invoke(this, key, defx);
          return result;
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  public <T> void putAnyElement(String name, T value, Class<T> cls) {
    if (cls.isEnum()) {
      putEnum(name, (Enum<?>) value);
    } else {
      if (cls.isAnnotationPresent(Preferenceble.class)) {
        putStructure(name, value);
      } else {
        Duo duo = sMetody.get(cls);
        if (duo == null) throw new RuntimeException("Neexistuje pristupova metoda pro element typu: " + cls + ", mozna je to struktura a postrada anotaci");
        try {
          duo.put.invoke(this, name, value);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  public <T> T getStructure(String name, T defautStructure) {
    try {
      @SuppressWarnings("unchecked")
      Class<T> cls = (Class<T>) defautStructure.getClass();
      T stucture = cls.newInstance();
      if (! cls.isAnnotationPresent(Preferenceble.class)) throw new RuntimeException("Klasa " + cls + " postrada anotaci " + Preferenceble.class);
      MyPreferences node = node(name);
      for (Map.Entry<String, PropertyDescriptor> entry : findProperties(cls).entrySet()) {
        Object defaultValue = entry.getValue().getReadMethod().invoke(defautStructure);
        @SuppressWarnings("unchecked")
        Class<Object> valueType = (Class<Object>) entry.getValue().getPropertyType();
        Object newValue = node.getAnyElement(entry.getKey(), defaultValue, valueType);
        entry.getValue().getWriteMethod().invoke(stucture, newValue);
      }
      return stucture;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void putStructure(String name, Object structure) {
    try {
      Class<?> cls = structure.getClass();
      if (! cls.isAnnotationPresent(Preferenceble.class)) throw new RuntimeException("Klasa " + cls + " postrada anotaci " + Preferenceble.class);
      MyPreferences node = node(name);
      for (Map.Entry<String, PropertyDescriptor> entry : findProperties(cls).entrySet()) {
        Object value = entry.getValue().getReadMethod().invoke(structure);
        @SuppressWarnings("unchecked")
        Class<Object> valueType = (Class<Object>) entry.getValue().getPropertyType();
        node.putAnyElement(entry.getKey(), value, valueType);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Map<String, PropertyDescriptor> findProperties(Class<?> cls) {
    try {
      Map<String, PropertyDescriptor> descrs = new HashMap<>();
      for (Method method : cls.getMethods()) {
        if (!method.isAnnotationPresent(PreferencebleIgnore.class)) {
          proverProperties("is", method, descrs, cls);
          proverProperties("get", method, descrs, cls);
          proverProperties("set", method, descrs, cls);
        }
      }
      return descrs;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @param string
   * @param methodName
   * @param descrs
   * @throws IntrospectionException
   */
  private static void proverProperties(String prefix, Method method, Map<String, PropertyDescriptor> descrs, Class<?> cls) throws IntrospectionException {
    String methodName = method.getName();
    if (methodName.startsWith(prefix)) {
      String javaPropertyName = uncapitalize(methodName.substring(prefix.length()));
      if ("class".equals(javaPropertyName)) return;
      PropertyDescriptor pd = new PropertyDescriptor(javaPropertyName, cls);
      PreferencebleProperty annotation = method.getAnnotation(PreferencebleProperty.class);
      String prefePropertyName = javaPropertyName;
      if (annotation != null) {
        prefePropertyName = annotation.name();
      }
      descrs.put(prefePropertyName, pd);
    }
  }

  public static String uncapitalize(String name) {
    if (name == null || name.length() == 0)
      return name;
    return name.substring(0, 1).toLowerCase(ENGLISH) + name.substring(1);
  }

  static {
    Method[] methods = MyPreferences.class.getMethods();
    for (Method method : methods) {
      Class<?>[] types = method.getParameterTypes();
      String name = method.getName();
      if (types.length != 2) {
        continue;
      }
      if (types[0] != String.class) {
        continue;
      }
      Class<?> valueType = types[1];
      if (valueType == Object.class) {
        continue;
      }
      if (valueType == Set.class) {
        continue;
      }
      if (valueType == Enum.class) {
        continue;
      }
      if (valueType == Atom.class) {
        continue;
      }
      if (valueType == EnumSet.class) {
        continue;
      }
      if (name.startsWith("get")) {
        if (valueType != method.getReturnType()) {
          continue;
        }
        Duo duo = duo(valueType);
        if (duo.get != null) throw new RuntimeException(String.format("Duplicita: %s, %s", method, duo.get));
        duo.get = method;
      }
      if (name.startsWith("put")) {
        if (void.class != method.getReturnType()) {
          continue;
        }
        Duo duo = duo(valueType);
        if (duo.put != null) throw new RuntimeException(String.format("Duplicita: %s, %s", method, duo.put));
        duo.put = method;
      }
    }
    for (Duo duo : sMetody.values()) {
      if (duo.get == null) throw new RuntimeException(String.format("Neexistuje get pro: %s, ale mame %s", duo.cls, duo.put));
      if (duo.put == null) throw new RuntimeException(String.format("Neexistuje put pro: %s, ale mame %s", duo.cls, duo.get));
    }
  }


  public static void main(String[] args ) {}
}
