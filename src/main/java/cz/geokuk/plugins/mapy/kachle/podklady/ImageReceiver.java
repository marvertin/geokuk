/**
 *
 */
package cz.geokuk.plugins.mapy.kachle.podklady;

/**
 * @author Martin Veverka
 *
 */
public interface ImageReceiver {

	/**
	 * @param kachloStav
	 *            TODO
	 * @param aTyp
	 * @param aImg
	 */
	public void send(KachloStav kachloStav);

}
