package cz.geokuk.framework;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Kontejner, který drží objekty a injektuje je.
 *
 * @author tatinek
 *
 */
public class BeanBag implements Factory {

	private boolean				initialized;

	private final List<Object>	beans	= new ArrayList<>();

	private EventManager		eveman;

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.program.Factory#create(java.lang.Class, java.lang.Object)
	 */
	@Override
	public <T> T create(final Class<T> klasa, final Object... params) {
		if (!initialized) {
			throw new RuntimeException("Kontejner jeste nebyl inicializovan");
		}
		final Class<?>[] types = new Class<?>[params.length];
		for (int i = 0; i < params.length; i++) {
			final Object param = params[i];
			types[i] = param == null ? null : param.getClass();
		}
		try {
			final Constructor<T> constructor = klasa.getConstructor(types);
			final T obj = constructor.newInstance(params);
			init(obj);
			return obj;
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * @param <T>
	 * @param obj
	 */
	@Override
	public <T> T init(final T obj) {
		inject(obj);
		callAfterInjectInit(obj);
		registerEventReceiver(obj, false);
		callAfterEventReceiverRegistrationInit(obj);
		return obj;
	}

	/**
	 * @param <T>
	 * @param obj
	 */
	@Override
	public <T> T initNow(final T obj) {
		inject(obj);
		callAfterInjectInit(obj);
		registerEventReceiver(obj, true);
		callAfterEventReceiverRegistrationInit(obj);
		return obj;
	}

	public <T> T registerSigleton(final T object) {
		if (initialized) {
			throw new RuntimeException("Kontejner uz byl inicializovan");
		}
		if (object == null) {
			throw new RuntimeException("Nelze inicializvat null objekt");
		}
		beans.add(object);
		return object;
	}

	public void registrFieldsAsSingleton(final Object obj) {
		try {
			final Field[] fields = obj.getClass().getFields();
			for (final Field field : fields) {
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				registerSigleton(field.get(obj));
			}
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void init() {

		for (final Object bean : beans) {
			inject(bean);
		}
		for (final Object bean : beans) {
			callAfterInjectInit(bean);
		}
		for (final Object bean : beans) {
			registerEventReceiver(bean, false);
		}
		for (final Object bean : beans) {
			callAfterEventReceiverRegistrationInit(bean);
		}
		initialized = true;
	}

	/**
	 * @param bean
	 */
	private void callAfterEventReceiverRegistrationInit(final Object bean) {
		if (bean instanceof AfterEventReceiverRegistrationInit) {
			((AfterEventReceiverRegistrationInit) bean).initAfterEventReceiverRegistration();
		}
	}

	/**
	 * @param bean
	 */
	private void callAfterInjectInit(final Object bean) {
		if (bean instanceof AfterInjectInit) {
			((AfterInjectInit) bean).initAfterInject();
		}
	}

	/**
	 * @param bean
	 */
	private void registerEventReceiver(final Object bean, final boolean aOnlyInvoke) {
		eveman.registerWeakly(bean, aOnlyInvoke);
	}

	/**
	 * @param targetBean
	 */
	private void inject(final Object targetBean) {
		for (final Method method : targetBean.getClass().getMethods()) {
			if (method.getName().equals("inject")) {
				final BeanType targetBeanType = BeanType.createForTargetMethod(targetBean, method);
				final boolean multiInjection = targetBeanType.isMultiInjectionSupported();
				Object injectedBean = null; // sem budeme injektovat, pokud nepodporujeme multiinjekci
				int pocetInjekci = 0;
				for (final Object injectedBeanCandidate : beans) {
					final BeanType injectedBeanType = BeanType.createForInjectedBean(injectedBeanCandidate);
					if (targetBeanType.canInjectFrom(injectedBeanType)) {
						if (injectedBean != null) {
							throw new RuntimeException("Prilis mnoho implementaci pro " + method + " v " + injectedBean.getClass() + " a " + injectedBeanCandidate.getClass());
						}
						if (multiInjection) {
							callInjection(method, targetBean, injectedBeanCandidate);
							pocetInjekci++;
						} else {
							injectedBean = injectedBeanCandidate;
						}
					}
				}
				if (injectedBean != null) {
					callInjection(method, targetBean, injectedBean);
					pocetInjekci++;
				}
				if (pocetInjekci == 0) {
					throw new RuntimeException(String.format("Nenalena injekce targetBean=%s ... targetBeanType=%s ... method=%s", targetBean, targetBeanType, method));
				}
			}
		}
	}

	/**
	 * @param method
	 * @param targetBean
	 * @param injectedBean
	 */
	private void callInjection(final Method method, final Object targetBean, final Object injectedBean) {
		try {
			// System.out.println("INJEKTUJI:: " + targetBean.getClass() + " <== " + injectedBean.getClass() + " ... " + method);
			// System.out.println(m);
			method.invoke(targetBean, injectedBean);
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void inject(final EventManager eveman) {
		this.eveman = eveman;
	}
}
