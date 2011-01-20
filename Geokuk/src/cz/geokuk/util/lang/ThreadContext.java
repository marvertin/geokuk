/* Created on 15.3.2006
 * (C) Copyright 2006, TurboConsult s.r.o.
 */
package cz.geokuk.util.lang;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author roztocil
 * @deprecated Používej ThreadLocal
 */
public final class ThreadContext {

  private static final Map<Thread, Object> contexts = new WeakHashMap<Thread, Object>();
  
  public static ThreadContext getInstance() {
    Thread currentThread = Thread.currentThread();
    ThreadContext result = (ThreadContext)contexts.get(currentThread);
    if (result == null) {
      result = new ThreadContext();
      contexts.put(currentThread, result);
    }
    return result;
  }
  
  private ThreadContext() {}
  
  private final Map<String, Object> storage = new HashMap<String, Object>();
  
  public void setAttribute(String key, Object value) {
    storage.put(key, value);
  }
  
  public Object getAttribute(String key) {
    Object result = storage.get(key);
    return result;
  }
  
  public void removeAttribute(String key) {
    storage.remove(key);
  }
}
