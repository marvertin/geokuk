package cz.geokuk.plugins.kesoid.kind.munzee;

import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.genetika.Genotyp;

class GenotypBuilderMunzee {

	private final Genom genom;

	public GenotypBuilderMunzee(final Genom genom) {
		this.genom = genom;
	}

	public Genotyp build(final Munzee munzee, final Genotyp g0) {
		final Genotyp g = g0.with(genom.ALELA_mz);
		switch (munzee.getVztah()) {
		case NORMAL:
			return g.with(genom.ALELA_hnf);
		case FOUND:
			return g.with(genom.ALELA_cpt);
		case OWN:
			return g.with(genom.ALELA_dpl);
		case NOT:
			return g.with(genom.ALELA_not);
		default: return g;
		}
	}
}