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

  public abstract <T> T init(T obj);

}