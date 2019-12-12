/**
 *
 */
package cz.geokuk.plugins.kesoid;

import cz.geokuk.plugins.kesoid.genetika.Genom;
import cz.geokuk.plugins.kesoid.genetika.Genotyp;

class GenotypBuilderWpt {

	/**
	 *
	 */
	private final Genom genom;

	/**
	 *
	 */
	public GenotypBuilderWpt(final Genom genom) {
		this.genom = genom;
	}

	Genotyp build(final Wpt wpt, final Genotyp g) {
		System.out.println("KEKESOI: " + wpt.getKesoid().getIdentifier());
		for (final Wpt w: wpt.getKesoid().getWpts()) {
			System.out.println("   " + w.getSym() + " " + w.isMainWpt() + " " + w.getName() + " " + w.getNazev());
		}
		return g
				.with(wpt.getKesoid().genProSymbol(genom, wpt).alela(wpt.getSym()))
				.with(wpt.isMainWpt() ? genom.ALELA_h : genom.ALELA_v);
	}

}