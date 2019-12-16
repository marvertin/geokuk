package cz.geokuk.plugins.kesoid.kind.photo;

import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.genetika.Genotyp;

/**
 * Created by dan on 16.3.15.
 */
public class GenotypBuilderPhoto {
	private final Genom genom;

	public GenotypBuilderPhoto(final Genom genom) {
		this.genom = genom;
	}

	public Genotyp build(final Photo p, final Genotyp g) {
		return g.with(genom.ALELA_pic);
	}
}
