/**
 * 
 */
package cz.geokuk.framework;

/**
 * @author veverka
 *
 */
public interface Factory {

  public abstract <T> T create(Class<T> klasa, Object... params);

  /** Incializuje objekt a nechá mu posílat eventy i do příště */
  public abstract <T> T init(T obj);

  /** Inicializuje objekt, pošle mu eventy, ale neregistruje ho jako event lsitener */
  public abstract <T> T initNow(T obj);

}