/**
 *
 */
package cz.geokuk.plugins.mapy;

import cz.geokuk.framework.Event0;
import cz.geokuk.plugins.mapy.kachle.data.KaType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Martin Veverka
 *
 */
@AllArgsConstructor
@Getter
public class ZmenaMapNastalaEvent extends Event0<MapyModel> {

	private final KaType katype;
}
