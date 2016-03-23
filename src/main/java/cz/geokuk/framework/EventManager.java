/**
 *
 */
package cz.geokuk.framework;

import java.lang.ref.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import javax.swing.SwingUtilities;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;
import cz.geokuk.util.pocitadla.*;

/**
 * @author veverka
 *
 */
public class EventManager implements EventFirer {

	private static final Logger		log							= LogManager.getLogger(EventManager.class.getSimpleName());

	private static Pocitadlo		pocitTypy					= new PocitadloMalo("Počet typů registrovaných eventů",
			"Kolik registrovaných typů eventů (tříd) je ve třídě Eventmanager, mělo by to být v v jednotkách či malých desítkách a nemělo by narůstat.");
	private static Pocitadlo		pocitObservery				= new PocitadloMalo("Počet observerů za všechny typy dohromady",
			"Kolik celkem observerů je regostrováno pro události. Musí být v desítkách a nesmí růst.");
	private static Pocitadlo		pocitReistraceOdregistrace	= new PocitadloRoste("Počet registrací observerů",
			"Kolikrát se registrovaly a odregistrovávaly observery (dohromady), číslo stále roste, ale nesmí moc rychle, neboť registrace a deregistrace nemusí být laciná,"
					+ " ale pravděpodobně poroste s otvíráním a zavíráním různých oken.");
	public ReferenceQueue<Object>	referencequeue				= new ReferenceQueue<>();

	Map<BeanType, Observry>			mapaclsobs					= new HashMap<>();

	private int						urovenZanoreni;

	public EventManager() {
		final Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				for (;;) {
					try {
						final Reference<?> ref = referencequeue.remove();
						final ObserverInvocation obsin = (ObserverInvocation) ref;
						synchronized (this) {
							obsin.iParentList.remove(obsin);
							aktualizujPocitadla();
						}
					} catch (final InterruptedException e) {
						FExceptionDumper.dump(e, EExceptionSeverity.WORKARROUND, "Přerušení metodou interrupt(), taková výjimka asi nikdy nenastane.");
					}
				}
			}
		}, "Likvidátor observerů");
		thread.start();
	}

	private void aktualizujPocitadla() {
		pocitReistraceOdregistrace.inc();
		pocitTypy.set(mapaclsobs.size());
		int n = 0;
		for (final Observry o : mapaclsobs.values()) {
			n += o.listo.size();
		}
		pocitObservery.set(n);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see cz.geokuk.events.EventFirer#fire(E)
	 */
	@Override
	public void fire(final Event0<?> event) {
		event.setEventFirer(this);
		event.lock();
		final boolean logovatx = maSeLogovat(event);
		urovenZanoreni++;
		if (logovatx) {
			log("FIRE beg: " + event);
		}
		try {
			if (!SwingUtilities.isEventDispatchThread()) {
				throw new RuntimeException("Event " + event + "poslan mimo eventove vlakno");
			}
			final BeanType beanType = BeanType.createForInjectedBean(event);
			_fire(beanType, event);
			if (beanType.hasSubtype()) {
				final BeanType beanTypeWithoutSubtype = beanType.cloneWithotSubtype();
				_fire(beanTypeWithoutSubtype, event);
			}
		} finally {
			if (logovatx) {
				log("FIRE end: " + event);
			}
			urovenZanoreni--;
		}
	}

	private void _fire(final BeanType beanType, final Event0<?> event) {
		ObserverInvocation[] invocations;
		synchronized (this) {
			final Observry observry = ziskejObservry(beanType);
			invocations = observry.listo.toArray(new ObserverInvocation[observry.listo.size()]);
			observry.lastEvent = event;
		}
		for (final ObserverInvocation invocation : invocations) {
			// System.out.println("FIRE: " + event + " ===> " + eveon);
			// long cas = System.currentTimeMillis();
			vyvolej(event, invocation);
			// System.out.printf("Cas %d zpracovani udalosti %s - %s\n", System.currentTimeMillis() - cas, event, invocation);
		}
	}

	/**
	 * @param <E>
	 * @param event
	 * @param invocation
	 */
	private <E> void vyvolej(final E event, final ObserverInvocation invocation) {
		final boolean logovat = maSeLogovat(event);
		urovenZanoreni++;
		if (logovat) {
			log("INVOKE beg " + invocation + " " + event);
		}
		try {
			invocation.invoke(event);
		} finally {
			if (logovat) {
				log("INVOKE end " + invocation + " " + event);
			}
			urovenZanoreni--;
		}
	}

	/**
	 * @param <E>
	 * @param event
	 * @return
	 */
	private <E> boolean maSeLogovat(final E event) {
		final boolean logovat = false;
		// boolean logovat = event.getClass() != ZmenaSouradnicMysiEvent.class;
		return logovat;
	}

	/**
	 * @param string
	 */
	private void log(final String text) {
		final String pading = "                                                                                 ".substring(0, urovenZanoreni * 3);
		log.debug(pading + text);
	}

	private synchronized <E> Observry ziskejObservry(final BeanType beanType) {
		Observry observry = mapaclsobs.get(beanType);
		if (observry == null) {
			// Už zde není slabá mapa
			observry = new Observry();
			mapaclsobs.put(beanType, observry);
		}
		return observry;
	}

	private class Observry {
		List<ObserverInvocation>	listo	= new LinkedList<>();
		private Object				lastEvent;
	}

	public void registerWeakly(final Object observer, final boolean aOnlyInvoke) {
		register(observer, aOnlyInvoke);
	}

	/**
	 * Zajistí, že se na daném objektu budou volat metody "onEvent" s příslušnými typy parametrů.
	 *
	 * @param observer
	 */
	private void register(final Object observer, final boolean aOnlyInvoke) {
		urovenZanoreni++;
		final boolean logovat = maSeLogovat(null);
		if (logovat) {
			log("REGISTER beg " + observer);
		}
		try {
			_register(observer, aOnlyInvoke);
		} finally {
			if (logovat) {
				log("REGISTER end " + observer);
			}
			urovenZanoreni--;
		}

	}

	private void _register(final Object observer, final boolean aOnlyInvoke) {
		for (final Method m : observer.getClass().getMethods()) {
			if (!m.getName().equals("onEvent")) {
				continue;
			}
			final Class<?>[] parameterTypes = m.getParameterTypes();
			if (parameterTypes.length != 1) {
				return;
			}
			register(observer, m, aOnlyInvoke);
		}
	}

	/**
	 * @param aTypeventu
	 * @param aObserver
	 * @param method
	 * @param aStrongly
	 * @param aOnlyInvoke
	 *            Parametr říká, že se vlastně neprovádí registrace, ele pouze se pošlou současné eventy
	 */
	private void register(final Object aObserver, final Method method, final boolean aOnlyInvoke) {
		Observry observry;
		ObserverInvocation invocation;
		synchronized (this) {
			observry = ziskejObservry(BeanType.createForTargetMethod(aObserver, method));
			invocation = new ObserverInvocation(aObserver, method, observry.listo);
			if (!aOnlyInvoke) {
				observry.listo.add(invocation);
			}
			aktualizujPocitadla();
		}
		if (observry.lastEvent != null) {
			vyvolej(observry.lastEvent, invocation);
		}
	}

	public void unregister(final Object aObserver) {
		synchronized (this) {
			for (final Observry observry : mapaclsobs.values()) {
				for (final Iterator<ObserverInvocation> it = observry.listo.iterator(); it.hasNext();) {
					final ObserverInvocation oi = it.next();
					if (oi.get() == aObserver) {
						it.remove(); // pro tento observer iž nebudeme poslouchat
					}
				}
			}
			aktualizujPocitadla();
		}
	}

	private class ObserverInvocation extends WeakReference<Object> {
		Method									observerOnEventMethod;
		private final List<ObserverInvocation>	iParentList;
		private final Class<?>					observerClass;

		/**
		 * @param aObserverObject
		 * @param aObserverOnEventMethod
		 * @param aStrongly
		 * @param aEvent
		 */
		public ObserverInvocation(final Object aObserverObject, final Method aObserverOnEventMethod, final List<ObserverInvocation> parentList) {
			super(aObserverObject, referencequeue);
			observerClass = aObserverObject.getClass();
			observerOnEventMethod = aObserverOnEventMethod;
			iParentList = parentList;
		}

		public void invoke(final Object event) {
			final Object observerObject = get();
			if (observerObject == null) {
				log.debug("Vypadlo nam: " + observerClass);
				return;
			}
			try {
				observerOnEventMethod.invoke(observerObject, event);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String toString() {
			return "ObserverInvocation [observerOnEventMethod=" + observerOnEventMethod + "]";
		}

	}

}
