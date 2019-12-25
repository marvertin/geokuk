package cz.geokuk.framework.container;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

import lombok.*;

/**
 * Třída zodpovědná za instanciaci beanů pomocí konstruktorů a dependency injekce.
 * @author Martin
 *
 */
public class Instantier {

	private final Map<Class<?>, Object> instances = new HashMap<Class<?>, Object>();
	private final Set<Class<?>> praveVytvarene = new HashSet<>();
	private final Map<Class<?>, List<Class<?>>> predkyNaPotomky;

	public Instantier(final Set<Class<?>> types) {

		final Map<Class<?>, Set<Duo>> map1 = types.stream()
				.flatMap(potomek -> expandToSuperTypes(potomek).stream()
						.map(predek -> new Duo(potomek, predek)))
				.collect(Collectors.groupingBy(Duo::getPredek,
						Collectors.toSet()));

		predkyNaPotomky = Collections.unmodifiableMap(
				map1.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey,
						es -> es.getValue().stream().map(d -> d.predek).collect(Collectors.toList()) ))
				);

	}

	/**
	 * Vyvoří novou instancí třídy.
	 * @param cls Třída musí být konkrétní a musí mít jediný kosntruktor a všechny jeho paramatry
	 * musí být jednoznačně nalezeny jako singletony.
	 * @return Nově stvořený objekt.
	 */
	public <T> T newInstance(final Class<T> cls) {
		final Constructor<T> constructor = findOneConstructor(cls);
		final Object[] args = Arrays.stream(constructor.getParameterTypes())
				.map(this::findByClass)
				.toArray();
		return newInstance(constructor, args);

	}

	@SuppressWarnings("unchecked")
	public synchronized <T> T getSingletonInstance(final Class<T> cls) {
		return (T) instances.computeIfAbsent(cls, this::newInstance);
	}

	@SneakyThrows
	private synchronized <T> T newInstance(final Constructor<T> constructor, final Object[] args)  {
		final boolean vlozeno = praveVytvarene.add(constructor.getDeclaringClass());
		if (!vlozeno) {
			throw new IllegalStateException("Circular dependency: " + praveVytvarene);
		}
		try {
			final T instance = constructor.newInstance(args);
			return instance;
		} finally {
			praveVytvarene.remove(constructor.getDeclaringClass());
		}
	}

	private Object findByClass(final Class<?> cls) {
		if (cls.isAssignableFrom(List.class)) {
			// todo pole vyřídit
			final Class<?> typPrvkuListu = (Class<?>) ((ParameterizedType)cls.getClass().getGenericSuperclass())
					.getActualTypeArguments()[0];
			final List<Class<?>> konkretClasses = predkyNaPotomky.get(typPrvkuListu);
			if (konkretClasses == null) {
				throw new IllegalStateException("Excpected 1 ormore but found no instances of class '" + cls.getName() +"'");
			}
			return konkretClasses.stream().map(this::getSingletonInstance).collect(Collectors.toList());
		} else {
			final List<Class<?>> konkretClases = predkyNaPotomky.getOrDefault(cls, Collections.emptyList());
			if (konkretClases.size() != 1) {
				throw new IllegalStateException("Excpected 1 but " + konkretClases.size() +  " instances of class '" + cls.getName() +"' :" + konkretClases);
			}
			return getSingletonInstance(konkretClases.get(0));
		}
	}


	/**
	 * Vyhledá konstruktor na třídě a zajistí, že se jedná o jediný konstruktor
	 * @param <T>
	 * @param cls
	 * @return
	 */
	private <T> Constructor<T> findOneConstructor(final Class<T> cls) {
		final Constructor<?>[] constructors = cls.getConstructors();
		if (constructors.length != 1) {
			throw new IllegalStateException("Bad number of constructors " + constructors.length + " in class " + cls.getName() + " " + Arrays.asList(constructors));
		}
		@SuppressWarnings("unchecked")
		final
		Constructor<T> constructor = (Constructor<T>) constructors[0];
		return constructor;
	}

	/**
	 * Expanduje class do množiny třídobsahující sebe všechny své předky a všechny interfacy.
	 * @param cls
	 * @return
	 */
	private Set<Class<?>> expandToSuperTypes(final Class<?> cls) {
		final Set<Class<?>> set = new HashSet<Class<?>>();
		_expandToSuperTypes(cls, set);
		return set;
	}


	private void _expandToSuperTypes(final Class<?> cls, final Set<Class<?>> result) {
		if (cls == null) {
			return;
		}
		result.add(cls);
		_expandToSuperTypes(cls.getSuperclass(), result);
		for (final Class<?> itf : cls.getInterfaces()) {
			_expandToSuperTypes(itf, result);
		}
	}

	@RequiredArgsConstructor
	@Getter @ToString
	private static class Duo {
		private final Class<?> potomek;
		private final Class<?> predek;
	}
}
