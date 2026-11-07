package cz.geokuk.plugins.challenge;

import java.util.Optional;

import cz.geokuk.api.mapicon.Imagant;
import cz.geokuk.core.coordinates.Mou;
import cz.geokuk.plugins.kesoid.Wpt;
import cz.geokuk.plugins.kesoid.mapicon.Sklivec;
import cz.geokuk.util.index2d.BoundingRect;
import cz.geokuk.util.index2d.Indexator;

/** Kde leží ikona keše vůči jejímu bodu na mapě, aby štítek kandidáta mohl stát těsně vedle ní. */
final class IkonaKese {

	/** Polovina ikony, když se skutečná ještě nezná (sklivec se plní postupně); ve značkách při zvětšení 1. */
	private static final int NEZNAMA_POLOVINA = 8;

	private IkonaKese() {
	}

	/** Pravý okraj ikony vůči bodu keše a svislý střed ikony vůči bodu, ve značkách při daném zvětšení ikon; {@code vysledek[0]} je pravý okraj, {@code vysledek[1]} svislý střed. */
	static void okraje(final Indexator<Wpt> indexator, final Mou mou, final double meritko, final int[] vysledek) {
		vysledek[0] = (int) Math.ceil(NEZNAMA_POLOVINA * meritko);
		vysledek[1] = 0;
		if (indexator == null) {
			return;
		}
		final Optional<Wpt> nejblizsi = indexator.bound(new BoundingRect(mou.xx - 2, mou.yy - 2, mou.xx + 2, mou.yy + 2)).locateNearestOne(mou.xx, mou.yy);
		if (!nejblizsi.isPresent()) {
			return;
		}
		final Sklivec sklivec = nejblizsi.get().getSklivec();
		if (sklivec == null) {
			return;
		}
		int pravy = Integer.MIN_VALUE;
		int nahore = Integer.MAX_VALUE;
		int dole = Integer.MIN_VALUE;
		for (final Imagant i : sklivec.imaganti) {
			if (i == null) {
				continue;
			}
			final Imagant z = i.zvetseny(meritko);
			pravy = Math.max(pravy, z.getXpos() + z.getImage().getWidth());
			nahore = Math.min(nahore, z.getYpos());
			dole = Math.max(dole, z.getYpos() + z.getImage().getHeight());
		}
		if (pravy != Integer.MIN_VALUE) {
			vysledek[0] = pravy;
			vysledek[1] = nahore + (dole - nahore) / 2;
		}
	}
}
