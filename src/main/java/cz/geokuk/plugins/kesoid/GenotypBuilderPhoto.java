package cz.geokuk.plugins.kesoid;

import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.genetika.Jedinec;

/**
 * Created by dan on 16.3.15.
 */
public class GenotypBuilderPhoto {
	private final Jedinec g;
	private final Genom genom;

	public GenotypBuilderPhoto(final Genom genom, final Jedinec g) {
		this.genom = genom;
		this.g = g;
	}

	public void build(final Photo p) {
		g.put(genom.ALELA_pic);
	}
}
