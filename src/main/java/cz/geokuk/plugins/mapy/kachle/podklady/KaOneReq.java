/**
 *
 */
package cz.geokuk.plugins.mapy.kachle.podklady;

import cz.geokuk.plugins.mapy.kachle.data.Ka;
import lombok.Data;

@Data
public class KaOneReq {

	private final Ka ka;

	private ImageReceiver imageReceiver;

	private final Priority priorita;

	public KaOneReq(final Ka ka, final ImageReceiver imageReceiver, final Priority priorita) {
		this.ka = ka;
		this.imageReceiver = imageReceiver;
		this.priorita = priorita;
	}

	/**
	 * Zahodíme tento požadavek a už nebudeme dotahovat
	 */
	public void uzToZahod() {
		imageReceiver = null;
	}

}
