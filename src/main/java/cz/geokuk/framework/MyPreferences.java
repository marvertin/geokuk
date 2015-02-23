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
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
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
  private static final char LIST_DELIMITER_CHAR = ';';
  private static final char ESCAPE_CHAR = '\\';
  private static final String PART_PATTERN = ";cont";
  private static final String NUMBERED_PART_PATTERN = PART_PATTERN + "%d";
  private final Preferences pref;


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
    put(key, pack(bod));
  }

  public Mou getMou(String key, Mou def) {
    String s = get(key, pack(def));
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
    put(key, pack(bod));
  }

  public Point getPoint(String key, Point def) {
    String s = get(key, pack(def));
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
    put(key, pack(bod));
  }

  public Dimension getDimension(String key, Dimension def) {
    String s = get(key, pack(def));
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
    put(key, pack(bod));
  }

  public Wgs getWgs(String key, Wgs def) {
    String s = get(key, pack(def));
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

  public void putFileCollection(String key, Collection<File> fileCollection) {
    put(key, pack(fileCollection));
  }

  public Collection<File> getFileCollection(String key, Collection<File> def) {
    String s = get(key, null);
    if (s == null) {
      return def;
    }
    return unpack(s, new Function<String, File>() {
      @Override
      public File apply(String s) {
        return new File(s);
      }
    });
  }

  public void putEnum(String key, Enum<?> e) {
    put(key, e == null ? null : e.name());
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

  public <E extends Enum<E>> void putEnumSet(String key, EnumSet<E> val) {
    put(key, pack(val));
  }

  public <E extends Atom> void putAtom(String key, E val) {
    put(key, val == null ? null : val.name());
  }

  public <E extends Atom> void putAtomSet(String key, Set<E> val) {
    put(key, pack(val));
  }

  public void putStringList(String key, List<String> val) {
    put(key, pack(val));
  }

  public List<String> getStringList(String key, List<String> defval) {
    String ss = get(key, null);
    if (ss == null) {
      return defval;
    }
    return unpack(ss);
  }

  public void putStringSet(String key, Set<String> val) {
    put(key, pack(new ArrayList<>(val)));
  }

  public Set<String> getStringSet(String key, Set<String> defval) {
    List<String> stringList = getStringList(key, null);
    if (stringList == null) {
      return defval;
    } else {
      return new HashSet<>(stringList);
    }
  }

  public <E extends Atom> E getAtom(String key, E def, Class<E> cls) {
    String val = get(key, def == null ? null : def.name());
    return Atom.valueOf(cls, val);
  }

  public <E extends Atom> Set<E> getAtomSet(String key, Set<E> def, final Class<E> cls) {
    String ss =  get(key, null);
    if (ss == null) {
      return def;
    }
    try {
      List<E> atoms = unpack(key, new Function<String, E>() {
        @Override
        public E apply(String s) {
          return Atom.valueOf(cls, s);
        }
      });
      Set<E> set = Atom.noneOf(cls);
      set.addAll(atoms);
      return set;
    } catch (Exception e) { // pokud je tam něco blbě, bere se default
      FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Nesmysl v preferencich: " + key + "=" + ss);
      remove(key);
      return def;
    }
  }

  public <E extends Enum<E>> EnumSet<E> getEnumSet(String key, EnumSet<E> def, final Class<E> cls) {
    String ss =  get(key, null);
    if (ss == null) {
      return def;
    }
    try {
      List<E> enums = unpack(key, new Function<String, E>() {
        @Override
        public E apply(String s) {
          return Enum.valueOf(cls, s);
        }
      });
      EnumSet<E> set = EnumSet.noneOf(cls);
      set.addAll(enums);
      return set;
    } catch (Exception e) {
      FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Nesmysl v preferencich: " + key + "=" + ss);
      remove(key);
      return def;
    }
  }

  private StringBuilder listEscapeToSb(String toEscape, StringBuilder sb) {
    int length = toEscape.length();
    for (int i = 0; i < length; ++i) {
      char c = toEscape.charAt(i);
      if (c == LIST_DELIMITER_CHAR || c == ESCAPE_CHAR) {
        sb.append(ESCAPE_CHAR);
      }
      sb.append(c);
    }
    return sb;
  }

  private String pack(Object o) {
    if (o == null) {
      return null;
    }

    if (o instanceof String) {
      return pack((String) o);
    }
    if (o instanceof Collection) {
      return pack((Collection) o);
    }
    if (o instanceof Point) {
      return pack((Point) o);
    }
    if (o instanceof Dimension) {
      return pack((Dimension) o);
    }
    if (o instanceof Wgs) {
      return pack((Wgs) o);
    }
    if (o instanceof Atom) {
      return pack((Atom) o);
    }
    if (o instanceof Enum) {
      return pack((Enum) o);
    }
    if (o instanceof Mou) {
      return pack((Mou)o);
    }
    if (o instanceof File) {
      return pack((File)o);
    }
    throw new IllegalArgumentException("Unknown object to pack : " + o.getClass() + " " + o);
  }

  private String pack(Mou bod) {
    return bod == null ? null : bod.xx + "," + bod.yy;
  }

  private String pack(Collection<?> val) {
    StringBuilder sb = new StringBuilder();
    for (Object singleVal : val) {
      listEscapeToSb(pack(singleVal), sb).append(LIST_DELIMITER_CHAR);
    }
    return sb.toString();
  }

  private String pack(Point bod) {
    return bod == null ? null : bod.x + "," + bod.y;
  }

  private String pack(Dimension bod) {
    return bod == null ? null : bod.width + "," + bod.height;
  }

  private String pack(Wgs bod) {
    return bod == null ? null : bod.lat + "," + bod.lon;
  }

  private String pack(Atom val) {
    return val == null ? null : val.name();
  }

  private String pack(Enum val) {
    return val == null ? null : val.name();
  }

  private String pack(String s) {
    return s;
  }

  private String pack(File f) {
    try {
      return f.getCanonicalPath();
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to retrieve canonical path from " + f, e);
    }
  }

  private List<String> unpack(String packedString) {
    if (packedString == null) {
      return null;
    }
    List<String> toReturn = new ArrayList<>();
    StringBuilder sb = new StringBuilder();
    boolean escaped = false;
    for (int i = 0; i < packedString.length(); ++i) {
      char c = packedString.charAt(i);
      if (escaped) {
        escaped = false;
        sb.append(c);
      } else if (c == ESCAPE_CHAR) {
        escaped = true;
      } else if (c == LIST_DELIMITER_CHAR) {
        toReturn.add(sb.toString());
        sb.setLength(0);
      } else {
        sb.append(c);
      }
    }
    return toReturn;
  }

  private <T> List<T> unpack(String packedString, Function<String, T> parseFunc) {
    return new ArrayList<>(Collections2.transform(unpack(packedString), parseFunc));
  }

  MyPreferences(Preferences aPreferences) {
    pref = aPreferences;
  }

  public static MyPreferences userRoot() {
    return new MyPreferences(Preferences.userRoot());
  }

  @Override
  public String absolutePath() {
    return pref.absolutePath();
  }

  @Override
  public void addNodeChangeListener(NodeChangeListener aArg0) {
    pref.addNodeChangeListener(aArg0);
  }

  @Override
  public void addPreferenceChangeListener(PreferenceChangeListener aArg0) {
    pref.addPreferenceChangeListener(aArg0);
  }

  @Override
  public void clear() throws BackingStoreException {
    pref.clear();
  }

  @Override
  public boolean equals(Object aObj) {
    return pref.equals(aObj);
  }

  @Override
  public void exportNode(OutputStream aArg0) throws IOException, BackingStoreException {
    pref.exportNode(aArg0);
  }

  @Override
  public void exportSubtree(OutputStream aArg0) throws IOException, BackingStoreException {
    pref.exportSubtree(aArg0);
  }

  @Override
  public void flush() throws BackingStoreException {
    pref.flush();
  }

  @Override
  public String get(String key, String defvalue) {
    StringBuilder sb = new StringBuilder();
    String s = pref.get(key, null);
    if (s == null) {
      return defvalue;
    }
    String contKey;
    int part = 0;
    while (s != null) {
      sb.append(s);
      contKey = key + String.format(NUMBERED_PART_PATTERN, part++);
      s = pref.get(contKey, null);
    }
    s = sb.toString();
    if (NULL.equals(s)) return null;
    return s;
  }

  @Override
  public boolean getBoolean(String key, boolean value) {
    return pref.getBoolean(key, value);
  }

  @Override
  public byte[] getByteArray(String key, byte[] value) {
    return pref.getByteArray(key, value);
  }

  @Override
  public double getDouble(String key, double value) {
    return pref.getDouble(key, value);
  }

  @Override
  public float getFloat(String key, float value) {
    return pref.getFloat(key, value);
  }

  @Override
  public int getInt(String key, int value) {
    return pref.getInt(key, value);
  }

  @Override
  public long getLong(String key, long value) {
    return pref.getLong(key, value);
  }

  @Override
  public int hashCode() {
    return pref.hashCode();
  }

  @Override
  public String[] childrenNames() throws BackingStoreException {
    return pref.childrenNames();
  }

  @Override
  public boolean isUserNode() {
    return pref.isUserNode();
  }

  @Override
  public String[] keys() throws BackingStoreException {
    return pref.keys();
  }

  @Override
  public String name() {
    return pref.name();
  }

  @Override
  public MyPreferences node(String aArg0) {
    return new MyPreferences(pref.node(aArg0));
  }

  @Override
  public boolean nodeExists(String aArg0) throws BackingStoreException {
    return pref.nodeExists(aArg0);
  }

  @Override
  public Preferences parent() {
    return pref.parent();
  }

  @Override
  public void put(String key, String value) {
    if (key.matches(PART_PATTERN + "[0-9]+")) {
      throw new IllegalArgumentException("Unable to insert a key with name " + key + "! This" +
              "key name is reserved for multi-part values");
    }
    if (value == null) {
      put(key, NULL);
    } else {
      if (value.length() <= Preferences.MAX_VALUE_LENGTH) {
        pref.put(key, value);
      } else {
        String first = value.substring(0, Preferences.MAX_VALUE_LENGTH);
        Iterable<String> rest = Splitter.fixedLength(Preferences.MAX_VALUE_LENGTH).split(
                value.substring(Preferences.MAX_VALUE_LENGTH));
        pref.put(key, first);
        int i = 0;
        for (String s : rest) {
          pref.put(key + String.format(NUMBERED_PART_PATTERN, i++), s);
        }
      }
    }
  }

  @Override
  public void putBoolean(String key, boolean value) {
    pref.putBoolean(key, value);
  }

  @Override
  public void putByteArray(String key, byte[] value) {
    pref.putByteArray(key, value);
  }

  @Override
  public void putDouble(String key, double value) {
    pref.putDouble(key, value);
  }

  @Override
  public void putFloat(String key, float value) {
    pref.putFloat(key, value);
  }

  @Override
  public void putInt(String key, int value) {
    pref.putInt(key, value);
  }

  @Override
  public void putLong(String key, long value) {
    pref.putLong(key, value);
  }

  @Override
  public void remove(String key) {
    pref.remove(key);
  }

  @Override
  public void removeNode() throws BackingStoreException {
    pref.removeNode();
  }

  @Override
  public void removeNodeChangeListener(NodeChangeListener aArg0) {
    pref.removeNodeChangeListener(aArg0);
  }

  @Override
  public void removePreferenceChangeListener(PreferenceChangeListener aArg0) {
    pref.removePreferenceChangeListener(aArg0);
  }

  @Override
  public void sync() throws BackingStoreException {
    pref.sync();
  }

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
}
