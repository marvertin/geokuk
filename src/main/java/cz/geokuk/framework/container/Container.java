package cz.geokuk.framework.container;

import java.util.Set;

import org.reflections.Reflections;

public class Container {


	private final Instantier instantier;


	public static Container scan(final String pack) {

		final Reflections refl = new Reflections(pack);
		final Set<Class<?>> types = refl.getTypesAnnotatedWith(Byn.class);
		return new Container(new Instantier(types));

	}

	public Container(final Instantier instantier) {
		this.instantier = instantier;
	}

	public <T> T newInstance(final Class<T> cls) {
		return instantier.newInstance(cls);
	}

	public <T> T getSingletonInstance(final Class<T> cls) {
		return instantier.getSingletonInstance(cls);
	}


}
