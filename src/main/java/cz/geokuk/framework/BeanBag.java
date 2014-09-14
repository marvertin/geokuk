package cz.geokuk.framework;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;


/**
 * Kontejner, který drží objekty a injektuje je.
 * @author tatinek
 *
 */
public class BeanBag implements Factory {

  private boolean initialized;

  private final List<Object> beans = new ArrayList<>();

  private EventManager eveman;

  /* (non-Javadoc)
   * @see cz.geokuk.program.Factory#create(java.lang.Class, java.lang.Object)
   */
  @Override
  public <T> T create(Class<T> klasa, Object... params) {
    if (! initialized) throw new RuntimeException("Kontejner jeste nebyl inicializovan");
    Class<?>[] types = new Class<?>[params.length];
    for (int i =0; i < params.length; i++) {
      Object param = params[i];
      types[i] = param == null ? null : param.getClass();
    }
    try {
      Constructor<T> constructor = klasa.getConstructor(types);
      T obj = constructor.newInstance(params);
      init(obj);
      return obj;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * @param <T>
   * @param obj
   */
  @Override
  public <T> T init(T obj) {
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
  public <T> T initNow(T obj) {
    inject(obj);
    callAfterInjectInit(obj);
    registerEventReceiver(obj, true);
    callAfterEventReceiverRegistrationInit(obj);
    return obj;
  }

  public <T> T registerSigleton(T object) {
    if (initialized) throw new RuntimeException("Kontejner uz byl inicializovan");
    if (object == null) throw new RuntimeException("Nelze inicializvat null objekt");
    beans.add(object);
    return object;
  }

  public void registrFieldsAsSingleton(Object obj) {
    try {
      Field[] fields = obj.getClass().getFields();
      for (Field field : fields) {
        if (Modifier.isStatic(field.getModifiers())) {
          continue;
        }
        registerSigleton(field.get(obj));
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  public void init() {

    for (Object bean : beans) {
      inject(bean);
    }
    for (Object bean : beans) {
      callAfterInjectInit(bean);
    }
    for (Object bean : beans) {
      registerEventReceiver(bean, false);
    }
    for (Object bean : beans) {
      callAfterEventReceiverRegistrationInit(bean);
    }
    initialized = true;
  }

  /**
   * @param bean
   */
  private void callAfterEventReceiverRegistrationInit(Object bean) {
    if ((bean instanceof AfterEventReceiverRegistrationInit)) {
      ((AfterEventReceiverRegistrationInit)bean).initAfterEventReceiverRegistration();
    }
  }

  /**
   * @param bean
   */
  private void callAfterInjectInit(Object bean) {
    if ((bean instanceof AfterInjectInit)) {
      ((AfterInjectInit)bean).initAfterInject();
    }
  }

  /**
   * @param bean
   */
  private void registerEventReceiver(Object bean, boolean aOnlyInvoke) {
    eveman.registerWeakly(bean, aOnlyInvoke);
  }

  /**
   * @param targetBean
   */
  private void inject(Object targetBean) {
    for (Method method: targetBean.getClass().getMethods()) {
      if (method.getName().equals("inject")) {
        BeanType targetBeanType = BeanType.createForTargetMethod(targetBean, method);
        boolean multiInjection = targetBeanType.isMultiInjectionSupported();
        Object injectedBean = null; // sem budeme injektovat, pokud nepodporujeme multiinjekci
        int pocetInjekci = 0;
        for (Object injectedBeanCandidate : beans) {
          BeanType injectedBeanType = BeanType.createForInjectedBean(injectedBeanCandidate);
          if (targetBeanType.canInjectFrom(injectedBeanType)) {
            if (injectedBean != null) throw new RuntimeException("Prilis mnoho implementaci pro " + method + " v " + injectedBean.getClass() + " a " + injectedBeanCandidate.getClass());
            if (multiInjection) {
              callInjection(method, targetBean, injectedBeanCandidate);
              pocetInjekci ++;
            } else {
              injectedBean = injectedBeanCandidate;
            }
          }
        }
        if (injectedBean != null) {
          callInjection(method, targetBean, injectedBean);
          pocetInjekci ++;
        }
        if (pocetInjekci == 0)
          throw new RuntimeException(String.format("Nenalena injekce targetBean=%s ... targetBeanType=%s ... method=%s", targetBean, targetBeanType, method));
      }
    }
  }


  /**
   * @param method
   * @param targetBean
   * @param injectedBean
   */
  private void callInjection(Method method, Object targetBean, Object injectedBean) {
    try {
      //      System.out.println("INJEKTUJI:: " + targetBean.getClass() + " <==  " + injectedBean.getClass() + " ... " + method);
      //          System.out.println(m);
      method.invoke(targetBean, injectedBean);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void inject(EventManager eveman) {
    this.eveman = eveman;
  }
}
