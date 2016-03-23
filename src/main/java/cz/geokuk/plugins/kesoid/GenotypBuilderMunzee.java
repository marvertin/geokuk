package cz.geokuk.plugins.kesoid;

import cz.geokuk.plugins.kesoid.mapicon.Genom;
import cz.geokuk.plugins.kesoid.mapicon.Genotyp;

class GenotypBuilderMunzee {

	private final Genotyp g;
	private final Genom genom;

	public GenotypBuilderMunzee(final Genom genom, final Genotyp g) {
		this.genom = genom;
		this.g = g;
	}

	public void build(final Munzee munzee) {
		g.put(genom.ALELA_mz);
		switch (munzee.getVztah()) {
		case NORMAL:
			g.put(genom.ALELA_hnf);
			break;
		case FOUND:
			g.put(genom.ALELA_cpt);
			break;
		case OWN:
			g.put(genom.ALELA_dpl);
			break;
		case NOT:
			g.put(genom.ALELA_not);
			break;
		}
	}
}