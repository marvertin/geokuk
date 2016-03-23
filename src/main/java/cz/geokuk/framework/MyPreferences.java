package cz.geokuk.framework;

import static java.util.Locale.ENGLISH;

import java.awt.*;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.List;
import java.util.prefs.*;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;

import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.core.coordinates.Wgs;
import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;
import cz.geokuk.util.file.Filex;

/**
 * @author Martin Veverka
 *
 */
public class MyPreferences extends Preferences {

	private static final class Duo {
		Method get;
		Method put;
		Class<?> cls;
	}

	private static final Map<Class<?>, Duo> sMetody = new HashMap<>();
	private static final String NULL = "<{<[NULL]>}>";
	private static final char LIST_DELIMITER_CHAR = ';';
	private static final char ESCAPE_CHAR = '\\';
	private static final String PART_PATTERN = ";cont";
	private static final String NUMBERED_PART_PATTERN = PART_PATTERN + "%d";

	static {
		final Method[] methods = MyPreferences.class.getMethods();
		for (final Method method : methods) {
			final Class<?>[] types = method.getParameterTypes();
			final String name = method.getName();
			if (types.length != 2) {
				continue;
			}
			if (types[0] != String.class) {
				continue;
			}
			final Class<?> valueType = types[1];
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
				final Duo duo = duo(valueType);
				if (duo.get != null) {
					throw new RuntimeException(String.format("Duplicita: %s, %s", method, duo.get));
				}
				duo.get = method;
			}
			if (name.startsWith("put")) {
				if (void.class != method.getReturnType()) {
					continue;
				}
				final Duo duo = duo(valueType);
				if (duo.put != null) {
					throw new RuntimeException(String.format("Duplicita: %s, %s", method, duo.put));
				}
				duo.put = method;
			}
		}
		for (final Duo duo : sMetody.values()) {
			if (duo.get == null) {
				throw new RuntimeException(String.format("Neexistuje get pro: %s, ale mame %s", duo.cls, duo.put));
			}
			if (duo.put == null) {
				throw new RuntimeException(String.format("Neexistuje put pro: %s, ale mame %s", duo.cls, duo.get));
			}
		}
	}

	private final Preferences pref;

	public static MyPreferences current() {
		final String root = System.getProperty("prefroot", "current");
		return root().node(root);
	}

	public static MyPreferences home() {
		return root().node("home");
	}

	public static MyPreferences root() {
		return MyPreferences.userRoot().node("geokuk");
	}

	public static String uncapitalize(final String name) {
		if (name == null || name.length() == 0) {
			return name;
		}
		return name.substring(0, 1).toLowerCase(ENGLISH) + name.substring(1);
	}

	public static MyPreferences userRoot() {
		return new MyPreferences(Preferences.userRoot());
	}

	private static Duo duo(final Class<?> cls) {
		Duo duo = sMetody.get(cls);
		if (duo == null) {
			duo = new Duo();
			duo.cls = cls;
			sMetody.put(cls, duo);
		}
		return duo;
	}

	private static Map<String, PropertyDescriptor> findProperties(final Class<?> cls) {
		try {
			final Map<String, PropertyDescriptor> descrs = new HashMap<>();
			for (final Method method : cls.getMethods()) {
				if (!method.isAnnotationPresent(PreferencebleIgnore.class)) {
					proverProperties("is", method, descrs, cls);
					proverProperties("get", method, descrs, cls);
					proverProperties("set", method, descrs, cls);
				}
			}
			return descrs;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param string
	 * @param methodName
	 * @param descrs
	 * @throws IntrospectionException
	 */
	private static void proverProperties(final String prefix, final Method method, final Map<String, PropertyDescriptor> descrs, final Class<?> cls) throws IntrospectionException {
		final String methodName = method.getName();
		if (methodName.startsWith(prefix)) {
			final String javaPropertyName = uncapitalize(methodName.substring(prefix.length()));
			if ("class".equals(javaPropertyName)) {
				return;
			}
			final PropertyDescriptor pd = new PropertyDescriptor(javaPropertyName, cls);
			final PreferencebleProperty annotation = method.getAnnotation(PreferencebleProperty.class);
			String prefePropertyName = javaPropertyName;
			if (annotation != null) {
				prefePropertyName = annotation.name();
			}
			descrs.put(prefePropertyName, pd);
		}
	}

	MyPreferences(final Preferences aPreferences) {
		pref = aPreferences;
	}

	@Override
	public String absolutePath() {
		return pref.absolutePath();
	}

	@Override
	public void addNodeChangeListener(final NodeChangeListener aArg0) {
		pref.addNodeChangeListener(aArg0);
	}

	@Override
	public void addPreferenceChangeListener(final PreferenceChangeListener aArg0) {
		pref.addPreferenceChangeListener(aArg0);
	}

	@Override
	public void clear() throws BackingStoreException {
		pref.clear();
	}

	@Override
	public boolean equals(final Object aObj) {
		return pref.equals(aObj);
	}

	@Override
	public void exportNode(final OutputStream aArg0) throws IOException, BackingStoreException {
		pref.exportNode(aArg0);
	}

	@Override
	public void exportSubtree(final OutputStream aArg0) throws IOException, BackingStoreException {
		pref.exportSubtree(aArg0);
	}

	@Override
	public void flush() throws BackingStoreException {
		pref.flush();
	}

	@Override
	public String get(final String key, final String defvalue) {
		final StringBuilder sb = new StringBuilder();
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
		if (NULL.equals(s)) {
			return null;
		}
		return s;
	}

	@SuppressWarnings("unchecked")
	public <T> T getAnyElement(final String key, final T defx, final Class<T> cls) {
		if (cls.isEnum()) {
			// Nedokázal jsem to jinak
			@SuppressWarnings("rawtypes")
			final Enum<?> enumxx = getEnumxx(key, (Enum<?>) defx, (Class<Enum>) cls);
			final T result = (T) enumxx;
			return result;
		} else {
			if (cls.isAnnotationPresent(Preferenceble.class)) {
				try {
					final T result = getStructure(key, cls.newInstance());
					return result;
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
			} else {
				final Duo duo = sMetody.get(cls);
				if (duo == null) {
					throw new RuntimeException("Neexistuje pristupova metoda pro element typu: " + cls + ", mozna je to struktura a postrada anotaci");
				}
				try {
					final T result = (T) duo.get.invoke(this, key, defx);
					return result;
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public <E extends Atom> E getAtom(final String key, final E def, final Class<E> cls) {
		final String val = get(key, def == null ? null : def.name());
		return Atom.valueOf(cls, val);
	}

	public <E extends Atom> Set<E> getAtomSet(final String key, final Set<E> def, final Class<E> cls) {
		final String ss = get(key, null);
		if (ss == null) {
			return def;
		}
		try {
			final List<E> atoms = unpack(ss, s -> Atom.valueOf(cls, s));
			final Set<E> set = Atom.noneOf(cls);
			set.addAll(atoms);
			return set;
		} catch (final Exception e) { // pokud je tam něco blbě, bere se default
			FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Nesmysl v preferencich: " + key + "=" + ss);
			remove(key);
			return def;
		}
	}

	@Override
	public boolean getBoolean(final String key, final boolean value) {
		return pref.getBoolean(key, value);
	}

	@Override
	public byte[] getByteArray(final String key, final byte[] value) {
		return pref.getByteArray(key, value);
	}

	public Color getColor(final String name, final Color defaultBarva) {
		return new Color(getInt(name, defaultBarva.getRGB()), true);
	}

	public Dimension getDimension(final String key, final Dimension def) {
		final String s = get(key, pack(def));
		if (s == null) {
			return null;
		}
		final String[] ss = s.split(",");
		try {
			return new Dimension(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
		} catch (final NumberFormatException e) {
			FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Nesmysl v preferencich: " + key + "=" + s);
			remove(key); // když je tam blbost, raději mažeme
			return def;
		}
	}

	@Override
	public double getDouble(final String key, final double value) {
		return pref.getDouble(key, value);
	}

	public <T extends Enum<T>> T getEnum(final String key, final T def, final Class<T> cls) {
		final String s = get(key, def == null ? null : def.name());
		if (s == null) {
			return null;
		}
		T result;
		try {
			result = Enum.valueOf(cls, s);
		} catch (final java.lang.IllegalArgumentException e) {
			// nebudeme nic dělat ani odstraňovat. Může se to hodit jiné verzi
			return def; // vrátit default, jako by tam nic nebylo
		} catch (final Exception e) { // když je to špatná hodnota, jako by nebyla žádná
			FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Nesmysl v preferencich: " + key + "=" + s);
			remove(key);
			return def;
		}
		return result;
	}

	public <E extends Enum<E>> EnumSet<E> getEnumSet(final String key, final EnumSet<E> def, final Class<E> cls) {
		final String ss = get(key, null);
		if (ss == null) {
			return def;
		}
		try {
			final List<E> enums = unpack(ss, s -> Enum.valueOf(cls, s));
			final EnumSet<E> set = EnumSet.noneOf(cls);
			set.addAll(enums);
			return set;
		} catch (final Exception e) {
			FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Nesmysl v preferencich: " + key + "=" + ss);
			remove(key);
			return def;
		}
	}

	// nedokázal jsem to jinak, abych to mohl mít v getAnyElement
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Enum getEnumxx(final String key, final Enum<?> def, final Class<Enum> cls) {
		final String s = get(key, def == null ? null : def.name());
		if (s == null) {
			return null;
		}
		Enum result;
		try {
			result = Enum.valueOf(cls, s);
		} catch (final Exception e) { // když je to špatná hodnota, jako by nebyla žádná
			FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Nesmysl v preferencich: " + key + "=" + s);
			remove(key);
			return def;
		}
		return result;
	}

	public File getFile(final String key, final File defalt) {
		File result;
		try {
			final String fileStr = get(key, defalt == null ? null : defalt.getPath());
			result = fileStr == null ? null : new File(fileStr);
		} catch (final RuntimeException e) {
			throw new RuntimeException("key=" + key + ", defalt=" + defalt, e);
		}
		return result;
	}

	public Collection<File> getFileCollection(final String key, final Collection<File> def) {
		final String s = get(key, null);
		if (s == null) {
			return def;
		}
		return unpack(s, s1 -> new File(s1));
	}

	public Filex getFilex(final String key, Filex defalt) {

		if (defalt == null) {
			defalt = new Filex(null, false, false);
		}
		final Filex result = new Filex(getFile(key, defalt.getFile()), getBoolean(key + "_relativeToProgram", defalt.isRelativeToProgram()), getBoolean(key + "_active", defalt.isActive()));
		return result.getFile() == null ? null : result;
	}

	@Override
	public float getFloat(final String key, final float value) {
		return pref.getFloat(key, value);
	}

	public Font getFont(final String name, final Font fontdef) {
		final String fontstr = get(name, fontToString(fontdef));
		final Font font = Font.decode(fontstr);
		return font;
	}

	@Override
	public int getInt(final String key, final int value) {
		return pref.getInt(key, value);
	}

	@Override
	public long getLong(final String key, final long value) {
		return pref.getLong(key, value);
	}

	public Mou getMou(final String key, final Mou def) {
		final String s = get(key, pack(def));
		if (s == null) {
			return null;
		}
		final String[] ss = s.split(",");
		try {
			return new Mou(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
		} catch (final NumberFormatException e) {
			FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Nesmysl v preferencich: " + key + "=" + s);
			remove(key); // když je tam blbost, raději mažeme
			return def;
		}
	}

	public Point getPoint(final String key, final Point def) {
		final String s = get(key, pack(def));
		if (s == null) {
			return null;
		}
		final String[] ss = s.split(",");
		try {
			return new Point(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]));
		} catch (final NumberFormatException e) {
			FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Nesmysl v preferencich: " + key + "=" + s);
			remove(key); // když je tam blbost, raději mažeme
			return def;
		}
	}

	public List<String> getStringList(final String key, final List<String> defval) {
		final String ss = get(key, null);
		if (ss == null) {
			return defval;
		}
		return unpack(ss);
	}

	public Set<String> getStringSet(final String key, final Set<String> defval) {
		final List<String> stringList = getStringList(key, null);
		if (stringList == null) {
			return defval;
		} else {
			return new HashSet<>(stringList);
		}
	}

	public <T> T getStructure(final String name, final T defautStructure) {
		try {
			@SuppressWarnings("unchecked")
			final Class<T> cls = (Class<T>) defautStructure.getClass();
			final T stucture = cls.newInstance();
			if (!cls.isAnnotationPresent(Preferenceble.class)) {
				throw new RuntimeException("Klasa " + cls + " postrada anotaci " + Preferenceble.class);
			}
			final MyPreferences node = node(name);
			for (final Map.Entry<String, PropertyDescriptor> entry : findProperties(cls).entrySet()) {
				final Object defaultValue = entry.getValue().getReadMethod().invoke(defautStructure);
				@SuppressWarnings("unchecked")
				final Class<Object> valueType = (Class<Object>) entry.getValue().getPropertyType();
				final Object newValue = node.getAnyElement(entry.getKey(), defaultValue, valueType);
				entry.getValue().getWriteMethod().invoke(stucture, newValue);
			}
			return stucture;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Wgs getWgs(final String key, final Wgs def) {
		final String s = get(key, pack(def));
		if (s == null) {
			return null;
		}
		final String[] ss = s.split(",");
		try {
			return new Wgs(Double.parseDouble(ss[0]), Double.parseDouble(ss[1]));
		} catch (final NumberFormatException e) {
			FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Nesmysl v preferencich: " + key + "=" + s);
			remove(key); // když je tam blbost, raději mažeme
			return def;
		}
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
	public MyPreferences node(final String aArg0) {
		return new MyPreferences(pref.node(aArg0));
	}

	@Override
	public boolean nodeExists(final String aArg0) throws BackingStoreException {
		return pref.nodeExists(aArg0);
	}

	@Override
	public Preferences parent() {
		return pref.parent();
	}

	@Override
	public void put(final String key, final String value) {
		if (key.matches(PART_PATTERN + "[0-9]+")) {
			throw new IllegalArgumentException("Unable to insert a key with name " + key + "! This" + "key name is reserved for multi-part values");
		}
		if (value == null) {
			put(key, NULL);
		} else {
			if (value.length() <= Preferences.MAX_VALUE_LENGTH) {
				pref.put(key, value);
			} else {
				final String first = value.substring(0, Preferences.MAX_VALUE_LENGTH);
				final Iterable<String> rest = Splitter.fixedLength(Preferences.MAX_VALUE_LENGTH).split(value.substring(Preferences.MAX_VALUE_LENGTH));
				pref.put(key, first);
				int i = 0;
				for (final String s : rest) {
					pref.put(key + String.format(NUMBERED_PART_PATTERN, i++), s);
				}
			}
		}
	}

	public <T> void putAnyElement(final String name, final T value, final Class<T> cls) {
		if (cls.isEnum()) {
			putEnum(name, (Enum<?>) value);
		} else {
			if (cls.isAnnotationPresent(Preferenceble.class)) {
				putStructure(name, value);
			} else {
				final Duo duo = sMetody.get(cls);
				if (duo == null) {
					throw new RuntimeException("Neexistuje pristupova metoda pro element typu: " + cls + ", mozna je to struktura a postrada anotaci");
				}
				try {
					duo.put.invoke(this, name, value);
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public <E extends Atom> void putAtom(final String key, final E val) {
		put(key, val == null ? null : val.name());
	}

	public <E extends Atom> void putAtomSet(final String key, final Set<E> val) {
		put(key, pack(val));
	}

	@Override
	public void putBoolean(final String key, final boolean value) {
		pref.putBoolean(key, value);
	}

	@Override
	public void putByteArray(final String key, final byte[] value) {
		pref.putByteArray(key, value);
	}

	public void putColor(final String nameg, final Color color) {
		putInt(nameg, color.getRGB());
	}

	// Dimension
	public void putDimension(final String key, final Dimension bod) {
		put(key, pack(bod));
	}

	@Override
	public void putDouble(final String key, final double value) {
		pref.putDouble(key, value);
	}

	public void putEnum(final String key, final Enum<?> e) {
		put(key, e == null ? null : e.name());
	}

	public <E extends Enum<E>> void putEnumSet(final String key, final EnumSet<E> val) {
		put(key, pack(val));
	}

	public void putFile(final String key, final File file) {
		put(key, file.getPath());
	}

	public void putFileCollection(final String key, final Collection<File> fileCollection) {
		put(key, pack(fileCollection));
	}

	public void putFilex(final String key, final Filex filex) {
		put(key, filex.getFile().getPath());
		putBoolean(key + "_relativeToProgram", filex.isRelativeToProgram());
		putBoolean(key + "_active", filex.isActive());
	}

	@Override
	public void putFloat(final String key, final float value) {
		pref.putFloat(key, value);
	}

	public void putFont(final String name, final Font font) {
		put(name, fontToString(font));
	}

	@Override
	public void putInt(final String key, final int value) {
		pref.putInt(key, value);
	}

	@Override
	public void putLong(final String key, final long value) {
		pref.putLong(key, value);
	}

	// Mou
	public void putMou(final String key, final Mou bod) {
		put(key, pack(bod));
	}

	// Point
	public void putPoint(final String key, final Point bod) {
		put(key, pack(bod));
	}

	public void putStringList(final String key, final List<String> val) {
		put(key, pack(val));
	}

	public void putStringSet(final String key, final Set<String> val) {
		put(key, pack(new ArrayList<>(val)));
	}

	public void putStructure(final String name, final Object structure) {
		try {
			final Class<?> cls = structure.getClass();
			if (!cls.isAnnotationPresent(Preferenceble.class)) {
				throw new RuntimeException("Klasa " + cls + " postrada anotaci " + Preferenceble.class);
			}
			final MyPreferences node = node(name);
			for (final Map.Entry<String, PropertyDescriptor> entry : findProperties(cls).entrySet()) {
				final Object value = entry.getValue().getReadMethod().invoke(structure);
				@SuppressWarnings("unchecked")
				final Class<Object> valueType = (Class<Object>) entry.getValue().getPropertyType();
				node.putAnyElement(entry.getKey(), value, valueType);
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	// Wgs
	public void putWgs(final String key, final Wgs bod) {
		put(key, pack(bod));
	}

	@Override
	public void remove(final String key) {
		pref.remove(key);
	}

	@Override
	public void removeNode() throws BackingStoreException {
		pref.removeNode();
	}

	@Override
	public void removeNodeChangeListener(final NodeChangeListener aArg0) {
		pref.removeNodeChangeListener(aArg0);
	}

	@Override
	public void removePreferenceChangeListener(final PreferenceChangeListener aArg0) {
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

	private String fontStyleToString(final int style) {
		switch (style) {
		case Font.PLAIN:
			return "PLAIN";
		case Font.ITALIC:
			return "ITALIC";
		case Font.BOLD:
			return "BOLD";
		case Font.BOLD + Font.ITALIC:
			return "BOLDITALIC";
		}
		return "";
	}

	private String fontToString(final Font font) {
		return font.getFamily() + "-" + fontStyleToString(font.getStyle()) + "-" + font.getSize();

	}

	private StringBuilder listEscapeToSb(final String toEscape, final StringBuilder sb) {
		final int length = toEscape.length();
		for (int i = 0; i < length; ++i) {
			final char c = toEscape.charAt(i);
			if (c == LIST_DELIMITER_CHAR || c == ESCAPE_CHAR) {
				sb.append(ESCAPE_CHAR);
			}
			sb.append(c);
		}
		return sb;
	}

	private String pack(final Atom val) {
		return val == null ? null : val.name();
	}

	private String pack(final Collection<?> val) {
		final StringBuilder sb = new StringBuilder();
		for (final Object singleVal : val) {
			listEscapeToSb(pack(singleVal), sb).append(LIST_DELIMITER_CHAR);
		}
		return sb.toString();
	}

	private String pack(final Dimension bod) {
		return bod == null ? null : bod.width + "," + bod.height;
	}

	private String pack(final Enum<?> val) {
		return val == null ? null : val.name();
	}

	private String pack(final File f) {
		try {
			return f.getCanonicalPath();
		} catch (final IOException e) {
			throw new IllegalArgumentException("Unable to retrieve canonical path from " + f, e);
		}
	}

	private String pack(final Mou bod) {
		return bod == null ? null : bod.xx + "," + bod.yy;
	}

	@SuppressWarnings("rawtypes")
	private String pack(final Object o) {
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
			return pack((Mou) o);
		}
		if (o instanceof File) {
			return pack((File) o);
		}
		throw new IllegalArgumentException("Unknown object to pack : " + o.getClass() + " " + o);
	}

	private String pack(final Point bod) {
		return bod == null ? null : bod.x + "," + bod.y;
	}

	private String pack(final String s) {
		return s;
	}

	private String pack(final Wgs bod) {
		return bod == null ? null : bod.lat + "," + bod.lon;
	}

	private List<String> unpack(final String packedString) {
		if (packedString == null) {
			return null;
		}
		final List<String> toReturn = new ArrayList<>();
		final StringBuilder sb = new StringBuilder();
		boolean escaped = false;
		for (int i = 0; i < packedString.length(); ++i) {
			final char c = packedString.charAt(i);
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

	private <T> List<T> unpack(final String packedString, final Function<String, T> parseFunc) {
		return new ArrayList<>(Collections2.transform(unpack(packedString), parseFunc));
	}
}
