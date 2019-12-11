package cz.geokuk.plugins.kesoid.mapicon;

import java.util.Set;
import java.util.stream.Collectors;

import cz.geokuk.plugins.kesoid.genetika.Alela;

/**
 * Definice ikony.
 *
 * @author Martin Veverka
 *
 */
public class IconSubDef {
	final Set<Alela> alely;

	public IconSubDef(final Set<Alela> alely) {
		this.alely = alely;
	}

	@Override
	public String toString() {
		return "[" + alely.stream().map(Alela::qualName).collect(Collectors.joining(", ")) + "]";
	}

}
