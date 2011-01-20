/**
 * 
 */
package cz.geokuk.framework;


import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import cz.geokuk.core.coord.ZmenaSouradnicMysiEvent;
import cz.geokuk.util.exception.EExceptionSeverity;
import cz.geokuk.util.exception.FExceptionDumper;
import cz.geokuk.util.pocitadla.Pocitadlo;
import cz.geokuk.util.pocitadla.PocitadloMalo;
import cz.geokuk.util.pocitadla.PocitadloRoste;



/**
 * @author veverka
 *
 */
public class EventManager implements EventFirer {

  private static Pocitadlo pocitTypy = new PocitadloMalo("Počet typů registrovaných eventů", "Kolik registrovaných typů eventů (tříd) je ve třídě Eventmanager, mělo by to být v v jednotkách či malých desítkách a nemělo by narůstat.");
  private static Pocitadlo pocitObservery = new PocitadloMalo("Počet observerů za všechny typy dohromady", "Kolik celkem observerů je regostrováno pro události. Musí být v desítkách a nesmí růst.");
  private static Pocitadlo pocitReistraceOdregistrace = new PocitadloRoste("Počet registrací observerů", "Kolikrát se registrovaly a odregistrovávaly observery (dohromady), číslo stále roste, ale nesmí moc rychle, neboť registrace a deregistrace nemusí být laciná," +
  " ale pravděpodobně poroste s otvíráním a zavíráním různých oken.");
  public ReferenceQueue<Object> referencequeue = new ReferenceQueue<Object>();

  Map<BeanType, Observry> mapaclsobs = new HashMap<BeanType, Observry>();

  private int urovenZanoreni;

  public EventManager() {
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        for (;;) {
          try {
            Reference<? extends Object> ref = referencequeue.remove();
            ObserverInvocation obsin = (ObserverInvocation) ref;
            synchronized(this) {
              obsin.iParentList.remove(obsin);
              aktualizujPocitadla();
            }
          } catch (InterruptedException e) {
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
    for (Observry o : mapaclsobs.values()) {
      n += o.listo.size();
    }
    pocitObservery.set(n);
  }

  /* (non-Javadoc)
   * @see cz.geokuk.events.EventFirer#fire(E)
   */
  @Override
  public void fire(Event0<?> event) {
    event.setEventFirer(this);
    event.lock();
    boolean logovatx = maSeLogovat(event);
    urovenZanoreni ++;
    if (logovatx) log("FIRE beg: " + event);
    try {
      if (!SwingUtilities.isEventDispatchThread()) {
        throw new RuntimeException("Event " + event + "poslan mimo eventove vlakno");
      }
      BeanType beanType = BeanType.createForInjectedBean(event);
      _fire(beanType, event);
      if (beanType.hasSubtype()) {
        BeanType beanTypeWithoutSubtype = beanType.cloneWithotSubtype();
        _fire(beanTypeWithoutSubtype, event);
      }
    } finally {
      if (logovatx) log("FIRE end: " + event);
      urovenZanoreni --;
    }
  }

  private void _fire(BeanType beanType, Event0<?> event) {
    ObserverInvocation[] invocations;
    synchronized (this) {
      Observry observry = ziskejObservry(beanType);
      invocations = observry.listo.toArray(new ObserverInvocation[observry.listo.size()]);
      observry.lastEvent = event;
    }
    for (ObserverInvocation invocation : invocations) {
      //      System.out.println("FIRE: " + event + " ===> " + eveon);
      //long cas = System.currentTimeMillis();
      vyvolej(event, invocation);
      //System.out.printf("Cas %d zpracovani udalosti %s - %s\n", System.currentTimeMillis() - cas, event, invocation);
    }
  }

  /**
   * @param <E>
   * @param event
   * @param invocation
   */
  private <E> void vyvolej(E event, ObserverInvocation invocation) {
    boolean logovat = maSeLogovat(event);
    urovenZanoreni ++;
    if (logovat) log("INVOKE beg " + invocation + " " + event);
    try {
      invocation.invoke(event);
    } finally {
      if (logovat) log("INVOKE end " + invocation + " " + event);
      urovenZanoreni --;
    }
  }

  /**
   * @param <E>
   * @param event
   * @return
   */
  private <E> boolean maSeLogovat(E event) {
    @SuppressWarnings("unused")
    boolean logovat = false
    && event.getClass() != ZmenaSouradnicMysiEvent.class;
    return logovat;
  }

  /**
   * @param string
   */
  private void log(String text) {
    String pading = "                                                                                 "
      .substring(0,urovenZanoreni*3);
    System.out.println(pading +  text);
  }

  private synchronized <E> Observry ziskejObservry(BeanType beanType) {
    Observry observry = mapaclsobs.get(beanType);
    if (observry == null) {
      // Už zde není slabá mapa
      observry = new Observry();
      mapaclsobs.put(beanType, observry);
    }
    return observry;
  }


  private class Observry {
    List<ObserverInvocation> listo = new LinkedList<ObserverInvocation>();
    private Object lastEvent;
  }


  public void registerWeakly(Object observer) {
    register(observer);
  }

  /**
   * Zajistí, že se na daném objektu zavolají metody "onEvent"
   * s příslušnými typy parametrů.
   * @param observer
   */
  private void register(Object observer) {
    urovenZanoreni ++;
    boolean logovat = maSeLogovat(null);
    if (logovat) log("REGISTER beg " + observer);
    try {
      _register(observer);
    } finally {
      if (logovat) log("REGISTER end " + observer);
      urovenZanoreni --;
    }

  }

  private void _register(Object observer) {
    for (Method m : observer.getClass().getMethods()) {
      if (! m.getName().equals("onEvent")) continue;
      Class<?>[] parameterTypes = m.getParameterTypes();
      if (parameterTypes.length != 1) return;
      register(observer, m);
    }
  }


  /**
   * @param aTypeventu
   * @param aObserver
   * @param method
   * @param aStrongly
   */
  private void register(Object aObserver, Method method) {
    Observry observry;
    ObserverInvocation invocation;
    synchronized (this) {
      observry = ziskejObservry(BeanType.createForTargetMethod(aObserver, method));
      invocation = new ObserverInvocation(aObserver, method, observry.listo);
      observry.listo.add(invocation);
      aktualizujPocitadla();
    }
    if (observry.lastEvent != null) {
      vyvolej(observry.lastEvent, invocation);
    }
  }

  public void unregister(Object aObserver) {
    synchronized (this) {
      for (Observry observry : mapaclsobs.values()) {
        for (Iterator<ObserverInvocation> it = observry.listo.iterator(); it.hasNext(); ) {
          ObserverInvocation oi = it.next();
          if (oi.get() == aObserver) {
            it.remove();  // pro tento observer iž nebudeme poslouchat
          }
        }
      }
      aktualizujPocitadla();
    }
  }


  private class ObserverInvocation extends WeakReference<Object> {
    Method observerOnEventMethod;
    private final List<ObserverInvocation> iParentList;
    private final Class<?> observerClass;

    /**
     * @param aObserverObject
     * @param aObserverOnEventMethod
     * @param aStrongly
     * @param aEvent
     */
    public ObserverInvocation(Object aObserverObject, Method aObserverOnEventMethod, List<ObserverInvocation> parentList) {
      super(aObserverObject, referencequeue );
      observerClass = aObserverObject.getClass();
      observerOnEventMethod = aObserverOnEventMethod;
      iParentList = parentList;
    }

    public void invoke(Object event) {
      Object observerObject = get();
      if (observerObject == null) {
        System.err.println("Vypadlo nam: " + observerClass);
        return;
      }
      try {
        observerOnEventMethod.invoke(observerObject, event);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public String toString() {
      return "ObserverInvocation [observerOnEventMethod="
      + observerOnEventMethod + "]";
    }

  }




}
