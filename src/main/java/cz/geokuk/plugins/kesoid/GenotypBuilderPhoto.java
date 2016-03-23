package cz.geokuk.plugins.kesoid;

import cz.geokuk.plugins.kesoid.mapicon.Genom;
import cz.geokuk.plugins.kesoid.mapicon.Genotyp;

/**
 * Created by dan on 16.3.15.
 */
public class GenotypBuilderPhoto {
	private final Genotyp g;
	private final Genom genom;

	public GenotypBuilderPhoto(final Genom genom, final Genotyp g) {
		this.genom = genom;
		this.g = g;
	}

	public void build(final Photo p) {
		g.put(genom.ALELA_pic);
	}
}
