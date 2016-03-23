package cz.geokuk.plugins.kesoid.mapicon;

import java.util.Set;

/**
 * Definice ikony.
 *
 * @author tatinek
 *
 */
public class IconSubDef {
	final Set<Alela> alely;

	public IconSubDef(final Set<Alela> alely) {
		this.alely = alely;
	}

	@Override
	public String toString() {
		return alely.toString();
	}

}
