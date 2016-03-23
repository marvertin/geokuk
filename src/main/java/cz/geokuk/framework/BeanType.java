package cz.geokuk.framework;

import java.lang.reflect.Method;

public class BeanType {

	private final Class<?> cls;
	private final String subType;
	private final boolean multinInjectionSupported;

	public static BeanType createForInjectedBean(final Object injectedBean) {
		String subType;
		if (injectedBean instanceof BeanSubtypable) {
			final BeanSubtypable beanSubtypable = (BeanSubtypable) injectedBean;
			subType = beanSubtypable.getSubType();
		} else {
			subType = null;
		}
		return new BeanType(injectedBean.getClass(), subType, false);
	}

	/**
	 * Vytvoří bean type pro cílovou metodu
	 *
	 * @param targetBean
	 *            Bean, na kterém má být voláno.
	 * @param method
	 *            Metoda, která má být volána.
	 */
	public static BeanType createForTargetMethod(final Object targetBean, final Method method) {
		final Class<?>[] parameterTypes = method.getParameterTypes();
		final Class<?> cls = parameterTypes[0];
		final BeanSubtype annotation = method.getAnnotation(BeanSubtype.class);
		String subType;
		if (annotation != null) {
			if ("_use_interface_BeanSubtypable_".equals(annotation.value())) {
				if (targetBean instanceof BeanSubtypable) {
					final BeanSubtypable beanSubtypable = (BeanSubtypable) targetBean;
					subType = beanSubtypable.getSubType();
				} else {
					throw new RuntimeException("Kdyz ma metoda anotaci BeanTyype bez parametru, tak objekt musi imlementovat BeanSubtypable");
				}
			} else {
				subType = annotation.value();
			}
		} else {
			subType = null;
		}
		final BeanType bt = new BeanType(cls, subType, method.isAnnotationPresent(MultiInjection.class));
		return bt;
	}

	private BeanType(final Class<?> cls, final String subType, final boolean multiInjection) {
		this.cls = cls;
		this.subType = subType;
		multinInjectionSupported = multiInjection;
	}

	public boolean canInjectFrom(final BeanType injectedBeanType) {
		if (!cls.isAssignableFrom(injectedBeanType.cls)) {
			return false; // při nekompatibilních třídách vůbec neuvažujma injektování
		}
		// teď už víme, že třídy jsou kompatibilní a že by to šlo rozhodnou subtypy
		if (subType == null) {
			return true; // bez subtypů jse cíl, takže mu nezáleží na to, co do něj přijde, čili injektujeme
		}
		// teď už víme, že cíl definuje subtyp
		if (subType.equals(injectedBeanType.subType)) {
			return true; // tak ten musí mít zdroj úplně stejný
		}
		return false; // tak to ono není nic pro injektování
	}

	public BeanType cloneWithotSubtype() {
		final BeanType result = new BeanType(cls, null, multinInjectionSupported);
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final BeanType other = (BeanType) obj;
		if (cls == null) {
			if (other.cls != null) {
				return false;
			}
		} else if (!cls.equals(other.cls)) {
			return false;
		}
		if (subType == null) {
			if (other.subType != null) {
				return false;
			}
		} else if (!subType.equals(other.subType)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (cls == null ? 0 : cls.hashCode());
		result = prime * result + (subType == null ? 0 : subType.hashCode());
		return result;
	}

	public boolean hasSubtype() {
		return subType != null;
	}

	/**
	 * @return
	 */
	public boolean isMultiInjectionSupported() {
		return multinInjectionSupported;
	}

	@Override
	public String toString() {
		return "BeanType [cls=" + cls + ", subType=" + subType + "]";
	}

}
