package cz.geokuk.framework;

import java.util.*;

public abstract class Atom implements Comparable<Atom> {

	@SuppressWarnings("rawtypes")
	private static Map<Class<? extends Atom>, TypAtomu>	repo	= new HashMap<>();

	String												name;
	int													ordinal;

	@SafeVarargs
	public static <E extends Atom> Set<E> of(final E... types) {
		return new HashSet<>(Arrays.asList(types));
	}

	public static <E extends Atom> E valueOf(final Class<E> clazz, final String jmeno) {
		clazz.getFields();
		try {
			Class.forName(clazz.getName(), true, clazz.getClassLoader());
		} catch (final ClassNotFoundException e) { // to urcite uspeje
		}
		// AWptType fINALLOCATION = AWptType.FINAL_LOCATION;
		final TypAtomu<E> ta = dejTyp(clazz);
		E atom = ta.mapa.get(jmeno);
		if (atom == null) {
			atom = vytvorInstanci(clazz);
			atom.setName(jmeno);
			atom.setOrdinal(ta.mapa.size());
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

	public void setName(final String name) {
		this.name = name;
	}

	public void setOrdinal(final int ordinal) {
		this.ordinal = ordinal;
	}

	public static <E> Set<E> noneOf(final Class<E> e) {
		return new HashSet<>();
	}

	private static <E extends Atom> E vytvorInstanci(final Class<E> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private static <E extends Atom> TypAtomu<E> dejTyp(final Class<? extends Atom> typ) {
		@SuppressWarnings("unchecked")
		TypAtomu<E> typAtomu = repo.get(typ);
		if (typAtomu == null) {
			typAtomu = new TypAtomu<>();
			repo.put(typ, typAtomu);
		}
		return typAtomu;
	}

	private static class TypAtomu<E extends Atom> {
		Map<String, E> mapa = new LinkedHashMap<>();
	}

	@Override
	public String toString() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final Atom o) {
		return name.compareTo(o.name);
	}
}
