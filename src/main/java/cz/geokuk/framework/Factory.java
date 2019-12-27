/**
 *
 */
package cz.geokuk.framework;

/**
 * @author Martin Veverka
 *
 */
public interface Factory {

	/** Incializuje objekt a nechá mu posílat eventy i do příště */
	public abstract <T> T init(T obj);

	/** Inicializuje objekt, pošle mu eventy, ale neregistruje ho jako event lsitener */
	public abstract <T> T initNow(T obj);

}